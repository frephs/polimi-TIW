package it.polimi.auctionapp.beans;

import it.polimi.auctionapp.utils.SQLConnectionHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class Product {

    private Integer id;
    private String name;
    private String description;
    private Float price;
    private String image_filename;
    private Integer auction_id;

    public Product(
        Integer id,
        String name,
        String description,
        Float price,
        String image_filename,
        Integer auction_id
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image_filename = image_filename;
        this.auction_id = auction_id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Float getPrice() {
        return price;
    }

    public String getImageFilename() {
        return image_filename;
    }

    public Integer getAuctionId() {
        return auction_id;
    }

    public boolean isAuctioned() {
        return auction_id != 0;
    }

    public boolean canChangeAuction(List<Auction> auctions) {
        for (Auction auction : auctions) {
            if (
                auction
                    .getProducts()
                    .stream()
                    .anyMatch(product -> Objects.equals(product.getId(), this.id))
            ) {
                return auction.getProducts().size() > 2;
            }
        }
        return false;
    }

    public boolean canChangePrice(List<Auction> auctions) {
        if (!this.isAuctioned()) return true;
        for (Auction auction : auctions) {
            if (
                auction
                    .getProducts()
                    .stream()
                    .anyMatch(product -> Objects.equals(product.getId(), this.id))
            ) {
                return auction.getCurrentHighestBid() == null;
            }
        }
        return false;
    }

    
}
