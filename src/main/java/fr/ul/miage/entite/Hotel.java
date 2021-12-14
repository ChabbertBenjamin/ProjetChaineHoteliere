package fr.ul.miage.entite;

public class Hotel {

    private int id;
    private String name;
    private int standing;
    private String city;
    private String country;
    private int nbRoom;

    public Hotel(int id, String name, int standing, String city, String country, int nbRoom) {
        this.name = name;
        this.standing = standing;
        this.city = city;
        this.country = country;
        this.nbRoom = nbRoom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStanding() {
        return standing;
    }

    public void setStanding(int standing) {
        this.standing = standing;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getNbRoom() {
        return nbRoom;
    }

    public void setNbRoom(int nbRoom) {
        this.nbRoom = nbRoom;
    }
}
