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
    /** TextInputEditText pour le nom d'utilisateur */
    private TextInputEditText etUsername;
    /** TextInputEditText pour le mot de passe */
     private TextInputEditText etPassword;
    /** AutoCompleteTextView pour l'URL du serveur Dolibarr, avec auto-complétion basée sur les
     * URLs précédemment utilisées */
    private AutoCompleteTextView etUrl;
    /** Bouton de connexion */
    private Button btnLogin;

    /** RequestQueue de Volley pour les requêtes réseau */
    private RequestQueue requestQueue;

    /** SharedPreferences sécurisées pour stocker les credentials de manière cryptée */
    private SharedPreferences securePrefs;

    /** ServiceUrl pour gérer les URLs enregistrées et l'auto-complétion */
     private ServiceUrl serviceUrl;

    /** Clé pour stocker le nom d'utilisateur dans les SharedPreferences cryptées */
    private static final String USER_NAME = "username";

    /** Tag pour les logs liés à l'activité de connexion */
    private static final String LOGIN_ACTIVITY = "LoginActivity";

    /** Nom du fichier pour les SharedPreferences cryptées */
    private static final String CRYPTED_FILE_NAME = "secure_prefs_crypto";

    /** Tag pour les logs de débogage liés à la connexion */
    private static final String DEBUG_TAG = "LOGIN_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation des vues
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etUrl = findViewById(R.id.etUrl);
        btnLogin = findViewById(R.id.btnLogin);

        // Initialisation de la RequestQueue de Volley
        requestQueue = Volley.newRequestQueue(this);

        // Initialisation du service pour gérer les URLs
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

        // Si une URL a été récupérée, la pré-remplir
        if (lastUrl != null && !lastUrl.isEmpty()) {
            etUrl.setText(lastUrl);
            urlWasPrefilled[0] = true;
            Log.d(LOGIN_ACTIVITY, "URL pré-remplie: " + lastUrl);
        }

        // Efface l'URL au clic pour permettre une nouvelle saisie
        etUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && urlWasPrefilled[0]) {
                etUrl.setText("");
                urlWasPrefilled[0] = false;
                Log.d(LOGIN_ACTIVITY, "URL effacée au focus");
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

        // Vérification si l'utilisateur est déjà connecté
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

    /** Initialise les SharedPreferences cryptées pour stocker les credentials de manière sécurisée */
     private SharedPreferences getEncryptedSharedPreferences()
            throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                this,
                CRYPTED_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /** Valide les entrées utilisateur pour le nom d'utilisateur, le mot de passe et l'URL.
     * Affiche des erreurs sur les champs correspondants si des entrées sont invalides.
     * @param username Le nom d'utilisateur saisi
     * @param password Le mot de passe saisi
     * @param url L'URL du serveur Dolibarr saisie
     * @return true si toutes les entrées sont valides, false sinon
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
     * @param username Le nom d'utilisateur saisi
     * @param password Le mot de passe saisi
     * @param baseUrl  L'URL du serveur Dolibarr saisi
     */
    private void login(String username, String password, String baseUrl) {

        btnLogin.setEnabled(false);

        // Service de gestion de session pour effectuer la connexion
        ServiceGestionSession api = new ServiceGestionSession(this);

        // Appel de la méthode de connexion du service, avec un callback pour gérer la réponse
        // Si la connexion est réussie, handleLoginSuccess sera appelé pour traiter la réponse
        // et sauvegarder les credentials
        // En cas d'erreur, un message d'erreur sera affiché à l'utilisateur
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

    /** Traite la réponse de connexion réussie, extrait la clé API, sauvegarde les credentials
     * de manière sécurisée et lance MainActivity.
     * @param response La réponse JSON de l'API contenant la clé API
     * @param username Le nom d'utilisateur utilisé pour la connexion
     * @param baseUrl L'URL du serveur Dolibarr utilisé pour la connexion
     */
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

    /** Sauvegarde les credentials de manière sécurisée dans les SharedPreferences cryptées.
     * @param username Le nom d'utilisateur à sauvegarder
     * @param apiKey La clé API à sauvegarder
     * @param baseUrl L'URL du serveur Dolibarr à sauvegarder
     */
     private void saveCredentials(String username, String apiKey, String baseUrl) {
        android.util.Log.d(DEBUG_TAG, "Sauvegarde des credentials - username: " + username);

        SharedPreferences.Editor editor = securePrefs.edit();
        editor.putString(USER_NAME, username);
        editor.putString("api_key", apiKey);
        editor.putString("base_url", baseUrl);
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        android.util.Log.d(DEBUG_TAG, "Identifiants sauvegardés de manière cryptée");

        // Vérification immédiate
        String verif = securePrefs.getString(USER_NAME, null);
        android.util.Log.d(DEBUG_TAG, "Vérification immédiate - username lu: " + verif);
    }

    public static String getApiKey(AppCompatActivity activity) {
        try {
            MasterKey masterKey = new MasterKey.Builder(activity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    activity,
                    CRYPTED_FILE_NAME,
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
     * @param context Le contexte de l'activité pour accéder aux SharedPreferences
     * @return Le nom d'utilisateur récupéré ou null en cas d'erreur
     */
    public static String getUsername(android.content.Context context) {
        try {
            android.util.Log.d(LOGIN_ACTIVITY, "Tentative de récupération du username");

            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            // Accès aux SharedPreferences cryptées
            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    context,
                    CRYPTED_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            // Récupération du nom d'utilisateur
            String username = securePrefs.getString(USER_NAME, null);
            android.util.Log.d(LOGIN_ACTIVITY, "Username récupéré: " + username);
            return username;
        } catch (GeneralSecurityException | IOException e) {
            android.util.Log.e(LOGIN_ACTIVITY, "Erreur lors de la récupération du username", e);
            e.printStackTrace();
            return null;
        }
    }
    /** Affiche un message d'erreur à l'utilisateur et réactive le bouton de connexion.
     * @param message Le message d'erreur à afficher
     */
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
            // D'abord, vérifie les SharedPreferences normales (après déconnexion)
            SharedPreferences normalPrefs = getSharedPreferences("DolOrdersPrefs", MODE_PRIVATE);
            String lastUrl = normalPrefs.getString("last_used_url", null);
            // Si une URL est trouvée dans les SharedPreferences normales, la retourner
            if (lastUrl != null && !lastUrl.isEmpty()) {
                Log.d(LOGIN_ACTIVITY, "URL récupérée depuis SharedPreferences normales: " + lastUrl);
                return lastUrl;
            }
            // Sinon, vérifier les SharedPreferences cryptées (si encore connecté)
            if (securePrefs != null) {
                lastUrl = securePrefs.getString("base_url", null);
                if (lastUrl != null && !lastUrl.isEmpty()) {
                    Log.d(LOGIN_ACTIVITY, "URL récupérée depuis SharedPreferences cryptées: " + lastUrl);
                    return lastUrl;
                }
            }
        } catch (Exception e) {
            Log.e(LOGIN_ACTIVITY, "Erreur lors de la récupération de la dernière URL", e);
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