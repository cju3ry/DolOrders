package com.example.dolorders.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dolorders.activity.LoginActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ServiceGestionSession {

    private final RequestQueue requestQueue;

    private static final String LOGOUT_DEBUG = "LOGOUT_DEBUG";

    public ServiceGestionSession(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * MÉTHODE DE DÉCONNEXION
     * Efface toutes les données cryptées et redirige vers LoginActivity
     * <p>
     * Utilisation dans une autre activité (ex: MainActivity):
     * DolOrdersManager.logout(this);
     */
    public static void logout(AppCompatActivity activity) {
        try {
            // Clé maître pour le cryptage
            MasterKey masterKey = new MasterKey.Builder(activity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // SharedPreferences cryptées
            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    activity,
                    "secure_prefs_crypto",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Sauvegarde l'URL avant effacement
            String lastUrl = securePrefs.getString("base_url", null);
            android.util.Log.d(LOGOUT_DEBUG, "URL sauvegardée avant effacement: " + lastUrl);

            // Efface toutes les données cryptées
            securePrefs.edit().clear().apply();

            // Restaure l'URL dans les SharedPreferences normales
            if (lastUrl != null && !lastUrl.isEmpty()) {
                SharedPreferences normalPrefs = activity.getSharedPreferences("DolOrdersPrefs", Context.MODE_PRIVATE);
                normalPrefs.edit().putString("last_used_url", lastUrl).apply();
                android.util.Log.d(LOGOUT_DEBUG, "URL restaurée pour la prochaine connexion");
            }

            android.util.Log.d(LOGOUT_DEBUG, "Données cryptées effacées avec succès");

            // Redirection vers LoginActivity
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();

            Toast.makeText(activity, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        } catch (GeneralSecurityException | IOException e) {
            android.util.Log.e(LOGOUT_DEBUG, "Erreur lors de la déconnexion", e);
            Toast.makeText(activity, "Erreur lors de la déconnexion", Toast.LENGTH_LONG).show();
        }
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
        if (error.networkResponse == null)
            return "Erreur réseau: Vérifiez votre connexion Internet et l'URL";
        int code = error.networkResponse.statusCode;

        switch (code) {
            case 400:
                return "Requête invalide (400)";
            case 401:
                return "Identifiants incorrects (401)";
            case 403:
                return "Identifiants et ou mot de passe incorrects (403)";
            case 404:
                return "URL Dolibarr incorrecte (404)";
            case 500:
                return "Erreur serveur Dolibarr (500)";
            default:
                return "Erreur de connexion (" + code + ")";
        }
    }

    public interface ApiCallback {
        void onSuccess(JSONObject response);

        void onError(String error);
    }
}
