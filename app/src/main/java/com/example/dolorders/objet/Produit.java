package com.example.dolorders.objet;

import java.util.Objects;

public class Produit {
    private final String id;
    private final String libelle;
    private final String description;
    private final double prixUnitaire;
    private final double tauxTva;

    // Nouveau constructeur complet avec TVA
    public Produit(String id, String nom, String description, double prixUnitaire, double tauxTva) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit ne peut pas être vide.");
        }
        if (prixUnitaire < 0) {
            throw new IllegalArgumentException("Le prix unitaire ne peut pas être négatif.");
        }
        if (tauxTva < 0) {
            throw new IllegalArgumentException("Le taux de TVA ne peut pas être négatif.");
        }
        this.id = id;
        this.libelle = nom;
        this.description = description != null ? description : "";
        this.prixUnitaire = prixUnitaire;
        this.tauxTva = tauxTva;
    }

    // Constructeur de compatibilité sans TVA (TVA par défaut = 20%)
    public Produit(String id, String nom, String description, double prixUnitaire) {
        this(id, nom, description, prixUnitaire, 20.0);
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

    public double getTauxTva() {
        return tauxTva;
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