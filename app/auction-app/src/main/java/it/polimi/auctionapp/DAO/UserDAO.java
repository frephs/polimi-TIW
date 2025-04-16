package it.polimi.auctionapp.DAO;

import it.polimi.auctionapp.beans.Address;
import it.polimi.auctionapp.beans.User;

import java.sql.*;

import it.polimi.auctionapp.utils.Hash;
import it.polimi.auctionapp.utils.SQLConnectionHandler;

public class UserDAO {
    public void checkExists(
            String username
    ) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection().prepareStatement(query);
        preparedStatement.setString(1, username);
        ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                throw new SQLWarning("Username already exists");
            }

    }

    public void addUser(User user, String password) throws SQLException {
        try {
            checkExists(user.getUsername());
        } catch (SQLWarning e) {
            throw new SQLWarning("Username already exists.");
        }

        String query = "INSERT INTO users (username, password, name, surname, country, zip_code, city, street, street_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection().prepareStatement(query);
        preparedStatement.setString(1, user.getUsername());
        preparedStatement.setString(2, Hash.sha512(password));
        preparedStatement.setString(3, user.getName());
        preparedStatement.setString(4, user.getSurname());
        preparedStatement.setString(5, user.getAddress().getCountry());
        preparedStatement.setInt(6, user.getAddress().getZipCode());
        preparedStatement.setString(7, user.getAddress().getCity());
        preparedStatement.setString(8, user.getAddress().getStreet());
        preparedStatement.setInt(9, user.getAddress().getStreet_number());

        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("There was a problem while creating your account: " + e.getMessage());
        }
    }

    public void updateAccountDetails(
            User user
    ) throws SQLException {
        String query = "UPDATE  name = ?, surname = ?, country = ?, zip_code = ?, city = ?, street = ?, street_number = ? WHERE username = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection().prepareStatement(query);
        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getSurname());
        preparedStatement.setString(3, user.getAddress().getCountry());
        preparedStatement.setInt(4, user.getAddress().getZipCode());
        preparedStatement.setString(5, user.getAddress().getCity());
        preparedStatement.setString(6, user.getAddress().getStreet());
        preparedStatement.setInt(7, user.getAddress().getStreet_number());
        preparedStatement.setString(8, user.getUsername());
        try {
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new SQLException("There was a problem while updating your account details: " + e.getMessage());
        }
    }

    public void updateUsername(
            String oldUsername,
            String newUsername,
            String password
    ) throws SQLException {
        try {
            checkExists(newUsername);
        } catch (SQLException e) {
            throw new SQLWarning("Username already exists.");
        }
        String query = "UPDATE users SET username = ? WHERE username = ? and password = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection().prepareStatement(query);
        preparedStatement.setString(1, newUsername);
        preparedStatement.setString(2, oldUsername);
        preparedStatement.setString(3, Hash.sha512(password));
        try {
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            try {
                checkExists(oldUsername);
            } catch (SQLException e1) {
                throw new SQLWarning("Invalid password");
            }
            throw new SQLException("There was a problem while updating your username: " + e.getMessage());
        }
    }

    public void updatePassword(
            String username,
            String newPassword,
            String oldPassword
    ) throws SQLException {
        String query = "UPDATE users SET password = ? WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection().prepareStatement(query);
        preparedStatement.setString(1, Hash.sha512(newPassword));
        preparedStatement.setString(2, username);
        preparedStatement.setString(3, Hash.sha512(oldPassword));
        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            try {
                checkExists(username);
            } catch (SQLException e1) {
                throw new SQLWarning("Invalid password");
            }
            throw new SQLException("There was a problem updating your password: " + e.getMessage());
        }
    }

    public void deleteAccount(String username, String password) throws SQLException {
        String query = "DELETE FROM users WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection().prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, Hash.sha512(password));
        try {
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            try {
                checkExists(username);
            } catch (SQLException e1) {
                throw new SQLWarning("Invalid password");
            }
            throw new SQLException("There was a problem while deleting your account: " + e.getMessage());
        }
    }

    public User getUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection().prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, Hash.sha512(password));
        ResultSet result = preparedStatement.executeQuery();
        if (result.next()) {
            String name = result.getString("name");
            String surname = result.getString("surname");
            String country = result.getString("country");
            Integer zip_code = result.getInt("zip_code");
            String city = result.getString("city");
            String street = result.getString("street");
            Integer street_number = result.getInt("street_number");

            return new User(username, name, surname, new Address(country, zip_code, city, street, street_number));
        } else {
            try{
                checkExists(username);
            }catch (SQLException e){
                throw new SQLWarning("Incorrect password");
            }
            throw new SQLWarning("User not found");
        }
    }


}
