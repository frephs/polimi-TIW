package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.DAO.ProductDAO;
import it.polimi.auctionapp.beans.User;
import it.polimi.auctionapp.utils.ImageServlet;
import it.polimi.auctionapp.utils.MessageType;
import it.polimi.auctionapp.utils.ThymeleafHTTPServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Objects;

public class ProductController {

    static ProductDAO productDataAccessObject = new ProductDAO();

    @WebServlet("/product/new")
    @MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
    )
    public static class ProductFactoryServlet extends ThymeleafHTTPServlet {

        public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            try {
                String filename = ImageServlet.saveImage(request, "product-image");

                productDataAccessObject.addProduct(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    request.getParameter("product-name"),
                    request.getParameter("product-description"),
                    Float.parseFloat(request.getParameter("product-price")),
                    filename
                );

                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Successfully added new product.")
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } catch (ServletException e) {
                contextAttributes.setFlash(
                    "message",
                    "Something went wrong with uploading your image " +
                    MessageType.ERROR.wrap(e.getMessage())
                );
            } finally {
                sendRedirect(request, response, "/sell");
            }
        }
    }

    @WebServlet("/product/edit/*")
    @MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
    )
    public static class ProductEditServlet extends ThymeleafHTTPServlet {

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            try {
                Integer productId = Integer.parseInt(request.getPathInfo().substring(1));

                productDataAccessObject.checkProductExists(productId);
                productDataAccessObject.checkProductIsOwnedBy(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    productId
                );

                String price_string = request.getParameter("product-price");
                productDataAccessObject.updateProductDetails(
                    productId,
                    request.getParameter("product-name"),
                    request.getParameter("product-description"),
                    price_string != null
                        ? Float.parseFloat(price_string)
                        : productDataAccessObject.getProductPrice(productId)
                ); //TODO ADD CHECK POSITIVE CONSTRAINTS

                if (
                    !Objects.equals(request.getParameter("product-image"), "") &&
                    request.getPart("product-image") != null &&
                    request.getPart("product-image").getSize() > 0
                ) {
                    String newFilename = ImageServlet.saveImage(request, "product-image");
                    productDataAccessObject.updateProductImage(productId, newFilename);
                }

                Integer currentAuctionId = productDataAccessObject.getProductAuctionId(productId);
                if (
                    request.getParameter("product-auction-id") != null &&
                    !currentAuctionId.equals(
                        Integer.parseInt(
                            !Objects.equals(request.getParameter("product-auction-id"), "")
                                ? request.getParameter("product-auction-id")
                                : "0"
                        )
                    )
                ) {
                    productDataAccessObject.updateProductAuction(
                        productId,
                        Integer.parseInt(request.getParameter("product-auction-id"))
                    );
                }

                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Successfully updated the product.")
                );
                sendRedirect(request, response, "/sell");
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
