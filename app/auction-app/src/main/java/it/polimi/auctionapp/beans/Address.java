package it.polimi.auctionapp.beans;

public class Address {
    private String country;
    private String zipCode;
    private String city;
    private String street;
    private Integer street_number;

    public Address(String country, String zipCode, String city, String street, Integer street_number) {
        this.country = country;
        this.zipCode = zipCode;
        this.city = city;
        this.street = street;
        this.street_number = street_number;
    }

    public String getCountry() {
        return country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public Integer getStreet_number() {
        return street_number;
    }
}