package fr.ul.miage.agent;

import fr.ul.miage.message.MessageRechercheHotel;
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
import java.text.DateFormat;

public class SenderTestAgent extends Agent {


    @Override
    protected void setup() {

        Date aujourdhui = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(aujourdhui);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        MessageRechercheHotel messageRechercheHotel = new MessageRechercheHotel(1,aujourdhui,cal.getTime(),20,2,"Nancy","3 étoiles","Ibis");


        System.out.println("Hello. My name is " + this.getLocalName());
        Object[] args = getArguments();
        //String message = (String) args[0];

        AID aid = new AID();

        DFAgentDescription dfd = new DFAgentDescription();

        try {
            DFAgentDescription[] result = DFService.search(this, dfd);
            String out = "";
            int i = 0;
            String service = "";
           // System.out.println(obj.get("nomChaine").toString());
            while ((service.compareTo(messageRechercheHotel.getNomChaine().toString()) != 0) && (i < result.length)) {
                DFAgentDescription desc = (DFAgentDescription) result[i];
                Iterator iter2 = desc.getAllServices();
                while (iter2.hasNext()) {
                    ServiceDescription sd = (ServiceDescription) iter2.next();
                    service = sd.getName();
                    if (service.compareTo(messageRechercheHotel.getNomChaine().toString()) == 0) {
                        aid = desc.getName();
                        break;
                    }
                }
                System.out.println(aid.getName());


                sendMessage(messageRechercheHotel, aid);
                i++;
            }
        } catch (FIPAException fe) {
        }
        responderTest RT = new responderTest(this);
        this.addBehaviour(RT);

    }



    private void sendMessage(MessageRechercheHotel mess, AID id) {
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
