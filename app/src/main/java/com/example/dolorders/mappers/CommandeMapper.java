package com.example.dolorders.mappers;

import com.example.dolorders.Commande;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Classe responsable de la conversion entre l'objet Commande et sa représentation JSON.
 */
public class CommandeMapper {

    private final Gson gson;

    public CommandeMapper() {
        // Il est crucial d'utiliser la même configuration Gson que pour les objets imbriqués
        // (comme Client) pour assurer la cohérence, notamment pour les dates.
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                // Si vos clés de Map (Produit) sont des objets complexes,
                // vous pourriez avoir besoin de l'activer ici : .enableComplexMapKeySerialization()
                .create();
    }

    /**
     * Convertit un objet Commande en sa représentation sous forme de chaîne JSON.
     *
     * @param commande L'objet Commande à convertir.
     * @return Une chaîne de caractères contenant le JSON représentant la commande.
     */
    public String toJson(Commande commande) {
        if (commande == null) {
            return null;
        }
        return gson.toJson(commande);
    }

    /**
     * Convertit une chaîne de caractères JSON en un objet Commande.
     *
     * @param commandeJson La chaîne JSON à convertir.
     * @return Un nouvel objet Commande peuplé avec les données du JSON.
     */
    public Commande fromJson(String commandeJson) {
        if (commandeJson == null || commandeJson.trim().isEmpty()) {
            return null;
        }
        return gson.fromJson(commandeJson, Commande.class);
    }
}
