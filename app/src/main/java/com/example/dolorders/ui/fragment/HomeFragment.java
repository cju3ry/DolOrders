package com.example.dolorders.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dolorders.R;
import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.data.stockage.commande.GestionnaireStockageCommande;
import com.example.dolorders.ui.util.NavigationUtils;
import com.example.dolorders.ui.viewModel.ClientsAjoutFragmentViewModel;
import com.example.dolorders.ui.viewModel.CommandesFragmentViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private TextView textClients, textCommandes, textTotal;
    private MaterialButton btnNewClient, btnNewCommande, btnPendingData, btnSyncProduits;
    private CommandesFragmentViewModel commandesViewModel;

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
        btnSyncProduits = view.findViewById(R.id.btnSyncProduits);
        commandesViewModel = new ViewModelProvider(requireActivity()).get(CommandesFragmentViewModel.class);

        // Récupération réelle des données
        GestionnaireStockageClient gestionnaireClient = new GestionnaireStockageClient(requireContext());
        GestionnaireStockageCommande gestionnaireCommande = new GestionnaireStockageCommande(requireContext());
        int nbClients = gestionnaireClient.loadClients().size();
        int nbCommandes = gestionnaireCommande.loadCommandes().size();
        updateStats(nbClients, nbCommandes);

        // Bouton de synchronisation des produits
        btnSyncProduits.setOnClickListener(v -> {
            btnSyncProduits.setEnabled(false);
            btnSyncProduits.setText("Synchronisation...");

            Toast.makeText(requireContext(),
                "Synchronisation des produits en cours...",
                Toast.LENGTH_SHORT).show();

            commandesViewModel.chargerProduits(requireContext());

            // Observer le résultat de la synchronisation
            commandesViewModel.getListeProduits().observe(getViewLifecycleOwner(), produits -> {
                if (produits != null) {
                    btnSyncProduits.setEnabled(true);
                    btnSyncProduits.setText("Synchroniser les produits");

                    if (!produits.isEmpty()) {
                        Toast.makeText(requireContext(),
                            produits.size() + " produit(s) synchronisé(s) avec succès !",
                            Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

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
