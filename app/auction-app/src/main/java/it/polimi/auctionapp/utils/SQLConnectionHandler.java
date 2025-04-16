package it.polimi.auctionapp.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class SQLConnectionHandler {
    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                return connection = createConnection();
            } else {
                return connection;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection createConnection() {
        Properties props = new Properties();
        try {
            InputStream input = new FileInputStream(SQLConnectionHandler.class.getResource("config.properties").getFile());
            props.load(input);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Remember to create a config.properties file in the src/main/resources folder to access your database");
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
            throw new RuntimeException("JDBC Driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database, check your config.properties and remember to create the database schema from the provided dump", e);
        }
    }

}
