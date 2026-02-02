package com.example.dolorders.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.dolorders.R;
import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.objet.Client;
import com.example.dolorders.repository.ClientApiRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class ListeAttenteFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liste_attente, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        Button btnEnvoyer = view.findViewById(R.id.btn_envoyer_dolibarr);

        // Configuration de l'adapter (Seulement 2 onglets maintenant)
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Liaison TabLayout <-> ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("CLIENTS");
                    break;
                case 1:
                    tab.setText("COMMANDES");
                    break;
            }
        }).attach();

        // Gestion du bouton Envoyer (Action globale)
        btnEnvoyer.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Synchronisation")
                    .setMessage("Voulez-vous envoyer tous les clients en attente vers Dolibarr ?")
                    .setPositiveButton("Envoyer", (dialog, which) -> {
                        envoyerClientsVersDolibarr();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    /**
     * Envoie tous les clients locaux vers Dolibarr.
     */
    private void envoyerClientsVersDolibarr() {
        // Afficher un dialogue de progression
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Préparation de l'envoi...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Charger les clients locaux
        GestionnaireStockageClient storageLocal = new GestionnaireStockageClient(requireContext());
        List<Client> clientsLocaux = storageLocal.loadClients();

        // Filtrer uniquement les clients locaux (pas ceux de l'API)
        List<Client> clientsAEnvoyer = new ArrayList<>();
        for (Client c : clientsLocaux) {
            if (!c.isFromApi()) {
                clientsAEnvoyer.add(c);
            }
        }

        if (clientsAEnvoyer.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Aucun client à envoyer", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ListeAttente", "Nombre de clients à envoyer: " + clientsAEnvoyer.size());

        // Envoyer chaque client séquentiellement
        ClientApiRepository repository = new ClientApiRepository(requireContext());
        envoyerClientRecursif(clientsAEnvoyer, 0, repository, storageLocal, progressDialog);
    }

    /**
     * Envoie les clients un par un de manière récursive.
     */
    private void envoyerClientRecursif(List<Client> clients, int index,
                                       ClientApiRepository repo,
                                       GestionnaireStockageClient storage,
                                       ProgressDialog progressDialog) {
        // Tous les clients ont été traités
        if (index >= clients.size()) {
            Log.d("ListeAttente", "Tous les clients traités. Re-synchronisation...");

            // Re-synchroniser depuis l'API
            resynchroniserClients(storage, progressDialog);
            return;
        }

        Client client = clients.get(index);
        Log.d("ListeAttente", "Envoi du client " + (index + 1) + "/" + clients.size() + ": " + client.getNom());

        progressDialog.setMessage("Envoi de " + client.getNom() + " (" + (index + 1) + "/" + clients.size() + ")...");

        repo.envoyerClient(client, new ClientApiRepository.ClientEnvoiCallback() {
            @Override
            public void onSuccess(String dolibarrId) {
                Log.d("ListeAttente", "✅ Client " + client.getNom() + " envoyé ! ID Dolibarr: " + dolibarrId);

                // Supprimer le client du stockage local
                boolean supprime = storage.deleteClient(client);

                if (supprime) {
                    Log.d("ListeAttente", "✅ Client " + client.getNom() + " supprimé du stockage local");
                } else {
                    Log.w("ListeAttente", "⚠️ Erreur suppression du client local: " + client.getNom());
                }

                // Envoyer le client suivant
                envoyerClientRecursif(clients, index + 1, repo, storage, progressDialog);
            }

            @Override
            public void onError(String message) {
                Log.e("ListeAttente", "❌ Erreur envoi " + client.getNom() + ": " + message);

                Toast.makeText(getContext(),
                        "Erreur : " + client.getNom() + " - " + message,
                        Toast.LENGTH_LONG).show();

                // Continuer avec le client suivant même en cas d'erreur
                envoyerClientRecursif(clients, index + 1, repo, storage, progressDialog);
            }
        });
    }

    /**
     * Re-synchronise les clients depuis l'API Dolibarr après l'envoi.
     */
    private void resynchroniserClients(GestionnaireStockageClient storage, ProgressDialog progressDialog) {
        progressDialog.setMessage("Récupération des clients depuis Dolibarr...");

        ClientApiRepository repo = new ClientApiRepository(requireContext());

        // Créer un gestionnaire pour les clients API
        GestionnaireStockageClient storageApi = new GestionnaireStockageClient(
                requireContext(),
                GestionnaireStockageClient.API_CLIENTS_FILE
        );

        repo.synchroniserDepuisApi(new ClientApiRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                Log.d("ListeAttente", "✅ " + clients.size() + " clients récupérés depuis l'API");

                // Sauvegarder dans le fichier API
                storageApi.saveClients(clients);

                progressDialog.dismiss();

                Toast.makeText(getContext(),
                        "✅ Synchronisation terminée ! " + clients.size() + " clients récupérés",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                Log.e("ListeAttente", "❌ Erreur synchronisation: " + message);

                progressDialog.dismiss();

                Toast.makeText(getContext(),
                        "Erreur synchronisation: " + message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Adapter interne réduit à 2 onglets
    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new TableauClientsFragment();
            }
            return new TableauCommandesFragment();
        }

        @Override
        public int getItemCount() {
            return 2; // Uniquement Clients et Commandes
        }
    }
}