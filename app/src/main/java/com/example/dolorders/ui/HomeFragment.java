package com.example.dolorders.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.dolorders.LoginActivity;
import com.example.dolorders.MainActivity;
import com.example.dolorders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

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
                // TODO : gérer la déconnexion
                return true;
            } else if (item.getItemId() == R.id.action_about) {
                // TODO : afficher les infos
                return true;
            }
            return false;
        });

        // Exemple de données fictives
        updateStats(28, 96);

        // Action au clic sur le bouton "+ Nouveau client"
        btnNewClient.setOnClickListener(v -> {
            // On récupère la barre de navigation depuis l'activité parente
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            // On sélectionne l'item du menu correspondant pour déclencher la navigation
            bottomNav.setSelectedItemId(R.id.nav_clients);
        });

        // Action au clic sur le bouton "+ Nouvelle commande"
        btnNewCommande.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            bottomNav.setSelectedItemId(R.id.nav_commandes);
        });

        // Action au clic sur le bouton "Données en attente"
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
}

