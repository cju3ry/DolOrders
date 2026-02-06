package com.example.dolorders.data.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO (Data Transfer Object) pour la réponse API de Dolibarr concernant les clients (thirdparties).
 * Représente la structure JSON renvoyée par l'API /thirdparties.
 * <p>
 * Appel API : GET /thirdparties?sortfield=t.rowid&sortorder=ASC&limit=100&properties=id,name,phone,email,address,zip,town
 */
public class ClientApiReponseDto {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("address")
    private String address;

    @SerializedName("zip")
    private String zip;

    @SerializedName("town")
    private String town;

    /**
     * Constructeur par defaut obligatoire pour la deserialisation (Jackson/JPA).
     */
    public ClientApiReponseDto() {
        // obligatoire pour le framework
    }


    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    @Override
    public String toString() {
        return "ClientApiReponseDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", zip='" + zip + '\'' +
                ", town='" + town + '\'' +
                '}';
    }
}

