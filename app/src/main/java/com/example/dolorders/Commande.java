package com.example.dolorders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Commande {
    private final String id;
    private final Client client;
    private final Date dateCommande;
    private final Map<Produit, Integer> produitsEtQuantites;
    private final double montantTotal;
    private final double remise;
    private final String utilisateur;

    private Commande(Builder builder) {
        this.id = builder.id;
        this.dateCommande = builder.dateCommande;
        this.client = builder.client;
        this.produitsEtQuantites = builder.produitsEtQuantites;
        this.montantTotal = calculerMontantTotal();
        this.utilisateur = builder.utilisateur;
        this.remise = builder.remise;
    }

    private double calculerMontantTotal() {
        if (produitsEtQuantites == null || produitsEtQuantites.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Map.Entry<Produit, Integer> entry : produitsEtQuantites.entrySet()) {
            total += entry.getKey().getPrixUnitaire() * entry.getValue();
        }
        return total;
    }

    public String getId() { return id; }
    public Date getDateCommande() { return dateCommande; }
    public Client getClient() { return client; }
    public Map<Produit, Integer> getProduitsEtQuantites() { return produitsEtQuantites; }
    public double getMontantTotal() { return montantTotal * (1 - remise / 100); }
    public String getUtilisateur() { return utilisateur; }
    public double getRemise() { return remise; }

    public static class Builder {
        private String id;
        private Date dateCommande;
        private Client client;
        private Map<Produit, Integer> produitsEtQuantites;
        private String utilisateur;
        private double remise;

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

        public Builder setProduitsEtQuantites(Map<Produit, Integer> produitsEtQuantites) {
            this.produitsEtQuantites = produitsEtQuantites;
            return this;
        }

        public Builder setUtilisateur(String utilisateur) {
            this.utilisateur = utilisateur;
            return this;
        }

        public Builder setRemise(double remise) {
            this.remise = remise;
            return this;
        }


        public Commande build() {
            if (client == null) {
                throw new IllegalStateException("Une commande doit être associée à un client.");
            }
            if (dateCommande == null) {
                this.dateCommande = new Date();
            }
            if (produitsEtQuantites == null || produitsEtQuantites.isEmpty()) {
                throw new IllegalStateException("Une commande doit contenir au moins un produit.");
            }
            if (utilisateur == null || utilisateur.isEmpty()) {
                throw new IllegalStateException("Un utilisateur doit être renseigné.");
            }
            if (remise < 0) {
                throw new IllegalStateException("La remise ne peut pas être négative.");
            }

            return new Commande(this);
        }
    }
}
