package fr.ul.miage.agent;

import fr.ul.miage.entite.Hotel;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
                    String messageContent = aclMessage.getContent();
                    //extraction de la date
                    int posDateDebut = messageContent.indexOf("\"date_debut\"");
                    int posDestination =messageContent.indexOf(",\"destination\"");

                    String dateDebut = messageContent.substring(posDateDebut+"\"date_debut\"".length()+1,posDestination);

                    int posDateFin = messageContent.indexOf("\"date_fin\"");
                    int posNomChaine =messageContent.indexOf(",\"nomChaine\"");


                    String dateFin = messageContent.substring(posDateFin+"\"date_fin\"".length()+1,posNomChaine);



                    messageContent = messageContent.replace(dateDebut, "\""+dateDebut+ "\"");
                    messageContent = messageContent.replace(dateFin, "\""+dateFin+ "\"");

                    System.out.println(myAgent.getLocalName() + ": I receive \n" + aclMessage + "\nwith content\n" + messageContent);

                    processMessage(messageContent);


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

    public void processMessage(String message) throws ParseException {


        JSONParser parser = new JSONParser();
        JSONObject msgJSON=(JSONObject) parser.parse(message);



        //renvoyer les hotels qui correspondent au message
        ArrayList<Hotel> listHotelToReturn = new ArrayList<>();
        for (Hotel h:listHotel) {
            if(h.getCity().equals(msgJSON.get("destination"))){
                listHotelToReturn.add(h);
            }
        }

        for (Hotel h:listHotelToReturn) {
            System.out.println(h.toString());
        }



        //renvoyer les hotels avec des chambres disponible


    }
}
