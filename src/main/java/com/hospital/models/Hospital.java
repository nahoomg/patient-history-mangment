package com.hospital.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Hospital {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty address;
    private final SimpleStringProperty contactNumber;
    private final SimpleStringProperty email;
    private final SimpleStringProperty website;
    private final SimpleStringProperty username;
    private final SimpleStringProperty password;
    private final SimpleStringProperty description;

    public Hospital(int id, String name, String address, String contactNumber, String email, 
                   String website, String username, String password, String description) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
        this.contactNumber = new SimpleStringProperty(contactNumber);
        this.email = new SimpleStringProperty(email);
        this.website = new SimpleStringProperty(website);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.description = new SimpleStringProperty(description);
    }

    // Getters
    public int getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public String getAddress() {
        return address.get();
    }

    public String getContactNumber() {
        return contactNumber.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getWebsite() {
        return website.get();
    }

    public String getUsername() {
        return username.get();
    }

    public String getPassword() {
        return password.get();
    }

    public String getDescription() {
        return description.get();
    }

    // Setters
    public void setName(String name) {
        this.name.set(name);
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber.set(contactNumber);
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public void setWebsite(String website) {
        this.website.set(website);
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    // Property getters for JavaFX binding
    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public SimpleStringProperty contactNumberProperty() {
        return contactNumber;
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public SimpleStringProperty websiteProperty() {
        return website;
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    @Override
    public String toString() {
        return name.get();
    }
} 