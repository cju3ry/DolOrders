package com.example.dolorders;

import android.content.Context;

import com.example.dolorders.data.storage.ClientStorageManager;

import java.util.List;
import java.util.stream.Collectors;

public class ClientService {

    private final ClientStorageManager storageManager;

    public ClientService(Context context) {
        this.storageManager = new ClientStorageManager(context);
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
