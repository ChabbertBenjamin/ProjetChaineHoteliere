package fr.ul.miage.testMessage;


import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONObject;


public class responderTestReservation extends Behaviour {
    private final static MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    public responderTestReservation(SenderTestReservation SenderTestReservation) {
        super(SenderTestReservation);

    }

    @Override
    public void action() {
        // On affiche la réponse de l'agentChaineHoteliere pour vérifier qu'elle à bien été reçu
        while (true) {
            ACLMessage aclMessage = myAgent.receive(mt);
            if (aclMessage != null) {
                try {
                    JSONObject msg = (JSONObject) aclMessage.getContentObject();
                    System.out.println(myAgent.getLocalName() + ": I receive a message with content\n" + msg.toString());
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




}
