package com.example.dolorders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private AutoCompleteTextView etUrl;
    private Button btnLogin;
    private RequestQueue requestQueue;
    private SharedPreferences securePrefs;
    private UrlManager urlManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etUrl = findViewById(R.id.etUrl);
        btnLogin = findViewById(R.id.btnLogin);

        requestQueue = Volley.newRequestQueue(this);
        urlManager = new UrlManager(this);

        // --- Initialisation des SharedPreferences cryptées ---
        try {
            securePrefs = getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(this, "Erreur d'initialisation du stockage sécurisé", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            // Si l'initialisation échoue, on ferme l'activité
            finish();
            return;
        }

        // --- Configuration de l'auto-complétion pour l'URL ---
        setupUrlAutoComplete();

        // --- Listener du bouton de connexion ---
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String url = etUrl.getText().toString().trim();

            if (validateInputs(username, password, url)) {
                login(username, password, url);
            }
        });

        // --- Vérification si déjà connecté ---
        if (securePrefs.getBoolean("is_logged_in", false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    /**
     * Configure l'auto-complétion pour le champ URL avec les URLs enregistrées.
     */

    private void setupUrlAutoComplete() {
        List<String> urls = urlManager.getAllUrls();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                urls
        );

        etUrl.setAdapter(adapter);

        etUrl.setThreshold(1); // Affiche la liste dès le premier caractère

        // Réautorise la saisie manuelle
        etUrl.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
    }

    private SharedPreferences getEncryptedSharedPreferences()
            throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                this,
                "secure_prefs_crypto",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

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

    private void login(final String username, final String password, String baseUrl) {
        btnLogin.setEnabled(false);

        // Mode bouchon
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

        android.util.Log.d("LOGIN_DEBUG", "URL appelée: " + apiUrl);

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
                            errorMessage = "Identifiants et/ou mot de passe incorrects";
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

    private void handleLoginSuccess(JSONObject response, String username, String baseUrl) {
        try {
            if (response.has("success")) {
                JSONObject successObj = response.getJSONObject("success");
                if (successObj.has("token")) {
                    String apiKey = successObj.getString("token");

                    // Sauvegarde les credentials cryptés
                    saveCredentials(username, apiKey, baseUrl);

                    // Lance MainActivity qui sauvegardera l'URL
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

    private void saveCredentials(String username, String apiKey, String baseUrl) {
        SharedPreferences.Editor editor = securePrefs.edit();
        editor.putString("username", username);
        editor.putString("api_key", apiKey);
        editor.putString("base_url", baseUrl);
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        android.util.Log.d("LOGIN_DEBUG", "Identifiants sauvegardés de manière cryptée");
    }

    public static String getApiKey(AppCompatActivity activity) {
        try {
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

            return securePrefs.getString("api_key", null);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showError(String message) {
        btnLogin.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static void logout(AppCompatActivity activity) {
        try {
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

            securePrefs.edit().clear().apply();
            android.util.Log.d("LOGOUT_DEBUG", "Données cryptées effacées avec succès");

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}