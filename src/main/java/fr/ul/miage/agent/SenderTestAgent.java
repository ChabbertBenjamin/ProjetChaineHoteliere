package fr.ul.miage.agent;

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
public class SenderTestAgent extends Agent {


    @Override
    protected void setup() {
        JSONObject obj=new JSONObject();
        obj.put("idHotel",1);
        obj.put("idChambre",1);
        obj.put("dateDebut","Jeudi");
        obj.put("dateFin","Vendredi");
        obj.put("nbPersonnes",2);





        System.out.println("Hello. My name is " + this.getLocalName());
        Object[] args = getArguments();
        String message = (String) args[0];

        AID aid = new AID();

        DFAgentDescription dfd = new DFAgentDescription();

        try {
            DFAgentDescription[] result = DFService.search(this, dfd);
            String out = "";
            int i = 0;
            String service = "";
            while ((service.compareTo("F1") != 0) && (i < result.length)) {
                DFAgentDescription desc = (DFAgentDescription) result[i];
                Iterator iter2 = desc.getAllServices();
                while (iter2.hasNext()) {
                    ServiceDescription sd = (ServiceDescription) iter2.next();
                    service = sd.getName();
                    if (service.compareTo("F1") == 0) {
                        aid = desc.getName();
                        break;
                    }
                }
                System.out.println(aid.getName());

                sendMessage(obj.toJSONString(), aid);
                i++;
            }
        } catch (FIPAException fe) {
        }

    }

    private void sendMessage(String mess, AID id) {
        try {
            ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            aclMessage.addReceiver(id);

            aclMessage.setContent(mess);

            super.send(aclMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
