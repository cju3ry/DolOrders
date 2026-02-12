package com.example.dolorders.data.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO (Data Transfer Object) pour la réponse API de Dolibarr concernant les produits.
 * Représente la structure JSON exacte renvoyée par l'API /products.
 * Appel API : GET /products
 */
public class ProduitApiReponseDto {
    /** Note : Les annotations @SerializedName sont utilisées pour faire correspondre les noms des
     * champs JSON avec les noms des variables Java, même s'ils diffèrent.
     * */

    @SerializedName("id")
    private String id;

    @SerializedName("label")
    private String label;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private String price;

    @SerializedName("tva_tx")
    private String tvaTx; // Taux de TVA (ex: "20.0000")

    @SerializedName("ref")
    private String ref;

    @SerializedName("status")
    private String status;

    /**
     * Constructeur par defaut obligatoire pour la deserialisation (Jackson/JPA).
     */
    public ProduitApiReponseDto() {
        // obligatoire pour le framework
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTvaTx() {
        return tvaTx;
    }

    public void setTvaTx(String tvaTx) {
        this.tvaTx = tvaTx;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ProduitApiReponseDto{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                ", tvaTx='" + tvaTx + '\'' +
                ", ref='" + ref + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
