package fr.ul.miage.launch;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.ExtendedProperties;
import jade.core.Runtime;

/**
 * launch the simulation of travelers and travel agencies
 *
 * @author emmanueladam
 */
public class Simu {

    public static final Logger logger = Logger.getLogger("simu");

    /**
     * @param args
     */
    public static void main(String... args) {

        logger.setLevel(Level.ALL);
        Handler fh;
        try {
            fh = new FileHandler("./simuAgences.xml", false);
            logger.addHandler(fh);
        }
        catch (SecurityException | IOException e) { e.printStackTrace(); }

        // ******************JADE******************

        // permettre d'envoyer des arguments au lancement de JADE
        var pp = new ExtendedProperties();
        // ajout de l'interface
        pp.setProperty(Profile.GUI, "true");
        // add the Topic Management Service
        //pp.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");

        var lesAgents = new StringBuilder();
        lesAgents.append("Ibis:fr.ul.miage.agent.AgentChaineHoteliere;");

        pp.setProperty(Profile.AGENTS, lesAgents.toString());
        // creation d'un profil par defaut
        var pMain = new ProfileImpl(pp);

        // lancement du main-container de JADE
        Runtime.instance().createMainContainer(pMain);

    }

}
