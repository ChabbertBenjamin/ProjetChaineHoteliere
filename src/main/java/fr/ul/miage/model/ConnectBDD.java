package fr.ul.miage.model;

import java.sql.*;
import java.util.Properties;

public class ConnectBDD {

    private final String url = "jdbc:postgresql://localhost/ChaineHoteliere";
    private final String user = "postgres";
    private final String password ="root";

    private  Connection conn = DriverManager.getConnection(url, user, password);
    public ConnectBDD() throws SQLException {

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            //System.out.println("Connected to the PostgreSQL server successfully.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

    public Connection getConn() {
        return conn;
    }
}
