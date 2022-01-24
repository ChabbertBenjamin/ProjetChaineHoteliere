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


public class SenderTestReservation extends Agent {


    @Override
    protected void setup() {



        // Création d'un objet JSON test similaire à ceux que notre agent doit recevoir avec le projet final
        Date aujourdhui = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(aujourdhui);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        JSONObject msgRechercheHotel = new JSONObject();
        msgRechercheHotel.put("idRequete",1);
        msgRechercheHotel.put("dateDemande",aujourdhui);
        msgRechercheHotel.put("dateDebut",aujourdhui);
        msgRechercheHotel.put("dateFin",cal.getTime());
        msgRechercheHotel.put("prix",20.0);
        msgRechercheHotel.put("nbPersonne",7);
        msgRechercheHotel.put("destination","Nancy");
        msgRechercheHotel.put("standing","3 étoiles");
        msgRechercheHotel.put("nomChaine","Ibis");


/*



"RechercheHotel": {
    "idRequete": "id", // identifiant de requete
    "dateDemande": “date” //date de la demande
    "dateDebut": "date", // date du début du séjour à l'hôtel
    "dateFin": "date", // date de fin du séjour à l'hôtel
    "prix": "double", // fourchette de prix
    "nbPersonne" : "int", //nombre de personne
    "destination": "string", // destination souhaité
    "standing": "int", // classe souhaité (3 étoiles)
       "nomChaine": "string", // nom de la chaine d’hôtel souhaité
}


"ReservationHotel": {
      "id_processus" : "id", //identifiant du processus de la recherche
      "id_proposition" : "int", //identifiant de la proposition de recherche
}
*/




        Date aujourdhui2 = new Date();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(aujourdhui2);
        cal2.add(Calendar.DAY_OF_MONTH, 1);

        JSONObject msgReservationHotel = new JSONObject();
        msgReservationHotel.put("idProcessus",0);
        msgReservationHotel.put("idProposition",0);



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
                //sendMessage(msgRechercheHotel, aid);
                sendMessage(msgReservationHotel, aid);
                i++;
            }
        } catch (FIPAException fe) {
        }

        // L'agent test est en attente d'une réponse
        //responderTest RT = new responderTest(this);
        //this.addBehaviour(RT);

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
