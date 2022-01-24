package fr.ul.miage.agent;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Reservation;
import fr.ul.miage.entite.Room;
import fr.ul.miage.launch.Simu;
import fr.ul.miage.model.ConnectBDD;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;

public class AgentChaineHoteliere extends Agent {
    public static final int EXIT = 0;

    // Contient seulement la list des hotels que notre agent doit gérer
    private ArrayList<Hotel> listHotel= new ArrayList<>();



    //Initialisation de l'agent
    protected void setup(){
        //Object[] args = getArguments();
        //chaineHotel = (String) args[0];
        //String test = (String) args[1];

        try {
            // Connexion à la BDD
            ConnectBDD DB = new ConnectBDD();
            Statement stmt = DB.getInstance().createStatement();
            // Récupération des hotels
            ResultSet res = stmt.executeQuery("SELECT * FROM hotel");
            while(res.next()){
                //System.out.println(res.getInt(1)+"  "+res.getString(2) +"  "+res.getString(3));
                Hotel h = new Hotel(res.getInt(1), res.getString(2), res.getInt(3), res.getString(4), res.getString(5), res.getInt(6), res.getString(7), res.getInt(8), res.getInt(10));
                listHotel.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Salut je suis "+getLocalName()+" et je gères "+listHotel.size()+" hotel(s).");
        this.registerService();


        // L'agent attent un message
        ResponderBehaviour RB = new ResponderBehaviour(this);
        this.addBehaviour(RB);





    }

    // On enregistre l'agent comme un service
    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(super.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(getLocalName());
        sd.setName(getLocalName());

        dfd.addServices(sd);
        try{
            DFService.register(this, dfd);
        }catch(FIPAException e){
            System.out.println(super.getLocalName() + " erreur");
            super.doDelete();
        }
    }

    // On termine l'agent proprement s'il y a un problème
    @Override
    protected void takeDown(){
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            Simu.logger.log(Level.SEVERE, fe.getMessage());
        }
        Simu.logger.log(Level.INFO, "Agent: " + getAID().getName() + " part en retraite.");
        //window.dispose();
    }



    public ArrayList<Hotel> getListHotel() {
        return listHotel;
    }

    public void setListHotel(ArrayList<Hotel> listHotel) {
        this.listHotel = listHotel;
    }
}
