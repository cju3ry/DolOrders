package com.example.dolorders.ui;

import com.example.dolorders.Client;

public class ClientFilter {
    final String nom, adresse, codePostal, ville, telephone;

    ClientFilter(String nom, String adresse, String codePostal, String ville, String telephone) {
        this.nom = nom;
        this.adresse = adresse;
        this.codePostal = codePostal;
        this.ville = ville;
        this.telephone = telephone;
    }

    static boolean matches(Client c, ClientFilter f) {
        if (f == null) return true;

        if (isNotBlank(f.nom) && notContainsIgnoreCase(c.getNom(), f.nom)) return false;
        if (isNotBlank(f.adresse) && notContainsIgnoreCase(c.getAdresse(), f.adresse)) return false;

        if (isNotBlank(f.codePostal)) {
            String cpClient = safe(c.getCodePostal());
            String cpWanted = f.codePostal.trim();
            // "startsWith" permet de filtrer par préfixe (ex: "12" match "12000")
            if (!cpClient.startsWith(cpWanted)) return false;
        }

        if (isNotBlank(f.ville) && notContainsIgnoreCase(c.getVille(), f.ville)) return false;

        if (isNotBlank(f.telephone)) {
            String telClient = normalizePhone(c.getTelephone());
            String telWanted = normalizePhone(f.telephone);
            return telClient.contains(telWanted);
        }

        return true;
    }

    static String textOf(com.google.android.material.textfield.TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString().trim() : "";
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean notContainsIgnoreCase(String field, String q) {
        return !safe(field).toLowerCase().contains(safe(q).trim().toLowerCase());
    }

    private static String normalizePhone(String tel) {
        return safe(tel).replaceAll("\\D+", "");
    }
}