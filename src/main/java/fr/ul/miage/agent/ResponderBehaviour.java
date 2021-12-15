package fr.ul.miage.agent;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Reservation;
import fr.ul.miage.entite.Room;
import fr.ul.miage.message.MessageRechercheHotel;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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

                    MessageRechercheHotel msg = (MessageRechercheHotel) aclMessage.getContentObject();

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

                    String mess = processMessage(msg);
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

    public String processMessage(MessageRechercheHotel message) throws ParseException, java.text.ParseException {
        String answer = "";

        //JSONParser parser = new JSONParser();
        //JSONObject msgJSON=(JSONObject) parser.parse(message);


        //trouver les hotels qui correspondent au message
       ArrayList<Hotel> listHotelFound = new ArrayList<>();
        for (Hotel h:listHotel) {
            if(h.getCity().equals(message.getDestination())){
                listHotelFound.add(h);
            }
        }

        Date dateDebut = message.getDateDebut();
        Date dateFin = message.getDateFin();




        /*
        Savoir si une chambre est libre lorsqu'on trouve une reservation déjà présente on check :
            - SI dateDebut de la reservation est entre dateDebutDemande et dateFinDemande
        ET  - SI dateFin de la reservation est entre dateDebutDemande et dateFinDemande
        ET  - SI dateDebut de la demande est entre dateDebut de la reservation et dateFin de la reservation
        ET  - SI dateFin de la demande est entre dateDebut de la reservation et dateFin de la reservation

        SI VRAI = CHAMBRE DISPONIBLE
        SINON = CHAMBRE INDISPONIBLE
         */
        for (Hotel h:listHotelFound) {
            for (Room r:h.getListRoom()) {
                Date dateDebutDemande = message.getDateDebut();
                Date dateFinDemande = message.getDateFin();

                ArrayList<Reservation> listReservation = Hotel.getReservationfromRoom(h.getListReservation(),r.getId());
                for (Reservation reservation:listReservation) {
                    Date dateDebutReservationTrouve = reservation.getDateStart();
                    Date dateFinReservationTrouve = reservation.getDateEnd();

                    // CONDITION
                    System.out.println(dateDebutReservationTrouve.toString());
                }



            }
        }



        //renvoyer les hotels avec des chambres disponible


        return answer;
    }

    private void sendMessage(String mess, AID id) {
        try {
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.addReceiver(id);

            aclMessage.setContent(mess);

            myAgent.send(aclMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
