package fr.ul.miage.testMessage;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import org.json.simple.JSONObject;


public class SenderTestRecherche extends Agent {


    @Override
    protected void setup() {
        // Création d'un objet JSON test similaire à ceux que notre agent doit recevoir avec le projet final
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date aujourdhui = new Date();
        Date dateDemande = new Date();
        try {
            dateDemande = formater.parse("2021-01-15 15:14:53");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(aujourdhui);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        JSONObject msgRechercheHotel = new JSONObject();
        msgRechercheHotel.put("idRequete",1);
        msgRechercheHotel.put("dateDemande",formater.format(dateDemande));
        msgRechercheHotel.put("dateDebut",formater.format(aujourdhui));
        msgRechercheHotel.put("dateFin",formater.format(cal.getTime()));
        msgRechercheHotel.put("prix",20.0);
        msgRechercheHotel.put("nbPersonne",7);
        msgRechercheHotel.put("destination","Bangkok");
        msgRechercheHotel.put("standing",4);
        msgRechercheHotel.put("nomChaine","F1");


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
            while ((service.compareTo(msgRechercheHotel.get("nomChaine").toString()) != 0) && (i < result.length)) {
                DFAgentDescription desc = (DFAgentDescription) result[i];
                Iterator iter2 = desc.getAllServices();
                while (iter2.hasNext()) {
                    ServiceDescription sd = (ServiceDescription) iter2.next();
                    service = sd.getName();
                    if (service.compareTo(msgRechercheHotel.get("nomChaine").toString()) == 0) {
                        aid = desc.getName();
                        break;
                    }
                }
                System.out.println(aid.getName());
                // On envoie le message à tous les agents trouvé
                sendMessage(msgRechercheHotel, aid);
                i++;
            }
        } catch (FIPAException fe) {
        }

        // L'agent test est en attente d'une réponse
        responderTestRecherche RT = new responderTestRecherche(this);
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
