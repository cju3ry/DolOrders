package com.example.dolorders;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.dolorders.ui.ClientsFragment;
import com.example.dolorders.ui.CommandesFragment;
import com.example.dolorders.ui.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNav;

    private static final String TAG = "MainActivity";
    private UrlManager urlManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar globale
        toolbar = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbar);

        // Désactive le titre par défaut de la Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Récupération du nom d'utilisateur et mise à jour du TextView
        TextView tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        String username = LoginActivity.getUsername(this);
        Log.d(TAG, "Username récupéré: " + username);
        if (username != null && !username.isEmpty()) {
            tvWelcomeUser.setText("Bienvenue " + username);
            Log.d(TAG, "TextView mis à jour: Bienvenue " + username);
        } else {
            tvWelcomeUser.setText("Bienvenue");
            Log.w(TAG, "Username null ou vide, texte par défaut utilisé");
        }

        // Overflow icon blanc
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_overflow_white));

        // Bottom Navigation
        bottomNav = findViewById(R.id.bottomNavigation);

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
                selectedFragment = new CommandesFragment();
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

        // Fragment par défaut
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
            SessionManager.logout(this);
            return true;
        } else if (id == R.id.action_about) {
            Toast.makeText(this, "À propos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
