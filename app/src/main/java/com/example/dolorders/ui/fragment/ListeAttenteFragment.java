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
import com.example.dolorders.data.stockage.commande.GestionnaireStockageCommande;
import com.example.dolorders.objet.Client;
import com.example.dolorders.objet.Commande;
import com.example.dolorders.repository.ClientApiRepository;
import com.example.dolorders.repository.CommandeApiRepository;
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

        // Gestion du bouton Envoyer - Envoie clients + leurs commandes
        btnEnvoyer.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Synchronisation complète")
                    .setMessage("Voulez-vous envoyer tous les clients et leurs commandes vers Dolibarr ?")
                    .setPositiveButton("Envoyer", (dialog, which) -> {
                        envoyerToutVersDolibarr();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    /**
     * Envoie tous les clients locaux et leurs commandes vers Dolibarr + historique.
     * Flux: Client → Historique client → Commandes du client → Historique commandes
     */
    //TODO gerer le fait que si on a cree une commande a partir dun client de dolibarr, il faut pas renvoyer le client
    private void envoyerToutVersDolibarr() {
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

        // Envoyer chaque client + ses commandes séquentiellement
        ClientApiRepository clientRepo = new ClientApiRepository(requireContext());
        CommandeApiRepository commandeRepo = new CommandeApiRepository(requireContext());
        GestionnaireStockageCommande commandeStorage = new GestionnaireStockageCommande(requireContext());

        envoyerClientEtCommandesRecursif(clientsAEnvoyer, 0, clientRepo, commandeRepo,
                                         storageLocal, commandeStorage, progressDialog);
    }

    /**
     * Envoie les clients un par un avec leurs commandes de manière récursive.
     */
    private void envoyerClientEtCommandesRecursif(List<Client> clients, int index,
                                                   ClientApiRepository clientRepo,
                                                   CommandeApiRepository commandeRepo,
                                                   GestionnaireStockageClient clientStorage,
                                                   GestionnaireStockageCommande commandeStorage,
                                                   ProgressDialog progressDialog) {
        // Tous les clients ont été traités
        if (index >= clients.size()) {
            Log.d("ListeAttente", "Tous les clients et commandes traités. Re-synchronisation...");
            resynchroniserClients(clientStorage, progressDialog);
            return;
        }

        Client client = clients.get(index);
        Log.d("ListeAttente", "Envoi du client " + (index + 1) + "/" + clients.size() + ": " + client.getNom());

        progressDialog.setMessage("Envoi du client " + client.getNom() + " (" + (index + 1) + "/" + clients.size() + ")...");

        // 1. Envoyer le client vers Dolibarr + historique
        clientRepo.envoyerClient(client, new ClientApiRepository.ClientEnvoiCallback() {
            @Override
            public void onSuccess(String dolibarrId) {
                Log.d("ListeAttente", "✅ Client " + client.getNom() + " envoyé ! ID Dolibarr: " + dolibarrId);


                Client clientAvecId = new Client.Builder()
                        .setId(dolibarrId)
                        .setNom(client.getNom())
                        .setAdresse(client.getAdresse())
                        .setCodePostal(client.getCodePostal())
                        .setVille(client.getVille())
                        .setAdresseMail(client.getAdresseMail())
                        .setTelephone(client.getTelephone())
                        .setUtilisateur(client.getUtilisateur())
                        .setDateSaisie(client.getDateSaisie())
                        .setFromApi(false)
                        .build();

                // 2. Envoyer les commandes de ce client
                envoyerCommandesDuClient(clientAvecId, commandeRepo, commandeStorage, () -> {
                    // 3. Supprimer le client du stockage local après tout
                    boolean supprime = clientStorage.deleteClient(client);

                    if (supprime) {
                        Log.d("ListeAttente", "✅ Client " + client.getNom() + " supprimé du stockage local");
                    } else {
                        Log.w("ListeAttente", "⚠️ Erreur suppression du client local: " + client.getNom());
                    }

                    // 4. Passer au client suivant
                    envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                                                     clientStorage, commandeStorage, progressDialog);
                });
            }

            @Override
            public void onError(String message) {
                Log.e("ListeAttente", "❌ Erreur envoi " + client.getNom() + ": " + message);

                Toast.makeText(getContext(),
                        "Erreur : " + client.getNom() + " - " + message,
                        Toast.LENGTH_LONG).show();

                // Continuer avec le client suivant même en cas d'erreur
                envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                                                 clientStorage, commandeStorage, progressDialog);
            }
        });
    }

    /**
     * Envoie toutes les commandes d'un client vers l'historique.
     */
    private void envoyerCommandesDuClient(Client client,
                                          CommandeApiRepository commandeRepo,
                                          GestionnaireStockageCommande commandeStorage,
                                          Runnable onTermine) {
        // Charger toutes les commandes
        List<Commande> toutesCommandes = commandeStorage.loadCommandes();

        if (toutesCommandes == null || toutesCommandes.isEmpty()) {
            Log.d("ListeAttente", "Aucune commande pour le client " + client.getNom());
            onTermine.run();
            return;
        }

        // Filtrer les commandes de ce client (basé sur le nom du client)
        List<Commande> commandesDuClient = new ArrayList<>();
        for (Commande cmd : toutesCommandes) {
            if (cmd.getClient() != null && cmd.getClient().getNom().equals(client.getNom())) {
                // Mettre à jour la commande avec le client qui a l'ID Dolibarr
                Commande commandeAvecClientId = new Commande.Builder()
                        .setId(cmd.getId())
                        .setClient(client)  // Client avec ID Dolibarr
                        .setDateCommande(cmd.getDateCommande())
                        .setLignesCommande(cmd.getLignesCommande())
                        .setUtilisateur(cmd.getUtilisateur())
                        .build();
                commandesDuClient.add(commandeAvecClientId);
            }
        }

        if (commandesDuClient.isEmpty()) {
            Log.d("ListeAttente", "Aucune commande pour le client " + client.getNom());
            onTermine.run();
            return;
        }

        Log.d("ListeAttente", "Envoi de " + commandesDuClient.size() + " commande(s) pour " + client.getNom());

        // Envoyer les commandes une par une
        envoyerCommandesRecursif(commandesDuClient, 0, commandeRepo, commandeStorage, onTermine);
    }

    /**
     * Envoie les commandes une par une de manière récursive.
     */
    private void envoyerCommandesRecursif(List<Commande> commandes, int index,
                                          CommandeApiRepository repo,
                                          GestionnaireStockageCommande storage,
                                          Runnable onTermine) {
        if (index >= commandes.size()) {
            Log.d("ListeAttente", "Toutes les commandes du client envoyées");
            onTermine.run();
            return;
        }

        Commande commande = commandes.get(index);
        Log.d("ListeAttente", "Envoi commande " + (index + 1) + "/" + commandes.size() +
              " - " + commande.getLignesCommande().size() + " ligne(s)");

        repo.envoyerCommandeVersHistorique(commande, new CommandeApiRepository.CommandeEnvoiCallback() {
            @Override
            public void onSuccess(String historiqueId) {
                Log.d("ListeAttente", "✅ Commande " + commande.getId() + " envoyée !");

                // Supprimer la commande du stockage local
                boolean supprime = storage.deleteCommande(commande.getId());

                if (supprime) {
                    Log.d("ListeAttente", "✅ Commande " + commande.getId() + " supprimée du stockage local");
                } else {
                    Log.w("ListeAttente", "⚠️ Erreur suppression de la commande locale: " + commande.getId());
                }

                // Envoyer la commande suivante
                envoyerCommandesRecursif(commandes, index + 1, repo, storage, onTermine);
            }

            @Override
            public void onError(String message) {
                Log.e("ListeAttente", "❌ Erreur envoi commande " + commande.getId() + ": " + message);

                Toast.makeText(getContext(),
                        "Erreur commande : " + message,
                        Toast.LENGTH_SHORT).show();

                // Continuer avec la commande suivante même en cas d'erreur
                envoyerCommandesRecursif(commandes, index + 1, repo, storage, onTermine);
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