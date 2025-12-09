package com.example.dolorders.data.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO (Data Transfer Object) pour la désérialisation d'un produit depuis l'API.
 * Ses champs et annotations correspondent EXACTEMENT à la structure du JSON reçu.
 */
public class ProduitApiReponseDto {

    @SerializedName("ref")
    public int id;

    @SerializedName("label")
    public String nom;

    @SerializedName("price")
    public double prixUnitaire;

    @SerializedName("category")
    public String categorie;
}
