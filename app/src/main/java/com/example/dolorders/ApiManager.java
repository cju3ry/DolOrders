package com.example.dolorders;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class ApiManager {

    private final RequestQueue requestQueue;

    public ApiManager(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * Login Dolibarr via API REST
     */
    public void login(String baseUrl, String username, String password, ApiCallback callback) {

        String apiUrl = baseUrl.endsWith("/") ?
                baseUrl + "api/index.php/login?login=" + username + "&password=" + password :
                baseUrl + "/api/index.php/login?login=" + username + "&password=" + password;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                callback::onSuccess,
                error -> callback.onError(parseError(error))
        );

        requestQueue.add(request);
    }

    private String parseError(VolleyError error) {
        if (error.networkResponse == null) return "Erreur réseau: Vérifiez votre connexion Internet et l'URL";
        int code = error.networkResponse.statusCode;

        switch (code) {
            case 400: return "Requête invalide (400)";
            case 401: return "Identifiants incorrects (401)";
            case 403: return "Identifiants et ou mot de passe incorrects (403)";
            case 404: return "URL Dolibarr incorrecte (404)";
            case 500: return "Erreur serveur Dolibarr (500)";
            default: return "Erreur de connexion (" + code + ")";
        }
    }

    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }
}
