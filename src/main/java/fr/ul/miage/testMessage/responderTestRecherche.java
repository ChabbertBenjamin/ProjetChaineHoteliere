package fr.ul.miage.testMessage;


import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONObject;

import java.util.ArrayList;


public class responderTestRecherche extends Behaviour {
    private final static MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    public responderTestRecherche(SenderTestRecherche senderTestRecherche) {
        super(senderTestRecherche);

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
