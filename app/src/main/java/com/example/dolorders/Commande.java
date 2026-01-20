package com.example.dolorders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;import java.util.Map;

public class Commande {
    private final String id;
    private final Client client;
    private final Date dateCommande;
    private final List<LigneCommande> lignesCommande;
    private final double montantTotal;
    private final String utilisateur;
    private final double remiseGlobale; // Non utilisé pour le moment

    private Commande(Builder builder) {
        this.id = builder.id;
        this.dateCommande = builder.dateCommande;
        this.client = builder.client;
        this.lignesCommande = builder.lignesCommande;
        this.montantTotal = calculerMontantTotal();
        this.utilisateur = builder.utilisateur;
        this.remiseGlobale = builder.remiseGlobale;
    }

    private double calculerMontantTotal() {
        if (lignesCommande == null || lignesCommande.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (LigneCommande ligne : lignesCommande) {
            total += ligne.getMontantLigne();
        }
        return total;
    }

    public String getId() { return id; }
    public Date getDateCommande() { return dateCommande; }
    public Client getClient() { return client; }
    public List<LigneCommande> getLignesCommande() { return lignesCommande; }
    public double getMontantTotal() { return montantTotal * (1 - remiseGlobale / 100); }
    public String getUtilisateur() { return utilisateur; }
    public double getRemiseGlobale() { return remiseGlobale; }

    public static class Builder {
        private String id;
        private Date dateCommande;
        private Client client;
        private List<LigneCommande> lignesCommande;
        private String utilisateur;
        private double remiseGlobale;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setDateCommande(Date dateCommande) {
            this.dateCommande = dateCommande;
            return this;
        }

        public Builder setClient(Client client) {
            this.client = client;
            return this;
        }

        public Builder setLignesCommande(List<LigneCommande> lignesCommande) {
            this.lignesCommande = lignesCommande;
            return this;
        }

        public Builder setUtilisateur(String utilisateur) {
            this.utilisateur = utilisateur;
            return this;
        }

        public Builder setRemiseGlobale(double remise) {
            this.remiseGlobale = remise;
            return this;
        }

        public Commande build() {
            if (client == null) {
                throw new IllegalStateException("Une commande doit être associée à un client.");
            }
            if (dateCommande == null) {
                this.dateCommande = new Date();
            }
            if (lignesCommande == null || lignesCommande.isEmpty()) { // Modifié
                throw new IllegalStateException("Une commande doit contenir au moins un produit.");
            }
            if (utilisateur == null || utilisateur.isEmpty()) {
                throw new IllegalStateException("Un utilisateur doit être renseigné.");
            }
            if (remiseGlobale < 0) { // Modifié
                throw new IllegalStateException("La remise globale ne peut pas être négative.");
            }

            return new Commande(this);
        }
    }
}
