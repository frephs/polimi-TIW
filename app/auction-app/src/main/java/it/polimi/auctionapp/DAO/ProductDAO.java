package it.polimi.auctionapp.DAO;

import it.polimi.auctionapp.beans.Product;
import it.polimi.auctionapp.utils.SQLConnectionHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public void checkProductExists(Integer product_id) throws SQLException {
        String query = "SELECT * FROM products WHERE product_id = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setInt(1, product_id);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.next()) {
            throw new SQLWarning("Product does not exist");
        }
    }

    public void checkProductIsOwnedBy(String username, Integer product_id) throws SQLException {
        String query = "SELECT * FROM products WHERE product_id = ? and seller_username=?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setInt(1, product_id);
        preparedStatement.setString(2, username);
        ResultSet result = preparedStatement.executeQuery();
        if (!result.next()) {
            throw new SQLWarning("Product does not exist or is not owned by the user");
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
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setString(1, seller_username);
        preparedStatement.setString(2, name);
        preparedStatement.setString(3, description);
        preparedStatement.setFloat(4, price);
        preparedStatement.setString(5, image_filename);
        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("There was a problem adding the product" + e.getMessage());
        }
    }

    public List<Product> getUnsoldProductBySeller(String username) throws SQLException {
        String query =
            "SELECT DISTINCT product_id, name, description, price, image_filename, p.auction_id AS auction_id " +
            "FROM products p LEFT JOIN auctions a ON a.auction_id = p.auction_id " +
            "WHERE p.seller_username = ? AND " +
            "(closed = false OR p.auction_id IS NULL)";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setString(1, username);
        ResultSet result = preparedStatement.executeQuery();
        List<Product> products = new ArrayList<>();
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
        return products;
    }

    public List<Product> getProductsByAuction(Integer auction_id) throws SQLException {
        String query =
            "SELECT product_id, name, description, price, image_filename, auction_id FROM products WHERE auction_id = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setInt(1, auction_id);
        ResultSet result = preparedStatement.executeQuery();
        List<Product> products = new ArrayList<>();
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
        return products;
    }

    public void updateProductDetails(
        Integer product_id,
        String name,
        String description,
        Float price
    ) throws SQLException {
        String query =
            "UPDATE products SET name = ?, description = ?, price = ? WHERE product_id = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, description);
        preparedStatement.setFloat(3, price);
        preparedStatement.setInt(4, product_id);
        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("There was a problem updating the product", e.getMessage());
        }
    }

    public void updateProductImage(Integer product_id, String image_filename) throws SQLException {
        String query = "UPDATE products SET image_filename=? WHERE product_id = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setString(1, image_filename);
        preparedStatement.setInt(2, product_id);
        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("There was a problem updating the product", e.getMessage());
        }
    }

    public void deleteProduct(int productId) throws SQLException {
        String query = "DELETE FROM products WHERE product_id = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);
        preparedStatement.setInt(1, productId);
        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("There was a problem deleting the product", e.getMessage());
        }
    }

    public void updateProductAuction(Integer productId, Integer productAuction)
        throws SQLException {
        String query = "UPDATE products SET auction_id = ? WHERE product_id = ?";
        PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
            .prepareStatement(query);

        preparedStatement.setObject(1, productAuction != 0 ? productAuction : null);
        preparedStatement.setInt(2, productId);
        try {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("There was a problem adding a product to your ", e);
        }
    }

    public Integer getProductAuctionId(Integer product_id) throws SQLException {
        String query = "SELECT auction_id FROM products WHERE product_id = ?";
        try (
            PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
                .prepareStatement(query)
        ) {
            preparedStatement.setInt(1, product_id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt("auction_id");
            } else {
                return null;
            }
        }
    }

    public Float getProductPrice(Integer product_id) throws SQLException {
        String query = "SELECT price FROM products WHERE product_id = ?";
        try (
            PreparedStatement preparedStatement = SQLConnectionHandler.getConnection()
                .prepareStatement(query)
        ) {
            preparedStatement.setInt(1, product_id);
            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getFloat("price");
            } else {
                return null;
            }
        }
    }
}
