package com.example.dolorders.data.stockage.commande;

import android.content.Context;
import android.util.Log;

import com.example.dolorders.objet.Client;
import com.example.dolorders.objet.Commande;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de stockage des commandes dans un fichier JSON local.
 * Le fichier est stocké dans le répertoire interne de l'application
 * et sera automatiquement supprimé si l'application est désinstallée.
 */
public class GestionnaireStockageCommande {

    /** Nom du fichier de stockage pour les commandes */
    private static final String FILE_NAME = "commandes_data.json";

    /** Tag de log pour le gestionnaire de stockage des commandes */
    private static final String TAG = "CommandeStorage";

    /** Contexte de l'application pour accéder au système de fichiers internes */
    private final Context context;

    /** Instance de Gson configurée avec l'adaptateur personnalisé pour Commande */
    private final Gson gson;

    /**
     * Constructeur du gestionnaire de stockage des commandes.
     * @param context Contexte de l'application
     */
    public GestionnaireStockageCommande(Context context) {
        this.context = context;
        // Configuration de Gson avec l'adaptateur personnalisé pour Commande
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Commande.class, new AdapteurStockageCommande())
                .create();
    }

    /**
     * Sauvegarde la liste des commandes dans un fichier JSON.
     * Cette opération écrase les données précédentes.
     *
     * @param commandes Liste des commandes à sauvegarder
     * @return true si la sauvegarde a réussi, false sinon
     */
    public boolean saveCommandes(List<Commande> commandes) {
        if (commandes == null) {
            Log.w(TAG, "Tentative de sauvegarde d'une liste null");
            return false;
        }

        try {
            // Conversion de la liste en JSON
            String jsonData = gson.toJson(commandes);

            // Écriture dans le fichier interne
            FileOutputStream fos = context.openFileOutput(getFileName(), Context.MODE_PRIVATE);
            try (OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                writer.write(jsonData);
            }
            fos.close();

            Log.d(TAG, "Commandes sauvegardées avec succès (" + commandes.size() + " commandes) dans : " + getFileName());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde des commandes", e);
            return false;
        }
    }

    /**
     * Charge la liste des commandes depuis le fichier JSON.
     *
     * @return Liste des commandes sauvegardées, ou une liste vide si aucune donnée
     */
    public List<Commande> loadCommandes() {
        File file = new File(context.getFilesDir(), getFileName());

        // Si le fichier n'existe pas, retourner une liste vide
        if (!file.exists()) {
            Log.d(TAG, "Aucun fichier de commandes trouvé : " + getFileName());
            return new ArrayList<>();
        }

        try {
            // Lecture du fichier
            StringBuilder jsonBuilder;
            try (FileInputStream fis = context.openFileInput(getFileName())) {
                InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader);

                jsonBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonBuilder.append(line);
                }

                bufferedReader.close();
                reader.close();
            }

            // Conversion du JSON en liste d'objets Commande
            String jsonData = jsonBuilder.toString();
            Type listType = new TypeToken<List<Commande>>() {
            }.getType();
            List<Commande> commandes = gson.fromJson(jsonData, listType);

            if (commandes == null) {
                commandes = new ArrayList<>();
            }

            Log.d(TAG, "Commandes chargées avec succès (" + commandes.size() + " commandes)");
            return commandes;

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des commandes", e);
            return new ArrayList<>();
        }
    }

    /**
     * Vérifie si des données commandes existent dans le stockage.
     *
     * @return true si le fichier existe et contient des données
     */
    public boolean hasStoredCommandes() {
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
        return FILE_NAME;
    }

    /**
     * Supprime toutes les données commandes stockées.
     *
     * @return true si la suppression a réussi, false sinon
     */
    public boolean clearCommandes() {
        File file = new File(context.getFilesDir(), getFileName());
        Path path = file.toPath();

        try {
            Files.deleteIfExists(path);
            Log.d(TAG, "Fichier de commandes supprimé avec succès");
            return true;
        } catch (IOException e) {
            Log.w(TAG, "Échec de la suppression du fichier de commandes");
            return false;
        }
    }

    /**
     * Ajoute une commande à la liste existante et sauvegarde.
     *
     * @param commande Commande à ajouter
     * @return true si l'ajout et la sauvegarde ont réussi
     */
    public boolean addCommande(Commande commande) {
        if (commande == null) {
            Log.w(TAG, "Tentative d'ajout d'une commande null");
            return false;
        }

        List<Commande> commandes = loadCommandes();
        commandes.add(commande);
        return saveCommandes(commandes);
    }

    /**
     * Supprime une commande par son ID.
     *
     * @param commandeId ID de la commande à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteCommande(String commandeId) {
        if (commandeId == null || commandeId.isEmpty()) {
            Log.w(TAG, "Tentative de suppression avec un ID null ou vide");
            return false;
        }

        List<Commande> commandes = loadCommandes();
        boolean removed = false;

        for (int i = 0; i < commandes.size(); i++) {
            if (commandeId.equals(commandes.get(i).getId())) {
                commandes.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            return saveCommandes(commandes);
        } else {
            Log.w(TAG, "Aucune commande trouvée avec l'ID : " + commandeId);
            return false;
        }
    }

    /**
     * Supprime toutes les commandes associées à un client
     *
     * @param clientId ID du client dont les commandes doivent être supprimées
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteCommandesByClient(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            Log.w(TAG, "Tentative de suppression avec un ID client null ou vide");
            return false;
        }

        List<Commande> commandes = loadCommandes();
        boolean modified = false;

        for (int i = commandes.size() - 1; i >= 0; i--) {
            if (commandes.get(i).getClient() != null &&
                    clientId.equals(commandes.get(i).getClient().getId())) {
                commandes.remove(i);
                modified = true;
            }
        }

        if (modified) {
            Log.d(TAG, "Commandes du client " + clientId + " supprimées");
            return saveCommandes(commandes);
        } else {
            Log.d(TAG, "Aucune commande trouvée pour le client : " + clientId);
            return true;
        }
    }

    /**
     * Recherche une commande par son ID.
     *
     * @param commandeId ID de la commande recherchée
     * @return La commande trouvée, ou null si non trouvée
     */
    public Commande findCommandeById(String commandeId) {
        if (commandeId == null || commandeId.isEmpty()) {
            return null;
        }

        List<Commande> commandes = loadCommandes();
        for (Commande commande : commandes) {
            if (commandeId.equals(commande.getId())) {
                return commande;
            }
        }

        return null;
    }

    /**
     * Modifie une commande existante dans la liste.
     * La commande est identifiée par son ID.
     *
     * @param updatedCommande La commande avec les nouvelles données.
     * @return true si la commande a été trouvée et la liste sauvegardée, false sinon.
     */
    public boolean modifierCommande(Commande updatedCommande) {
        if (updatedCommande == null || updatedCommande.getId() == null) {
            Log.w(TAG, "Tentative de modification avec une commande ou un ID null");
            return false;
        }

        List<Commande> commandes = loadCommandes();
        boolean found = false;

        // On parcourt la liste pour trouver la commande et la remplacer
        for (int i = 0; i < commandes.size(); i++) {
            if (updatedCommande.getId().equals(commandes.get(i).getId())) {
                commandes.set(i, updatedCommande);
                found = true;
                break; // On a trouvé, on peut arrêter la boucle
            }
        }

        if (found) {
            // Si on a trouvé la commande, on sauvegarde la liste complète mise à jour
            return saveCommandes(commandes);
        } else {
            Log.w(TAG, "Aucune commande à modifier trouvée avec l'ID : " + updatedCommande.getId());
            return false; // La commande n'a pas été trouvée, on ne sauvegarde rien
        }
    }

    /**
     * Obtient le nombre de commandes stockées.
     *
     * @return Nombre de commandes
     */
    public int getCommandeCount() {
        return loadCommandes().size();
    }

    /**
     * Récupère toutes les commandes d'un client spécifique.
     *
     * @param clientId ID du client
     * @return Liste des commandes du client
     */
    public List<Commande> getCommandesByClient(String clientId) {
        List<Commande> result = new ArrayList<>();

        if (clientId == null || clientId.isEmpty()) {
            return result;
        }

        List<Commande> commandes = loadCommandes();
        for (Commande commande : commandes) {
            if (commande.getClient() != null && clientId.equals(commande.getClient().getId())) {
                result.add(commande);
            }
        }

        return result;
    }

    /**
     * Met à jour le client dans toutes ses commandes associées.
     * Parcourt toutes les commandes, identifie celles qui contiennent le client
     * et reconstruit ces commandes avec les nouvelles informations du client.
     *
     * @param updatedClient Le client avec les nouvelles informations
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateClientInCommandes(Client updatedClient) {
        if (updatedClient == null || updatedClient.getId() == null) {
            Log.w(TAG, "Tentative de mise à jour avec un client ou un ID null");
            return false;
        }

        List<Commande> commandes = loadCommandes();
        boolean modified = false;

        // Parcourt toutes les commandes
        for (int i = 0; i < commandes.size(); i++) {
            Commande commande = commandes.get(i);

            // Si la commande contient le client à mettre à jour
            if (commande.getClient() != null &&
                    updatedClient.getId().equals(commande.getClient().getId())) {

                // Reconstruire la commande avec le client mis à jour
                Commande updatedCommande = new Commande.Builder()
                        .setId(commande.getId())
                        .setClient(updatedClient)
                        .setDateCommande(commande.getDateCommande())
                        .setLignesCommande(commande.getLignesCommande())
                        .setUtilisateur(commande.getUtilisateur())
                        .build();

                commandes.set(i, updatedCommande);
                modified = true;

                Log.d(TAG, "Client mis à jour dans la commande : " + commande.getId());
            }
        }

        if (modified) {
            boolean saved = saveCommandes(commandes);
            Log.d(TAG, "Mise à jour du client dans les commandes : " +
                    (saved ? "réussie" : "échec de sauvegarde"));
            return saved;
        } else {
            Log.d(TAG, "Aucune commande trouvée pour le client : " + updatedClient.getId());
            return true;
        }
    }
}
