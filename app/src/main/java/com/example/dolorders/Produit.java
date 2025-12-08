package com.example.dolorders;

import java.util.Objects;

public class Produit {
    private final int id;
    private final String nom;
    private final double prixUnitaire;
    private final String categorie;

    public Produit(int id, String nom, double prixUnitaire, String categorie) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit ne peut pas être vide.");
        }
        if (prixUnitaire < 0) {
            throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif.");
        }
        this.id = id;
        this.nom = nom;
        this.prixUnitaire = prixUnitaire;
        this.categorie = categorie;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public String getCategorie() { return categorie; }

    // --- Equals & HashCode pour les comparaisons dans les listes ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return id == produit.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nom;
    }
}
