package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.DAO.AuctionDAO;
import it.polimi.auctionapp.DAO.BidDao;
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
import java.util.stream.Collectors;

public class BuyingController {

    static AuctionDAO auctionDataAccessObject = new AuctionDAO();
    static BidDao bidDataAccessObject = new BidDao();

    @WebServlet({ "/buy/search", "/buy" })
    public static class BuyingManagerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            String searchQuery = request.getParameter("q");
            try {
                if (searchQuery != null) {
                    contextAttributes.set(
                        "auctions",
                        auctionDataAccessObject.getAuctionsByKeyword(
                            Arrays.stream(searchQuery.split("\\+")).toList()
                        )
                    );
                }

                List<Auction> wonAuctions = auctionDataAccessObject.getAuctionsWonBy(
                    ((User) request.getSession().getAttribute("user")).getUsername()
                );

                contextAttributes.set("wonAuctions", wonAuctions);
                contextAttributes.set(
                    "closed_auction_shipping_addresses",
                    wonAuctions
                        .stream()
                        .collect(
                            Collectors.toMap(Auction::getId, auction -> {
                                try {
                                    return auctionDataAccessObject.getCurrentShippingAddressForAuction(
                                        auction.getId()
                                    );
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                        )
                );
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } finally {
                processTemplate(request, response, "/buy/index");
            }
        }
    }

    @WebServlet("/buy/auction")
    public static class BuyerAuctionDetailServlet extends ThymeleafHTTPServlet {

        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            try {
                int auctionId;
                checkAllRequiredParams(request, response, "id");
                String auctionIdParam = request.getParameter("id");

                auctionId = Integer.parseInt(auctionIdParam);
                Auction auction = auctionDataAccessObject.getAuctionById(auctionId);
                List<Bid> bids = auctionDataAccessObject.getBidsByAuction(auctionId);

                contextAttributes.set("bids", bids);
                contextAttributes.set("auction", auction);

                processTemplate(request, response, "/buy/auction-detail");
            } catch (NumberFormatException e) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.ERROR.wrap("Invalid auction ID.")
                );
                sendRedirect(request, response, "/buy");
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
                sendRedirect(request, response, "/buy");
            }
        }
    }

    @WebServlet("/buy/auction/bid/")
    public static class BidPlacerServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            int auctionId;
            String auctionIdParam = request.getParameter("auction-id");
            String bidAmountParam = request.getParameter("bid-amount");

            if (auctionIdParam == null || bidAmountParam == null) {
                contextAttributes.setFlash(
                    "message",
                    MessageType.ERROR.wrap("Auction ID and bid amount are required.")
                );
                sendRedirect(request, response, "/buy");
                return;
            }

            try {
                auctionId = Integer.parseInt(auctionIdParam);
                float bidAmount = Float.parseFloat(bidAmountParam);
                String username = ((User) request.getSession().getAttribute("user")).getUsername();

                bidDataAccessObject.placeBid(auctionId, username, bidAmount);

                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Bid placed successfully.")
                );
                sendRedirect(request, response, "/buy/auction?id=" + auctionId);
            } catch (NumberFormatException e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap("Invalid input."));
                sendRedirect(request, response, "/buy");
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
                sendRedirect(request, response, "/buy");
            }
        }
    }
}
