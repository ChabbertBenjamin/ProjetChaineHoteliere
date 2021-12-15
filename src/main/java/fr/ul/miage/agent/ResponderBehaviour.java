package fr.ul.miage.agent;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Room;
import fr.ul.miage.message.MessageRechercheHotel;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

public class ResponderBehaviour extends Behaviour {
    private final static MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    private ArrayList<Hotel> listHotel;
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



        //renvoyer les hotels qui correspondent au message
       ArrayList<Hotel> listHotelFound = new ArrayList<>();
        for (Hotel h:listHotel) {
            if(h.getCity().equals(message.getDestination())){
                listHotelFound.add(h);
            }
        }


        for (Hotel h:listHotelFound) {




            for (Room r:h.getListRoom()) {

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
