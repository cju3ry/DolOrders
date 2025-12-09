package com.example.dolorders.data.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO pour la sérialisation d'une ligne de commande vers l'API.
 * Chaque instance de cette classe représente UN produit d'une commande.
 */
public class CommandeApiRequeteDto {

    @SerializedName("idcommande")
    public String idCommande;

    @SerializedName("idclient")
    public String idClient;

    @SerializedName("nomclient")
    public String nomClient;

    @SerializedName("datecommande")
    public String dateCommande; // Format "dd/MM/yyyy"

    @SerializedName("codearticle")
    public int codeArticle;

    @SerializedName("label")
    public String labelProduit;

    @SerializedName("qte")
    public int quantite;

    @SerializedName("price")
    public double prixUnitaire;

    @SerializedName("remise")
    public double remise;

    @SerializedName("creator_name")
    public String nomCreateur;

    @SerializedName("creation_date")
    public String dateCreation; // Format "dd/MM/yyyy"

    @SerializedName("submitted_by_name")
    public String nomEnvoi;

    @SerializedName("submission_date")
    public String dateEnvoi; // Format "dd/MM/yyyy"

    @SerializedName("update_date")
    public String dateMiseAJour; // Format "dd/MM/yyyy"
}
