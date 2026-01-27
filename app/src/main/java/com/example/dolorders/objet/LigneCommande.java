package com.example.dolorders.objet;

public class LigneCommande {
    private final Produit produit;
    private final int quantite;
    private final double remise; // Remise en pourcentage pour cette ligne

    public LigneCommande(Produit produit, int quantite, double remise) {
        if (produit == null) {
            throw new IllegalArgumentException("Le produit ne peut pas être nul.");
        }
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive.");
        }
        if (remise < 0 || remise > 100) {
            throw new IllegalArgumentException("La remise doit être comprise entre 0 et 100.");
        }
        this.produit = produit;
        this.quantite = quantite;
        this.remise = remise;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getRemise() {
        return remise;
    }

    public double getMontantLigne() {
        double montantInitial = produit.getPrixUnitaire() * quantite;
        return montantInitial * (1 - remise / 100);
    }
}
