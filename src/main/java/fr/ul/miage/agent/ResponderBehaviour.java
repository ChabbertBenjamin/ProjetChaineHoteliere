package fr.ul.miage.agent;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Reservation;
import fr.ul.miage.entite.Room;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Date;

public class ResponderBehaviour extends Behaviour {
    private final static MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    private final ArrayList<Hotel> listHotel;
    public ResponderBehaviour(AgentChaineHoteliere agentChaineHoteliere, ArrayList<Hotel> listHotel) {
        super(agentChaineHoteliere);
        this.listHotel = listHotel;
    }

    @Override
    public void action() {
        while (true) {
            ACLMessage aclMessage = myAgent.receive(mt);
            if (aclMessage != null) {
                try {
                    //String messageContent = aclMessage.getContent();

                    JSONObject msg = (JSONObject) aclMessage.getContentObject();

                    //extraction de la date
                    /*
                    int posDateDebut = messageContent.indexOf("\"date_debut\"");
                    int posDestination =messageContent.indexOf(",\"destination\"");

                    String dateDebut = messageContent.substring(posDateDebut+"\"date_debut\"".length()+1,posDestination);

                    int posDateFin = messageContent.indexOf("\"date_fin\"");
                    int posNomChaine =messageContent.indexOf(",\"nomChaine\"");


                    String dateFin = messageContent.substring(posDateFin+"\"date_fin\"".length()+1,posNomChaine);



                    messageContent = messageContent.replace(dateDebut, "\""+dateDebut+ "\"");
                    messageContent = messageContent.replace(dateFin, "\""+dateFin+ "\"");
*/
                    System.out.println(myAgent.getLocalName() + ": I receive \n" + aclMessage + "\nwith content\n" + msg.toString());

                    JSONObject mess = processMessage(msg);
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

    public JSONObject processMessage(JSONObject message) throws ParseException, java.text.ParseException {
        JSONObject answer = new JSONObject();

        //JSONParser parser = new JSONParser();
        //JSONObject msgJSON=(JSONObject) parser.parse(message);


        //trouver les hotels qui correspondent au message
       ArrayList<Hotel> listHotelFound = new ArrayList<>();
        for (Hotel h:listHotel) {
            if(h.getCity().equals(message.get("destination"))){
                listHotelFound.add(h);
            }
        }




        Date dateDebutDemande = (Date) message.get("dateDebut");
        Date dateFinDemande = (Date) message.get("dateFin");

        int counter = 0;
        for (Hotel h:listHotelFound) {
           ArrayList<Room> listRoom = new ArrayList<>();
            for (Room r:h.getListRoom()) {

                boolean chambreDisponible = true;
                ArrayList<Reservation> listReservation = Hotel.getReservationfromRoom(h.getListReservation(),r.getId());
                for (Reservation reservation:listReservation) {
                    Date dateDebutReservationTrouve = reservation.getDateStart();
                    Date dateFinReservationTrouve = reservation.getDateEnd();

                     /*
                    Savoir si une chambre est libre lorsqu'on trouve une reservation déjà présente on check :
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
                    //listRoom.add(r);

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
