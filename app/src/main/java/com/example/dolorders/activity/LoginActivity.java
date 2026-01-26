package com.example.dolorders.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.dolorders.R;
import com.example.dolorders.service.ServiceGestionSession;
import com.example.dolorders.service.ServiceUrl;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private AutoCompleteTextView etUrl;
    private Button btnLogin;
    private RequestQueue requestQueue;
    private SharedPreferences securePrefs;
    private ServiceUrl serviceUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etUrl = findViewById(R.id.etUrl);
        btnLogin = findViewById(R.id.btnLogin);

        requestQueue = Volley.newRequestQueue(this);
        serviceUrl = new ServiceUrl(this);

        // Initialisation des SharedPreferences cryptées
        try {
            securePrefs = getEncryptedSharedPreferences();
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(this, "Erreur d'initialisation du stockage sécurisé", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            // Si l'initialisation échoue, on ferme l'activité
            finish();
            return;
        }

        // Configuration de l'auto-complétion pour l'URL
        setupUrlAutoComplete();

        // Pré-remplir avec la dernière URL utilisée
        final String lastUrl = getLastUsedUrl();
        final boolean[] urlWasPrefilled = {false};

        if (lastUrl != null && !lastUrl.isEmpty()) {
            etUrl.setText(lastUrl);
            urlWasPrefilled[0] = true;
            Log.d("LoginActivity", "URL pré-remplie: " + lastUrl);
        }

        // Efface l'URL au clic pour permettre une nouvelle saisie
        etUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && urlWasPrefilled[0]) {
                etUrl.setText("");
                urlWasPrefilled[0] = false;
                Log.d("LoginActivity", "URL effacée au focus");
            }
        });

        // Listener du bouton de connexion
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
        List<String> urls = serviceUrl.getAllUrls();
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

    /**
     * Effectue la connexion au serveur Dolibarr.
     * Si baseUrl est "stub" ou "bouchon", simule une connexion réussie.
     *
     * @param username
     * @param password
     * @param baseUrl
     */
    private void login(String username, String password, String baseUrl) {

        btnLogin.setEnabled(false);

        ServiceGestionSession api = new ServiceGestionSession(this);

        api.login(baseUrl, username, password, new ServiceGestionSession.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                handleLoginSuccess(response, username, baseUrl);
                btnLogin.setEnabled(true);
            }

            @Override
            public void onError(String error) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
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
        android.util.Log.d("LOGIN_DEBUG", "Sauvegarde des credentials - username: " + username);

        SharedPreferences.Editor editor = securePrefs.edit();
        editor.putString("username", username);
        editor.putString("api_key", apiKey);
        editor.putString("base_url", baseUrl);
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        android.util.Log.d("LOGIN_DEBUG", "Identifiants sauvegardés de manière cryptée");

        // Vérification immédiate
        String verif = securePrefs.getString("username", null);
        android.util.Log.d("LOGIN_DEBUG", "Vérification immédiate - username lu: " + verif);
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

    /**
     * Récupère le nom d'utilisateur depuis les SharedPreferences cryptées
     */
    public static String getUsername(android.content.Context context) {
        try {
            android.util.Log.d("LoginActivity", "Tentative de récupération du username");

            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs_crypto",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String username = securePrefs.getString("username", null);
            android.util.Log.d("LoginActivity", "Username récupéré: " + username);
            return username;
        } catch (GeneralSecurityException | IOException e) {
            android.util.Log.e("LoginActivity", "Erreur lors de la récupération du username", e);
            e.printStackTrace();
            return null;
        }
    }

    private void showError(String message) {
        btnLogin.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Récupère la dernière URL utilisée depuis les SharedPreferences cryptées
     * ou les SharedPreferences normales (après déconnexion)
     */
    private String getLastUsedUrl() {
        try {
            // D'abord, vérifier les SharedPreferences normales (sauvegardées lors de la déconnexion)
            SharedPreferences normalPrefs = getSharedPreferences("DolOrdersPrefs", MODE_PRIVATE);
            String lastUrl = normalPrefs.getString("last_used_url", null);

            if (lastUrl != null && !lastUrl.isEmpty()) {
                Log.d("LoginActivity", "URL récupérée depuis SharedPreferences normales: " + lastUrl);
                return lastUrl;
            }

            // Sinon, vérifier les SharedPreferences cryptées (si encore connecté)
            if (securePrefs != null) {
                lastUrl = securePrefs.getString("base_url", null);
                if (lastUrl != null && !lastUrl.isEmpty()) {
                    Log.d("LoginActivity", "URL récupérée depuis SharedPreferences cryptées: " + lastUrl);
                    return lastUrl;
                }
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Erreur lors de la récupération de la dernière URL", e);
        }
        return null;
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