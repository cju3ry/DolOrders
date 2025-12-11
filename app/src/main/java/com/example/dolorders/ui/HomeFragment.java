package com.example.dolorders.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.dolorders.LoginActivity;
import com.example.dolorders.MainActivity;
import com.example.dolorders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class HomeFragment extends Fragment {

    private TextView textClients, textCommandes, textTotal;
    private MaterialButton btnNewClient, btnNewCommande, btnPendingData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialisation des vues
        textClients = view.findViewById(R.id.textClients);
        textCommandes = view.findViewById(R.id.textCommandes);
        textTotal = view.findViewById(R.id.textTotal);
        btnNewClient = view.findViewById(R.id.btnNewClient);
        btnNewCommande = view.findViewById(R.id.btnNewCommande);
        btnPendingData = view.findViewById(R.id.btnPendingData);

        Toolbar toolbar = view.findViewById(R.id.toolbarHome);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                LoginActivity.logout((AppCompatActivity) requireActivity());
                return true;
            } else if (item.getItemId() == R.id.action_about) {
                // TODO : afficher les infos
                return true;
            }
            return false;
        });

        // Exemple de données fictives
        updateStats(28, 96);

        btnNewClient.setOnClickListener(v -> {
            // On récupère la barre de navigation depuis l'activité parente
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            // On sélectionne l'item du menu correspondant pour déclencher la navigation
            bottomNav.setSelectedItemId(R.id.nav_clients);
        });

        btnNewCommande.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            bottomNav.setSelectedItemId(R.id.nav_commandes);
        });

        btnPendingData.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            bottomNav.setSelectedItemId(R.id.nav_en_attentes);
        });

        return view;
    }

    private void updateStats(int nbClients, int nbCommandes) {
        int total = nbClients + nbCommandes;
        textClients.setText(String.valueOf(nbClients));
        textCommandes.setText(String.valueOf(nbCommandes));
        textTotal.setText(String.valueOf(total));
    }

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
}

