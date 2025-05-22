package it.polimi.auctionapp.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class SQLConnectionHandler {

    public static Connection getConnection() throws SQLException {
        try {
            return createConnection();
        } catch (Exception e) {
            throw new SQLException(
                "There was a problem enstablishing a connection: " + e.getMessage()
            );
        }
    }

    public static Connection createConnection() throws Exception {
        Properties props = new Properties();
        try {
            InputStream input = new FileInputStream(SQLConnectionHandler.class.getResource("config.properties").getFile());
            props.load(input);
        } catch (FileNotFoundException e) {
            throw new Exception(
                "Remember to create a config.properties file in the src/main/resources folder to access your database"
            );
        } catch (IOException e) {
            throw new RuntimeException("Error loading config.properties file");
        }
        final String USER = props.getProperty("db.user");
        final String DB_URL = props.getProperty("db.url");
        final String PASS = props.getProperty("db.password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);

            return connection;
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("JDBC Driver not found" + e.getMessage());
        } catch (SQLException e) {
            throw new SQLException(
                "Error connecting to the database, check your config.properties and remember to create the database schema from the provided dump" +
                e.getMessage()
            );
        }
    }

}
