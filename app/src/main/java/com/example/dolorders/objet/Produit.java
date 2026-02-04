package com.example.dolorders.objet;

import java.util.Objects;

public class Produit {
    private final String id;
    private final String libelle;
    private final String description;
    private final double prixUnitaire;

    // Nouveau constructeur complet
    public Produit(String id, String nom, String description, double prixUnitaire) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit ne peut pas être vide.");
        }
        if (prixUnitaire < 0) {
            throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif.");
        }
        this.id = id;
        this.libelle = nom;
        this.description = description != null ? description : "";
        this.prixUnitaire = prixUnitaire;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    // --- Equals & HashCode pour les comparaisons dans les listes ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return Objects.equals(id, produit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return libelle;
    }
}