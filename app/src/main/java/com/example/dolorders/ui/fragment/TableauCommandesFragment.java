package com.example.dolorders.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolorders.R;
import com.example.dolorders.data.stockage.commande.GestionnaireStockageCommande;
import com.example.dolorders.objet.Commande;
import com.example.dolorders.ui.adapteur.CommandesAttenteAdapteur;

import java.util.ArrayList;
import java.util.List;

public class TableauCommandesFragment extends Fragment {

    private GestionnaireStockageCommande commandeStorage;
    private CommandesAttenteAdapteur adapter;
    private List<Commande> commandes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_commandes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialisation du gestionnaire de stockage
        commandeStorage = new GestionnaireStockageCommande(requireContext());

        // Configuration du RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_commandes_attente);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Chargement des commandes depuis le stockage local
        commandes = new ArrayList<>();
        adapter = new CommandesAttenteAdapteur(commandes);
        recyclerView.setAdapter(adapter);

        // Chargement des données
        chargerCommandes();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les commandes à chaque fois que le fragment devient visible
        chargerCommandes();
    }

    /**
     * Charge les commandes depuis le stockage local et met à jour l'affichage
     */
    private void chargerCommandes() {
        List<Commande> commandesChargees = commandeStorage.loadCommandes();

        commandes.clear();
        commandes.addAll(commandesChargees);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}