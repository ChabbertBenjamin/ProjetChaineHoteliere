package fr.ul.miage.repository;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Room;
import fr.ul.miage.model.ConnectBDD;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.Month;

public class DatabaseManager {

    private  Connection connect = ConnectBDD.getInstance();

    public double getHighSeasonIndex() throws SQLException {
        return getIndex("high_season");
    }

    public double getLowSeasonIndex() throws SQLException {
        return getIndex("low_season");
    }

    public double getWeekendIndex() throws SQLException {
        return getIndex("weekend");
    }

    public double getWeekIndex() throws SQLException {
        return getIndex("week");
    }

    //High Season
    public int getHighSeasonStartMonth() throws SQLException {
        return getGlobalDataMonth("high_season_start");
    }

    public int getHighSeasonStartDay() throws SQLException {
        return getGlobalDataDay("high_season_start");
    }

    public int getHighSeasonEndMonth() throws SQLException {
        return getGlobalDataMonth("high_season_end");
    }

    public int getHighSeasonEndDay() throws SQLException {
        return getGlobalDataDay("high_season_end");
    }

    //Low Season
    public int getLowSeasonStartMonth() throws SQLException {
        return getGlobalDataMonth("low_season_start");
    }

    public int getLowSeasonStartDay() throws SQLException {
        return getGlobalDataDay("low_season_start");
    }

    public int getLowSeasonEndMonth() throws SQLException {
        return getGlobalDataMonth("low_season_end");
    }

    public int getLowSeasonEndDay() throws SQLException {
        return getGlobalDataDay("low_season_end");
    }

    public Room getRoomById(int id) throws SQLException {
        Room room = null;
        String sql = "SELECT *"
                + "FROM room"
                + "WHERE id = ?";
        ResultSet result = getResultSetForGetById(id, sql);
        while (result.next()) {
            room = new Room(result.getInt(1), result.getDouble(2), result.getInt(3), result.getInt(4));
        }

        return room;
    }

    public Hotel getHotelById(int id) throws SQLException {
        Hotel hotel = null;
        String sql = "SELECT *"
                + "FROM hotel"
                + "WHERE id = ?";
        ResultSet result = getResultSetForGetById(id, sql);
        while (result.next()) {
            hotel = new Hotel(result.getInt(1), result.getString(2), result.getInt(3), result.getString(4), result.getString(5), result.getInt(6));
        }

        return hotel;
    }

    public double getIndex(String indexName) throws SQLException {
        String sql = "SELECT my_index"
                + "FROM my_indexes"
                + "WHERE name = ?";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setString(1, indexName);
        ResultSet result = stmt.executeQuery();
        return result.getDouble(3);
    }

    public int getGlobalDataMonth(String valueName) throws SQLException {
        String sql = "SELECT my_month"
                + "FROM global_data"
                + "WHERE name = ?";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setString(1, valueName);
        ResultSet result = stmt.executeQuery();
        return result.getInt(1);
    }

    public int getGlobalDataDay(String valueName) throws SQLException {
        String sql = "SELECT my_day"
                + "FROM global_data"
                + "WHERE name = ?";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setString(1, valueName);
        ResultSet result = stmt.executeQuery();
        return result.getInt(1);
    }

    private ResultSet getResultSetForGetById(int id, String sql) throws SQLException {
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet result = stmt.executeQuery();
        return result;
    }
}
