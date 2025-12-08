package com.example.dolorders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.dolorders.ui.ClientsFragment;
import com.example.dolorders.ui.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {
    private Button btnLogout;

    private static final String TAG = "MainActivity";
    private UrlManager urlManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> LoginActivity.logout(this));

        urlManager = new UrlManager(this);

        // Récupère l'URL utilisée pour se connecter et la sauvegarde
        String baseUrl = getBaseUrl();
        if (baseUrl != null) {
            boolean success = urlManager.addUrl(baseUrl);
            if (success) {
                Log.d(TAG, "URL sauvegardée avec succès: " + baseUrl);
            } else {
                Log.e(TAG, "Erreur lors de la sauvegarde de l'URL");
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_commandes) {
                // selectedFragment = new CommandesFragment();
            } else if (id == R.id.nav_clients) {
                selectedFragment = new ClientsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Par défaut, on charge l’accueil
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
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
}