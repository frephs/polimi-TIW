package it.polimi.auctionapp.controllers;

import it.polimi.auctionapp.DAO.AuctionDAO;
import it.polimi.auctionapp.DAO.ProductDAO;
import it.polimi.auctionapp.beans.Auction;
import it.polimi.auctionapp.beans.Product;
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
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

                contextAttributes.set(
                    "closed_auction_shipping_addresses",
                    auctions
                        .stream()
                        .filter(auction -> !auction.isOpen())
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
                contextAttributes.setFlash(
                    "message",
                    MessageType.ERROR.wrap(
                        "There was a problem retrieving your products: " + e.getMessage()
                    )
                );
            } finally {
                processTemplate(request, response, "/sell/index");
            }
        }
    }

    @MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
    )
    @WebServlet("/sell/auction/new")
    public static class AuctionFactoryServlet extends ThymeleafHTTPServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            try {
                checkAllRequiredParams(
                    request,
                    response,
                    "final-bid-submission-date",
                    "final-bid-submission-time"
                );

                List<Product> products = productDataAccessObject
                    .getUnsoldProductBySeller(
                        ((User) request.getSession().getAttribute("user")).getUsername()
                    )
                    .stream()
                    .filter(product -> !product.isAuctioned())
                    .toList();

                String[] productIds = request.getParameterValues("product-ids");

                if (products.isEmpty()) {
                    throw new SQLException(
                        "You cannot create an auction while you have no available products"
                    );
                }

                if (productIds == null) {
                    throw new SQLWarning(
                        "You cannot create an auction without selecting any products"
                    );
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

    @MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
    )
    @WebServlet("/sell/auction/close/")
    public static class AuctionDestroyerServlet extends ThymeleafHTTPServlet {

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            try {
                checkAllRequiredParams(request, response, "id");
                int auctionId = Integer.parseInt(request.getParameter("id"));

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

    @MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
    )
    @WebServlet("/sell/auction/delete/*")
    public static class AuctionDeleteServlet extends ThymeleafHTTPServlet {

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            try {
                checkAllRequiredParams(request, response, "id");
                int auctionId = Integer.parseInt(request.getParameter("id"));

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
            try {
                checkAllRequiredParams(request, response, "id");
                String auctionIdParam = request.getParameter("id");
                System.out.println("auctionIdParam: " + auctionIdParam);
                int auctionId = Integer.parseInt(auctionIdParam);
                auctionDataAccessObject.checkAuctionExists(auctionId);
                auctionDataAccessObject.checkAuctionIsOwnedBy(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    auctionId
                );
                contextAttributes.set("auction", auctionDataAccessObject.getAuctionById(auctionId));
                contextAttributes.set("bids", auctionDataAccessObject.getBidsByAuction(auctionId));
                contextAttributes.set(
                    "closed_auction_shipping_address",
                    auctionDataAccessObject.getCurrentShippingAddressForAuction(auctionId)
                );
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

    @WebServlet("/sell/product/new")
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

    @WebServlet("/sell/product/edit/")
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
                checkAllRequiredParams(
                    request,
                    response,
                    "product-name",
                    "product-description",
                    "product-id"
                );
                Integer productId = Integer.parseInt(request.getParameter("product-id"));
                productDataAccessObject.checkProductExists(productId);
                productDataAccessObject.checkProductIsOwnedBy(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    productId
                );

                productDataAccessObject.updateProductDetails(
                    productId,
                    request.getParameter("product-name"),
                    request.getParameter("product-description")
                );

                String price_string = request.getParameter("product-price");

                if (!Objects.equals(price_string, "") && price_string != null) {
                    productDataAccessObject.updateProductPrice(
                        productId,
                        Float.parseFloat(price_string)
                    );
                }

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
            } catch (SQLWarning e) {
                contextAttributes.setFlash("message", MessageType.WARNING.wrap(e.getMessage()));
            } catch (SQLException e) {
                contextAttributes.setFlash("message", MessageType.ERROR.wrap(e.getMessage()));
            } finally {
                sendRedirect(request, response, "/sell");
            }
        }
    }

    @MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
    )
    @WebServlet("/sell/product/delete/")
    public static class ProductDeleteServlet extends ThymeleafHTTPServlet {

        public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
            try {
                checkAllRequiredParams(request, response, "id");
                Integer productId = Integer.parseInt(request.getParameter("id"));
                productDataAccessObject.checkProductExists(productId);
                productDataAccessObject.checkProductIsOwnedBy(
                    ((User) request.getSession().getAttribute("user")).getUsername(),
                    productId
                );

                productDataAccessObject.deleteProduct(productId);

                contextAttributes.setFlash(
                    "message",
                    MessageType.SUCCESS.wrap("Successfully deleted the product.")
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
}
