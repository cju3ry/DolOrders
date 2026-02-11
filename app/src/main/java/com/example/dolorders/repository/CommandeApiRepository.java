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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository pour gérer l'envoi des commandes vers Dolibarr.
 * - POST /orders : Module natif Dolibarr (commandes avec lignes)
 * - POST /dolordersapi/fournisseurss : Module d'historique (ligne par ligne)
 * <p>
 * Flux d'envoi :
 * 1. Envoyer vers le module natif → récupérer l'ID de la commande Dolibarr
 * 2. Envoyer vers l'historique avec l'ID de la commande Dolibarr
 */
public class CommandeApiRepository {

    private static final String TAG = "CommandeApiRepository";

    private final Context context;
    private final RequestQueue requestQueue;
    private static final String JSON_APPLICATION = "application/json";

    private static final String UNKNOWN_LIBELLE = "Unknown";

    private static final String FICHIER_CRYPTE = "secure_prefs_crypto";

    /**
     * Interface de callback pour l'envoi d'une ligne de commande vers l'historique.
     */
    public interface CommandeEnvoiCallback {
        void onSuccess(String historiqueId);

        void onError(String message);
    }

    /**
     * Interface de callback pour l'envoi d'une commande vers le module natif Dolibarr.
     */
    public interface CommandeNativeEnvoiCallback {
        void onSuccess(String dolibarrCommandeId);

        void onError(String message);
    }

    public CommandeApiRepository(Context context) {
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Envoie une commande vers le module natif Dolibarr.
     * POST /orders
     * <p>
     * Structure JSON :
     * {
     * "socid": id_client,
     * "date": timestamp,
     * "type": 0,
     * "lines": [
     * {
     * "fk_product": id_produit,
     * "qty": quantite,
     * "subprice": prix_unitaire,
     * "tva_tx": 0,
     * "remise_percent": remise
     * }
     * ]
     * }
     *
     * @param commande Commande à envoyer
     * @param callback Callback pour notifier du résultat (retourne l'ID de la commande Dolibarr)
     */
    public void envoyerCommandeVersModuleNatif(Commande commande, CommandeNativeEnvoiCallback callback) {
        if (commande.getLignesCommande() == null || commande.getLignesCommande().isEmpty()) {
            callback.onError("La commande ne contient aucune ligne");
            return;
        }

        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        if (baseUrl == null || apiKey == null) {
            callback.onError("Configuration manquante");
            return;
        }

        String url = baseUrl.endsWith("/")
                ? baseUrl + "api/index.php/orders"
                : baseUrl + "/api/index.php/orders";

        try {
            final String jsonBodyString = creerJsonCommandeNative(commande).toString();

            Log.d(TAG, "Envoi commande vers module natif: " + jsonBodyString);

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        try {
                            Log.d(TAG, "Réponse module natif: " + response);

                            // Extraire l'ID de la commande Dolibarr
                            String dolibarrCommandeId;
                            response = response.trim();

                            if (response.startsWith("{")) {
                                // Réponse JSON
                                JSONObject jsonResponse = new JSONObject(response);
                                dolibarrCommandeId = jsonResponse.has("id") ? jsonResponse.getString("id") : response;
                            } else {
                                // Réponse simple (juste l'ID)
                                dolibarrCommandeId = response;
                            }

                            Log.d(TAG, "✅ Commande créée dans Dolibarr. ID: " + dolibarrCommandeId);
                            callback.onSuccess(dolibarrCommandeId);
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur parsing réponse module natif", e);
                            callback.onError("Erreur parsing: " + e.getMessage());
                        }
                    },
                    error -> {
                        String errorMsg = "Erreur envoi module natif";
                        if (error.networkResponse != null) {
                            errorMsg += " (Code: " + error.networkResponse.statusCode + ")";
                            if (error.networkResponse.data != null) {
                                String body = new String(error.networkResponse.data);
                                Log.e(TAG, "Réponse serveur module natif: " + body);
                                errorMsg += " - " + body;
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
                    headers.put("Content-Type", JSON_APPLICATION);
                    headers.put("Accept", JSON_APPLICATION);
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return jsonBodyString.getBytes();
                }
            };

            requestQueue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "Erreur création requête module natif", e);
            callback.onError("Erreur création requête: " + e.getMessage());
        }
    }

    /**
     * Crée le JSON pour envoyer une commande vers le module natif Dolibarr.
     * <p>
     * Structure JSON :
     * {
     * "socid": id_client,
     * "date": timestamp,
     * "type": 0,
     * "lines": [
     * {
     * "fk_product": id_produit,
     * "qty": quantite,
     * "subprice": prix_unitaire,
     * "tva_tx": 0,
     * "remise_percent": remise
     * }
     * ]
     * }
     */
    private JSONObject creerJsonCommandeNative(Commande commande) throws JSONException {
        JSONObject json = new JSONObject();

        // ID du client (socid)
        String idClient = commande.getClient() != null ? commande.getClient().getId() : null;
        if (idClient == null) {
            throw new IllegalArgumentException("Le client doit avoir un ID Dolibarr");
        }
        json.put("socid", Integer.parseInt(idClient));

        // TODO enlever ca car c'est pour tester
        //json.put("socid", 200000);
        // Date de la commande (timestamp Unix - secondes)
        long dateCommande = commande.getDateCommande() != null ?
                commande.getDateCommande().getTime() / 1000 :
                System.currentTimeMillis() / 1000;
        json.put("date", dateCommande);
        // Type de commande (0 par défaut)
        json.put("type", 0);

        // Lignes de commande
        JSONArray lines = new JSONArray();
        for (LigneCommande ligne : commande.getLignesCommande()) {
            JSONObject ligneJson = new JSONObject();

            // ID du produit
            ligneJson.put("fk_product", Integer.parseInt(ligne.getProduit().getId()));

            // TODO enlever ca car c'est pour tester
            // ligneJson.put("fk_product", 200000000);
            // Quantité
            ligneJson.put("qty", ligne.getQuantite());

            // Prix unitaire
            ligneJson.put("subprice", ligne.getProduit().getPrixUnitaire());

            // TVA (récupérée depuis le produit)
            ligneJson.put("tva_tx", ligne.getProduit().getTauxTva());

            // Remise
            ligneJson.put("remise_percent", ligne.getRemise());

            lines.put(ligneJson);
        }
        json.put("lines", lines);

        return json;
    }

    /**
     * Envoie une commande vers l'historique en utilisant l'ID Dolibarr.
     * Cette méthode doit être appelée APRÈS l'envoi vers le module natif.
     *
     * @param commande           Commande à envoyer
     * @param dolibarrCommandeId ID de la commande dans Dolibarr (récupéré lors de l'envoi natif)
     * @param callback           Callback pour notifier du résultat
     */
    public void envoyerCommandeVersHistoriqueAvecId(Commande commande, String dolibarrCommandeId, CommandeEnvoiCallback callback) {
        if (commande.getLignesCommande() == null || commande.getLignesCommande().isEmpty()) {
            callback.onError("La commande ne contient aucune ligne");
            return;
        }

        String username = getUsername();

        Log.d(TAG, "Début envoi commande vers historique avec ID Dolibarr: " + dolibarrCommandeId +
                " (" + commande.getLignesCommande().size() + " lignes)");

        // Envoyer chaque ligne de commande séparément avec l'ID Dolibarr
        envoyerLigneRecursiveAvecId(commande, dolibarrCommandeId, 0, username, callback);
    }

    /**
     * Envoie une commande vers l'historique SANS ID Dolibarr (en cas d'échec du module natif).
     * Utilisé quand l'envoi vers le module natif a échoué.
     * Toutes les lignes sont enregistrées avec update_date = "Non".
     *
     * @param commande Commande à enregistrer dans l'historique
     * @param callback Callback pour notifier du résultat
     */
    public void envoyerCommandeVersHistoriqueSansId(Commande commande, CommandeEnvoiCallback callback) {
        if (commande.getLignesCommande() == null || commande.getLignesCommande().isEmpty()) {
            callback.onError("La commande ne contient aucune ligne");
            return;
        }

        String username = getUsername();

        Log.d(TAG, "Envoi commande vers historique SANS ID Dolibarr (update_date=Non) - " +
                commande.getLignesCommande().size() + " ligne(s)");

        // Envoyer chaque ligne avec idcommande="0" et update_date="Non"
        envoyerLigneRecursiveSansId(commande, 0, username, callback);
    }

    /**
     * Envoie les lignes de commande vers l'historique avec l'ID Dolibarr.
     */
    private void envoyerLigneRecursiveAvecId(Commande commande, String dolibarrCommandeId, int index, String username, CommandeEnvoiCallback callback) {
        if (index >= commande.getLignesCommande().size()) {
            // Toutes les lignes ont été envoyées
            Log.d(TAG, "✅ Toutes les lignes de la commande envoyées vers l'historique");
            callback.onSuccess("all_lines_sent");
            return;
        }

        LigneCommande ligne = commande.getLignesCommande().get(index);

        Log.d(TAG, "Envoi ligne " + (index + 1) + "/" + commande.getLignesCommande().size() +
                " vers l'historique - Produit: " + ligne.getProduit().getLibelle());

        envoyerLigneVersHistoriqueAvecId(commande, ligne, dolibarrCommandeId, username, new CommandeEnvoiCallback() {
            @Override
            public void onSuccess(String historiqueId) {
                Log.d(TAG, "✅ Ligne " + (index + 1) + " envoyée vers l'historique. ID: " + historiqueId);
                // Envoyer la ligne suivante
                envoyerLigneRecursiveAvecId(commande, dolibarrCommandeId, index + 1, username, callback);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "❌ Erreur envoi ligne " + (index + 1) + " vers l'historique: " + message);
                // Continuer avec la ligne suivante même en cas d'erreur
                envoyerLigneRecursiveAvecId(commande, dolibarrCommandeId, index + 1, username, callback);
            }
        });
    }

    /**
     * Envoie les lignes de commande vers l'historique SANS ID Dolibarr (récursif).
     */
    private void envoyerLigneRecursiveSansId(Commande commande, int index, String username, CommandeEnvoiCallback callback) {
        if (index >= commande.getLignesCommande().size()) {
            // Toutes les lignes ont été envoyées
            Log.d(TAG, "✅ Toutes les lignes de la commande envoyées vers l'historique (update_date=Non)");
            callback.onSuccess("all_lines_sent_without_id");
            return;
        }

        LigneCommande ligne = commande.getLignesCommande().get(index);

        Log.d(TAG, "Envoi ligne " + (index + 1) + "/" + commande.getLignesCommande().size() +
                " vers l'historique (update_date=Non) - Produit: " + ligne.getProduit().getLibelle());

        envoyerLigneVersHistoriqueSansId(commande, ligne, username, new CommandeEnvoiCallback() {
            @Override
            public void onSuccess(String historiqueId) {
                Log.d(TAG, "✅ Ligne " + (index + 1) + " envoyée vers l'historique (update_date=Non). ID: " + historiqueId);
                // Envoyer la ligne suivante
                envoyerLigneRecursiveSansId(commande, index + 1, username, callback);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "❌ Erreur envoi ligne " + (index + 1) + " vers l'historique: " + message);
                // Continuer avec la ligne suivante même en cas d'erreur
                envoyerLigneRecursiveSansId(commande, index + 1, username, callback);
            }
        });
    }

    /**
     * Envoie une ligne de commande vers le module d'historique avec l'ID Dolibarr.
     * POST /dolordersapi/fournisseurss
     */
    private void envoyerLigneVersHistoriqueAvecId(Commande commande, LigneCommande ligne, String dolibarrCommandeId, String username, CommandeEnvoiCallback callback) {
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
            final String jsonBodyString = creerJsonLigneCommandeAvecId(commande, ligne, dolibarrCommandeId, username).toString();

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
                    headers.put("Content-Type", JSON_APPLICATION);
                    headers.put("Accept", JSON_APPLICATION);
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
     * Envoie une ligne de commande vers le module d'historique SANS ID Dolibarr (en cas d'échec).
     * POST /dolordersapi/fournisseurss avec idcommande=0 et update_date="Non"
     */
    private void envoyerLigneVersHistoriqueSansId(Commande commande, LigneCommande ligne, String username, CommandeEnvoiCallback callback) {
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
            final String jsonBodyString = creerJsonLigneCommandeSansId(commande, ligne, username).toString();

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        try {
                            Log.d(TAG, "Réponse historique commande (update_date=Non): " + response);

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
                        String errorMsg = "Erreur envoi historique commande (update_date=Non)";
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
                    headers.put("Content-Type", JSON_APPLICATION);
                    headers.put("Accept", JSON_APPLICATION);
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return jsonBodyString.getBytes();
                }
            };

            requestQueue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "Erreur création requête historique commande (update_date=Non)", e);
            callback.onError("Erreur création requête: " + e.getMessage());
        }
    }

    /**
     * Crée le JSON pour envoyer une ligne de commande vers le module d'historique avec l'ID Dolibarr.
     */
    private JSONObject creerJsonLigneCommandeAvecId(Commande commande, LigneCommande ligne, String dolibarrCommandeId, String username) throws JSONException {
        JSONObject json = new JSONObject();

        // ID du client (récupéré depuis le client de la commande)
        String idClient = commande.getClient() != null ? commande.getClient().getId() : "1";

        json.put("idclient", Integer.parseInt(idClient));

        // ID de la commande (ID Dolibarr retourné par le module natif)
        json.put("idcommande", Integer.parseInt(dolibarrCommandeId));

        // Nom du client (username)
        String nomClient = commande.getClient().getNom();
        json.put("nomclient", nomClient != null ? nomClient : UNKNOWN_LIBELLE);

        // Date de la commande (format JJ/MM/AAAA - date sélectionnée via DatePicker)
        String dateCommandeFormatee;
        if (commande.getDateCommande() != null) {
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE);
            dateCommandeFormatee = sdfDate.format(commande.getDateCommande());
        } else {
            dateCommandeFormatee = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(new java.util.Date());
        }
        json.put("datecommande", dateCommandeFormatee);

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
        json.put("creator_name", username != null ? username : UNKNOWN_LIBELLE);

        // Date de création (timestamp Unix - secondes) - MAINTENANT BASÉE SUR LA DATE DE CRÉATION DE LA LIGNE
        long dateCreation = ligne.getDateCreation() != null ?
                ligne.getDateCreation().getTime() / 1000 :
                System.currentTimeMillis() / 1000;
        json.put("creation_date", dateCreation);

        // Soumis par
        json.put("submitted_by_name", username != null ? username : UNKNOWN_LIBELLE);

        // Date de soumission (timestamp Unix - secondes - maintenant)
        long submissionDate = System.currentTimeMillis() / 1000;
        json.put("submission_date", submissionDate);

        // Update date (Oui car la commande a été créée dans le module natif)
        json.put("update_date", "Oui");

        Log.d(TAG, "JSON ligne commande avec ID Dolibarr créé: " + json);

        return json;
    }

    /**
     * Crée le JSON pour envoyer une ligne de commande vers l'historique SANS ID Dolibarr.
     * Utilisé en cas d'échec de l'envoi vers le module natif.
     * idcommande = 0, update_date = "Non"
     */
    private JSONObject creerJsonLigneCommandeSansId(Commande commande, LigneCommande ligne, String username) throws JSONException {
        JSONObject json = new JSONObject();

        // ID du client (récupéré depuis le client de la commande)
        String idClient = commande.getClient() != null ? commande.getClient().getId() : "1";
        json.put("idclient", Integer.parseInt(idClient));

        // ID de la commande = 0 (pas encore créée dans Dolibarr)
        json.put("idcommande", 0);

        // Nom du client
        String nomClient = commande.getClient().getNom();
        json.put("nomclient", nomClient != null ? nomClient : UNKNOWN_LIBELLE);

        // Date de la commande (format JJ/MM/AAAA)
        String dateCommandeFormatee;
        if (commande.getDateCommande() != null) {
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE);
            dateCommandeFormatee = sdfDate.format(commande.getDateCommande());
        } else {
            dateCommandeFormatee = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(new java.util.Date());
        }
        json.put("datecommande", dateCommandeFormatee);

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
        json.put("creator_name", username != null ? username : UNKNOWN_LIBELLE);

        // Date de création (timestamp Unix - secondes) - basée sur la date de création de la ligne
        long dateCreation = ligne.getDateCreation() != null ?
                ligne.getDateCreation().getTime() / 1000 :
                System.currentTimeMillis() / 1000;
        json.put("creation_date", dateCreation);

        // Soumis par
        json.put("submitted_by_name", username != null ? username : UNKNOWN_LIBELLE);

        // Date de soumission (timestamp Unix - secondes - maintenant)
        long submissionDate = System.currentTimeMillis() / 1000;
        json.put("submission_date", submissionDate);

        // ✅ Update date = "Non" car la commande n'a pas été créée dans le module natif
        json.put("update_date", "Non");

        Log.d(TAG, "JSON ligne commande SANS ID Dolibarr créé (update_date=Non): " + json);

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
                    FICHIER_CRYPTE,
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
                    FICHIER_CRYPTE,
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
                    FICHIER_CRYPTE,
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
