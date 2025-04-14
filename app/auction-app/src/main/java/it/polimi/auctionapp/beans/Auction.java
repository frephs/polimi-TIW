package it.polimi.auctionapp.beans;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction {
    private Integer id;
    private String seller_username;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private Integer minimum_bid_increment;
    private Boolean is_closed;
    private List<Product> products= new ArrayList<>();


    public Auction(Integer id, String seller_username, LocalDateTime start_time, LocalDateTime end_time,
            Integer minimum_bid_increment, List<Product> products) {
        this.id = id;
        this.seller_username = seller_username;
        this.start_time = start_time;
        this.end_time = end_time;
        this.minimum_bid_increment = minimum_bid_increment;
        this.is_closed = false;
    }

    public Integer getId() {
        return id;
    }

    public String getSeller_username() {
        return seller_username;
    }

    public LocalDateTime getStart_time() {
        return start_time;
    }

    public LocalDateTime getEnd_time() {
        return end_time;
    }

    public Integer getMinimum_bid_increment() {
        return minimum_bid_increment;
    }

    public Boolean getIs_closed() {
        return is_closed;
    }

    public List<Product> getProducts() {
        return products;
    }
}
