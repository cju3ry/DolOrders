package com.example.dolorders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dolorders.data.dto.ClientRepository;
import com.example.dolorders.data.storage.ClientStorageManager;
import com.example.dolorders.ui.ClientsFragment;
import com.example.dolorders.ui.CommandesFragment;
import com.example.dolorders.ui.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button btnLogout, btnSyncroClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        btnLogout = findViewById(R.id.btnLogout);
        // 1. On récupère le bouton de la vue
        Button btnSyncroClient = findViewById(R.id.btnSyncroClient);

        // 2. On définit l'action au clic
        btnSyncroClient.setOnClickListener(v -> {
            chargerLesClients();
        });
        btnLogout.setOnClickListener(v -> LoginActivity.logout(this));
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

        // Par défaut, on charge l’accueil
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
    private void chargerLesClients() {
        // A. On instancie le repository et le gestionnaire de stockage
        ClientRepository repository = new ClientRepository(this);
        ClientStorageManager storageManager = new ClientStorageManager(this);

        // B. On lance l'appel
        Toast.makeText(this, "Chargement en cours...", Toast.LENGTH_SHORT).show();

        repository.recupererClientsDolibarr(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                // C. Sauvegarde des clients dans le fichier local
                boolean saved = storageManager.saveClients(clients);

                if (saved) {
                    String message = "Succès ! " + clients.size() + " clients récupérés et sauvegardés.";
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    Log.d("API_DOLIBARR", message);
                } else {
                    String message = "Clients récupérés mais erreur de sauvegarde.";
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    Log.w("API_DOLIBARR", message);
                }

                // Pour le debug, on affiche les clients dans la console (Logcat)
                for (Client c : clients) {
                    Log.d("API_DOLIBARR", "Client reçu : " + c.toString());
                }

                // TODO: Ici, vous passerez la liste 'clients' à votre RecyclerView (Adapter)
                // monAdapter.setClients(clients);
            }

            @Override
            public void onError(String message) {
                // D. Gestion des erreurs
                Toast.makeText(MainActivity.this, "Erreur : " + message, Toast.LENGTH_LONG).show();
                Log.e("API_DOLIBARR", "Erreur récupération clients : " + message);
            }
        });
    }

    /**
     * Charge les clients depuis le stockage local (fichier JSON).
     * Utile pour afficher les clients hors ligne ou au démarrage de l'app.
     *
     * @return Liste des clients sauvegardés
     */
    private List<Client> chargerClientsLocaux() {
        ClientStorageManager storageManager = new ClientStorageManager(this);
        List<Client> clients = storageManager.loadClients();

        Log.d("STORAGE", "Clients chargés depuis le fichier : " + clients.size());
        return clients;
    }
}