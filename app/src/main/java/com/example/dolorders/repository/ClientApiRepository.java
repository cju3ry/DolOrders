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
                        errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
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
                headers.put("DOLAPIKEY", apiKey);
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * Parse la réponse JSON de l'API en liste de Clients.
     */
    private List<Client> parseJsonResponse(JSONArray jsonArray) throws Exception {
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
     * @param client Client local à envoyer (sans ID Dolibarr)
     * @param callback Callback pour notifier du résultat
     */
    public void envoyerClient(Client client, ClientEnvoiCallback callback) {
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

        Log.d(TAG, "Envoi du client vers Dolibarr : " + client.getNom());

        try {
            final String jsonBodyString = creerJsonClient(client).toString();

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
                            callback.onSuccess(dolibarrId);
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur parsing réponse Dolibarr", e);
                            callback.onError("Erreur parsing réponse: " + e.getMessage());
                        }
                    },
                    error -> {
                        String errorMsg = "Erreur envoi client";
                        if (error.networkResponse != null) {
                            errorMsg += " (Code: " + error.networkResponse.statusCode + ")";
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
            Log.e(TAG, "Erreur création requête POST", e);
            callback.onError("Erreur création requête: " + e.getMessage());
        }
    }

    /**
     * Crée le JSON pour envoyer un client vers Dolibarr.
     * NE PAS INCLURE l'ID local - Dolibarr génère son propre ID.
     */
    private JSONObject creerJsonClient(Client client) throws Exception {
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
        json.put("commercial_id", 2); // TODO remplacer par l'ID de l'utilisateur connecté
        json.put("code_client","auto"); // Auto-génération du code client par Dolibarr
        Log.d(TAG, "JSON créé pour client: " + json.toString());

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
}
