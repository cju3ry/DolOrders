package com.example.dolorders;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour sauvegarder les URLs Dolibarr utilisées.
 * Le fichier est automatiquement supprimé lors de la désinstallation.
 *
 * @author INFO
 * @version 1.0
 */
public class UrlManager {

    /** Nom du fichier de stockage des URLs */
    private static final String FILENAME = "dolibarr_urls.json";

    /** Tag pour les logs */
    private static final String TAG = "UrlManager";

    private Context context;

    /**
     * Constructeur
     * @param context Le contexte de l'application
     */
    public UrlManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Ajoute une URL dans le fichier si elle n'existe pas déjà.
     * L'URL est ajoutée en première position (la plus récente).
     *
     * @param url L'URL à sauvegarder
     * @return true si l'opération a réussi, false sinon
     */
    public boolean addUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            Log.w(TAG, "URL vide, non sauvegardée");
            return false;
        }

        try {
            JSONArray urls = loadUrls();

            // Vérifie si l'URL existe déjà et la supprime pour la remettre en premier
            for (int i = 0; i < urls.length(); i++) {
                if (urls.getString(i).equals(url)) {
                    urls.remove(i);
                    break;
                }
            }

            // Crée un nouveau tableau avec l'URL en premier
            JSONArray newUrls = new JSONArray();
            newUrls.put(url);

            // Ajoute les autres URLs (max 10)
            for (int i = 0; i < urls.length() && i < 9; i++) {
                newUrls.put(urls.getString(i));
            }

            boolean success = saveUrls(newUrls);
            if (success) {
                Log.d(TAG, "URL sauvegardée: " + url);
            }
            return success;

        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de l'ajout de l'URL", e);
            return false;
        }
    }

    /**
     * Récupère la liste de toutes les URLs enregistrées.
     *
     * @return Liste des URLs, ou liste vide si aucune URL
     */
    public List<String> getAllUrls() {
        List<String> urlList = new ArrayList<>();
        try {
            JSONArray urls = loadUrls();
            for (int i = 0; i < urls.length(); i++) {
                urlList.add(urls.getString(i));
            }
            Log.d(TAG, "Récupération de " + urlList.size() + " URL(s)");
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la récupération des URLs", e);
        }
        return urlList;
    }

    /**
     * Supprime une URL du fichier.
     *
     * @param url L'URL à supprimer
     * @return true si l'URL a été supprimée, false sinon
     */
    public boolean removeUrl(String url) {
        try {
            JSONArray urls = loadUrls();

            for (int i = 0; i < urls.length(); i++) {
                if (urls.getString(i).equals(url)) {
                    urls.remove(i);
                    boolean success = saveUrls(urls);
                    if (success) {
                        Log.d(TAG, "URL supprimée: " + url);
                    }
                    return success;
                }
            }

            Log.w(TAG, "URL non trouvée pour suppression: " + url);
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la suppression de l'URL", e);
            return false;
        }
    }

    /**
     * Supprime toutes les URLs enregistrées.
     *
     * @return true si l'opération a réussi
     */
    public boolean clearAllUrls() {
        boolean success = context.deleteFile(FILENAME);
        if (success) {
            Log.d(TAG, "Toutes les URLs ont été supprimées");
        }
        return success;
    }

    /**
     * Charge le tableau JSON des URLs depuis le fichier.
     *
     * @return Le tableau JSON, ou un nouveau tableau vide si le fichier n'existe pas
     */
    private JSONArray loadUrls() {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            reader.close();

            Log.d(TAG, "Fichier chargé: " + json.toString());
            return new JSONArray(json.toString());

        } catch (FileNotFoundException e) {
            Log.d(TAG, "Fichier inexistant, création d'un nouveau tableau");
            return new JSONArray();
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Erreur lors de la lecture du fichier", e);
            return new JSONArray();
        }
    }

    /**
     * Sauvegarde le tableau JSON dans le fichier.
     *
     * @param urls Le tableau JSON à sauvegarder
     * @return true si l'opération a réussi, false sinon
     */
    private boolean saveUrls(JSONArray urls) {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(urls.toString().getBytes());
            fos.close();

            Log.d(TAG, "Fichier sauvegardé: " + urls.toString());
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de l'écriture du fichier", e);
            return false;
        }
    }
}