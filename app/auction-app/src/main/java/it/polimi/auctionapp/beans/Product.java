package it.polimi.auctionapp.beans;

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
}
