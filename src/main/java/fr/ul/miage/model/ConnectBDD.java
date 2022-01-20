package fr.ul.miage.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectBDD {

    private static final String url = "jdbc:postgresql://plg-broker.ad.univ-lorraine.fr:5432/HotelAgency";
    private static final String user = "m1user1_18";
    private static final String password ="m1user1_18";

    private static Connection connect;

    public static Connection getInstance() {
        if (connect == null) {
            try {
                connect = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                System.err.println("Vous devez être connecté au serveur de l'IDMC pour pouvoir utiliser l'application");
            }
        }

        return connect;
    }
}
