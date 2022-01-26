package fr.ul.miage.testMessage;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import org.json.simple.JSONObject;


public class SenderTestReservation extends Agent {


    @Override
    protected void setup() {



        // Création d'un objet JSON test similaire à ceux que notre agent doit recevoir avec le projet final
        Date aujourdhui2 = new Date();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(aujourdhui2);
        cal2.add(Calendar.DAY_OF_MONTH, 1);

        JSONObject msgReservationHotel = new JSONObject();
        msgReservationHotel.put("idProcessus",1);
        msgReservationHotel.put("idProposition",0);
        msgReservationHotel.put("nomChaine","Ibis");

        System.out.println("Hello. My name is " + this.getLocalName());
        //Object[] args = getArguments();
        //String message = (String) args[0];

        AID aid = new AID();
        DFAgentDescription dfd = new DFAgentDescription();
        // Recherche d'un agent pour lui envoyer un message
        try {
            DFAgentDescription[] result = DFService.search(this, dfd);
            String out = "";
            int i = 0;
            String service = "";
            // On cherche un agent avec le nomChaine "Ibis" (car notre objet JSON à pour nomChaine: "Ibis"
            while ((service.compareTo(msgReservationHotel.get("nomChaine").toString()) != 0) && (i < result.length)) {
                DFAgentDescription desc = (DFAgentDescription) result[i];
                Iterator iter2 = desc.getAllServices();
                while (iter2.hasNext()) {
                    ServiceDescription sd = (ServiceDescription) iter2.next();
                    service = sd.getName();
                    if (service.compareTo(msgReservationHotel.get("nomChaine").toString()) == 0) {
                        aid = desc.getName();
                        break;
                    }
                }
                System.out.println(aid.getName());
                // On envoie le message à tous les agents trouvé
                sendMessage(msgReservationHotel, aid);
                i++;
            }
        } catch (FIPAException fe) {
        }

        /// L'agent test est en attente d'une réponse
        responderTestReservation RT = new responderTestReservation(this);
        this.addBehaviour(RT);

    }



    private void sendMessage(JSONObject mess, AID id) {
        try {
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.addReceiver(id);
            aclMessage.setContentObject(mess);
            super.send(aclMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
