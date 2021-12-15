package fr.ul.miage.entite;

public class Room {

    private int id;
    private Double price;
    private int nbBed;
    private int idHotel;

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
