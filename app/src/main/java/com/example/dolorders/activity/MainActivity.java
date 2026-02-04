package com.example.dolorders.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.dolorders.R;
import com.example.dolorders.service.ServiceConnexionInternet;
import com.example.dolorders.service.ServiceGestionSession;
import com.example.dolorders.service.ServiceUrl;
import com.example.dolorders.ui.fragment.ClientsFragment;
import com.example.dolorders.ui.fragment.CommandesFragment;
import com.example.dolorders.ui.fragment.HomeFragment;
import com.example.dolorders.ui.fragment.ListeAttenteFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ServiceUrl serviceUrl;
    private ServiceConnexionInternet serviceConnexion;
    private View connectionIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar globale
        Toolbar toolbar = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);

        // Désactive le titre par défaut de la Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialiser l'indicateur de connexion
        connectionIndicator = findViewById(R.id.connectionIndicator);
        setupConnectionMonitoring();

        // Récupération du nom d'utilisateur et mise à jour du TextView
        recupererUtilisateur();

        // Overflow icon blanc
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_overflow_white));

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        serviceUrl = new ServiceUrl(this);

        // Récupère l'URL utilisée pour se connecter et la sauvegarde
        recupererUrl();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_commandes) {
                selectedFragment = new CommandesFragment();
            } else if (id == R.id.nav_clients) {
                selectedFragment = new ClientsFragment();
            } else if (id == R.id.nav_en_attentes) {
                selectedFragment = new ListeAttenteFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Fragment par défaut
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void recupererUrl() {
        String baseUrl = getBaseUrl();
        if (baseUrl != null) {
            boolean success = serviceUrl.addUrl(baseUrl);
            if (success) {
                Log.d(TAG, "URL sauvegardée avec succès: " + baseUrl);
            } else {
                Log.e(TAG, "Erreur lors de la sauvegarde de l'URL");
            }
        }
    }

    private void recupererUtilisateur() {
        String username = LoginActivity.getUsername(this);
        Log.d(TAG, "Username récupéré: " + username);
        TextView tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        if (username != null && !username.isEmpty()) {
            tvWelcomeUser.setText("Bienvenue " + username);
            Log.d(TAG, "TextView mis à jour: Bienvenue " + username);
        } else {
            tvWelcomeUser.setText("Bienvenue");
            Log.w(TAG, "Username null ou vide, texte par défaut utilisé");
        }
    }

    /**
     * Récupère l'URL de base depuis les SharedPreferences cryptées
     */
    private String getBaseUrl() {
        try {
            MasterKey masterKey = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    this,
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
     * Configuration de la surveillance de la connexion Internet
     */
    private void setupConnectionMonitoring() {
        serviceConnexion = new ServiceConnexionInternet(this);

        // Mettre à jour l'indicateur avec l'état initial
        updateConnectionIndicator(serviceConnexion.isInternetAvailable());

        // Démarrer la surveillance en temps réel
        serviceConnexion.startMonitoring(isConnected ->
                runOnUiThread(() -> {
                    updateConnectionIndicator(isConnected);

                    // Afficher un Toast pour informer l'utilisateur
                    if (isConnected) {
                        Toast.makeText(MainActivity.this,
                                "✅ Connexion Internet rétablie",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "❌ Connexion Internet perdue",
                                Toast.LENGTH_SHORT).show();
                    }
                }));

        Log.d(TAG, "Surveillance de la connexion Internet initialisée");
    }

    /**
     * Met à jour l'indicateur visuel de connexion
     */
    private void updateConnectionIndicator(boolean isConnected) {
        if (connectionIndicator != null) {
            if (isConnected) {
                connectionIndicator.setBackgroundResource(R.drawable.ic_connection_online);
                Log.d(TAG, "✅ Indicateur : CONNECTÉ");
            } else {
                connectionIndicator.setBackgroundResource(R.drawable.ic_connection_offline);
                Log.d(TAG, "❌ Indicateur : DÉCONNECTÉ");
            }
        }
    }

    // Inflater le menu utilisateur pour la Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    // Gérer les clics sur les items du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            ServiceGestionSession.logout(this);
            return true;
        } else if (id == R.id.action_about) {
            Toast.makeText(this, "À propos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Arrêter la surveillance lors de la destruction de l'activité
        if (serviceConnexion != null) {
            serviceConnexion.stopMonitoring();
        }
    }
}
