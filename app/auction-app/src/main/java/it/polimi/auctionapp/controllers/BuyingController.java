package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.DAO.AuctionDAO;
import it.polimi.auctionapp.beans.Auction;
import it.polimi.auctionapp.beans.Bid;
import it.polimi.auctionapp.beans.User;
import it.polimi.auctionapp.utils.MessageType;
import it.polimi.auctionapp.utils.ThymeleafHTTPServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class BuyingController {

    static AuctionDAO auctionDataAccessObject = new AuctionDAO();

    @WebServlet({ "/buy/search", "/buy" })
    public static class BuyingManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            if (request.getSession().getAttribute("user") == null) {
                request
                    .getSession()
                    .setAttribute(
                        "message",
                        MessageType.INFO.wrap("To buy items, log in or sign up first.")
                    );
                request.getSession().setAttribute("from", "buy");

                sendRedirect(request, response, "/account");
            } else {
                String searchQuery = request.getParameter("q");
                try {
                    if (searchQuery != null) {
                        List<Auction> auctions = auctionDataAccessObject.getAuctionsByKeyword(
                            Arrays.stream(searchQuery.split("\\+")).toList()
                        );
                        request.getSession().setAttribute("auctions", auctions);
                    }
                    List<Auction> wonAuctions = auctionDataAccessObject.getAuctionsWonBy(
                        ((User) request.getSession().getAttribute("user")).getUsername()
                    );

                    request.getSession().setAttribute("wonAuctions", wonAuctions);
                } catch (SQLException e) {
                    contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
                }
                processTemplate(request, response, "/buy/index");
            }
        }
    }

    @WebServlet("/buy/auction")
    public static class BuyerAuctionDetailServlet extends ThymeleafHTTPServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            int auctionId;

            String auctionIdParam = request.getParameter("id");

            if (auctionIdParam == null) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.ERROR.wrap("Auction ID is required.")
                );
                sendRedirect(request, response, "/sell");
                return;
            }

            try {
                auctionId = Integer.parseInt(auctionIdParam);
                Auction auction = auctionDataAccessObject.getAuctionById(auctionId);
                List<Bid> bids = auctionDataAccessObject.getBidsByAuction(auctionId);

                request.getSession().setAttribute("bids", bids);
                request.getSession().setAttribute("auction", auction);

                processTemplate(request, response, "/buy/auction-detail");
            } catch (NumberFormatException e) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.WARNING.wrap("Invalid auction ID.")
                );
                sendRedirect(request, response, "/buy");
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            }
        }
    }

    @WebServlet("/buy/auction/bid/")
    public static class BidPlacerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            if (request.getSession().getAttribute("user") == null) {
                request
                    .getSession()
                    .setAttribute(
                        "message",
                        MessageType.INFO.wrap("To buy items, log in or sign up first.")
                    );
                request.getSession().setAttribute("from", "sell");
                sendRedirect(request, response, "/account");
            } else {
                int auctionId;
                String auctionIdParam = request.getParameter("auction-id");
                String bidAmountParam = request.getParameter("bid-amount");

                if (auctionIdParam == null || bidAmountParam == null) {
                    request
                        .getSession()
                        .setAttribute(
                            "message",
                            MessageType.ERROR.wrap("Auction ID and bid amount are required.")
                        );
                    sendRedirect(request, response, "/buy");
                    return;
                }

                try {
                    auctionId = Integer.parseInt(auctionIdParam);
                    float bidAmount = Float.parseFloat(bidAmountParam);
                    String username =
                        ((User) request.getSession().getAttribute("user")).getUsername();

                    auctionDataAccessObject.placeBid(auctionId, username, bidAmount);

                    request
                        .getSession()
                        .setAttribute(
                            "message",
                            MessageType.SUCCESS.wrap("Bid placed successfully.")
                        );
                    sendRedirect(request, response, "/buy/auction?id=" + auctionId);
                } catch (NumberFormatException e) {
                    contextAttributes.setFlash(
                        "message",
                        MessageType.WARNING.wrap("Invalid input.")
                    );
                    sendRedirect(request, response, "/buy");
                } catch (SQLException e) {
                    contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
                    sendRedirect(request, response, "/buy");
                }
            }
        }
    }
}
