package fr.ul.miage.agent;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Reservation;
import fr.ul.miage.entite.Room;
import fr.ul.miage.launch.Simu;
import fr.ul.miage.model.ConnectBDD;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;

public class AgentChaineHoteliere extends GuiAgent {
    public static final int EXIT = 0;

    private ArrayList<Hotel> listHotel= new ArrayList<>();



    //Initialisation de l'agent
    protected void setup(){
        //Object[] args = getArguments();
        //chaineHotel = (String) args[0];
        //String test = (String) args[1];

        try {
            ConnectBDD DB = new ConnectBDD();
            Statement stmt = DB.getConn().createStatement();
            ResultSet res = stmt.executeQuery("SELECT * FROM hotel");
            while(res.next()){
                System.out.println(res.getInt(1)+"  "+res.getString(2) +"  "+res.getString(3));
                Hotel h = new Hotel(res.getInt(1),res.getString(2),res.getInt(3),res.getString(4),res.getString(5),res.getInt(6));


                Statement stmt2 = DB.getConn().createStatement();
                ResultSet res2 = stmt2.executeQuery("SELECT * FROM reservation WHERE idhotel="+res.getInt(1));
                ArrayList<Reservation> listReservation = new ArrayList<>();
                while(res2.next()) {
                    Reservation reservation = new Reservation(res2.getInt(1),res2.getInt(2),res2.getInt(3),res2.getDate(4),res2.getDate(5),res2.getDouble(6),res2.getInt(7));
                    listReservation.add(reservation);
                }
                h.setListReservation(listReservation);


                Statement stmt3 = DB.getConn().createStatement();
                ResultSet res3 = stmt3.executeQuery("SELECT * FROM room WHERE idhotel="+res.getInt(1));
                ArrayList<Room> listRoom = new ArrayList<>();
                while(res3.next()) {
                    Room room = new Room(res3.getInt(1),res3.getDouble(2),res3.getInt(3),res3.getInt(4));
                    listRoom.add(room);
                }
                h.setListRoom(listRoom);

                listHotel.add(h);
            }




        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Salut je suis "+getLocalName()+" et je gères "+listHotel.size()+" hotel(s).");
        this.registerService();
        ResponderBehaviour RB = new ResponderBehaviour(this, listHotel);
        this.addBehaviour(RB);





    }

    // On enregistre l'agent
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

    // On termine l'agent
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

    @Override
    protected void onGuiEvent(GuiEvent guiEvent) {
        if (guiEvent.getType() == AgentChaineHoteliere.EXIT) {
            doDelete();
        }
    }

    public ArrayList<Hotel> getListHotel() {
        return listHotel;
    }

    public void setListHotel(ArrayList<Hotel> listHotel) {
        this.listHotel = listHotel;
    }
}
