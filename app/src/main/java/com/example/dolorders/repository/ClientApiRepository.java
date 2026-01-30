package com.example.dolorders.repository;

import android.content.Context;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.dolorders.data.dto.ClientApiReponseDto;
import com.example.dolorders.mapper.ClientApiMapper;
import com.example.dolorders.objet.Client;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository pour gérer la récupération des clients depuis l'API Dolibarr.
 * Ce repository se concentre uniquement sur l'appel API.
 * Le stockage local est géré séparément via GestionnaireStockageClient avec le fichier API_CLIENTS_FILE.
 */
public class ClientApiRepository {

    private static final String TAG = "ClientApiRepository";

    private final Context context;
    private final RequestQueue requestQueue;
    private final Gson gson;

    /**
     * Interface de callback pour les opérations asynchrones.
     */
    public interface ClientCallback {
        void onSuccess(List<Client> clients);
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

