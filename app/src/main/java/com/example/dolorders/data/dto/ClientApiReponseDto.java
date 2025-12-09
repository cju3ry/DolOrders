package com.example.dolorders.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * DTO (Data Transfer Object) pour la désérialisation de la réponse de l'API lors de la récupération d'un client.
 * Ses champs et annotations correspondent EXACTEMENT à la structure du JSON reçu.
 */
public class ClientApiReponseDto {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String nom;

    @SerializedName("adress")
    public String adresse;

    @SerializedName("zip")
    public String codePostal;

    @SerializedName("town")
    public String ville;

    @SerializedName("email")
    public String mail;

    @SerializedName("phone")
    public String numTel;

    @SerializedName("date_creation")
    public long dateSaisie;
}
