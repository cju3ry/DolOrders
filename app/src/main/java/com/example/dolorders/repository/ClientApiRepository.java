package com.example.dolorders.repository;

import android.content.Context;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dolorders.data.dto.ClientApiReponseDto;
import com.example.dolorders.mapper.ClientApiMapper;
import com.example.dolorders.objet.Client;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repository pour gérer la récupération et l'envoi des clients vers l'API Dolibarr.
 * Ce repository se concentre uniquement sur l'appel API.
 * Le stockage local est géré séparément via GestionnaireStockageClient avec le fichier API_CLIENTS_FILE.
 */
public class ClientApiRepository {

    private static final String TAG = "ClientApiRepository";
    private static final String FICHIER_CRYPTE = "secure_prefs_crypto";
    private static final String CODE_ERREUR = " (Code: ";
    private static final String LIBELLE_ACCEPT = "Accept";
    private static final String JSON_APPLICATION = "application/json";
    private static final String APIKEY = "DOLAPIKEY";

    private final Context context;
    private final RequestQueue requestQueue;
    private final Gson gson;

    /**
     * Interface de callback pour les opérations asynchrones (GET).
     */
    public interface ClientCallback {
        void onSuccess(List<Client> clients);

        void onError(String message);
    }

    /**
     * Interface de callback pour l'envoi d'un client vers Dolibarr (POST).
     */
    public interface ClientEnvoiCallback {
        void onSuccess(String dolibarrId);

        void onError(String message);
    }

    /**
     * Interface de callback pour récupérer l'ID utilisateur.
     */
    public interface UserIdCallback {
        void onSuccess(String userId);

        void onError(String message);
    }

    public ClientApiRepository(Context context) {
        this.context = context.getApplicationContext();
        this.requestQueue = Volley.newRequestQueue(context);
        this.gson = new Gson();
    }

    /**
     * Synchronise les clients avec l'API Dolibarr.
     * Cette méthode appelle uniquement l'API et retourne les clients via callback.
     * La sauvegarde dans le cache est gérée par le ViewModel ou le fragment appelant.
     *
     * @param callback Callback pour notifier du résultat
     */
    public void synchroniserDepuisApi(ClientCallback callback) {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        if (baseUrl == null || apiKey == null) {
            Log.e(TAG, "URL ou clé API manquante");
            callback.onError("Configuration manquante (URL ou clé API)");
            return;
        }

        // Construction de l'URL de l'API pour les thirdparties (clients)
        // GET /thirdparties?sortfield=t.rowid&sortorder=ASC&limit=100&properties=id,name,phone,email,address,zip,town
        String url = baseUrl.endsWith("/")
                ? baseUrl + "api/index.php/thirdparties?sortfield=t.rowid&sortorder=ASC&limit=100&properties=id%2Cname%2Cphone%2Cemail%2Caddress%2Czip%2Ctown"
                : baseUrl + "/api/index.php/thirdparties?sortfield=t.rowid&sortorder=ASC&limit=100&properties=id%2Cname%2Cphone%2Cemail%2Caddress%2Czip%2Ctown";

        Log.d(TAG, "Récupération des clients depuis l'API : " + url);

        // Requête Volley
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<Client> clients = parseJsonResponse(response);

                        Log.d(TAG, "Clients récupérés depuis l'API : " + clients.size());
                        callback.onSuccess(clients);

                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du parsing JSON", e);
                        callback.onError("Erreur de traitement des données : " + e.getMessage());
                    }
                },
                error -> {
                    String errorMessage = "Erreur API";
                    if (error.networkResponse != null) {
                        errorMessage += CODE_ERREUR + error.networkResponse.statusCode + ")";
                    }
                    if (error.getMessage() != null) {
                        errorMessage += " - " + error.getMessage();
                    }

                    Log.e(TAG, "Erreur lors de la requête API", error);
                    callback.onError(errorMessage);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put(APIKEY, apiKey);
                headers.put(LIBELLE_ACCEPT, JSON_APPLICATION);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Parse la réponse JSON de l'API en liste de Clients.
     */
    private List<Client> parseJsonResponse(JSONArray jsonArray) throws JSONException {
        List<Client> clients = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            // Conversion JSON → DTO
            ClientApiReponseDto dto = gson.fromJson(jsonObject.toString(), ClientApiReponseDto.class);

            // Conversion DTO → Modèle métier
            Client client = ClientApiMapper.fromDto(dto);
            if (client != null) {
                clients.add(client);
            }
        }

        return clients;
    }

    /**
     * Envoie un client local vers Dolibarr.
     * POST /thirdparties
     *
     * @param client   Client local à envoyer (sans ID Dolibarr)
     * @param callback Callback pour notifier du résultat
     */
    public void envoyerClient(Client client, ClientEnvoiCallback callback) {
        // D'abord récupérer l'ID utilisateur
        recupererIdUtilisateur(new UserIdCallback() {
            @Override
            public void onSuccess(String userId) {
                // Une fois l'ID récupéré, envoyer le client
                envoyerClientAvecUserId(client, userId, callback);
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Impossible de récupérer l'ID utilisateur: " + message);
                // Utiliser un ID par défaut (-1) si échec
                envoyerClientAvecUserId(client, "-1", callback);
            }
        });
    }

    /**
     * Envoie un client vers Dolibarr avec l'ID utilisateur fourni.
     */
    private void envoyerClientAvecUserId(Client client, String userId, ClientEnvoiCallback callback) {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        if (baseUrl == null || apiKey == null) {
            Log.e(TAG, "URL ou clé API manquante pour envoi client");
            callback.onError("Configuration manquante (URL ou clé API)");
            return;
        }

        String url = baseUrl.endsWith("/")
                ? baseUrl + "api/index.php/thirdparties"
                : baseUrl + "/api/index.php/thirdparties";

        Log.d(TAG, "Envoi du client vers Dolibarr : " + client.getNom() + " (userId: " + userId + ")");

        try {
            final String jsonBodyString = creerJsonClient(client, userId).toString();
            final String username = getUsername(); // Récupérer le username pour l'historique

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        try {
                            // La réponse de Dolibarr peut être juste un nombre (ex: "4") ou un objet JSON
                            Log.d(TAG, "Réponse brute de l'API: " + response);

                            String dolibarrId;
                            response = response.trim();

                            // Si la réponse commence par {, c'est un objet JSON
                            if (response.startsWith("{")) {
                                JSONObject jsonResponse = new JSONObject(response);
                                dolibarrId = jsonResponse.getString("id");
                            } else {
                                // Sinon c'est juste l'ID (nombre)
                                dolibarrId = response;
                            }

                            Log.d(TAG, "✅ Client envoyé avec succès. ID Dolibarr: " + dolibarrId);

                            // Maintenant envoyer vers le module d'historique
                            envoyerVersHistorique(client, dolibarrId, username, new ClientEnvoiCallback() {
                                @Override
                                public void onSuccess(String historiqueId) {
                                    Log.d(TAG, "✅ Client enregistré dans l'historique. ID: " + historiqueId);
                                    // Retourner le succès avec l'ID Dolibarr original
                                    callback.onSuccess(dolibarrId);
                                }

                                @Override
                                public void onError(String message) {
                                    Log.w(TAG, "⚠️ Erreur enregistrement historique: " + message);
                                    // Même si l'historique échoue, on considère la création du client comme réussie
                                    callback.onSuccess(dolibarrId);
                                }
                            });

                        } catch (Exception e) {
                            Log.e(TAG, "Erreur parsing réponse Dolibarr", e);
                            callback.onError("Erreur parsing réponse: " + e.getMessage());
                        }
                    },
                    error -> {
                        String errorMsg = "Erreur envoi client";
                        if (error.networkResponse != null) {
                            errorMsg += CODE_ERREUR + error.networkResponse.statusCode + ")";
                            if (error.networkResponse.data != null) {
                                String body = new String(error.networkResponse.data);
                                Log.e(TAG, "Réponse serveur: " + body);
                            }
                        }
                        Log.e(TAG, errorMsg, error);
                        callback.onError(errorMsg);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put(APIKEY, apiKey);
                    headers.put("Content-Type", JSON_APPLICATION);
                    headers.put(LIBELLE_ACCEPT, JSON_APPLICATION);
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return jsonBodyString.getBytes();
                }
            };

            requestQueue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "Erreur création requête POST", e);
            callback.onError("Erreur création requête: " + e.getMessage());
        }
    }

    /**
     * Crée le JSON pour envoyer un client vers Dolibarr.
     * NE PAS INCLURE l'ID local - Dolibarr génère son propre ID.
     */
    private JSONObject creerJsonClient(Client client, String userId) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("name", client.getNom());
        json.put("address", client.getAdresse() != null ? client.getAdresse() : "");
        json.put("zip", client.getCodePostal() != null ? client.getCodePostal() : "");
        json.put("town", client.getVille() != null ? client.getVille() : "");
        json.put("email", client.getAdresseMail());
        json.put("phone", client.getTelephone());

        // Convertir la date de création (Date → format dd/MM/yyyy)
        if (client.getDateSaisie() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            json.put("creation_date", sdf.format(client.getDateSaisie()));
        }

        // Valeurs fixes pour tous les clients
        json.put("client", "1");       // 1 = client
        json.put("prospect", "0");     // 0 = pas un prospect
        json.put("fournisseur", "0");  // 0 = pas un fournisseur
        json.put("commercial_id", userId); // ID de l'utilisateur connecté
        json.put("code_client", "auto"); // Auto-génération du code client par Dolibarr

        Log.d(TAG, "JSON créé pour client: " + json);
        Log.d(TAG, "ID utilisateur utilisé: " + userId);

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

    /**
     * Envoie les informations du client vers le module d'historique custom.
     * POST /dolcustomersapi/clients
     */
    private void envoyerVersHistorique(Client client, String dolibarrId, String username, ClientEnvoiCallback callback) {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        if (baseUrl == null || apiKey == null) {
            callback.onError("Configuration manquante");
            return;
        }

        String url = baseUrl.endsWith("/")
                ? baseUrl + "api/index.php/dolcustomersapi/clients"
                : baseUrl + "/api/index.php/dolcustomersapi/clients";

        Log.d(TAG, "Envoi vers historique : " + client.getNom() + " (ID Dolibarr: " + dolibarrId + ")");

        try {
            final String jsonBodyString = creerJsonHistorique(client, dolibarrId, username).toString();

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        try {
                            Log.d(TAG, "Réponse historique: " + response);

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
                            Log.e(TAG, "Erreur parsing réponse historique", e);
                            callback.onError("Erreur parsing: " + e.getMessage());
                        }
                    },
                    error -> {
                        String errorMsg = "Erreur envoi historique";
                        if (error.networkResponse != null) {
                            errorMsg += CODE_ERREUR + error.networkResponse.statusCode + ")";
                            if (error.networkResponse.data != null) {
                                String body = new String(error.networkResponse.data);
                                Log.e(TAG, "Réponse serveur historique: " + body);
                            }
                        }
                        Log.e(TAG, errorMsg, error);
                        callback.onError(errorMsg);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put(APIKEY, apiKey);
                    headers.put("Content-Type", JSON_APPLICATION);
                    headers.put(LIBELLE_ACCEPT, JSON_APPLICATION);
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return jsonBodyString.getBytes();
                }
            };

            requestQueue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "Erreur création requête historique", e);
            callback.onError("Erreur création requête: " + e.getMessage());
        }
    }

    /**
     * Crée le JSON pour envoyer un client vers le module d'historique.
     */
    private JSONObject creerJsonHistorique(Client client, String dolibarrId, String username) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("idclient", dolibarrId); // ID du client dans Dolibarr
        json.put("nom", client.getNom());
        json.put("adresse", client.getAdresse() != null ? client.getAdresse() : "");

        // Convertir code postal en int (ou 0 si vide/null)
        int codePostal = 0;
        if (client.getCodePostal() != null && !client.getCodePostal().isEmpty()) {
            try {
                codePostal = Integer.parseInt(client.getCodePostal());
            } catch (NumberFormatException e) {
                Log.w(TAG, "Code postal invalide: " + client.getCodePostal());
            }
        }
        json.put("codepostal", codePostal);

        json.put("ville", client.getVille() != null ? client.getVille() : "");

        json.put("telephone", client.getTelephone() != null ? client.getTelephone() : "");

        json.put("mail", client.getAdresseMail());
        json.put("creator_name", username != null ? username : "Unknown");

        // Date de création du client (en timestamp Unix - secondes)
        long creationDate = client.getDateSaisie() != null ?
                client.getDateSaisie().getTime() / 1000 :
                System.currentTimeMillis() / 1000;
        json.put("creation_date", creationDate);

        json.put("submitted_by_name", username != null ? username : "Unknown");

        // Date d'envoi (maintenant, en timestamp Unix - secondes)
        long submissionDate = System.currentTimeMillis() / 1000;
        json.put("submission_date", submissionDate);

        json.put("update_date", "Oui"); // Client inséré avec succès

        Log.d(TAG, "JSON historique créé: " + json);

        return json;
    }


    /**
     * Récupère l'ID de l'utilisateur connecté depuis l'API Dolibarr.
     * GET /users/login/{username}
     *
     * @param callback Callback pour notifier du résultat
     */
    private void recupererIdUtilisateur(UserIdCallback callback) {
        String baseUrl = getBaseUrl();
        String apiKey = getApiKey();

        if (baseUrl == null || apiKey == null) {
            Log.e(TAG, "Configuration manquante pour récupérer l'ID utilisateur");
            callback.onError("Configuration manquante");
            return;
        }

        String url = baseUrl.endsWith("/")
                ? baseUrl + "api/index.php/users/info/"
                : baseUrl + "/api/index.php/users/info/";

        Log.d(TAG, "Récupération de l'ID ");

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {

                        JSONObject jsonResponse = new JSONObject(response);
                        String userId = jsonResponse.getString("id");

                        Log.d(TAG, "✅ ID utilisateur récupéré: " + userId);
                        callback.onSuccess(userId);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur parsing réponse ID utilisateur", e);
                        callback.onError("Erreur parsing: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMsg = "Erreur récupération ID utilisateur";
                    if (error.networkResponse != null) {
                        errorMsg += CODE_ERREUR + error.networkResponse.statusCode + ")";
                    }
                    Log.e(TAG, errorMsg, error);
                    callback.onError(errorMsg);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put(APIKEY, apiKey);
                headers.put(LIBELLE_ACCEPT, JSON_APPLICATION);
                return headers;
            }
        };

        requestQueue.add(request);
    }
}
