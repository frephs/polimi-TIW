package it.polimi.auctionapp.beans;

import java.time.LocalDateTime;

public class Bid {
    private Integer auction_id;
    private Integer bidder_username;
    private Integer bid_amount;
    private LocalDateTime bid_timestamp;

    public Bid(Integer auction_id, Integer bidder_username, Integer bid_amount, LocalDateTime bid_timestamp) {
        this.auction_id = auction_id;
        this.bidder_username = bidder_username;
        this.bid_amount = bid_amount;
        this.bid_timestamp = bid_timestamp;
    }

    public Integer getAuction_id() {
        return auction_id;
    }

    public Integer getBidder_username() {
        return bidder_username;
    }

    public Integer getBid_amount() {
        return bid_amount;
    }

    public LocalDateTime getBid_timestamp() {
        return bid_timestamp;
    }
}
