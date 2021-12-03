package utils;

import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

/** Handles the connection process to the mySQL database.*/
public class DBConnection {
    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String ipAddress = "//wgudb.ucertify.com/WJ06bxN";

    private static final String jdbcURL = protocol + vendorName + ipAddress + "?useSSL=false";

    private static final String MYSQLJDBCDriver = "com.mysql.jdbc.Driver";
    private static Connection conn = null;

    private static final String username = "U06bxN";
    private static final String password = "53688720996";

    /** Attempts to establish the connection to the mySQL database with the pertinent information provided.*/
    public static Connection startConnection() {
        try {
            Class.forName(MYSQLJDBCDriver);
            conn = (Connection) DriverManager.getConnection(jdbcURL, username, password);
            System.out.println("Connection successful!");
        }
        catch (ClassNotFoundException | SQLException e){
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /** Attempts to close the connection to the mySQL database.*/
    public static void closeConnection() {
        try {
            conn.close();
            System.out.println("Connection closed!");
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }

    }

}
