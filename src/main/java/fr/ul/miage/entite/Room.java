package fr.ul.miage.entite;

import fr.ul.miage.repository.DatabaseManager;

import java.sql.SQLException;

public class Room {

    private int id;
    private Double price;
    private int nbBed;
    private int idHotel;
    private int floorPrice;

    DatabaseManager dm = new DatabaseManager();

    public Room(int id, Double price, int nbBed, int idHotel) {
        this.id = id;
        this.price = price;
        this.nbBed = nbBed;
        this.idHotel = idHotel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public int getFloorPrice() {return floorPrice;}

    public void setFloorPrice(int floorPrice) {this.floorPrice = floorPrice;}

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getNbBed() {
        return nbBed;
    }

    public void setNbBed(int nbBed) {
        this.nbBed = nbBed;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public double getHighSeasonPrice() throws SQLException {
        return this.price * dm.getHighSeasonIndex();
    }

    public double getLowSeasonPrice() throws SQLException {
        return this.price * dm.getLowSeasonIndex();
    }

    public double getWeekPrice() throws SQLException {
        return this.price * dm.getWeekIndex();
    }

    public double getWeekendPrice() throws SQLException {
        return this.price * dm.getWeekendIndex();
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", price=" + price +
                ", nbBed=" + nbBed +
                ", idHotel=" + idHotel +
                '}';
    }
}
