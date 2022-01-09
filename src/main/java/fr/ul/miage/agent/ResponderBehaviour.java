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

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else {
                this.block();
            }
        }
    }

    @Override
    public boolean done() {
        return false;
    }

    public String getTypeMessage(JSONObject message){
        if(message.get("idHotel") != null){
            return "reservation";
        }else{
            return "recherche";
        }
    }

    public JSONObject processMessage(JSONObject message, String msgType) throws SQLException {
        JSONObject answer = new JSONObject();
        // Si c'est une réservation, on cherche l'hotel en fonction de l'id de l'hôtel dans le message
        if(msgType.equals("reservation")){
            ArrayList<Hotel> listHotelFound = new ArrayList<>();
            for (Hotel h:listHotel) {
                if(message.get("idHotel").equals(h.getId())){
                    listHotelFound.add(h);
                }
            }
            JSONObject resultRecherche = rechercheHotel(listHotelFound, message);

            System.out.println("Résultat de la recherche : " + resultRecherche.toString());
            // La réponse est une confirmation ou un refus, si il n'y a pas de chambre disponible
            answer = reservationMessage(message, jsonToList(resultRecherche));
        }
        // Si c'est une recherche, on cherche les hotels en fonction de la destination
        if(msgType.equals("recherche")){
            //trouver les hotels qui correspondent au message
            ArrayList<Hotel> listHotelFound = new ArrayList<>();
            for (Hotel h:listHotel) {
                if(h.getCity().equals(message.get("destination"))){
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
        for (int i = 0; i < jsonlist.size() ; i++) {
            JSONObject tmp = (JSONObject) jsonlist.get(i);

            ConnectBDD DB = new ConnectBDD();
            Statement stmt = DB.getConn().createStatement();
            ResultSet res = stmt.executeQuery("SELECT * FROM room WHERE id="+tmp.get("idChambre"));
            while(res.next()) {
                Room room = new Room(res.getInt(1),res.getDouble(2),res.getInt(3),res.getInt(4));
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
        for (Hotel h:listHotelFound) {

            ConnectBDD DB = new ConnectBDD();
            Statement stmt = DB.getConn().createStatement();
            ResultSet res = stmt.executeQuery("SELECT * FROM room WHERE idhotel="+h.getId()/*+"and nbbed >=" + message.get("nbPersonne")*/);
            ArrayList<Room> listRoom = new ArrayList<>();
            while(res.next()) {
                Room room = new Room(res.getInt(1),res.getDouble(2),res.getInt(3),res.getInt(4));
                listRoom.add(room);
            }
            for (Room r:listRoom) {

                boolean chambreDisponible = true;

                Statement stmt2 = DB.getConn().createStatement();
                // Récupération des reservation par rapport aux hotels
                ResultSet res2 = stmt2.executeQuery("SELECT * FROM reservation WHERE idroom="+r.getId());
                ArrayList<Reservation> listReservation = new ArrayList<>();
                while(res2.next()) {
                    Reservation reservation = new Reservation(res2.getInt(1),res2.getInt(2),res2.getInt(3),res2.getDate(4),res2.getDate(5),res2.getDouble(6),res2.getInt(7));
                    listReservation.add(reservation);
                }

                for (Reservation reservation:listReservation) {
                    Date dateDebutReservationTrouve = reservation.getDateStart();
                    Date dateFinReservationTrouve = reservation.getDateEnd();

                     /*
                    Savoir si une chambre est libre : lorsqu'on trouve une reservation déjà présente on check :
                        - SI dateDebut de la reservation est entre dateDebutDemande et dateFinDemande
                    ET  - SI dateFin de la reservation est entre dateDebutDemande et dateFinDemande
                    ET  - SI dateDebut de la demande est entre dateDebut de la reservation et dateFin de la reservation
                    ET  - SI dateFin de la demande est entre dateDebut de la reservation et dateFin de la reservation
                     */
                    if(dateDebutReservationTrouve.after(dateDebutDemande) && dateDebutReservationTrouve.before(dateFinDemande)){
                        chambreDisponible = false;
                    }
                    if(dateFinReservationTrouve.after(dateDebutDemande) && dateFinReservationTrouve.before(dateFinDemande)){
                        chambreDisponible = false;
                    }
                    if(dateDebutDemande.after(dateDebutReservationTrouve) && dateDebutDemande.before(dateFinReservationTrouve)){
                        chambreDisponible = false;
                    }
                    if(dateFinDemande.after(dateDebutReservationTrouve) && dateFinDemande.before(dateFinReservationTrouve)){
                        chambreDisponible = false;
                    }

                }
                if (chambreDisponible){
                    this.totalNbBedDispo += r.getNbBed();
                    // Si on trouve une chambre disponible alors on créer un objet JSON correspondant à cette chambre pour la réponse
                    JSONObject tmp = new JSONObject();
                    tmp.put("idHotel",h.getId());
                    tmp.put("idChambre",r.getId());
                    tmp.put("dateDebut",dateDebutDemande);
                    tmp.put("dateFin",dateFinDemande);
                    tmp.put("nbPersonne",message.get("nbPersonne"));
                    tmp.put("prix",message.get("prix"));
                    tmp.put("standing",message.get("standing"));
                    tmp.put("ville",h.getCity());
                    tmp.put("pays",h.getCountry());

                    answer.put(counter,tmp);
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
        if((int) message.get("nbPersonne") > this.totalNbBedDispo){
            answer.put("IdRequete",message.get("idRequete"));
            answer.put("erreur","Aucune chambre disponible pour le nombre de personnes demandé");
        }else{
            //Tri des chambres par rapport à leurs nombre de lit (décroissant)
            listRoom.sort((r1, r2) -> r2.getNbBed()-r1.getNbBed());
            ArrayList<Room> bestCombinaison = new ArrayList<>();
            ArrayList<ArrayList<Room>> combinaisonDejaTest = new ArrayList();
            // Une perfectCombinaison est une combinaison de chambre qui ne laisse aucun lit vide
            boolean perfectCombinaison = false;
            int idRoomInList=-1;
            ArrayList<ArrayList<Room>> listRoomPotentiel = new ArrayList<>();
            int bestRoomIdFromList = -1;
            for (int k = 0; k < listRoom.size(); k++) {

                //Combinaison de 1 seule chambre
                if(k==0){

                    Room room = null;
                    for (int i = 0; i <listRoom.size() ; i++) {
                        if(listRoom.get(i).getNbBed() >= (int) message.get("nbPersonne")){
                            room = listRoom.get(i);
                            bestRoomIdFromList = i;
                        }
                    }
                    if(bestRoomIdFromList == (int) message.get("nbPersonne")){
                        idRoomInList=bestRoomIdFromList;
                        break;
                    }

                    if (bestRoomIdFromList != -1){
                        ArrayList<Room> uniqueRoom = new ArrayList<>();
                        uniqueRoom.add(room);
                        listRoomPotentiel.add(uniqueRoom);
                    }
                // Combinaison de plusieurs chambres
                }else{

                }



                if ((bestRoomIdFromList == -1 || listRoom.get(bestRoomIdFromList).getNbBed() > (int) message.get("nbPersonne")) && listRoom.size() > 1){
                    // Passe à une combinaison de 2 chambres
                    int nbBed =0;
                    ArrayList<Room> combinaison = new ArrayList<>();
                    for (int i = 0; i < listRoom.size() ; i++) {
                        combinaison.add(listRoom.get(i));
                        nbBed += listRoom.get(i).getNbBed();
                        for (int j = i+1; j < listRoom.size() ; j++) {
                            combinaison.add(listRoom.get(j));
                            nbBed += listRoom.get(j).getNbBed();
                            if(nbBed >= (int) message.get("nbPersonne")){
                                bestCombinaison = findBestCombinaison(bestCombinaison,combinaison);
                            }
                            int bedInCombinaison =0;
                            for (Room r2:bestCombinaison) {
                                bedInCombinaison += r2.getNbBed();
                            }
                            if(bedInCombinaison ==(int) message.get("nbPersonne")){

                                perfectCombinaison = true;
                                break;
                            }
                            combinaison.remove(combinaison.size()-1);
                            nbBed -= listRoom.get(j).getNbBed();
                        }
                        if(perfectCombinaison){
                            break;
                        }
                        combinaison.remove(combinaison.size()-1);
                        nbBed -= listRoom.get(i).getNbBed();
                    }
                    if (perfectCombinaison){
                        break;
                    }
                    listRoomPotentiel.add(bestCombinaison);
                    bestCombinaison =null;
                }
                if (perfectCombinaison){
                    break;
                }
            }

            System.out.println("on a trouvé les combinaisons : " + listRoomPotentiel);

            System.out.println("la meilleur combinaison est : " + bestCombinaison);
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

        }
        return answer;
    }

    public ArrayList<Room> findBestCombinaison(ArrayList<Room> combinaison1,ArrayList<Room> combinaison2){
        ArrayList<Room> answer = new ArrayList<>();
        int nbBedCombinaison1 =0;
        int nbBedCombinaison2 =0;
        boolean test = false;
        if(combinaison1 != null && !combinaison1.isEmpty()){
            for (Room r: combinaison1) {
                nbBedCombinaison1 += r.getNbBed();
            }
        }else{
            answer.addAll(combinaison2);
            test = true;
        }
        if(!test){
            for (Room r: combinaison2) {
                nbBedCombinaison2 += r.getNbBed();
            }

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
