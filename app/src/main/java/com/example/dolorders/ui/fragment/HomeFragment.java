package com.example.dolorders.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dolorders.R;
import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.data.stockage.commande.GestionnaireStockageCommande;
import com.example.dolorders.ui.util.NavigationUtils;
import com.example.dolorders.ui.viewModel.CommandesFragmentViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private TextView textClients;

    private TextView textCommandes;

    private TextView textTotal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialisation des vues
        textClients = view.findViewById(R.id.textClients);
        textCommandes = view.findViewById(R.id.textCommandes);
        textTotal = view.findViewById(R.id.textTotal);
        MaterialButton btnNewClient = view.findViewById(R.id.btnNewClient);
        MaterialButton btnNewCommande = view.findViewById(R.id.btnNewCommande);
        MaterialButton btnPendingData = view.findViewById(R.id.btnPendingData);
        CommandesFragmentViewModel commandesViewModel = new ViewModelProvider(requireActivity()).get(CommandesFragmentViewModel.class);

        // Récupération réelle des données
        GestionnaireStockageClient gestionnaireClient = new GestionnaireStockageClient(requireContext());
        GestionnaireStockageCommande gestionnaireCommande = new GestionnaireStockageCommande(requireContext());
        int nbClients = gestionnaireClient.loadClients().size();
        int nbCommandes = gestionnaireCommande.loadCommandes().size();
        updateStats(nbClients, nbCommandes);

        // Navigation via les boutons
        btnNewClient.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            bottomNav.setSelectedItemId(R.id.nav_clients);
            NavigationUtils.navigateToClientAjout(this);
        });

        btnNewCommande.setOnClickListener(v -> {
            commandesViewModel.setFromAccueil();
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
}
