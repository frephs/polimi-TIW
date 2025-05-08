package it.polimi.auctionapp.beans;

import java.io.Serializable;

public class Address implements Serializable {

    private String country;
    private Integer zip_code;
    private String city;
    private String street;
    private Integer street_number;

    public Address(
        String country,
        Integer zip_code,
        String city,
        String street,
        Integer street_number
    ) {
        this.country = country;
        this.zip_code = zip_code;
        this.city = city;
        this.street = street;
        this.street_number = street_number;
    }

    public String getCountry() {
        return country;
    }

    public Integer getZipCode() {
        return zip_code;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public Integer getStreetNumber() {
        return street_number;
    }
}