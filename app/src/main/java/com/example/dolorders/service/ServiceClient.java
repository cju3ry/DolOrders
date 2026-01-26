package com.example.dolorders.service;

import android.content.Context;

import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.objet.Client;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceClient {

    private final GestionnaireStockageClient storageManager;

    public ServiceClient(Context context) {
        this.storageManager = new GestionnaireStockageClient(context);
    }

    /**
     * Filtre les clients selon les champs
     */
    public List<Client> filter(String nom, String adresse, String codePostal, String ville, String telephone) {
        List<Client> allClients = storageManager.loadClients();

        return allClients.stream()
                .filter(c -> (nom == null || nom.isEmpty() || c.getNom().toLowerCase().contains(nom.toLowerCase())))
                .filter(c -> (adresse == null || adresse.isEmpty() || c.getAdresse().toLowerCase().contains(adresse.toLowerCase())))
                .filter(c -> (codePostal == null || codePostal.isEmpty() || c.getCodePostal().toLowerCase().contains(codePostal.toLowerCase())))
                .filter(c -> (ville == null || ville.isEmpty() || c.getVille().toLowerCase().contains(ville.toLowerCase())))
                .filter(c -> (telephone == null || telephone.isEmpty() || c.getTelephone().toLowerCase().contains(telephone.toLowerCase())))
                .collect(Collectors.toList());
    }
}
