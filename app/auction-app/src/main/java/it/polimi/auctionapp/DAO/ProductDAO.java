package it.polimi.auctionapp.DAO;

import it.polimi.auctionapp.beans.Product;
import it.polimi.auctionapp.utils.SQLConnectionHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public void checkProductExists(Integer product_id) throws SQLException {
        String query = "SELECT * FROM products WHERE product_id = ?";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, product_id);
            ResultSet result = preparedStatement.executeQuery();
            if (!result.next()) {
                throw new SQLWarning("Product does not exist. ");
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException("There was a problem checking the product: " + e.getMessage());
        }
    }

    public void checkProductIsOwnedBy(String username, Integer product_id) throws SQLException {
        String query = "SELECT * FROM products WHERE product_id = ? and seller_username=?";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, product_id);
            preparedStatement.setString(2, username);
            ResultSet result = preparedStatement.executeQuery();
            if (!result.next()) {
                throw new SQLWarning("Product does not exist or is not owned by the user. ");
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException(
                "There was a problem checking the product ownership: " + e.getMessage()
            );
        }
    }

    public void addProduct(
        String seller_username,
        String name,
        String description,
        Float price,
        String image_filename
    ) throws SQLException {
        String query =
            "INSERT INTO products(seller_username, name, description, price, image_filename) VALUES (?, ?, ?, ?, ?)";
        Connection connection = SQLConnectionHandler.getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, seller_username);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, description);
            preparedStatement.setFloat(4, price);
            preparedStatement.setString(5, image_filename);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("There was a problem adding the product: " + e.getMessage());
        } finally {
            connection.close();
        }
    }

    public List<Product> getUnsoldProductBySeller(String username) throws SQLException {
        List<Product> products = new ArrayList<>();
        String query =
            "SELECT DISTINCT product_id, name, description, price, image_filename, p.auction_id AS auction_id " +
            "FROM products p LEFT JOIN auctions a ON a.auction_id = p.auction_id " +
            "WHERE p.seller_username = ? AND " +
            "(closed = false OR p.auction_id IS NULL)";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                products.add(
                    new Product(
                        result.getInt("product_id"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getFloat("price"),
                        result.getString("image_filename"),
                        result.getInt("auction_id")
                    )
                );
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException(
                "There was a problem getting your unsold products: " + e.getMessage()
            );
        }
        return products;
    }

    public List<Product> getProductsByAuction(Integer auction_id) throws SQLException {
        List<Product> products = new ArrayList<>();
        String query =
            "SELECT product_id, name, description, price, image_filename, auction_id FROM products WHERE auction_id = ?";
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, auction_id);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                products.add(
                    new Product(
                        result.getInt("product_id"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getFloat("price"),
                        result.getString("image_filename"),
                        result.getInt("auction_id")
                    )
                );
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException(
                "There was a problem getting the products for the auction: " + e.getMessage()
            );
        }
        return products;
    }

    public void updateProductDetails(Integer product_id, String name, String description)
        throws SQLException {
        String query = "UPDATE products SET name = ?, description = ? WHERE product_id = ?";
        Connection connection = SQLConnectionHandler.getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, product_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException(
                "There was a problem updating the product details: " + e.getMessage()
            );
        } finally {
            connection.close();
        }
    }

    public void updateProductPrice(Integer product_id, Float price) throws SQLException {
        String query = "UPDATE products SET price=? WHERE product_id = ?";
        Connection connection = SQLConnectionHandler.getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setFloat(1, price);
            preparedStatement.setInt(2, product_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException(
                "There was a problem updating the product's price: " + e.getMessage()
            );
        } finally {
            connection.close();
        }
    }

    public void updateProductImage(Integer product_id, String image_filename) throws SQLException {
        String query = "UPDATE products SET image_filename=? WHERE product_id = ?";
        Connection connection = SQLConnectionHandler.getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, image_filename);
            preparedStatement.setInt(2, product_id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("There was a problem updating the product: " + e.getMessage());
        } finally {
            connection.close();
        }
    }

    public void deleteProduct(int productId) throws SQLException {
        String query = "DELETE FROM products WHERE product_id = ?";
        Connection connection = SQLConnectionHandler.getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, productId);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("There was a problem deleting the product: " + e.getMessage());
        } finally {
            connection.close();
        }
    }

    public void updateProductAuction(Integer productId, Integer productAuction)
        throws SQLException {
        String query = "UPDATE products SET auction_id = ? WHERE product_id = ?";
        Connection connection = SQLConnectionHandler.getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
                .prepareStatement(query);
            preparedStatement.setObject(1, productAuction != 0 ? productAuction : null);
            preparedStatement.setInt(2, productId);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException(
                "There was a problem changing your product's auction: " + e.getMessage()
            );
        } finally {
            connection.close();
        }
    }

    public Integer getProductAuctionId(Integer product_id) throws SQLException {
        String query = "SELECT auction_id FROM products WHERE product_id = ?";
        Integer auctionId = null;
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, product_id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                auctionId = result.getInt("auction_id");
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException(
                "There was a problem getting the product's auction id: " + e.getMessage()
            );
        }
        return auctionId;
    }

    public Float getProductPrice(Integer product_id) throws SQLException {
        String query = "SELECT price FROM products WHERE product_id = ?";
        Float productPrice = null;
        try (Connection connection = SQLConnectionHandler.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, product_id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                productPrice = result.getFloat("price");
            }
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException(
                "There was a problem getting the product's price: " + e.getMessage()
            );
        }
        return productPrice;
    }
}
