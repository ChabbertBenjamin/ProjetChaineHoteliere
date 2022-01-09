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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class ResponderBehaviour extends Behaviour {
    private final static MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    private final ArrayList<Hotel> listHotel;
    private int totalNbBedDispo;

    public ResponderBehaviour(AgentChaineHoteliere agentChaineHoteliere) {
        super(agentChaineHoteliere);
        this.listHotel = agentChaineHoteliere.getListHotel();
        this.totalNbBedDispo = 0;
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
        // Si c'est une réservation, on cherche l'hotel en fonction de l'id de l'hôtel dans le message
        if (msgType.equals("reservation")) {
            ArrayList<Hotel> listHotelFound = new ArrayList<>();
            for (Hotel h : listHotel) {
                if (message.get("idHotel").equals(h.getId())) {
                    listHotelFound.add(h);
                }
            }
            JSONObject resultRecherche = rechercheHotel(listHotelFound, message);

            System.out.println("Résultat de la recherche : " + resultRecherche.toString());
            // La réponse est une confirmation ou un refus, si il n'y a pas de chambre disponible
            answer = reservationMessage(message, jsonToList(resultRecherche));
        }
        // Si c'est une recherche, on cherche les hotels en fonction de la destination
        if (msgType.equals("recherche")) {
            //trouver les hotels qui correspondent au message
            ArrayList<Hotel> listHotelFound = new ArrayList<>();
            for (Hotel h : listHotel) {
                if (h.getCity().equals(message.get("destination"))) {
                    listHotelFound.add(h);
                }
            }
            answer = rechercheHotel(listHotelFound, message);
        }
        // answer contient soit les chambres disponible soit une confirmation de reservation ou un refus suivant si la chambre est dispobible
        return answer;
    }

    public ArrayList<Room> jsonToList(JSONObject jsonlist) throws SQLException {
        ArrayList<Room> listRoom = new ArrayList<>();
        for (int i = 0; i < jsonlist.size(); i++) {
            JSONObject tmp = (JSONObject) jsonlist.get(i);

            ConnectBDD DB = new ConnectBDD();
            Statement stmt = DB.getConn().createStatement();
            ResultSet res = stmt.executeQuery("SELECT * FROM room WHERE id=" + tmp.get("idChambre"));
            while (res.next()) {
                Room room = new Room(res.getInt(1), res.getDouble(2), res.getInt(3), res.getInt(4));
                listRoom.add(room);
            }
        }

        return listRoom;
    }

    public JSONObject rechercheHotel(ArrayList<Hotel> listHotelFound, JSONObject message) throws SQLException {
        JSONObject answer = new JSONObject();
        Date dateDebutDemande = (Date) message.get("dateDebut");
        Date dateFinDemande = (Date) message.get("dateFin");

        int counter = 0;
        for (Hotel h : listHotelFound) {

            ConnectBDD DB = new ConnectBDD();
            Statement stmt = DB.getConn().createStatement();
            ResultSet res = stmt.executeQuery("SELECT * FROM room WHERE idhotel=" + h.getId()/*+"and nbbed >=" + message.get("nbPersonne")*/);
            ArrayList<Room> listRoom = new ArrayList<>();
            while (res.next()) {
                Room room = new Room(res.getInt(1), res.getDouble(2), res.getInt(3), res.getInt(4));
                listRoom.add(room);
            }
            for (Room r : listRoom) {

                boolean chambreDisponible = true;

                Statement stmt2 = DB.getConn().createStatement();
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
                    tmp.put("nbPersonne", message.get("nbPersonne"));
                    tmp.put("prix", message.get("prix"));
                    tmp.put("standing", message.get("standing"));
                    tmp.put("ville", h.getCity());
                    tmp.put("pays", h.getCountry());

                    answer.put(counter, tmp);
                    counter++;
                }
            }

            //System.out.println("liste des chambres disponible pour l'hotel : "+ h.getId() + " : " +listRoom.toString());

        }
        return answer;
    }

    public JSONObject reservationMessage(JSONObject message, ArrayList<Room> listRoom) throws SQLException {
        JSONObject answer = new JSONObject();

        // Si listRoomDispo est vide ou que l'hôtel n'a pas la capacité pour le nombre de personnes demandé, on envoie un refus

        if ((int) message.get("nbPersonne") > this.totalNbBedDispo) {
            answer.put("IdRequete", message.get("idRequete"));
            answer.put("erreur", "Aucune chambre disponible pour le nombre de personnes demandé");
        } else {
            //Tri des chambres par rapport à leurs nombre de lit (décroissant)
            listRoom.sort((r1, r2) -> r2.getNbBed() - r1.getNbBed());
            ArrayList<Room> bestCombinaison = new ArrayList<>();
            ArrayList<ArrayList<Room>> combinaisonDejaTest = new ArrayList();
            // Une perfectCombinaison est une combinaison de chambre qui ne laisse aucun lit vide
            boolean perfectCombinaison = false;
            int idRoomInList = -1;
            ArrayList<ArrayList<Room>> listRoomPotentiel = new ArrayList<>();
            int bestRoomIdFromList = -1;

            for (int k = 1; k <= listRoom.size(); k++) {
                ArrayList<Room> combinaison = new ArrayList<>();
                combinaison.clear();
                //Combinaison de 1 seule chambre

                if (k == 1) {

                    for (int i = 0; i < listRoom.size(); i++) {
                        if (listRoom.get(i).getNbBed() == (int) message.get("nbPersonne")) {
                            combinaison.add(listRoom.get(i));
                            listRoomPotentiel.add(combinaison);
                            bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);
                            perfectCombinaison = true;
                            break;
                        }
                        if (listRoom.get(i).getNbBed() > (int) message.get("nbPersonne")) {
                            combinaison.add(listRoom.get(i));
                            bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);

                        }
                        combinaison.clear();
                    }
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

                        //Créer la combinaison de k chambres
                        combinaison = createCombinaison(k, listRoom, new ArrayList<>(), combinaison);
                        if(combinaison == null){
                            break;
                        }
                        for (Room r : combinaison) {
                            nbBedInCombinaison += r.getNbBed();
                        }
                        if(nbBedInCombinaison == (int) message.get("nbPersonne")){
                            bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);
                            perfectCombinaison=true;
                            break;
                        }
                        if(nbBedInCombinaison > (int) message.get("nbPersonne")){
                            bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);
                        }else{
                            break;
                        }
                    }
                    if(perfectCombinaison){
                        break;
                    }
                    ArrayList<Room> result = new ArrayList<>(bestCombinaison);
                    listRoomPotentiel.add(result);
                    bestCombinaison.clear();
                }
            }

            System.out.println("on a trouvé les combinaisons : " + listRoomPotentiel);

            // Regarder la meilleur combinaison dans listRoomPotentiel
            if(!perfectCombinaison){
                bestCombinaison.clear();
            }
            for (ArrayList<Room> combinaison:listRoomPotentiel) {
                bestCombinaison = findBestCombinaison(bestCombinaison,combinaison);
            }

            System.out.println("La meilleure combinaison de chambre est : " + bestCombinaison);


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
            ConnectBDD DB = new ConnectBDD();
            Statement stmt = DB.getConn().createStatement();
            ResultSet res = stmt.executeQuery("SELECT * FROM hotel WHERE id="+message.get("idHotel")/*+"and nbbed >=" + message.get("nbPersonne")*/);
            ArrayList<Room> listHotel = new ArrayList<>();
            Hotel hotel = null;
            while (res.next()) {
                hotel = new Hotel(res.getInt(1), res.getString(2), res.getInt(3), res.getString(4),res.getString(5),res.getInt(6));
            }

            double prix=0;

            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");


            Date dateDebutDemande = (Date) message.get("dateDebut");
            Date dateFinDemande = (Date) message.get("dateFin");
            for (Room r:bestCombinaison) {
                prix += r.getPrice();

                //System.out.println("INSERT INTO reservation VALUES ("+hotel.getId()+","+r.getId()+",'"+formater.format(dateDebutDemande)+"','"+formater.format(dateFinDemande)+"',"+r.getPrice()+","+r.getNbBed()+")");
                String SQL_INSERT = "INSERT INTO reservation (idhotel, idroom, datestart, dateend, price, nbpeople) VALUES (?,?,?,?,?,?)";
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:postgresql://localhost/ChaineHoteliere", "postgres", "root");
                     PreparedStatement myStmt = conn.prepareStatement(SQL_INSERT)) {

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





            answer.put("idReservation", 1);
            answer.put("idHotel", message.get("idHotel"));
            answer.put("nbChambres", bestCombinaison.size());
            answer.put("ville", hotel.getCity());
            answer.put("pays", hotel.getCountry());
            answer.put("nbPersonnes", message.get("nbPersonne"));
            answer.put("prix", prix);
            answer.put("dateDebut", message.get("dateDebut"));
            answer.put("dateFin", message.get("dateFin"));


        }
        return answer;
    }

    public ArrayList<Room> createCombinaison(int tailleCombinaison, ArrayList<Room> listRoom, ArrayList<Room> combinaison, ArrayList<Room> previousCombinaison) {
        if (previousCombinaison.size()==0){
            for (int i = 0; i < tailleCombinaison; i++) {
                combinaison.add(listRoom.get(i));
            }
            return combinaison;
        }

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
        if(combinaison1 != null && !combinaison1.isEmpty()){
            for (Room r: combinaison1) {
                nbBedCombinaison1 += r.getNbBed();
            }
        }else{
            answer.addAll(combinaison2);
            testCombinaison1Empty = true;
        }

        if(combinaison2 != null && !combinaison2.isEmpty()){
            for (Room r: combinaison2) {
                nbBedCombinaison2 += r.getNbBed();
            }
        }else{
            answer.addAll(combinaison1);
            testCombinaison1Empty = true;
        }
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
