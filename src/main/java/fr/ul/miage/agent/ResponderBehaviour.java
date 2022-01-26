package fr.ul.miage.agent;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Reservation;
import fr.ul.miage.entite.Room;
import fr.ul.miage.model.ConnectBDD;
import fr.ul.miage.testMessage.responderTestRecherche;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONObject;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

import static java.lang.Thread.sleep;

public class ResponderBehaviour extends Behaviour {
    private  Connection connect = ConnectBDD.getInstance();
    private final static MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    private final ArrayList<Hotel> listHotel;

    //Permet l'historique des anciennes recherches
    private ArrayList<ArrayList<Room>> listCombinaison;
    private int idProcessus = 0;
    private HashMap<Integer, ArrayList<ArrayList<Room>>> idProcessusListCombinaison = new HashMap<>();
    private HashMap<Integer,JSONObject> idProcessusResultRecherche = new HashMap<>();

    public ResponderBehaviour(AgentChaineHoteliere agentChaineHoteliere) {
        super(agentChaineHoteliere);
        this.listHotel = agentChaineHoteliere.getListHotel();
        this.listCombinaison = new ArrayList<>();
    }

    @Override
    public void action() {
        int time=0;
        while (true) {
            ACLMessage aclMessage = myAgent.receive(mt);
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            time++;
            // Si ça fait 10 minutes que la recherche n'a pas été faite
            if(time == 1000*60*10 ){
                idProcessusListCombinaison.clear();
                idProcessusResultRecherche.clear();
                time=0;
                //System.out.println("ancienne reserche supprimé");
            }

            if (aclMessage != null) {
                time=0;
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
        if(message.get("nomChaine").equals("Ibis")){
            return "reponse concurent";
        }
        if (message.get("idProposition") != null) {
            return "reservation";
        } else {
            // On augmente le l'idProcessus de 1 qui correspond à la nouvelle recherche
            this.idProcessus++;
            return "recherche";
        }
    }

    public JSONObject processMessage(JSONObject message, String msgType) throws SQLException, ParseException, InterruptedException {
        JSONObject answer = new JSONObject();
        // Si c'est une recherche
        if (msgType.equals("recherche")) {
            int counter =0;
            ArrayList<JSONObject> tmp = new ArrayList<>();

            //Boucle sur chaque hotel
            for (Hotel h:listHotel) {
                // On cherche les chambres disponible dans l'hotel
                ArrayList<Room> listRoomAvailable = rechercheRoomAvailable(h, message);
                int nbBedDispo=0;
                for (Room room:listRoomAvailable) {
                    nbBedDispo += room.getNbBed();
                }
                // La réponse est une confirmation ou un refus s'l n'y a pas assez de lits disponibles
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
                answer.put("idRequete", message.get("idRequete"));
                answer.put("idProcessus", this.idProcessus);
                answer.put("propositionReservation", tmp);
                idProcessusListCombinaison.put(idProcessus,this.listCombinaison);
                idProcessusResultRecherche.put(idProcessus,answer);
            }
            return answer;
        }else{
            // Si c'est une reservation
            int idProcessus = (int) message.get("idProcessus");
            int idProposition = (int) message.get("idProposition");

            if(idProcessusResultRecherche.get(idProcessus) == null){
                // Si aucune reserche n'a été faites
                answer.put("idProposition",idProposition);
                answer.put("erreur","Faites une reserche avant une reservation");
            }else{
                JSONObject resultRecherche = idProcessusResultRecherche.get(idProcessus);
                ArrayList<JSONObject> listProposition = (ArrayList<JSONObject>) resultRecherche.get("propositionReservation");
                JSONObject propositionChoisi = listProposition.get(idProposition);
                ArrayList<ArrayList<Room>> listCombinaison = idProcessusListCombinaison.get(idProcessus);
                ArrayList<Room> bestCombinaison = new ArrayList<>();
                bestCombinaison = listCombinaison.get(idProposition);
                // Enregistrer la reservation
                registerReservation(bestCombinaison, propositionChoisi);

                answer.put("idProposition",message.get("idProposition"));
                answer.put("nomHotel",propositionChoisi.get("nomHotel"));
                answer.put("nbChambres",propositionChoisi.get("nbChambres"));
                answer.put("ville",propositionChoisi.get("ville"));
                answer.put("pays",propositionChoisi.get("pays"));
                answer.put("nbPersonnes",propositionChoisi.get("nbPersonnes"));
                answer.put("prix",propositionChoisi.get("prix"));
                answer.put("standing",propositionChoisi.get("standing"));
                answer.put("dateDebut",propositionChoisi.get("dateDebut"));
                answer.put("dateFin",propositionChoisi.get("dateFin"));


                idProcessusResultRecherche.clear();
                idProcessusListCombinaison.clear();

            }

            return answer;
        }

    }

    public ArrayList<Room> rechercheRoomAvailable(Hotel h, JSONObject message) throws SQLException, ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        Date dateDebutDemande = simpleDateFormat.parse((String) message.get("dateDebut"));
        Date dateFinDemande = simpleDateFormat.parse((String) message.get("dateFin"));

        ArrayList<Room> answer = new ArrayList<Room>();
        //Date dateDebutDemande = new Date(String.valueOf((Date) message.get("dateDebut")));
        // dateFinDemande = new Date(String.valueOf((Date) message.get("dateFin")));





        Statement stmt = connect.createStatement();
        ResultSet res = stmt.executeQuery("SELECT * FROM room WHERE idhotel=" + h.getId()/*+"and nbbed >=" + message.get("nbPersonne")*/);
        ArrayList<Room> listRoom = new ArrayList<>();
        while (res.next()) {
            Room room = new Room(res.getInt(1), res.getDouble(2), res.getInt(3), res.getInt(4));
            listRoom.add(room);
        }
        for (Room r : listRoom) {
            boolean chambreDisponible = true;
            Statement stmt2 = connect.createStatement();
            // Récupération des reservation par rapport aux hotels
            ResultSet res2 = stmt2.executeQuery("SELECT * FROM reservation WHERE idroom=" + r.getId());
            ArrayList<Reservation> listReservation = new ArrayList<>();
            while (res2.next()) {
                Reservation reservation = new Reservation(res2.getInt(1), res2.getInt(2), res2.getInt(3), res2.getDate(6), res2.getDate(7), res2.getDouble(4), res2.getInt(5));
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
                answer.add(r);
            }
        }
        //System.out.println("liste des chambres disponible pour l'hotel : "+ h.getId() + " : " +answer);
        return answer;
    }

    public JSONObject reservationMessage(JSONObject message, ArrayList<Room> listRoom, Hotel hotel, int nbBedDispo) throws SQLException, ParseException, InterruptedException {
        JSONObject answer = new JSONObject();
        //Tri des chambres par rapport à leurs nombre de lit (décroissant) pour stoper l'algorithme dès qu'on trouve une combinaison parfaite
        listRoom.sort((r1, r2) -> r2.getNbBed() - r1.getNbBed());
        ArrayList<Room> bestCombinaison = new ArrayList<>();
        // Une perfectCombinaison est une combinaison de chambre qui ne laisse aucun lit vide
        boolean perfectCombinaison = false;
        int idRoomInList = -1;
        // Contiendra la meilleur combinaison pour chaque k (taille de combinaison)
        ArrayList<ArrayList<Room>> listRoomPotentiel = new ArrayList<>();
        int bestRoomIdFromList = -1;

        // On va regarder les combinaisons de chambres possible et trouver la meilleure (k = le nombre de chambre dans la combinaison)
        for (int k = 1; k <= listRoom.size(); k++) {
            // La combinaison qui est actuellement testé
            ArrayList<Room> combinaison = new ArrayList<>();
            combinaison.clear();
            //Combinaison de 1 seule chambre
            if (k == 1) {
                for (int i = 0; i < listRoom.size(); i++) {
                    // Si on trouve une chambre qui correspond exactement au nombre de personnes demandées
                    if (listRoom.get(i).getNbBed() == (int) message.get("nbPersonne")) {
                        bestCombinaison.add(listRoom.get(i));
                        /*
                        listRoomPotentiel.add(combinaison);
                        // On garde à chaque fois que la meilleur combinaison
                        bestCombinaison = findBestCombinaison(bestCombinaison, combinaison);*/
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
                int nbBedInCombinaison = Integer.MAX_VALUE;

                // On stop quand le nbBed de la combinaison actuel est inférieur aux nombre de lit demandé ou qu'on a testé toutes les combinaisons
                while (nbBedInCombinaison > (int) message.get("nbPersonne") || combinaison!=null) {
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
                        // Si le nombre de lit suffit pas, alors toutes les combinaisons pour ce même k ne suffiront pas
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

        //Si on a pas trouvé de combinaison qui ne fait pas perdre de lit, on clear la dernière combinaison trouvé
        if(!perfectCombinaison){
            bestCombinaison.clear();
        }
        // On regarde quelle est la meilleure combinaison (listRoomPotentiel contient la meilleur combinaison pour chaque k)
        for (ArrayList<Room> combinaison:listRoomPotentiel) {
            bestCombinaison = findBestCombinaison(bestCombinaison,combinaison);
        }
        //System.out.println("La meilleure combinaison de chambre est : " + bestCombinaison);
        this.listCombinaison.add(bestCombinaison);




        //On change le nom de la chaine qui doit recevoir le message
        message.put("nomChaine", "Ibis");
        AID aid = new AID();
        // Envoie du message au concurent pour voir si il peut gérer la même recherche
        sendMessageConcurent(message, aid);
        // On attend 1 seconde pour avoir une réponse
        sleep(1000);
        ACLMessage aclMessage = myAgent.receive(mt);
        boolean concurentPlace = true;
        // On a recu une réponse
        if(aclMessage != null){
            try {
                JSONObject reponseConcurent = (JSONObject) aclMessage.getContentObject();
                ArrayList<JSONObject> listPropositionConcurent = (ArrayList<JSONObject>) reponseConcurent.get("propositionReservation");
                if(listPropositionConcurent == null){
                    concurentPlace = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        // ON RENVOIT LA PROPOSITION AVEC SON PRIX

        double prix = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        Date dateDebutDemande = simpleDateFormat.parse((String) message.get("dateDebut"));
        Date dateFinDemande = simpleDateFormat.parse((String) message.get("dateFin"));
        for (Room roomToReserve : bestCombinaison) {
            Reservation res = new Reservation((int) ((Math.random() * (99999999)) + 0),
                    hotel.getId(),
                    roomToReserve.getId(),
                    dateDebutDemande,
                    dateFinDemande,
                    roomToReserve.getPrice(),
                    (int) message.get("nbPersonne")
            );
            prix += res.calculatePriceBasedOnDates();
        }
        // Si les concurents non pas de place on augmente de 15% les prix
        if(!concurentPlace){
            prix = prix*1.15;
        }
        answer.put("nomHotel", hotel.getName());
        answer.put("nbChambres", bestCombinaison.size());
        answer.put("dateDebut", message.get("dateDebut"));
        answer.put("dateFin", message.get("dateFin"));
        answer.put("nbPersonnes", message.get("nbPersonne"));
        answer.put("prix", prix);
        answer.put("standing",hotel.getStanding());
        answer.put("ville", hotel.getCity());
        answer.put("pays", hotel.getCountry());

        return answer;
    }

    public void registerReservation(ArrayList<Room> bestCombinaison, JSONObject message) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateDebutDemandeString = (String) message.get("dateDebut");
        String dateFinDemandeString = (String) message.get("dateFin");

        LocalDateTime dateDebutResa = LocalDateTime.parse(dateDebutDemandeString, formatter);
        LocalDateTime dateFinResa = LocalDateTime.parse(dateFinDemandeString, formatter);

        Statement stmt = connect.createStatement();
        ResultSet res = stmt.executeQuery("SELECT * FROM hotel WHERE namehotel='" + message.get("nomHotel")+"'");
        ArrayList<Room> listHotel = new ArrayList<>();
        Hotel hotel = null;
        while (res.next()) {
            hotel = new Hotel(res.getInt(1), res.getString(2), res.getInt(3), res.getString(4), res.getString(5), res.getInt(6), res.getString(7), res.getInt(8), res.getInt(9));
        }

        double prix = 0;

        for (Room roomToReserve:bestCombinaison) {

            Reservation resa = new Reservation((int) ((Math.random() * (99999999)) + 0),
                    hotel.getId(),
                    roomToReserve.getId(),
                    new Date(),
                    new Date(),
                    roomToReserve.getPrice(),
                    roomToReserve.getNbBed()
            );
            prix = resa.calculatePriceBasedOnDates();

            String SQL_INSERT = "INSERT INTO reservation (idhotel, idroom,  price, nbpeople, datestart, dateend) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement myStmt = connect.prepareStatement(SQL_INSERT)) {
                java.sql.Date date = new java.sql.Date(0000 - 00 - 00);
                myStmt.setInt(1, hotel.getId());
                myStmt.setInt(2, roomToReserve.getId());
                myStmt.setDouble(3, prix);
                myStmt.setInt(4, roomToReserve.getNbBed());
                myStmt.setString(5, dateDebutDemandeString);
                myStmt.setString(6, dateFinDemandeString);

                int row = myStmt.executeUpdate();
                // rows affected
                //System.out.println(row); //1
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
                    //Si on est sur la dernière combinaison alors on retourne null car on a fini
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

    private void sendMessageConcurent(JSONObject message, AID id){
        DFAgentDescription dfd = new DFAgentDescription();

        // Recherche d'un agent pour lui envoyer un message
        try {
            DFAgentDescription[] result = DFService.search(myAgent, dfd);
            String out = "";
            int i = 0;
            String service = "";
            // On cherche un agent avec le nomChaine "Ibis" (car notre objet JSON à pour nomChaine: "Ibis"
            while ((service.compareTo(message.get("nomChaine").toString()) != 0) && (i < result.length)) {
                DFAgentDescription desc = (DFAgentDescription) result[i];
                Iterator iter2 = desc.getAllServices();
                while (iter2.hasNext()) {
                    ServiceDescription sd = (ServiceDescription) iter2.next();
                    service = sd.getName();
                    if (service.compareTo(message.get("nomChaine").toString()) == 0) {
                        id = desc.getName();
                        break;
                    }
                }
                System.out.println(id.getName());
                // On envoie le message à tous les agents trouvé
                sendMessage(message, id);
                i++;
            }
        } catch (FIPAException fe) {
        }
    }
}
