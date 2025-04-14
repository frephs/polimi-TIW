package it.polimi.auctionapp.beans;

public class User {
    private String username;
    private String password;
    private String name;
    private String surname;
    private Address address;

    public User(String username, String password, String name, String surname, Address address) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

