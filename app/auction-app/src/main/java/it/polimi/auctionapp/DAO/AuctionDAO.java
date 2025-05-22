package it.polimi.auctionapp.DAO;

import it.polimi.auctionapp.beans.Auction;
import it.polimi.auctionapp.beans.Bid;
import it.polimi.auctionapp.utils.SQLConnectionHandler;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    static ProductDAO productDataAccessObject = new ProductDAO();
    static BidDao bidDataAccessObject = new BidDao();

    public void checkAuctionExists(Integer auction_id) throws SQLException {
        String query = "SELECT * FROM auctions WHERE auction_id = ?";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);
            ResultSet result = preparedStatement.executeQuery();
            if (!result.next()) {
                throw new SQLWarning("Auction does not exist. ");
            }
            preparedStatement.close();
        } catch (SQLWarning e) {
            throw new SQLWarning(e);
        } catch (SQLException e) {
            throw new SQLException("There was a problem checking the auction: " + e.getMessage());
        }
    }

    public void checkAuctionIsOwnedBy(String username, Integer auction_id) throws SQLException {
        String query = "SELECT * FROM auctions WHERE auction_id = ? and seller_username=?";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);
            preparedStatement.setString(2, username);
            ResultSet result = preparedStatement.executeQuery();
            if (!result.next()) {
                throw new SQLWarning("Auction does not exist or is not owned by the user. ");
            }
            preparedStatement.close();
        } catch (SQLWarning e) {
            throw new SQLWarning(e);
        } catch (SQLException e) {
            throw new SQLException("There was a problem checking the auction: " + e.getMessage());
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
        Connection connection = SQLConnectionHandler.getConnection();
        connection.setAutoCommit(false);
        Integer auction_id = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                auction_query,
                PreparedStatement.RETURN_GENERATED_KEYS
            );
            preparedStatement.setString(1, sellerUsername);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setTimestamp(3, final_bid_submission_time);
            preparedStatement.setFloat(4, minBidIncrement);
            preparedStatement.setBoolean(5, false);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows != 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        auction_id = generatedKeys.getInt(1);
                    }
                }
            }
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("There was a problem creating the auction: " + e.getMessage());
        } finally {
            connection.close();
        }
        return auction_id;
    }

    public void closeAuction(Integer auction_id) throws SQLException {
        String query =
            "UPDATE auctions SET closed = ?, " +
            "final_bid_submission_time = CURRENT_TIMESTAMP " +
            "WHERE auction_id = ?";

        Connection connection = SQLConnectionHandler.getConnection();
        connection.setAutoCommit(false);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, auction_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("There was a problem closing the auction: " + e.getMessage());
        } finally {
            connection.close();
        }
    }

    public void deleteAuction(Integer auction_id) throws SQLException {
        String query = "DELETE FROM auctions WHERE auction_id = ?";
        Connection connection = SQLConnectionHandler.getConnection();
        connection.setAutoCommit(false);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("There was a problem deleting the auction: " + e.getMessage());
        } finally {
            connection.close();
        }
    }

    public List<Auction> getAuctionsBySeller(String seller_username) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        String query =
            "SELECT auction_id,seller_username,start_time,start_time," +
            "final_bid_submission_time,min_bid_increment, closed FROM auctions WHERE seller_username=? " +
            "ORDER BY final_bid_submission_time ASC";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
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
                        result.getBoolean("closed"),
                        bidDataAccessObject.getCurrentHighestBid(result.getInt("auction_id")),
                        productDataAccessObject.getProductsByAuction(result.getInt("auction_id"))
                    )
                );
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException("There was a problem getting the auctions: " + e.getMessage());
        }
        return auctions;
    }

    public Auction getAuctionById(Integer auction_id) throws SQLException {
        Auction auction = null;
        String query =
            "SELECT auction_id,seller_username,start_time,start_time," +
            "final_bid_submission_time,min_bid_increment, closed FROM auctions WHERE auction_id=? ";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);

            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                auction = new Auction(
                    auction_id,
                    result.getString("seller_username"),
                    result.getTimestamp("start_time"),
                    result.getTimestamp("final_bid_submission_time"),
                    result.getInt("min_bid_increment"),
                    result.getBoolean("closed"),
                    bidDataAccessObject.getCurrentHighestBid(result.getInt("auction_id")),
                    productDataAccessObject.getProductsByAuction(result.getInt("auction_id"))
                );
                preparedStatement.close();
            } else {
                throw new SQLException("There is no auction with id " + auction_id + ".");
            }
        } catch (SQLException e) {
            throw new SQLException("There was a problem getting the auction: " + e.getMessage());
        }

        return auction;
    }

    public List<Bid> getBidsByAuction(Integer auction_id) throws SQLException {
        List<Bid> bids = new ArrayList<>();
        String query =
            "SELECT bidder_username, bid_amount, bid_timestamp FROM bids WHERE auction_id=?";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                bids.add(
                    new Bid(
                        auction_id,
                        result.getString("bidder_username"),
                        result.getFloat("bid_amount"),
                        result.getTimestamp("bid_timestamp")
                    )
                );
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException("There was a problem getting the bids: " + e.getMessage());
        }
        return bids;
    }

    public List<Auction> getAuctionsByKeyword(List<String> keywords) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        String query =
            "SELECT DISTINCT a.auction_id, a.seller_username, a.start_time, a.final_bid_submission_time, " +
            "a.min_bid_increment, a.closed " +
            "FROM auctions a " +
            "JOIN products p ON a.auction_id = p.auction_id " +
            "WHERE a.closed = 0 AND " +
            "(" +
            String.join(
                " OR ",
                keywords
                    .stream()
                    .map(keyword ->
                        "(p.name LIKE ? OR p.description LIKE ? OR p.seller_username LIKE ?)"
                    )
                    .toArray(String[]::new)
            ) +
            ") ORDER BY a.final_bid_submission_time ASC";

        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            int index = 1;
            for (String keyword : keywords) {
                preparedStatement.setString(index++, "%" + keyword + "%");
                preparedStatement.setString(index++, "%" + keyword + "%");
                preparedStatement.setString(index++, "%" + keyword + "%");
            }

            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                auctions.add(
                    new Auction(
                        result.getInt("auction_id"),
                        result.getString("seller_username"),
                        result.getTimestamp("start_time"),
                        result.getTimestamp("final_bid_submission_time"),
                        result.getInt("min_bid_increment"),
                        result.getBoolean("closed"),
                        bidDataAccessObject.getCurrentHighestBid(result.getInt("auction_id")),
                        productDataAccessObject.getProductsByAuction(result.getInt("auction_id"))
                    )
                );
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException("There was a problem getting the auctions: " + e.getMessage());
        }
        return auctions;
    }

    public List<Auction> getAuctionsWonBy(String username) throws SQLException {
        List<Auction> auctions = new ArrayList<>();
        String query =
            "SELECT b.auction_id, seller_username, start_time, final_bid_submission_time, " +
            "min_bid_increment, closed, bid_amount " +
            "FROM auctions a " +
            "JOIN bids b ON a.auction_id = b.auction_id " +
            "WHERE b.bidder_username = ? AND a.closed = 1 AND b.bid_amount = " +
            "(SELECT MAX(b2.bid_amount) " +
            "FROM bids b2 " +
            "WHERE b2.auction_id = b.auction_id)";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);

            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                auctions.add(
                    new Auction(
                        result.getInt("auction_id"),
                        result.getString("seller_username"),
                        result.getTimestamp("start_time"),
                        result.getTimestamp("final_bid_submission_time"),
                        result.getInt("min_bid_increment"),
                        result.getBoolean("closed"),
                        bidDataAccessObject.getCurrentHighestBid(result.getInt("auction_id")),
                        productDataAccessObject.getProductsByAuction(result.getInt("auction_id"))
                    )
                );
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException("There was a problem getting the auctions: " + e.getMessage());
        }
        return auctions;
    }

    public String getCurrentShippingAddressForAuction(Integer auction_id) throws SQLException {
        String address = null;
        String query =
            "SELECT s.auction_id, s.address " +
            "FROM shipping_addresses s " +
            "WHERE s.auction_id = ?";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                address = result.getString(2);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException(
                "There was a problem getting the shipping address for this auction: " +
                e.getMessage()
            );
        }
        return address;
    }
}
