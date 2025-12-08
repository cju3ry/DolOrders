package com.example.dolorders.mappers;

import com.example.dolorders.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Classe responsable de la conversion entre l'objet Client et sa représentation JSON.
 * Elle respecte le Principe de Responsabilité Unique en isolant la logique de sérialisation
 * des modèles de données.
 */
public class ClientMapper {

    private final Gson gson;

    public ClientMapper() {
        // Gson est thread-safe, mais il est bon de le configurer une seule fois.
        // On configure Gson pour qu'il formate correctement les dates.
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ") // Format de date standard ISO 8601
                .create();
    }

    /**
     * Convertit un objet Client en sa représentation sous forme de chaîne JSON.
     *
     * @param client L'objet Client à convertir.
     * @return Une chaîne de caractères contenant le JSON représentant le client.
     */
    public String toJson(Client client) {
        if (client == null) {
            return null;
        }
        // La librairie Gson s'occupe de toute la complexité de la conversion.
        return gson.toJson(client);
    }

    /**
     * Convertit une chaîne de caractères JSON en un objet Client.
     *
     * @param clientJson La chaîne JSON à convertir.
     * @return Un nouvel objet Client peuplé avec les données du JSON.
     * @throws com.google.gson.JsonSyntaxException si la chaîne n'est pas un JSON valide.
     */
    public Client fromJson(String clientJson) {
        if (clientJson == null || clientJson.trim().isEmpty()) {
            return null;
        }
        // Gson mappe automatiquement les champs du JSON aux champs de la classe Client.
        return gson.fromJson(clientJson, Client.class);
    }
}
