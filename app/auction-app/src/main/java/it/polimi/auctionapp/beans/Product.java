package it.polimi.auctionapp.beans;

public class Product {
    private Integer id;
    private String name;
    private String description;
    private Integer price;
    private String image_url;

    public Product(Integer id, String name, String description, Integer price, String image_url) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image_url = image_url;
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

    public Integer getPrice() {
        return price;
    }

    public String getImage_url() {
        return image_url;
    }
}
