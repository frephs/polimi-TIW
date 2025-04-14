package it.polimi.auctionapp;

import it.polimi.auctionapp.utils.SQLConnectionHandler;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.fail;


public class DBMSConnectionTester  {
    private static final long serialVersionUID = 1L;

    @Test
    public void testConnection() {
        try{
            Connection connection = SQLConnectionHandler.getConnection();
            //test an empty statement
            Statement statement = connection.createStatement();
            assert connection != null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail("Connection failed, remember to set up the database and the config.properties file");
        }
    }

}
