package it.polimi.auctionapp.DAO;

import it.polimi.auctionapp.beans.Auction;
import it.polimi.auctionapp.utils.SQLConnectionHandler;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    public void checkAuctionExists(Integer auction_id) throws SQLException {
        String query = "SELECT * FROM auctions WHERE auction_id = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setInt(1, auction_id);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.next()) {
            throw new SQLWarning("Auction does not exist");
        }
    }

    public void checkAuctionIsOwnedBy(String username, Integer auction_id) throws SQLException {
        String query = "SELECT * FROM auctions WHERE auction_id = ? and seller_username=?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setInt(1, auction_id);
        preparedStatement.setString(2, username);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.next()) {
            throw new SQLWarning("Auction does not exist or is not owned by the user");
        }
    }

    public Integer createAuction(
        String sellerUsername,
        Timestamp final_bid_submission_time,
        Float minBidIncrement
    ) throws SQLException {
        String auction_query =
            "INSERT INTO auctions(seller_username, start_time, final_bid_submission_time, min_bid_increment, closed) " +
            "VALUES(?, ?, ?, ?, ? )";
        try {
            PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
                .prepareStatement(auction_query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, sellerUsername);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setTimestamp(3, final_bid_submission_time);
            preparedStatement.setFloat(4, minBidIncrement);
            preparedStatement.setBoolean(5, false);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows != 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            preparedStatement.close(); // FIXME close all prepared statements
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return null;
    }

    public void closeAuction(Integer auction_id) throws SQLException {
        String query = "UPDATE auctions SET closed = ? WHERE auction_id = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setBoolean(1, true);
        preparedStatement.setInt(2, auction_id);
        preparedStatement.executeUpdate();
    }

    public List<Auction> getAuctionsBySeller(String seller_username) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        String query =
            "SELECT auction_id,seller_username,start_time,start_time," +
            "final_bid_submission_time,min_bid_increment, closed FROM auctions WHERE seller_username=?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setString(1, seller_username);

        ResultSet result = preparedStatement.executeQuery();
        while (result.next()) {
            auctions.add(
                new Auction(
                    result.getInt("auction_id"),
                    result.getString("seller_username"),
                    result.getTimestamp("start_time"),
                    result.getTimestamp("final_bid_submission_time"),
                    result.getInt("min_bid_increment"),
                    getAuctionItemsCount(result.getInt("auction_id")),
                    result.getBoolean("closed")
                )
            );
        }
        return auctions;
    }

    public Integer getAuctionItemsCount(Integer auction_id) throws SQLException {
        String query = "SELECT COUNT(*) FROM products WHERE auction_id=?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setInt(1, auction_id);
        ResultSet result = preparedStatement.executeQuery();
        if (result.next()) {
            return result.getInt(1);
        }
        return 0;
    }
}
