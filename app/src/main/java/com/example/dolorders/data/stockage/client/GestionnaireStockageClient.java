package com.example.dolorders.data.stockage.client;

import android.content.Context;
import android.util.Log;

import com.example.dolorders.objet.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gestionnaire unifié de stockage des clients dans un fichier JSON local.
 * Gère à la fois les clients créés localement et ceux provenant de l'API Dolibarr.
 * Le fichier est stocké dans le répertoire interne de l'application
 * et sera automatiquement supprimé si l'application est désinstallée.
 */
public class GestionnaireStockageClient {

    private static final String DEFAULT_FILE_NAME = "clients_data.json";
    public static final String API_CLIENTS_FILE = "clients_api_data.json";
    private static final String TAG = "ClientStorage";

    private final Context context;
    private final Gson gson;
    private final String fileName;

    private boolean isCommandeClientSuppr;

    /**
     * Constructeur par défaut utilisant le fichier de clients locaux.
     *
     * @param context Contexte de l'application
     */
    public GestionnaireStockageClient(Context context) {
        this(context, DEFAULT_FILE_NAME);
    }

    /**
     * Constructeur avec nom de fichier personnalisé.
     * Permet de créer des gestionnaires pour différents types de stockage
     * (clients locaux, clients API, etc.)
     *
     * @param context Contexte de l'application
     * @param fileName Nom du fichier de stockage
     */
    public GestionnaireStockageClient(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
        // Configuration de Gson avec l'adaptateur unifié pour Client
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Client.class, new AdaptateurStockageClient())
                .create();
    }

    /**
     * Sauvegarde la liste des clients dans un fichier JSON.
     * Cette opération écrase les données précédentes.
     *
     * @param clients Liste des clients à sauvegarder
     * @return true si la sauvegarde a réussi, false sinon
     */
    public boolean saveClients(List<Client> clients) {
        if (clients == null) {
            Log.w(TAG, "Tentative de sauvegarde d'une liste null");
            return false;
        }

        try {
            // Conversion de la liste en JSON
            String jsonData = gson.toJson(clients);

            // Écriture dans le fichier interne
            try (FileOutputStream fos = context.openFileOutput(getFileName(), Context.MODE_PRIVATE)) {
                OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                writer.write(jsonData);
                writer.close();
            }

            Log.d(TAG, "Clients sauvegardés avec succès (" + clients.size() + " clients) dans : " + getFileName());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde des clients", e);
            return false;
        }
    }

    /**
     * Charge la liste des clients depuis le fichier JSON.
     *
     * @return Liste des clients sauvegardés, ou une liste vide si aucune donnée
     */
    public List<Client> loadClients() {
        File file = new File(context.getFilesDir(), getFileName());

        // Si le fichier n'existe pas, retourner une liste vide
        if (!file.exists()) {
            Log.d(TAG, "Aucun fichier de clients trouvé : " + getFileName());
            return new ArrayList<>();
        }

        try {
            // Lecture du fichier
            StringBuilder jsonBuilder;
            try (FileInputStream fis = context.openFileInput(getFileName())) {
                jsonBuilder = new StringBuilder();

                try (InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                    try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            jsonBuilder.append(line);
                        }
                    }
                }
            }

            // Conversion du JSON en liste d'objets Client
            String jsonData = jsonBuilder.toString();
            Type listType = new TypeToken<List<Client>>() {
            }.getType();
            List<Client> clients = gson.fromJson(jsonData, listType);

            if (clients == null) {
                clients = new ArrayList<>();
            }

            Log.d(TAG, "Clients chargés avec succès (" + clients.size() + " clients)");
            return clients;

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des clients", e);
            return new ArrayList<>();
        }
    }

    /**
     * Vérifie si des données clients existent dans le stockage.
     *
     * @return true si le fichier existe et contient des données
     */
    public boolean hasStoredClients() {
        File file = new File(context.getFilesDir(), getFileName());
        return file.exists() && file.length() > 0;
    }

    /**
     * Retourne le nom du fichier à utiliser pour le stockage.
     * Cette méthode peut être surchargée dans les tests pour utiliser un fichier différent.
     *
     * @return Le nom du fichier de stockage
     */
    protected String getFileName() {
        return fileName;
    }

    /**
     * Supprime toutes les données clients stockées.
     *
     * @return true si la suppression a réussi, false sinon
     */
    public boolean clearClients() {
        File file = new File(context.getFilesDir(), getFileName());

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d(TAG, "Fichier de clients supprimé avec succès");
            } else {
                Log.w(TAG, "Échec de la suppression du fichier de clients");
            }
            return deleted;
        }

        Log.d(TAG, "Aucun fichier de clients à supprimer");
        return true;
    }

    /**
     * Ajoute un client à la liste existante et sauvegarde.
     *
     * @param client Client à ajouter
     * @return true si l'ajout et la sauvegarde ont réussi
     */
    public boolean addClient(Client client) {
        if (client == null) {
            Log.w(TAG, "Tentative d'ajout d'un client null");
            return false;
        }

        List<Client> clients = loadClients();
        clients.add(client);
        return saveClients(clients);
    }

    /**
     * Obtient le nombre de clients stockés.
     *
     * @return Nombre de clients
     */
    public int getClientCount() {
        return loadClients().size();
    }

    /**
     * Modifie un client existant dans le stockage.
     * Si le client n'existe pas (pas trouvé par ID), aucune modification n'est faite.
     *
     * @param updatedClient Le client contenant les nouvelles données (doit avoir un ID existant)
     * @return true si le client a été trouvé et modifié, false sinon
     */
    public boolean modifierClient(Client updatedClient) {
        if (updatedClient == null) {
            Log.w(TAG, "Tentative de modification d'un client null");
            return false;
        }

        List<Client> clients = loadClients();

        // Remplacer le client
        boolean trouve = false;
        for (int i = 0; i < clients.size() && !trouve; i++) {
            if (Objects.equals(clients.get(i).getId(), updatedClient.getId())) {
                clients.set(i, updatedClient);
                trouve = true;
            }
        }

        // Sauvegarde centralisée
        return saveClients(clients);
    }

    /**
     * Supprime un client existant du stockage.
     *
     * @param client Client à supprimer (identifié par son ID)
     * @return true si le client a été trouvé et supprimé, false sinon
     */
    public boolean deleteClient(Client client) {
        boolean isCommandeClientSuppr = false;
        if (client == null) {
            Log.w(TAG, "Tentative de suppression d'un client null");
            return false;
        }

        List<Client> clients = loadClients();
        int indexToDelete = -1;

        // On parcourt la liste pour trouver l'index du client
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            // Vérification sécurisée de l'ID
            if (c.getId() != null && c.getId().equals(client.getId())) {
                indexToDelete = i;
            }
        }

        // Si l'index est resté à -1, c'est qu'on n'a rien trouvé
        if (indexToDelete == -1) {
            Log.w(TAG, "Client à supprimer non trouvé : ID=" + client.getId());
            return false;
        }


        // Suppression effective du client
        clients.remove(indexToDelete);
        Log.d(TAG, "Client supprimé avec succès : ID=" + client.getId());

        // Sauvegarde de la liste mise à jour
        return saveClients(clients);
    }
}

