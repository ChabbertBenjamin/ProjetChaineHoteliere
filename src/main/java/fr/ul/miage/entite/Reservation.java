package fr.ul.miage.entite;

import java.util.Date;

public class Reservation {

    private int id;
    private int idHotel;
    private int idRoom;
    private Date dateStart;
    private Date dateEnd;
    private double price;
    private int nbPeople;

    public Reservation(int id, int idHotel, int idRoom, Date dateStart, Date dateEnd, double price, int nbPeople) {
        this.id = id;
        this.idHotel = idHotel;
        this.idRoom = idRoom;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.price = price;
        this.nbPeople = nbPeople;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public int getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(int idRoom) {
        this.idRoom = idRoom;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getNbPeople() {
        return nbPeople;
    }

    public void setNbPeople(int nbPeople) {
        this.nbPeople = nbPeople;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idHotel=" + idHotel +
                ", idRoom=" + idRoom +
                ", dateStart=" + dateStart +
                ", dateEnd=" + dateEnd +
                ", price=" + price +
                ", nbPeople=" + nbPeople +
                '}';
    }
}
