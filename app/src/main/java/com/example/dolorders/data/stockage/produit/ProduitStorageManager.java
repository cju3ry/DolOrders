package com.example.dolorders.data.stockage.produit;

import android.content.Context;
import android.util.Log;

import com.example.dolorders.objet.Produit;
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
 * Gestionnaire de stockage local pour les produits.
 * Utilise un fichier JSON dans le répertoire interne de l'application
 * pour sauvegarder/charger les produits de manière persistante.
 * Le fichier sera automatiquement supprimé si l'application est désinstallée.
 */
public class ProduitStorageManager {

    private static final String FILE_NAME = "produits_data.json";
    private static final String TAG = "ProduitStorage";

    private final Context context;
    private final Gson gson;

    /**
     * Constructeur.
     *
     * @param context Contexte Android nécessaire pour accéder au système de fichiers
     */
    public ProduitStorageManager(Context context) {
        this.context = context.getApplicationContext();

        // Configuration Gson avec l'adapter personnalisé
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Produit.class, new ProduitTypeAdapter());
        builder.setPrettyPrinting(); // Pour un JSON lisible
        this.gson = builder.create();
    }

    /**
     * Sauvegarde la liste des produits dans un fichier JSON.
     * Cette opération écrase les données précédentes.
     *
     * @param produits Liste des produits à sauvegarder
     * @return true si la sauvegarde a réussi, false sinon
     */
    public boolean saveProduits(List<Produit> produits) {
        if (produits == null) {
            produits = new ArrayList<>();
        }

        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            String json = gson.toJson(produits);
            writer.write(json);

            writer.close();
            fos.close();

            Log.d(TAG, "Produits sauvegardés avec succès (" + produits.size() + " produits)");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde des produits", e);
            return false;
        }
    }

    /**
     * Charge la liste des produits depuis le fichier JSON.
     *
     * @return Liste des produits sauvegardés, ou une liste vide si aucune donnée
     */
    public List<Produit> loadProduits() {
        File file = new File(context.getFilesDir(), FILE_NAME);

        if (!file.exists()) {
            Log.d(TAG, "Aucun fichier de produits trouvé");
            return new ArrayList<>();
        }

        try {
            FileInputStream fis = new FileInputStream(file);
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

            // Conversion du JSON en liste d'objets Produit
            String jsonData = jsonBuilder.toString();
            Type listType = new TypeToken<List<Produit>>() {}.getType();
            List<Produit> produits = gson.fromJson(jsonData, listType);

            if (produits == null) {
                produits = new ArrayList<>();
            }

            Log.d(TAG, "Produits chargés avec succès (" + produits.size() + " produits)");
            return produits;

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des produits", e);
            return new ArrayList<>();
        }
    }

    /**
     * Supprime tous les produits du stockage.
     *
     * @return true si la suppression a réussi, false sinon
     */
    public boolean clearProduits() {
        File file = new File(context.getFilesDir(), FILE_NAME);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d(TAG, "Fichier de produits supprimé avec succès");
            } else {
                Log.w(TAG, "Échec de la suppression du fichier de produits");
            }
            return deleted;
        }

        Log.d(TAG, "Aucun fichier de produits à supprimer");
        return true;
    }

    /**
     * Vérifie si des produits sont présents dans le stockage.
     *
     * @return true si des produits existent, false sinon
     */
    public boolean hasProduits() {
        File file = new File(context.getFilesDir(), FILE_NAME);
        return file.exists() && file.length() > 0;
    }

    /**
     * Retourne le chemin du fichier de stockage des produits.
     * Utile pour le debug.
     *
     * @return Chemin absolu du fichier
     */
    public String getFilePath() {
        return new File(context.getFilesDir(), FILE_NAME).getAbsolutePath();
    }
}


