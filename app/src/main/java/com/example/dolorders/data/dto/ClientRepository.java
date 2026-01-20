package com.example.dolorders.data.dto;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dolorders.LoginActivity;
import com.example.dolorders.Client;
import com.example.dolorders.data.dto.ClientApiReponseDto;
import com.example.dolorders.mappers.ClientMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientRepository {

    private final RequestQueue requestQueue;
    private final ClientMapper mapper;
    private final Context context;

    public ClientRepository(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.mapper = new ClientMapper();
    }

    public interface ClientCallback {
        void onSuccess(List<Client> clients);
        void onError(String message);
    }

    public void recupererClientsDolibarr(ClientCallback callback) {
        // Récupération de l'URL de base (stockée en clair) et de la clé API (cryptée)
        String baseUrl = LoginActivity.getBaseUrlFromContext(context);
        String apiKey = LoginActivity.getApiKeyFromContext(context);

        // Vérification que les identifiants sont bien récupérés
        if (baseUrl == null || baseUrl.isEmpty()) {
            callback.onError("URL de base non trouvée. Veuillez vous reconnecter.");
            return;
        }
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("Clé API non trouvée. Veuillez vous reconnecter.");
            return;
        }

        // Construction de l'URL complète
        String url = baseUrl.endsWith("/")
                ? baseUrl + "api/index.php/thirdparties?sortfield=t.rowid&sortorder=ASC&limit=100"
                : baseUrl + "/api/index.php/thirdparties?sortfield=t.rowid&sortorder=ASC&limit=100";

        Log.d("ClientRepo", "URL d'appel: " + url);

        final String finalApiKey = apiKey;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Log.d("ClientRepo", "Réponse reçue: " + response); // enlever trop long
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<ClientApiReponseDto>>(){}.getType();
                        List<ClientApiReponseDto> dtos = gson.fromJson(response, listType);

                        List<Client> clientsMetier = new ArrayList<>();
                        if (dtos != null) {
                            for (ClientApiReponseDto dto : dtos) {
                                try {
                                    clientsMetier.add(mapper.getClient(dto));
                                } catch (Exception e) {
                                    Log.e("ClientRepo", "Erreur mapping client ID: " + dto.id, e);
                                    // Continue avec les autres clients même si un échoue
                                }
                            }
                        }
                        callback.onSuccess(clientsMetier);

                    } catch (Exception e) {
                        Log.e("ClientRepo", "Erreur parsing JSON", e);
                        callback.onError("Erreur lors de la lecture des données reçues.");
                    }
                },
                error -> {
                    Log.e("ClientRepo", "Erreur Volley", error);
                    String errorMessage = "Erreur réseau";
                    if (error.networkResponse != null) {
                        errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                    }
                    callback.onError(errorMessage + ": " + error.getMessage());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("DOLAPIKEY", finalApiKey);
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
}
