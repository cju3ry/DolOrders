package com.example.dolorders.mappers;

import com.example.dolorders.Produit;
import com.example.dolorders.data.dto.ProduitApiReponseDto;

/**
 * Mapper responsable de la conversion entre le DTO Produit
 * et le modèle de domaine Produit.
 */
public class ProduitMapper {

    /**
     * Convertit un DTO de réponse API en un objet Produit du domaine.
     * C'est ici qu'on transforme les données brutes en un objet métier valide.
     *
     * @param dto Le DTO reçu de l'API.
     * @return Un objet Produit valide.
     */
    public Produit getProduit(ProduitApiReponseDto dto) {
        if (dto == null) {
            return null;
        }

        return new Produit(
                dto.id,
                dto.nom,
                dto.prixUnitaire,
                dto.categorie
        );
    }
}
