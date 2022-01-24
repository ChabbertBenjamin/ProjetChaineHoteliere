package fr.ul.miage.entite;

import fr.ul.miage.repository.DatabaseManager;

import java.sql.SQLException;

public class Hotel {

    private int id;
    private String name;
    private int standing;
    private String city;
    private String country;
    private double floorPrice;
    private int nbServices;
    private int nbRoom;
    private int nbEmployees;

    DatabaseManager dm = new DatabaseManager();

    public Hotel(int id, String name, int standing, String city, String country, int nbRoom, String nameChain, int nbEmployees, int nbServices) throws SQLException {
        this.id = id;
        this.name = name;
        this.standing = standing;
        this.city = city;
        this.country = country;
        this.nbRoom = nbRoom;
        this.nbEmployees = nbEmployees;
        this.nbServices = nbServices;
        calculateFloorPrice();
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

    public void setStanding(int standing) throws Exception {
        if (standing > 10 || standing < 0) {
            throw new Exception("Le standing doit être compris entre 0 et 10");
        }
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

    public double getFloorPrice() {return floorPrice;}

    public void setFloorPrice(double floorPrice) {
        this.floorPrice = floorPrice;
    }

    public int getNbServices() {
        return nbServices;
    }

    public void setNbServices(int nbServices) {
        this.nbServices = nbServices;
    }

    public int getNbEmployees() {
        return nbEmployees;
    }

    public void setNbEmployees(int nbEmployees) {
        this.nbEmployees = nbEmployees;
    }

    public double normalizeStanding() throws SQLException {
        if ((dm.getMaxStanding() - dm.getMinStanding()) != 0) {
            return (this.standing - dm.getMinStanding()) / (dm.getMaxStanding() - dm.getMinStanding());
        } else {
            return 0;
        }
    }

    public double normalizeNbRoom() throws SQLException {
        if (dm.getMaxNbRoom() - dm.getMinNbRoom() != 0) {
            return (this.nbRoom - dm.getMinNbRoom()) / (dm.getMaxNbRoom() - dm.getMinNbRoom());
        } else {
            return 0;
        }
    }

    public double normalizeNbEmployees() throws SQLException {
        if ((dm.getMaxNbEmployees() - dm.getMinNbEmployees() != 0)) {
            return (this.nbEmployees - dm.getMinNbEmployees()) / (dm.getMaxNbEmployees() - dm.getMinNbEmployees());
        } else {
            return 0;
        }
    }

    public double normalizeNbServices() throws SQLException {
        if ((dm.getMaxNbServices() - dm.getMinNbServices()) != 0) {
            return (this.nbServices - dm.getMinNbServices()) / (dm.getMaxNbServices() - dm.getMinNbServices());
        } else {
            return 0;
        }
    }

    //Normalize and calculate floor price
    public void calculateFloorPrice() throws SQLException {
        double standingNormalized = normalizeStanding();
        double nbRoomNormalized = normalizeNbRoom();
        double nbEmployeesNormalized = normalizeNbEmployees();
        double nbServicesNormalized = normalizeNbServices();
        int basePrice = 40;

        double newFloorPrice = basePrice * (standingNormalized + nbRoomNormalized + nbEmployeesNormalized + nbServicesNormalized);

        setFloorPrice(newFloorPrice);
    }


    @Override
    public String toString() {
        return "Hotel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", standing=" + standing +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", nbRoom=" + nbRoom +
                '}';
    }


}
