package fr.ul.miage.message;

import java.io.Serializable;
import java.util.Date;

public class MessageRechercheHotel implements Serializable {

   private int idRequete;
   private Date dateDebut;
   private Date dateFin;
   private double prix;
   private int nbPersonne;
   private String destination;
   private String standing;
   private String nomChaine;

    public MessageRechercheHotel(int idRequete, Date dateDebut, Date dateFin, double prix, int nbPersonne, String destination, String standing, String nomChaine) {
        this.idRequete = idRequete;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prix = prix;
        this.nbPersonne = nbPersonne;
        this.destination = destination;
        this.standing = standing;
        this.nomChaine = nomChaine;
    }

    public int getIdRequete() {
        return idRequete;
    }

    public void setIdRequete(int idRequete) {
        this.idRequete = idRequete;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getNbPersonne() {
        return nbPersonne;
    }

    public void setNbPersonne(int nbPersonne) {
        this.nbPersonne = nbPersonne;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStanding() {
        return standing;
    }

    public void setStanding(String standing) {
        this.standing = standing;
    }

    public String getNomChaine() {
        return nomChaine;
    }

    public void setNomChaine(String nomChaine) {
        this.nomChaine = nomChaine;
    }

    @Override
    public String toString() {
        return "MessageRechercheHotel{" +
                "idRequete=" + idRequete +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", prix=" + prix +
                ", nbPersonne=" + nbPersonne +
                ", destination='" + destination + '\'' +
                ", standing='" + standing + '\'' +
                ", nomChaine='" + nomChaine + '\'' +
                '}';
    }
}
