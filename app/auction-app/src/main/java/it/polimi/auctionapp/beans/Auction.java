package it.polimi.auctionapp.beans;

import java.sql.Timestamp;

public class Auction {

    private Integer id;
    private String seller_username;
    private Timestamp start_time;
    private Timestamp end_time;
    private Integer minimum_bid_increment;
    private Boolean is_closed;
    private Integer item_count;

    public Auction(
        Integer id,
        String seller_username,
        Timestamp start_time,
        Timestamp end_time,
        Integer minimum_bid_increment,
        Integer item_count,
        Boolean is_closed
    ) {
        this.id = id;
        this.seller_username = seller_username;
        this.start_time = start_time;
        this.end_time = end_time;
        this.minimum_bid_increment = minimum_bid_increment;
        this.item_count = item_count;
        this.is_closed = is_closed;
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

    public Integer getMinimumBidIncrement() {
        return minimum_bid_increment;
    }

    public Boolean isOpen() {
        return !is_closed;
    }

    public String getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        long remainingMillis = end_time.getTime() - currentTime;

        if (remainingMillis <= 0) {
            return "00:00";
        }

        long days = remainingMillis / (24 * 60 * 60 * 1000);
        long hours = (remainingMillis / (60 * 60 * 1000)) % 24;

        return String.format("%02d" + "d " + "%02d" + "h", days, hours);
    }

    public Integer getItemCount() {
        return item_count;
    }
}
