package com.example.dolorders.objet;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Représente un Client au sein de l'application (modèle de domaine).
 * Cette classe est "pure" et ne contient aucune logique de sérialisation.
 * Elle représente la source de vérité pour la logique métier.
 */
public class Client {
    // Déclaration d'une expression régulière simple pour la validation d'e-mail
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
    );

    private final String id;
    private final String nom;
    private final String adresse;
    private final String codePostal;
    private final String ville;
    private final String adresseMail;
    private final String telephone;
    private final String utilisateur;
    private final Date dateSaisie;

    private final boolean fromApi;

    private Client(Builder builder) {
        this.id = builder.id;
        this.nom = builder.nom;
        this.adresse = builder.adresse;
        this.codePostal = builder.codePostal;
        this.ville = builder.ville;
        this.adresseMail = builder.adresseMail;
        this.telephone = builder.telephone;
        this.utilisateur = builder.utilisateur;
        this.dateSaisie = builder.dateSaisie;
        this.fromApi = builder.fromApi;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public String getVille() {
        return ville;
    }

    public String getAdresseMail() {
        return adresseMail;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public Date getDateSaisie() {
        return dateSaisie;
    }

    public boolean isFromApi() {
        return fromApi;
    }

    // --- Builder ---
    public static class Builder {
        private String id;
        private String nom;
        private String adresse;
        private String codePostal;
        private String ville;
        private String adresseMail;
        private String telephone;
        private String utilisateur;
        private Date dateSaisie;
        private boolean fromApi;


        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setNom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder setAdresse(String adresse) {
            this.adresse = adresse;
            return this;
        }

        public Builder setCodePostal(String codePostal) {
            this.codePostal = codePostal;
            return this;
        }

        public Builder setVille(String ville) {
            this.ville = ville;
            return this;
        }

        public Builder setAdresseMail(String adresseMail) {
            this.adresseMail = adresseMail;
            return this;
        }

        public Builder setTelephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public Builder setUtilisateur(String utilisateur) {
            this.utilisateur = utilisateur;
            return this;
        }

        public Builder setDateSaisie(Date dateSaisie) {
            this.dateSaisie = dateSaisie;
            return this;
        }

        public Builder setFromApi(boolean fromApi) {
            this.fromApi = fromApi;
            return this;
        }


        public Client build() {
            // La logique de validation reste ici, garantissant que tout objet Client
            // dans l'application est toujours valide.
            if (nom == null || nom.trim().isEmpty()) {
                throw new IllegalStateException("Le client doit avoir un nom !");
            }
            if (adresse == null || adresse.trim().isEmpty()) {
                throw new IllegalStateException("Le client doit avoir une adresse !");
            }
            if (codePostal == null || !codePostal.matches("\\d{5}")) {
                throw new IllegalStateException("Le client doit avoir un code postal valide à 5 chiffres !");
            }
            if (ville == null || ville.trim().isEmpty()) {
                throw new IllegalStateException("Le client doit avoir une ville !");
            }
            if (adresseMail == null || !EMAIL_PATTERN.matcher(adresseMail).matches()) {
                throw new IllegalStateException("Le client doit avoir une adresse mail valide !");
            }
            if (telephone == null || !telephone.matches("\\d{10}")) {
                throw new IllegalStateException("Le client doit avoir un numéro de téléphone valide à 10 chiffres !");
            }
            if (utilisateur == null || utilisateur.trim().isEmpty()) {
                throw new IllegalStateException("Le client doit avoir un utilisateur !");
            }
            if (dateSaisie == null) {
                throw new IllegalStateException("Le client doit avoir une date de saisie !");
            }
            return new Client(this);
        }

        /**
         * Construit un Client provenant de l'API Dolibarr sans validation stricte.
         * Les champs manquants sont remplacés par des valeurs par défaut.
         * Le flag fromApi est automatiquement mis à true.
         *
         * @return Un objet Client avec des valeurs par défaut si nécessaire
         */
        public Client buildFromApi() {
            // Valeurs par défaut pour les champs manquants de l'API
            if (nom == null || nom.trim().isEmpty()) {
                nom = "Client inconnu";
            }
            if (adresse == null || adresse.trim().isEmpty()) {
                adresse = "Adresse non renseignée";
            }
            if (codePostal == null || codePostal.trim().isEmpty() || !codePostal.matches("\\d{5}")) {
                codePostal = "00000";
            }
            if (ville == null || ville.trim().isEmpty()) {
                ville = "Ville non renseignée";
            }
            if (adresseMail == null || adresseMail.trim().isEmpty() || !EMAIL_PATTERN.matcher(adresseMail).matches()) {
                adresseMail = "noemail@inconnu.com";
            }
            if (telephone == null || telephone.trim().isEmpty() || !telephone.matches("\\d{10}")) {
                telephone = "0000000000";
            }
            if (utilisateur == null || utilisateur.trim().isEmpty()) {
                utilisateur = "API_DOLIBARR";
            }
            if (dateSaisie == null) {
                dateSaisie = new Date();
            }

            // Force le flag fromApi à true pour les clients de l'API
            fromApi = true;

            return new Client(this);
        }
    }

    @Override
    public String toString() {
        return this.nom;
    }
}
