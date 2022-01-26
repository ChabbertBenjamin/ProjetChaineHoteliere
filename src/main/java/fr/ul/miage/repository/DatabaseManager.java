package fr.ul.miage.repository;

import fr.ul.miage.entite.Hotel;
import fr.ul.miage.entite.Room;
import fr.ul.miage.model.ConnectBDD;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.Calendar;

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

    public double getNoConcurrentIndex() throws SQLException {
        return getIndex("no_concurrent");
    }

    public double getNoBookInAdvanceIndex() throws SQLException {
        return getIndex("book_in_advance");
    }

    public double getLackOfReservationIndex() throws SQLException {
        return getIndex("lackofreservation");
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
                + " FROM room"
                + " WHERE id = ?";
        ResultSet result = getResultSetForGetById(id, sql);
        while (result.next()) {
            room = new Room(result.getInt(1), result.getDouble(2), result.getInt(3), result.getInt(4));
        }

        return room;
    }

    public Hotel getHotelById(int id) throws SQLException {
        Hotel hotel = null;
        String sql = "SELECT *"
                + " FROM hotel"
                + " WHERE id = ?";
        ResultSet result = getResultSetForGetById(id, sql);
        while (result.next()) {
            hotel = new Hotel(result.getInt(1), result.getString(2), result.getInt(3), result.getString(4), result.getString(5), result.getInt(6), result.getString(7), result.getInt(8), result.getInt(9));
        }

        return hotel;
    }

    public double getIndex(String indexName) throws SQLException {
        String sql = "SELECT my_index"
                + " FROM my_indexes"
                + " WHERE name = ?";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setString(1, indexName);
        ResultSet result = stmt.executeQuery();
        double res = 0;
        while (result.next()) {
            res = result.getDouble(1);
        }
        return res;
    }

    public int getGlobalDataMonth(String valueName) throws SQLException {
        String sql = "SELECT my_month"
                + " FROM global_data"
                + " WHERE name = ?";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setString(1, valueName);
        ResultSet result = stmt.executeQuery();
        int res = 0;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getGlobalDataDay(String valueName) throws SQLException {
        String sql = "SELECT my_day"
                + " FROM global_data"
                + " WHERE name = ?";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setString(1, valueName);
        ResultSet result = stmt.executeQuery();
        int res = 0;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    private ResultSet getResultSetForGetById(int id, String sql) throws SQLException {
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setInt(1, id);
        ResultSet result = stmt.executeQuery();
        return result;
    }

    public int getMinStanding() throws SQLException {
        String sql = "SELECT min(standing)"
                + " FROM hotel";
        PreparedStatement stmt = connect.prepareStatement(sql);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getMaxStanding() throws SQLException {
        String sql = "SELECT max(standing)"
                + " FROM hotel";
        PreparedStatement stmt = connect.prepareStatement(sql);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getMinNbRoom() throws SQLException {
        String sql = "SELECT min(nbroom)"
                + " FROM hotel";
        PreparedStatement stmt = connect.prepareStatement(sql);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getMaxNbRoom() throws SQLException {
        String sql = "SELECT max(nbroom)"
                + " FROM hotel";
        PreparedStatement stmt = connect.prepareStatement(sql);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getMinNbEmployees() throws SQLException {
        String sql = "SELECT min(nbemployees)"
                + " FROM hotel";
        PreparedStatement stmt = connect.prepareStatement(sql);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getMaxNbEmployees() throws SQLException {
        String sql = "SELECT max(nbemployees)"
                + " FROM hotel";
        PreparedStatement stmt = connect.prepareStatement(sql);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getMinNbServices() throws SQLException {
        String sql = "SELECT min(nbservices)"
                + " FROM hotel";
        PreparedStatement stmt = connect.prepareStatement(sql);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getMaxNbServices() throws SQLException {
        String sql = "SELECT max(nbservices)"
                + " FROM hotel";
        PreparedStatement stmt = connect.prepareStatement(sql);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public int getNbReservationInTwoWeeksByHotel(int idHotel) throws SQLException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cals = Calendar.getInstance();
        Calendar cals2 = Calendar.getInstance();
        cals2.add(Calendar.DATE, 15);
        String currentDate = simpleDateFormat.format(cals.getTime());
        String dateInTwoWeeks = simpleDateFormat.format(cals.getTime());

        String sql = "SELECT count(id)" +
                " FROM reservation" +
                " WHERE datestart >= ?" +
                " AND datestart <= ?" +
                " AND idhotel = ?";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setString(1, currentDate);
        stmt.setString(2, dateInTwoWeeks);
        stmt.setInt(3, idHotel);
        ResultSet result = stmt.executeQuery();
        int res = -1;
        while (result.next()) {
            res = result.getInt(1);
        }
        return res;
    }

    public void applyLackOfReservationPromotion(int idHotel) throws SQLException {
        String sql = "UPDATE reservation" +
                " SET price = price * 0.8" +
                " WHERE idhotel = ?";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setInt(1, idHotel);
        stmt.executeQuery();
    }


    public boolean isNbResaInTwoWeeksUnder60Percent(int idHotel) throws SQLException {
        int nbResa = getNbReservationInTwoWeeksByHotel(idHotel);
        Hotel hotel = getHotelById(idHotel);
        return (nbResa * 100 / hotel.getNbRoom()) < 60;
    }

    public double applyLackOfReservationPromotion(double price, int idHotel) throws SQLException {
        if(isNbResaInTwoWeeksUnder60Percent(idHotel)) {
            return price * getLackOfReservationIndex();
        }
        return price;
    }


}
