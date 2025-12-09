package com.example.dolorders.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * DTO (Data Transfer Object) pour la sérialisation d'un client à envoyer à l'API.
 * Ses champs et annotations correspondent EXACTEMENT à la structure du JSON attendu par l'API.
 */
public class ClientApiRequeteDto {

    @SerializedName("idclient")
    public String id;

    @SerializedName("nom")
    public String nom;

    @SerializedName("adresse")
    public String adresse;

    @SerializedName("codepostal")
    public String codePostal;

    @SerializedName("ville")
    public String ville;

    @SerializedName("mail")
    public String mail;

    @SerializedName("telephone")
    public String numTel;

    @SerializedName("creation_date")
    public String dateSaisie;

    @SerializedName("creator_name")
    public String utilisateur;

    @SerializedName("submitted_by_name")
    public String utilisateurEnvoie;

    @SerializedName("submission_date")
    public String dateEnvoie;

    @SerializedName("update_date")
    public String dateMiseAJour;
}
