package it.polimi.auctionapp.DAO;

import it.polimi.auctionapp.beans.Bid;
import it.polimi.auctionapp.utils.SQLConnectionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BidDao {

    public void placeBid(Integer auction_id, String bidder_username, Float bid_amount)
        throws SQLException {
        String query =
            "INSERT INTO bids(auction_id, bidder_username, bid_amount, bid_timestamp) " +
            "VALUES(?, ?, ?, CURRENT_TIMESTAMP)";
        Connection connection = SQLConnectionHandler.getConnection();
        try (connection) {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);
            preparedStatement.setString(2, bidder_username);
            preparedStatement.setFloat(3, bid_amount);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("There was a problem placing the bid: " + e.getMessage());
        }
    }

    public Bid getCurrentHighestBid(Integer auction_id) throws SQLException {
        String query =
            "SELECT auction_id, bidder_username, bid_amount, bid_timestamp FROM bids " +
            " WHERE auction_id = ? AND bid_amount =" +
            " (SELECT MAX(bid_amount) FROM bids b2 WHERE b2.auction_id=?)" +
            "ORDER BY bid_timestamp DESC";
        Bid bid = null;
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);
            preparedStatement.setInt(2, auction_id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                bid = new Bid(
                    auction_id,
                    result.getString("bidder_username"),
                    result.getFloat("bid_amount"),
                    result.getTimestamp("bid_timestamp")
                );
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException(
                "There was a problem getting the current highest bid: " + e.getMessage()
            );
        }
        return bid;
    }
}
