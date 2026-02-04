package com.example.dolorders.mapper;

import com.example.dolorders.data.dto.ClientApiReponseDto;
import com.example.dolorders.objet.Client;

import java.util.Date;

/**
 * Mapper pour convertir un DTO API en objet métier Client.
 * Utilise buildFromApi() pour gérer les champs manquants de l'API Dolibarr.
 */
public class ClientApiMapper {

    /**
     * Convertit un ClientApiReponseDto en Client.
     * Les champs manquants ou null sont gérés avec des valeurs par défaut
     * via la méthode buildFromApi() du Builder.
     *
     * @param dto Le DTO provenant de l'API
     * @return Un objet Client avec valeurs par défaut si nécessaire, ou null si le DTO est null
     */
    public static Client fromDto(ClientApiReponseDto dto) {
        if (dto == null) {
            return null;
        }

        return new Client.Builder()
                .setId(dto.getId())
                .setNom(dto.getName())
                .setAdresse(dto.getAddress())
                .setCodePostal(dto.getZip())
                .setVille(dto.getTown())
                .setAdresseMail(dto.getEmail())
                .setTelephone(formatPhoneNumber(dto.getPhone()))
                .setUtilisateur("API_DOLIBARR")
                .setDateSaisie(new Date())
                .setFromApi(true)
                .buildFromApi();
    }

    /**
     * Formate le numéro de téléphone pour qu'il soit compatible avec la validation.
     * Retire les espaces, tirets et le préfixe +33.
     *
     * @param phone Numéro de téléphone brut
     * @return Numéro de téléphone formaté (10 chiffres) ou null
     */
    private static String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        // Retirer tous les caractères non numériques sauf le +
        String cleaned = phone.replaceAll("[^0-9+]", "");

        // Gérer le préfixe +33 (France)
        if (cleaned.startsWith("+33")) {
            cleaned = "0" + cleaned.substring(3);
        }

        // Garder seulement les 10 premiers chiffres
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(0, 10);
        }

        return cleaned;
    }
}

