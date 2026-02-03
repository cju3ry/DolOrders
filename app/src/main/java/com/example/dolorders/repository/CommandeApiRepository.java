package com.example.dolorders.repository;

import android.content.Context;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dolorders.objet.Commande;
import com.example.dolorders.objet.LigneCommande;

import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository pour gérer l'envoi des commandes vers le module d'historique Dolibarr.
 * POST /dolordersapi/fournisseurss
 *
 * IMPORTANT : Chaque ligne de commande est envoyée séparément.
 * Exemple : Une commande avec 3 lignes → 3 appels API distincts
 */
public class CommandeApiRepository {

    private static final String TAG = "CommandeApiRepository";

    private final Context context;
    private final RequestQueue requestQueue;

    /**
     * Interface de callback pour l'envoi d'une ligne de commande vers l'historique.
     */
    public interface CommandeEnvoiCallback {
        void onSuccess(String historiqueId);
        void onError(String message);
    }

    public CommandeApiRepository(Context context) {
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Envoie une commande vers le module d'historique.
     * Chaque ligne de commande est envoyée séparément.
     *
     * @param commande Commande à envoyer
     * @param callback Callback pour notifier du résultat final
     */
    public void envoyerCommandeVersHistorique(Commande commande, CommandeEnvoiCallback callback) {
        if (commande.getLignesCommande() == null || commande.getLignesCommande().isEmpty()) {
            callback.onError("La commande ne contient aucune ligne");
            return;
        }

        String username = getUsername();

        Log.d(TAG, "Début envoi commande " + commande.getId() + " (" +
                   commande.getLignesCommande().size() + " lignes)");

        // Envoyer chaque ligne de commande séparément
        envoyerLigneRecursive(commande, 0, username, callback);
    }

    /**
     * Envoie les lignes de commande une par une de manière récursive.
     */
    private void envoyerLigneRecursive(Commande commande, int index, String username, CommandeEnvoiCallback callback) {
        if (index >= commande.getLignesCommande().size()) {
            // Toutes les lignes ont été envoyées
            Log.d(TAG, "✅ Toutes les lignes de la commande " + commande.getId() + " envoyées");
            callback.onSuccess("all_lines_sent");
            return;
        }

        LigneCommande ligne = commande.getLignesCommande().get(index);

        Log.d(TAG, "Envoi ligne " + (index + 1) + "/" + commande.getLignesCommande().size() +
                   " de la commande " + commande.getId() + " - Produit: " + ligne.getProduit().getLibelle());

        envoyerLigneVersHistorique(commande, ligne, username, new CommandeEnvoiCallback() {
            @Override
            public void onSuccess(String historiqueId) {
                Log.d(TAG, "✅ Ligne " + (index + 1) + " envoyée. ID: " + historiqueId);
                // Envoyer la ligne suivante
                envoyerLigneRecursive(commande, index + 1, username, callback);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "❌ Erreur envoi ligne " + (index + 1) + ": " + message);
                // Continuer avec la ligne suivante même en cas d'erreur
                envoyerLigneRecursive(commande, index + 1, username, callback);
            }
        });
    }

    /**
     * Envoie une ligne de commande vers le module d'historique.
     * POST /dolordersapi/fournisseurss
     */
    private void envoyerLigneVersHistorique(Commande commande, LigneCommande ligne, String username, CommandeEnvoiCallback callback) {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        if (baseUrl == null || apiKey == null) {
            callback.onError("Configuration manquante");
            return;
        }

        String url = baseUrl.endsWith("/")
                ? baseUrl + "api/index.php/dolordersapi/fournisseurss"
                : baseUrl + "/api/index.php/dolordersapi/fournisseurss";

        try {
            final String jsonBodyString = creerJsonLigneCommande(commande, ligne, username).toString();

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        try {
                            Log.d(TAG, "Réponse historique commande: " + response);

                            String historiqueId;
                            response = response.trim();

                            if (response.startsWith("{")) {
                                JSONObject jsonResponse = new JSONObject(response);
                                historiqueId = jsonResponse.has("id") ? jsonResponse.getString("id") : "success";
                            } else {
                                historiqueId = response;
                            }

                            callback.onSuccess(historiqueId);
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur parsing réponse historique commande", e);
                            callback.onError("Erreur parsing: " + e.getMessage());
                        }
                    },
                    error -> {
                        String errorMsg = "Erreur envoi historique commande";
                        if (error.networkResponse != null) {
                            errorMsg += " (Code: " + error.networkResponse.statusCode + ")";
                            if (error.networkResponse.data != null) {
                                String body = new String(error.networkResponse.data);
                                Log.e(TAG, "Réponse serveur historique commande: " + body);
                            }
                        }
                        Log.e(TAG, errorMsg, error);
                        callback.onError(errorMsg);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("DOLAPIKEY", apiKey);
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return jsonBodyString.getBytes();
                }
            };

            requestQueue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "Erreur création requête historique commande", e);
            callback.onError("Erreur création requête: " + e.getMessage());
        }
    }

    /**
     * Crée le JSON pour envoyer une ligne de commande vers le module d'historique.
     *
     * Structure JSON :
     * {
     *   "idclient": id,
     *   "idcommande": id,
     *   "nomclient": "username",
     *   "datecommande": date,
     *   "codearticle": "code",
     *   "label": "Nom du produit",
     *   "qte": int,
     *   "price": int,
     *   "remise": int,
     *   "creator_name": "username",
     *   "creation_date": date,
     *   "submitted_by_name": "username",
     *   "submission_date": date,
     *   "update_date": "Oui ou non"
     * }
     */
    private JSONObject creerJsonLigneCommande(Commande commande, LigneCommande ligne, String username) throws Exception {
        JSONObject json = new JSONObject();

        // ID du client (récupéré depuis le client de la commande)
        String idClient = commande.getClient() != null ? commande.getClient().getId() : "1";

        json.put("idclient", Integer.parseInt(idClient));

        // ID de la commande (pour le moment on met 1)
        json.put("idcommande", 1);

        // Nom du client (username)
        String nomClient= commande.getClient().getNom();
        json.put("nomclient", nomClient != null ? nomClient : "Unknown");

        // Date de la commande (timestamp Unix - secondes)
        long dateCommande = commande.getDateCommande() != null ?
                commande.getDateCommande().getTime() / 1000 :
                System.currentTimeMillis() / 1000;
        json.put("datecommande", dateCommande);

        // Code article (ID du produit)
        json.put("codearticle", ligne.getProduit().getId());

        // Label (Libellé du produit)
        json.put("label", ligne.getProduit().getLibelle());

        // Quantité
        json.put("qte", ligne.getQuantite());

        // Prix unitaire
        json.put("price", ligne.getProduit().getPrixUnitaire());

        // Remise
        json.put("remise", ligne.getRemise());

        // Créateur
        json.put("creator_name", username != null ? username : "Unknown");

        // Date de création (date de la commande)
        json.put("creation_date", dateCommande);

        // Soumis par
        json.put("submitted_by_name", username != null ? username : "Unknown");

        // Date de soumission (maintenant)
        long submissionDate = System.currentTimeMillis() / 1000;
        json.put("submission_date", submissionDate);

        // Update date (Non par défaut)
        json.put("update_date", "Non");

        Log.d(TAG, "JSON ligne commande créé: " + json);

        return json;
    }

    /**
     * Récupère l'URL de base depuis les SharedPreferences cryptées.
     */
    private String getBaseUrl() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            android.content.SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs_crypto",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            return securePrefs.getString("base_url", null);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur lors de la récupération de l'URL", e);
            return null;
        }
    }

    /**
     * Récupère la clé API depuis les SharedPreferences cryptées.
     */
    private String getApiKey() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            android.content.SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs_crypto",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            return securePrefs.getString("api_key", null);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur lors de la récupération de la clé API", e);
            return null;
        }
    }

    /**
     * Récupère le nom d'utilisateur depuis les SharedPreferences cryptées.
     */
    private String getUsername() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            android.content.SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs_crypto",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            return securePrefs.getString("username", null);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Erreur lors de la récupération du username", e);
            return null;
        }
    }
}

