package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Handles all queries made to the mySQL database.*/
public class DBQuery {

    private static PreparedStatement statement;

    /** Handles the formatting for mySQL queries.
     @param conn The connection object to the MySQL database.
     @param sqlStatement The statement to query the MySQL database with.*/
    public static void setPreparedStatement(Connection conn, String sqlStatement) throws SQLException {
        statement = conn.prepareStatement(sqlStatement);
    }

    /** The results of a MySQL query.
     @return Returns the results of a MySQL query.*/
    public static PreparedStatement getPreparedStatement(){
        return statement;
    }

}
