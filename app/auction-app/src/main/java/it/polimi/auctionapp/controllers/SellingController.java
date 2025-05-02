package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.DAO.AuctionDAO;
import it.polimi.auctionapp.DAO.ProductDAO;
import it.polimi.auctionapp.beans.Auction;
import it.polimi.auctionapp.beans.Product;
import it.polimi.auctionapp.beans.User;
import it.polimi.auctionapp.utils.MessageType;
import it.polimi.auctionapp.utils.ThymeleafHTTPServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

public class SellingController {

    static ProductDAO productDataAccessObject = new ProductDAO();
    static AuctionDAO auctionDataAccessObject = new AuctionDAO();

    @WebServlet("/sell")
    public static class SellingManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            try {
                List<Product> unsold_products = productDataAccessObject.getUnsoldProductBySeller(
                    ((User) request.getSession().getAttribute("user")).getUsername()
                );

                contextAttributes.set("products", unsold_products);

                contextAttributes.set(
                    "unauctioned_products",
                    unsold_products
                        .stream()
                        .filter(
                            product -> product.getAuctionId() == null || product.getAuctionId() == 0
                        )
                        .toList()
                );

                List<Auction> auctions = auctionDataAccessObject.getAuctionsBySeller(
                    ((User) request.getSession().getAttribute("user")).getUsername()
                );

                contextAttributes.set(
                    "open_auctions",
                    auctions.stream().filter(Auction::isOpen).toList()
                );

                contextAttributes.set(
                    "closed_auctions",
                    auctions.stream().filter(auction -> !auction.isOpen()).toList()
                );
            } catch (SQLException e) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.ERROR.wrap(
                        "There was a problem retrieving your products: " + e.getMessage()
                    )
                );
            }
            processTemplate(request, response, "/sell/index");
        }
    }

    @WebServlet("/auction/new")
    public static class AuctionFactoryServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            try {
                List<Product> products = productDataAccessObject
                    .getUnsoldProductBySeller(
                        ((User) request.getSession().getAttribute("user")).getUsername()
                    )
                    .stream()
                    .filter(product -> !product.isAuctioned())
                    .toList();

                String[] productIds = request.getParameterValues("product-ids");

                if (products.isEmpty()) {
                    throw new SQLWarning(
                        "You cannot create an auction while you have no available products"
                    );
                }

                if (productIds == null) {
                    throw new SQLWarning("You cannot create an auction with no products");
                }

                List<Integer> selectedProductIds = Arrays.stream(productIds)
                    .map(Integer::parseInt)
                    .toList();

                if (selectedProductIds.size() <= 1) {
                    throw new SQLWarning(
                        "You cannot create an auction with less than two products"
                    );
                }

                List<Product> selected_products = products
                    .stream()
                    .filter(product -> selectedProductIds.contains(product.getId()))
                    .toList();

                Integer auction_id = auctionDataAccessObject.createAuction(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    Timestamp.valueOf(
                        request.getParameter("final-bid-submission-date") +
                        " " +
                        request.getParameter("final-bid-submission-time") +
                        ":00"
                    ),
                    Float.parseFloat(request.getParameter("min-bid-increment"))
                );

                for (Product product : selected_products) {
                    productDataAccessObject.updateProductAuction(product.getId(), auction_id);
                }

                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap(
                        "Successfully added new auction and updated the products details."
                    )
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                sendRedirect(request, response, "/sell");
            }
        }
    }

    @WebServlet("/auction/close/*")
    public static class AuctionDestroyerServlet extends ThymeleafHTTPServlet {

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            try {
                String auctionIdParam = request.getPathInfo().substring(1); // Extract auction ID from URL
                int auctionId = Integer.parseInt(auctionIdParam);

                auctionDataAccessObject.checkAuctionExists(auctionId);
                auctionDataAccessObject.checkAuctionIsOwnedBy(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    auctionId
                );
                auctionDataAccessObject.closeAuction(auctionId);

                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Auction closed successfully.")
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } catch (NumberFormatException e) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.ERROR.wrap("Invalid auction ID.")
                );
            } finally {
                sendRedirect(request, response, "/sell");
            }
        }
    }

    @WebServlet("/auction/delete/*")
    public static class AuctionDeleteServlet extends ThymeleafHTTPServlet {

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            try {
                String auctionIdParam = request.getPathInfo().substring(1); // Extract auction ID from URL
                int auctionId = Integer.parseInt(auctionIdParam);

                auctionDataAccessObject.checkAuctionExists(auctionId);
                auctionDataAccessObject.checkAuctionIsOwnedBy(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    auctionId
                );
                auctionDataAccessObject.deleteAuction(auctionId);

                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Auction deleted successfully.")
                );
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } catch (NumberFormatException e) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.ERROR.wrap("Invalid auction ID.")
                );
            } finally {
                sendRedirect(request, response, "/sell");
            }
        }
    }

    @WebServlet("/sell/auction")
    public static class SellerAuctionDetailServlet extends ThymeleafHTTPServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            String auctionIdParam = request.getParameter("id");
            System.out.println("auctionIdParam: " + auctionIdParam);
            if (auctionIdParam == null) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.ERROR.wrap("Auction ID is required.")
                );
                sendRedirect(request, response, "/sell");
                return;
            }
            int auctionId = Integer.parseInt(auctionIdParam);

            try {
                auctionDataAccessObject.checkAuctionExists(auctionId);
                auctionDataAccessObject.checkAuctionIsOwnedBy(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    auctionId
                );
                contextAttributes.set("auction", auctionDataAccessObject.getAuctionById(auctionId));
                contextAttributes.set("bids", auctionDataAccessObject.getBidsByAuction(auctionId));
                processTemplate(request, response, "/sell/auction-detail");
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
                sendRedirect(request, response, "/sell");
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
                sendRedirect(request, response, "/sell");
            }
        }
    }
}
