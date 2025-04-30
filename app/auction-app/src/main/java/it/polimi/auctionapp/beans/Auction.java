package it.polimi.auctionapp.beans;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Auction {

    private Integer id;
    private String seller_username;
    private Timestamp start_time;
    private Timestamp end_time;
    private Integer minimum_bid_increment;
    private Boolean is_closed;
    private Bid currentHighestBid;
    private List<Product> products;

    public Auction(
        Integer id,
        String seller_username,
        Timestamp start_time,
        Timestamp end_time,
        Integer minimum_bid_increment,
        Boolean is_closed,
        Bid currentHighestBid,
        List<Product> products
    ) {
        this.id = id;
        this.seller_username = seller_username;
        this.start_time = start_time;
        this.end_time = end_time;
        this.minimum_bid_increment = minimum_bid_increment;
        this.is_closed = is_closed;
        this.currentHighestBid = currentHighestBid;
        this.products = new ArrayList<>(products);
    }

    public Integer getId() {
        return id;
    }

    public String getSellerUsername() {
        return seller_username;
    }

    public Timestamp getStartTime() {
        return start_time;
    }

    public Timestamp getEndTime() {
        // return remaining time in
        return end_time;
    }

    public String getFormattedEndTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getEndTime());
    }

    public Float getMinimumBidIncrement() {
        return (float) minimum_bid_increment;
    }

    public Boolean isOpen() {
        return !is_closed;
    }

    public String getRemainingTimeString() {
        long currentTime = System.currentTimeMillis();
        long remainingMillis = end_time.getTime() - currentTime;

        if (remainingMillis <= 0) {
            return "0d 0h";
        }

        long days = remainingMillis / (24 * 60 * 60 * 1000);
        long hours = (remainingMillis / (60 * 60 * 1000)) % 24;

        return String.format("%02d" + "d " + "%02d" + "h", days, hours);
    }

    public Bid getCurrentHighestBid() {
        return currentHighestBid;
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    public boolean canBeDeleted() {
        return currentHighestBid == null;
    }

    public boolean canBeClosed() {
        return (
            isOpen() &&
            currentHighestBid != null &&
            currentHighestBid.getBidAmount() != 0 &&
            end_time.getTime() - System.currentTimeMillis() < 0
        );
    }

    public Float getStartingPrice() {
        return (
            (float) Math.round(products.stream().mapToDouble(Product::getPrice).sum() * 100) / 100
        );
    }
}
