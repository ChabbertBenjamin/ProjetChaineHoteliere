package fr.ul.miage.agent;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Reservation;
import fr.ul.miage.entite.Room;
import fr.ul.miage.model.ConnectBDD;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class ResponderBehaviour extends Behaviour {
    private  Connection connect = ConnectBDD.getInstance();
    private final static MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    private final ArrayList<Hotel> listHotel;
    private int totalNbBedDispo;
    private JSONObject result;
    private ArrayList<ArrayList<Room>> listCombinaison;

    public ResponderBehaviour(AgentChaineHoteliere agentChaineHoteliere) {
        super(agentChaineHoteliere);
        this.listHotel = agentChaineHoteliere.getListHotel();
        this.totalNbBedDispo = 0;
        this.listCombinaison = new ArrayList<>();
    }

    @Override
    public void action() {
        while (true) {
            ACLMessage aclMessage = myAgent.receive(mt);
            if (aclMessage != null) {
                try {
                    JSONObject msg = (JSONObject) aclMessage.getContentObject();


                    System.out.println(myAgent.getLocalName() + ": I receive a message with content\n" + msg.toString());
                    String msgType = getTypeMessage(msg);
                    //System.out.println(msgType);
                    JSONObject mess = processMessage(msg, msgType);
                    // On envoit la réponse après avoir traité le message
                    sendMessage(mess, aclMessage.getSender());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                this.block();
            }
        }
    }

    @Override
    public boolean done() {
        return false;
    }

    public String getTypeMessage(JSONObject message) {
        if (message.get("idHotel") != null) {
            return "reservation";
        } else {
            return "recherche";
        }
    }

    public JSONObject processMessage(JSONObject message, String msgType) throws SQLException {
        JSONObject answer = new JSONObject();
        // Si c'est une recherche
        if (msgType.equals("recherche")) {
            //Boucle sur chaque hotel
            int counter =0;
            ArrayList<JSONObject> tmp = new ArrayList<>();
            answer.put("idRequete", message.get("idRequete"));
            for (Hotel h:listHotel) {
                //JSONObject tmp = new JSONObject();
                // ResultRecherche sur un seul hotel
                JSONObject resultRecherche = rechercheRoomAvailable(h, message);
                System.out.println("Résultat de la recherche : " + resultRecherche.toString());

                ArrayList<Room> listRoomAvailable= jsonToList(resultRecherche);
                int nbBedDispo=0;
                for (Room room:listRoomAvailable) {
                    nbBedDispo += room.getNbBed();
                }
                // La réponse est une confirmation ou un refus, si il n'y a pas de chambre disponible
                if ((int) message.get("nbPersonne") < nbBedDispo) {
                    JSONObject proposition = reservationMessage(message,listRoomAvailable,h,nbBedDispo);
                    proposition.put("idProposition", counter);
                    tmp.add(proposition);
                    counter++;
                }
            }
            // On a aucune place dans aucun hôtel
            if(counter==0){
                answer.put("IdRequete", message.get("idRequete"));
                answer.put("erreur", "Aucune chambre disponible pour le nombre de personnes demandé");
            }else{
                answer.put("propositionReservation", tmp);
            }

        }

        // Si c'est une reservation
        if (msgType.equals("reservation")) {
            //trouver les hotels qui correspondent au message

            //answer = rechercheHotel(listHotelFound, message);
        }

        // On sauvegarde la dernière recherche
        result =answer;
        System.out.println("RESULT : " + result);
        return answer;
    }

    public ArrayList<Room> jsonToList(JSONObject jsonlist) throws SQLException {
        ArrayList<Room> listRoom = new ArrayList<>();
        for (int i = 0; i < jsonlist.size(); i++) {
            JSONObject tmp = (JSONObject) jsonlist.get(i);

            Statement stmt = connect.createStatement();
            ResultSet res = stmt.executeQuery("SELECT * FROM room WHERE id=" + tmp.get("idChambre"));
            while (res.next()) {
                Room room = new Room(res.getInt(1), res.getDouble(2), res.getInt(3), res.getInt(4));
                listRoom.add(room);
            }
        }

        return listRoom;
    }

    public JSONObject rechercheRoomAvailable(Hotel h, JSONObject message) throws SQLException {
        JSONObject answer = new JSONObject();
        Date dateDebutDemande = (Date) message.get("dateDebut");
        Date dateFinDemande = (Date) message.get("dateFin");

        Statement stmt = connect.createStatement();
        ResultSet res = stmt.executeQuery("SELECT * FROM room WHERE idhotel=" + h.getId()/*+"and nbbed >=" + message.get("nbPersonne")*/);
        ArrayList<Room> listRoom = new ArrayList<>();
        while (res.next()) {
            Room room = new Room(res.getInt(1), res.getDouble(2), res.getInt(3), res.getInt(4));
            listRoom.add(room);
        }
        int counter = 0;
        for (Room r : listRoom) {

            boolean chambreDisponible = true;

            Statement stmt2 = connect.createStatement();
            // Récupération des reservation par rapport aux hotels
            ResultSet res2 = stmt2.executeQuery("SELECT * FROM reservation WHERE idroom=" + r.getId());
            ArrayList<Reservation> listReservation = new ArrayList<>();
            while (res2.next()) {
                Reservation reservation = new Reservation(res2.getInt(1), res2.getInt(2), res2.getInt(3), res2.getDate(4), res2.getDate(5), res2.getDouble(6), res2.getInt(7));
                listReservation.add(reservation);
            }

            for (Reservation reservation : listReservation) {
                Date dateDebutReservationTrouve = reservation.getDateStart();
                Date dateFinReservationTrouve = reservation.getDateEnd();

                 /*
                Savoir si une chambre est libre : lorsqu'on trouve une reservation déjà présente on check :
                    - SI dateDebut de la reservation est entre dateDebutDemande et dateFinDemande
                ET  - SI dateFin de la reservation est entre dateDebutDemande et dateFinDemande
                ET  - SI dateDebut de la demande est entre dateDebut de la reservation et dateFin de la reservation
                ET  - SI dateFin de la demande est entre dateDebut de la reservation et dateFin de la reservation
                 */
                if (dateDebutReservationTrouve.after(dateDebutDemande) && dateDebutReservationTrouve.before(dateFinDemande)) {
                    chambreDisponible = false;
                }
                if (dateFinReservationTrouve.after(dateDebutDemande) && dateFinReservationTrouve.before(dateFinDemande)) {
                    chambreDisponible = false;
                }
                if (dateDebutDemande.after(dateDebutReservationTrouve) && dateDebutDemande.before(dateFinReservationTrouve)) {
                    chambreDisponible = false;
                }
                if (dateFinDemande.after(dateDebutReservationTrouve) && dateFinDemande.before(dateFinReservationTrouve)) {
                    chambreDisponible = false;
                }

            }
            if (chambreDisponible) {
                this.totalNbBedDispo += r.getNbBed();
                // Si on trouve une chambre disponible alors on créer un objet JSON correspondant à cette chambre pour la réponse
                JSONObject tmp = new JSONObject();
                tmp.put("idHotel", h.getId());
                tmp.put("idChambre", r.getId());
                tmp.put("dateDebut", dateDebutDemande);
                tmp.put("dateFin", dateFinDemande);
                tmp.put("nbPersonne", r.getNbBed());
                tmp.put("prix", r.getPrice());
                tmp.put("standing", message.get("standing"));
                tmp.put("ville", h.getCity());
                tmp.put("pays", h.getCountry());

                answer.put(counter, tmp);
                counter++;
            }
        }
        //System.out.println("liste des chambres disponible pour l'hotel : "+ h.getId() + " : " +listRoom.toString());
        return answer;
    }

    public JSONObject reservationMessage(JSONObject message, ArrayList<Room> listRoom, Hotel hotel, int nbBedDispo) throws SQLException {
        JSONObject answer = new JSONObject();

        // Si listRoomDispo est vide ou que l'hôtel n'a pas la capacité pour le nombre de personnes demandé, on envoie un refus

        //Tri des chambres par rapport à leurs nombre de lit (décroissant)
        listRoom.sort((r1, r2) -> r2.getNbBed() - r1.getNbBed());
        System.out.println("LISTE DES CHAMBRES : " + listRoom);
        ArrayList<Room> bestCombinaison = new ArrayList<>();
        ArrayList<ArrayList<Room>> combinaisonDejaTest = new ArrayList();
        // Une perfectCombinaison est une combinaison de chambre qui ne laisse aucun lit vide
        boolean perfectCombinaison = false;
        int idRoomInList = -1;
        ArrayList<ArrayList<Room>> listRoomPotentiel = new ArrayList<>();
        int bestRoomIdFromList = -1;

        // On va regarder les combinaisons de chambres possible et trouver la meilleure (k = le nombre de chambre dans la combinaison)
        for (int k = 1; k <= listRoom.size(); k++) {
            ArrayList<Room> combinaison = new ArrayList<>();
            combinaison.clear();
            //Combinaison de 1 seule chambre
            if (k == 1) {


                for (int i = 0; i < listRoom.size(); i++) {
                    if (listRoom.get(i).getNbBed() == (int) message.get("nbPersonne")) {
                        combinaison.add(listRoom.get(i));
                        listRoomPotentiel.add(combinaison);
                        // On garde à chaque fois que la meilleur combinaison
                        bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);
                        perfectCombinaison = true;
                        break;
                    }
                    // Si la combinaison respecte le nombre de place demandé
                    if (listRoom.get(i).getNbBed() > (int) message.get("nbPersonne")) {
                        combinaison.add(listRoom.get(i));
                        bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);

                    }
                    combinaison.clear();
                }
                //Si on a trouvé une combinaison sans perdre de lit alors on stop tout car c'est la meilleur possible
                if (perfectCombinaison) {
                    break;
                }
                if (!combinaison.isEmpty()) {
                    listRoomPotentiel.add(combinaison);
                }
                combinaison.clear();
                // Combinaison de plusieurs chambres
            } else {
                int nbBedInCombinaison = 999999;

                // On stop quand le nbBed de la combinaison actuel est inférieur aux nombre de lit demandé
                while (nbBedInCombinaison > (int) message.get("nbPersonne")) {
                    nbBedInCombinaison=0;

                    // créer la combinaison de k chambre grâce à la combinaison précédente
                    combinaison = createCombinaison(k, listRoom, combinaison);
                    // Si combinaison == null c'est qu'on a plus de combinaison à tester pour ce k
                    if(combinaison == null){
                        break;
                    }
                    for (Room r : combinaison) {
                        nbBedInCombinaison += r.getNbBed();
                    }
                    // Si la combinaison ne fait pas perdre de lit on stop tout car on a trouvé la meilleure possible
                    if(nbBedInCombinaison == (int) message.get("nbPersonne")){
                        bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);
                        perfectCombinaison=true;
                        break;
                    }
                    // On garde que la meilleure combinaison pour ce k
                    if(nbBedInCombinaison > (int) message.get("nbPersonne")){
                        bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);
                    }else{
                        break;
                    }
                }
                //Si on a trouvé une combinaison sans perdre de lit alors on stop tout car c'est la meilleur possible
                if(perfectCombinaison){
                    break;
                }
                // On ajoute dans une listRoomPotentiel la meilleure combinaison trouvé pour ce k
                ArrayList<Room> result = new ArrayList<>(bestCombinaison);
                listRoomPotentiel.add(result);
                bestCombinaison.clear();
            }
        }

        //System.out.println("on a trouvé les combinaisons : " + listRoomPotentiel);

        //Si on a pas trouvé de combinaison qui ne fait pas perdre de lit, on clear la dernière combinaison trouvé
        if(!perfectCombinaison){
            bestCombinaison.clear();
        }
        // On regarde quelle est la meilleure combinaison (listRoomPotentiel contient la meilleur combinaison pour chaque k)
        for (ArrayList<Room> combinaison:listRoomPotentiel) {
            bestCombinaison = findBestCombinaison(bestCombinaison,combinaison);
        }

        System.out.println("La meilleure combinaison de chambre est : " + bestCombinaison);
        this.listCombinaison.add(bestCombinaison);

          /*
    RENVOYER CONFIRMATION RESERVATION
    {
        idReservation : id
        idHotel : id
        nbChambres : int
        ville : string
        pays : string
        nbPersonnes : int
        prix : Double
        standing : int
        dateDebut : Date
        dateFin : Date
    }
     */




        double prix = 0;
        for (Room roomToReserve : bestCombinaison) {
            Reservation res = new Reservation((int) ((Math.random() * (99999999)) + 0),
                    hotel.getId(),
                    roomToReserve.getId(),
                    (Date) message.get("dateDebut"),
                    (Date) message.get("dateFin"),
                    roomToReserve.getPrice(),
                    (int) message.get("nbPersonne")
            );
            prix = res.calculatePriceBasedOnDates();
        }

        //answer.put("id_proposition", 1);
        answer.put("nomHotel", hotel.getName());
        answer.put("nbChambres", bestCombinaison.size());
        answer.put("dateDebut", message.get("dateDebut"));
        answer.put("dateFin", message.get("dateFin"));
        answer.put("nbPersonnes", message.get("nbPersonne"));
        answer.put("prix", prix);
        answer.put("standing",hotel.getStanding());
        answer.put("ville", hotel.getCity());
        answer.put("pays", hotel.getCountry());

        //  A verifier
        //registerReservation(bestCombinaison,message);
        return answer;
    }

    public void registerReservation(ArrayList<Room> bestCombinaison, JSONObject message) throws SQLException {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");

        Date dateDebutDemande = (Date) message.get("dateDebut");
        Date dateFinDemande = (Date) message.get("dateFin");
        Statement stmt = connect.createStatement();
        ResultSet res = stmt.executeQuery("SELECT * FROM hotel WHERE id=" + message.get("idHotel")/*+"and nbbed >=" + message.get("nbPersonne")*/);
        ArrayList<Room> listHotel = new ArrayList<>();
        Hotel hotel = null;
        while (res.next()) {
            hotel = new Hotel(res.getInt(1), res.getString(2), res.getInt(3), res.getString(4), res.getString(5), res.getInt(6), res.getString(7), res.getInt(8), res.getInt(9));
        }

        double prix=0;
        for (Room r:bestCombinaison) {
            prix += r.getPrice();

            //System.out.println("INSERT INTO reservation VALUES ("+hotel.getId()+","+r.getId()+",'"+formater.format(dateDebutDemande)+"','"+formater.format(dateFinDemande)+"',"+r.getPrice()+","+r.getNbBed()+")");
            String SQL_INSERT = "INSERT INTO reservation (idhotel, idroom, datestart, dateend, price, nbpeople) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement myStmt = connect.prepareStatement(SQL_INSERT)) {
                //res = stmt.executeQuery("INSERT INTO reservation (idhotel, idroom, datestart, dateend, price, nbpeople) VALUES ("+hotel.getId()+","+r.getId()+",\'"+formater.format(dateDebutDemande)+"\',\'"+formater.format(dateFinDemande)+"\',"+r.getPrice()+","+r.getNbBed()+")");
                java.sql.Date date = new java.sql.Date(0000 - 00 - 00);
                myStmt.setInt(1, hotel.getId());
                myStmt.setInt(2, r.getId());
                myStmt.setDate(3, date.valueOf(formater.format(dateDebutDemande)));
                myStmt.setDate(4, date.valueOf(formater.format(dateFinDemande)));
                myStmt.setDouble(5, r.getPrice());
                myStmt.setInt(6, r.getNbBed());

                int row = myStmt.executeUpdate();
                // rows affected
                System.out.println(row); //1
            } catch (SQLException e) {
                System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public ArrayList<Room> createCombinaison(int tailleCombinaison, ArrayList<Room> listRoom, ArrayList<Room> previousCombinaison) {
        // S'il n'y a pas de combinaison précédente, on créer la première dans l'ordre
        ArrayList<Room> combinaison = new ArrayList<>();
        if (previousCombinaison.size()==0){
            for (int i = 0; i < tailleCombinaison; i++) {
                combinaison.add(listRoom.get(i));
            }
            return combinaison;
        }

        // Pour créer la combinaison en fonction de la précédente, on va regarder l'index+1, Si l'index+1 peut changer, alors on ne change pas l'index actuel, si l'index+1 ne peut pas changer alors on changer l'index actuel
        int index=-1;
        for (int i = -1; i < tailleCombinaison; i++) {
            if (i!= tailleCombinaison-1){
                Room roomInCombinaison = previousCombinaison.get(i+1);
                index = listRoom.indexOf(roomInCombinaison);
            }else{
                Room roomInCombinaison = previousCombinaison.get(i);
                index = listRoom.indexOf(roomInCombinaison);
                combinaison.add(listRoom.get(index+1));
                break;
            }
            // Si l'index1 ne peut pas changer
            if(index >= listRoom.size()-(tailleCombinaison-1)+i){
                if(i==-1){
                    return null;
                }
                Room room = previousCombinaison.get(i);
                int index2 = listRoom.indexOf(room);
                for (int j = 1; j <= tailleCombinaison-i; j++) {
                    combinaison.add(listRoom.get(index2+j));
                }
                break;
            }else{
                //Si l'index+1 peut changer alors on remet l'index à la même position
                if(i != -1){
                    combinaison.add(previousCombinaison.get(i));
                }
            }
        }
        return combinaison;
    }





    public ArrayList<Room> findBestCombinaison(ArrayList<Room> combinaison1,ArrayList<Room> combinaison2){
        ArrayList<Room> answer = new ArrayList<>();
        int nbBedCombinaison1 =0;
        int nbBedCombinaison2 =0;
        boolean testCombinaison1Empty = false;
        boolean testCombinaison2Empty = false;
        // Si la combinaison1 n'est pas null alors on compte le nombre de lit qu'elle offre
        if(combinaison1 != null && !combinaison1.isEmpty()){
            for (Room r: combinaison1) {
                nbBedCombinaison1 += r.getNbBed();
            }
        }else{
            answer.addAll(combinaison2);
            testCombinaison1Empty = true;
        }
        // Si la combinaison2 n'est pas null alors on compte le nombre de lit qu'elle offre
        if(combinaison2 != null && !combinaison2.isEmpty()){
            for (Room r: combinaison2) {
                nbBedCombinaison2 += r.getNbBed();
            }
        }else{
            answer.addAll(combinaison1);
            testCombinaison2Empty = true;
        }
        // Si les combinaisons ne sont pas vides alors on regarde celle qui offrent le moins de lit (car dans tous les cas elles offrent plus ou autant de lit que demandé)
        if(!testCombinaison1Empty || !testCombinaison2Empty){
            if(nbBedCombinaison1 <= nbBedCombinaison2){
                answer.addAll(combinaison1);
            }else{
                answer.addAll(combinaison2);
            }
        }



        return answer;

    }


    private void sendMessage(JSONObject mess, AID id) {
        try {
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.addReceiver(id);

            aclMessage.setContentObject(mess);

            myAgent.send(aclMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
