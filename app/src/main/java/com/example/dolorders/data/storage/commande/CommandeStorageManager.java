package com.example.dolorders.data.storage.commande;

import android.content.Context;
import android.util.Log;

import com.example.dolorders.Commande;
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

/**
 * Gestionnaire de stockage des commandes dans un fichier JSON local.
 * Le fichier est stocké dans le répertoire interne de l'application
 * et sera automatiquement supprimé si l'application est désinstallée.
 */
public class CommandeStorageManager {

    private static final String FILE_NAME = "commandes_data.json";
    private static final String TAG = "CommandeStorage";

    private final Context context;
    private final Gson gson;

    public CommandeStorageManager(Context context) {
        this.context = context;
        // Configuration de Gson avec l'adaptateur personnalisé pour Commande
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Commande.class, new CommandeTypeAdapter())
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
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write(jsonData);
            writer.close();
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
            FileInputStream fis = context.openFileInput(getFileName());
            InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            bufferedReader.close();
            reader.close();
            fis.close();

            // Conversion du JSON en liste d'objets Commande
            String jsonData = jsonBuilder.toString();
            Type listType = new TypeToken<List<Commande>>(){}.getType();
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

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d(TAG, "Fichier de commandes supprimé avec succès");
            } else {
                Log.w(TAG, "Échec de la suppression du fichier de commandes");
            }
            return deleted;
        }

        Log.d(TAG, "Aucun fichier de commandes à supprimer");
        return true;
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
     * Recherche une commande par son ID.
     *
     * @param commandeId ID de la commande recherchée
     * @return La commande trouvée, ou null si non trouvée
     */
    //TODO A DEPLACER DANS LE FUTUR SERVICE POUR FILTRER ET AUTRES
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
    //TODO A DEPLACER DANS LE FUTUR SERVICE POUR FILTRER ET AUTRES
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
}

