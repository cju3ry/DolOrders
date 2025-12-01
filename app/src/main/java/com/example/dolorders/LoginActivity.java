// java
package com.example.dolorders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etUrl;
    private Button btnLogin;
    private RequestQueue requestQueue;

    private SharedPreferences securePrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etUrl = findViewById(R.id.etUrl);
        btnLogin = findViewById(R.id.btnLogin);

        requestQueue = Volley.newRequestQueue(this);

        // Initialise les SharedPreferences cryptées
        try {
            securePrefs = getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(this, "Erreur d'initialisation du stockage sécurisé", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String url = etUrl.getText().toString().trim();

            if (validateInputs(username, password, url)) {
                login(username, password, url);
            }
        });

        // Si déjà connecté, démarrer directement MainActivity
        if (securePrefs.getBoolean("is_logged_in", false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    /**
     * Crée et retourne une instance de SharedPreferences cryptées.
     * Les données sont automatiquement cryptées lors de l'écriture
     * et décryptées lors de la lecture.
     */
    private SharedPreferences getEncryptedSharedPreferences()
            throws GeneralSecurityException, IOException {
        // Crée ou récupére la clé maître pour le cryptage
        MasterKey masterKey = new MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        // Crée les SharedPreferences cryptées
        return EncryptedSharedPreferences.create(
                this,
                "secure_prefs_crypto",  // Nom du fichier
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /**
     * Valide les champs d'entrée de l'utilisateur.
     *
     * @param username
     * @param password
     * @param url
     * @return
     */
    private boolean validateInputs(String username, String password, String url) {
        if (username.isEmpty()) {
            etUsername.setError("Identifiant requis");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Mot de passe requis");
            return false;
        }
        if (url.isEmpty()) {
            etUrl.setError("URL requise");
            return false;
        }
        return true;
    }

    /**
     * Effectue la connexion au serveur Dolibarr.
     * Si baseUrl est "stub" ou "bouchon", simule une connexion réussie.
     *
     * @param username
     * @param password
     * @param baseUrl
     */
    private void login(final String username, final String password, String baseUrl) {
        btnLogin.setEnabled(false);

        // Mode bouchon : si baseUrl est "stub" ou "bouchon", simule une connexion réussie
        if (baseUrl != null && (baseUrl.equalsIgnoreCase("stub") || baseUrl.equalsIgnoreCase("bouchon"))) {
            try {
                JSONObject fakeResponse = new JSONObject();
                JSONObject successObj = new JSONObject();
                successObj.put("token", "FAKE_TOKEN_1234567890");
                fakeResponse.put("success", successObj);
                handleLoginSuccess(fakeResponse, username, baseUrl);
            } catch (JSONException e) {
                showError("Erreur interne du mode bouchon");
            }
            return;
        }

        String apiUrl = baseUrl.endsWith("/")
                ? baseUrl + "api/index.php/login?login=" + username + "&password=" + password
                : baseUrl + "/api/index.php/login?login=" + username + "&password=" + password;

        // Debug
        android.util.Log.d("LOGIN_DEBUG", "URL appelée: " + apiUrl);
        Toast.makeText(this, "Tentative de connexion à: " + apiUrl, Toast.LENGTH_LONG).show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                apiUrl,
                null,
                response -> {
                    android.util.Log.d("LOGIN_DEBUG", "Réponse reçue: " + response.toString());
                    handleLoginSuccess(response, username, baseUrl);
                },
                error -> {
                    btnLogin.setEnabled(true);
                    // debug
                    android.util.Log.e("LOGIN_DEBUG", "Erreur: " + error.toString());
                    if (error.networkResponse != null) {
                        android.util.Log.e("LOGIN_DEBUG", "Code erreur: " + error.networkResponse.statusCode);
                    }

                    String errorMessage;
                    if (error.networkResponse == null) {
                        errorMessage = "Erreur réseau: Vérifiez votre connexion Internet et l'URL";
                    } else {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 400) errorMessage = "Erreur 400: Requête invalide";
                        else if (statusCode == 401) errorMessage = "Identifiants incorrects";
                        else if (statusCode == 403)
                            errorMessage = "Identifiants et ou mot de passe " +
                                    "incorrects";
                        else if (statusCode == 404) errorMessage = "URL Dolibarr incorrecte (404)";
                        else if (statusCode == 500) errorMessage = "Erreur serveur Dolibarr (500)";
                        else errorMessage = "Erreur de connexion (" + statusCode + ")";
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Gère la réponse de la requête de connexion.
     *
     * @param response     La réponse du serveur.
     * @param username     Le nom d'utilisateur utilisé pour la connexion.
     * @param baseUrl      L'URL de base utilisée pour la connexion.
     */
    private void handleLoginSuccess(JSONObject response, String username, String baseUrl) {
        try {
            if (response.has("success")) {
                JSONObject successObj = response.getJSONObject("success");
                if (successObj.has("token")) {
                    String apiKey = successObj.getString("token");
                    saveCredentials(username, apiKey, baseUrl);
                    // TODO Lance .... La page principale de l'application
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    showError("Clé API non trouvée dans la réponse");
                }
            } else {
                showError("Format de réponse invalide");
            }
        } catch (JSONException e) {
            showError("Erreur lors du traitement de la réponse: " + e.getMessage());
        } finally {
            btnLogin.setEnabled(true);
        }
    }

    /**
     * Sauvegarde les informations de connexion de manière CRYPTÉE.
     * Les données sont automatiquement cryptées par EncryptedSharedPreferences.
     *
     * @param username Le nom d'utilisateur
     * @param apiKey   La clé API (sera cryptée automatiquement)
     * @param baseUrl  L'URL de base (sera cryptée automatiquement)
     */
    private void saveCredentials(String username, String apiKey, String baseUrl) {
        SharedPreferences.Editor editor = securePrefs.edit();

        // Toutes ces données sont automatiquement cryptées lors de l'écriture
        editor.putString("username", username);
        editor.putString("api_key", apiKey);
        editor.putString("base_url", baseUrl);
        editor.putBoolean("is_logged_in", true);

        editor.apply();

        android.util.Log.d("LOGIN_DEBUG", "Identifiants sauvegardés de manière cryptée");
    }

    /**
     * MÉTHODE UTILITAIRE pour récupérer la clé API décryptée
     */
    public static String getApiKey(AppCompatActivity activity) {
        try {
            MasterKey masterKey = new MasterKey.Builder(activity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    activity,
                    "secure_prefs_crypto",  // Nom du fichier
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // La clé est automatiquement décryptée lors de la lecture
            return securePrefs.getString("api_key", null);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Affiche un message d'erreur. Pour le debug
     *
     * @param message
     */
    private void showError(String message) {
        btnLogin.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    /**
     * MÉTHODE DE DÉCONNEXION
     * Efface toutes les données cryptées et redirige vers LoginActivity
     *
     * Utilisation dans une autre activité (ex: MainActivity):
     * LoginActivity.logout(this);
     */
    public static void logout(AppCompatActivity activity) {
        try {
            // Récupére les SharedPreferences cryptées
            MasterKey masterKey = new MasterKey.Builder(activity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    activity,
                    "secure_prefs_crypto",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Efface TOUTES les données cryptées
            securePrefs.edit().clear().apply();

            android.util.Log.d("LOGOUT_DEBUG", "Données cryptées effacées avec succès");

            // Redirige vers LoginActivity
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();

            Toast.makeText(activity, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        } catch (GeneralSecurityException | IOException e) {
            android.util.Log.e("LOGOUT_DEBUG", "Erreur lors de la déconnexion", e);
            Toast.makeText(activity, "Erreur lors de la déconnexion", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Annule les requêtes en cours lorsque l'activité est détruite.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}
