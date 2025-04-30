package it.polimi.auctionapp.beans;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Bid {

    private Integer auction_id;
    private String bidder_username;
    private Float bid_amount;
    private Timestamp bid_timestamp;

    public Bid(
        Integer auction_id,
        String bidder_username,
        Float bid_amount,
        Timestamp bid_timestamp
    ) {
        this.auction_id = auction_id;
        this.bidder_username = bidder_username;
        this.bid_amount = bid_amount;
        this.bid_timestamp = bid_timestamp;
    }

    public Integer getAuctionId() {
        return auction_id;
    }

    public String getBidderUsername() {
        return bidder_username;
    }

    public Float getBidAmount() {
        return Math.round(bid_amount * 100) / 100.0f;
    }

    public Timestamp getBidTimestamp() {
        return bid_timestamp;
    }

    public String getFormattedBidTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bid_timestamp);
    }
}
