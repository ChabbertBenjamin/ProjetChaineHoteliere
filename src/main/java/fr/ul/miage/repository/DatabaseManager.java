package fr.ul.miage.repository;

import fr.ul.miage.model.ConnectBDD;

import java.sql.*;

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

    public Date getHighSeasonStart() throws SQLException {
        return getGlobalDataValue(3, "high_season_start");
    }

    public Date getHighSeasonEnd() throws SQLException {
        return getGlobalDataValue(1, "high_season_end");
    }

    public Date getLowSeasonStart() throws SQLException {
        return getGlobalDataValue(4, "low_season_start");
    }

    public Date getLowSeasonEnd() throws SQLException {
        return getGlobalDataValue(2, "low_season_end");
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

    public Date getGlobalDataValue(int columnNo, String columnName) throws SQLException {
        String sql = "SELECT ?"
                + "FROM global_data";
        PreparedStatement stmt = connect.prepareStatement(sql);
        stmt.setString(1, columnName);
        ResultSet result = stmt.executeQuery();
        return result.getDate(columnNo);
    }
}
