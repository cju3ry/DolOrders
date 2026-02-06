package com.example.dolorders.mapper;

import com.example.dolorders.data.dto.ProduitApiReponseDto;
import com.example.dolorders.objet.Produit;

/**
 * Mapper pour convertir un DTO API en objet métier Produit.
 * Applique des valeurs par défaut si les champs sont null ou invalides.
 */
public class ProduitMapper {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private ProduitMapper() {
        // Classe utilitaire : ne doit pas être instanciée
    }

    /**
     * Convertit un ProduitApiReponseDto en Produit.
     *
     * @param dto Le DTO provenant de l'API
     * @return Un objet Produit avec valeurs par défaut si nécessaire
     */
    public static Produit fromDto(ProduitApiReponseDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Le DTO ne peut pas être null");
        }

        // ID : doit toujours exister
        String id = dto.getId() != null && !dto.getId().isEmpty()
                ? dto.getId()
                : "0";

        // Libellé : utilise label ou ref, sinon valeur par défaut
        String libelle;
        if (dto.getLabel() != null && !dto.getLabel().trim().isEmpty()) {
            libelle = dto.getLabel().trim();
        } else if (dto.getRef() != null && !dto.getRef().trim().isEmpty()) {
            libelle = dto.getRef().trim();
        } else {
            libelle = "Produit sans nom";
        }

        // Description : peut être vide
        String description = dto.getDescription() != null
                ? dto.getDescription().trim()
                : "";

        // Prix : conversion String → double avec gestion d'erreur
        double prix = 0.0;
        try {
            if (dto.getPrice() != null && !dto.getPrice().isEmpty()) {
                prix = Double.parseDouble(dto.getPrice());
                // Assurer que le prix est positif
                if (prix < 0) {
                    prix = 0.0;
                }
            }
        } catch (NumberFormatException e) {
            // En cas d'erreur de parsing, prix = 0.0
            prix = 0.0;
        }

        // Taux de TVA : conversion String → double avec gestion d'erreur (défaut = 20%)
        double tauxTva = 20.0; // Taux par défaut
        try {
            if (dto.getTvaTx() != null && !dto.getTvaTx().isEmpty()) {
                tauxTva = Double.parseDouble(dto.getTvaTx());
                // Assurer que le taux est positif
                if (tauxTva < 0) {
                    tauxTva = 20.0;
                }
            }
        } catch (NumberFormatException e) {
            // En cas d'erreur de parsing, taux = 20.0
            tauxTva = 20.0;
        }

        return new Produit(id, libelle, description, prix, tauxTva);
    }
}
