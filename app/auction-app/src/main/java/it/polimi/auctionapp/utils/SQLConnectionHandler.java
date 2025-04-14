package it.polimi.auctionapp.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SQLConnectionHandler {
    public static Connection getConnection()   {
        Properties props = new Properties();

        // load config
        try {
            InputStream input = new FileInputStream(SQLConnectionHandler.class.getResource("config.properties").getFile());
            props.load(input);
        }catch (FileNotFoundException e) {
            throw new RuntimeException("Remember to create a config.properties file in the src/main/resources folder to access your database");
        }catch (IOException e) {
            throw new RuntimeException("Error loading config.properties file");
        }
        final String USER = props.getProperty("db.user");
        final String DB_URL = props.getProperty("db.url");
        final String PASS = props.getProperty("db.password");
        final String DB_SCHEMA = props.getProperty("db.schema");

        String result = "Connection worked";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
            connection.setSchema(DB_SCHEMA);
            return connection;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found", e);
        }catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database, remember to create the schema from the provided dump", e);
        }
    }

}
