package it.polimi.auctionapp.beans;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String name;
    private String surname;
    private Address address;

    public User(String username,  String name, String surname, Address address) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Address getAddress() {
        return address;
    }
}

