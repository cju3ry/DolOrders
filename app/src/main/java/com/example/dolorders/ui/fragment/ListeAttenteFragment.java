package com.example.dolorders.ui.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.example.dolorders.service.ServiceClient;
import com.example.dolorders.ui.util.RapportSynchronisation;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class ListeAttenteFragment extends Fragment {
    /** String de log pour ce fragment */
    private static final String VALIDE_CLIENT = "✅ Client ";
    private static final String VALIDE_COMMANDE = "✅ Commande ";
    private static final String LISTE_ATTENTE = "ListeAttente";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liste_attente, container, false);
    }
    /** Configuration du ViewPager2 et du bouton d'envoi lors de la création de la vue */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        Button btnEnvoyer = view.findViewById(R.id.btn_envoyer_dolibarr);

        // Configuration de l'adapter (Seulement 2 onglets maintenant)
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Liaison TabLayout <-> ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("CLIENTS");
            } else if (position == 1) {
                tab.setText("COMMANDES");
            }
        }).attach();

        // Gestion du bouton Envoyer - Envoie clients + leurs commandes
        btnEnvoyer.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Synchronisation complète")
                        .setMessage("Voulez-vous envoyer tous les clients et leurs commandes vers Dolibarr ?")
                        .setPositiveButton("Envoyer", (dialog, which) ->
                                envoyerToutVersDolibarr())
                        .setNegativeButton("Annuler", null)
                        .show());
    }

    /**
     * Envoie tous les clients et leurs commandes vers Dolibarr + historique.
     * - Clients locaux : envoyés vers Dolibarr puis leurs commandes
     * - Clients API : seulement leurs commandes (client existe déjà)
     * Flux: Client (si local) → Commandes du client → Historique commandes
     */
    private void envoyerToutVersDolibarr() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Préparation de l'envoi...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RapportSynchronisation rapport = new RapportSynchronisation();

        // Charge TOUS les clients (locaux + API)
        GestionnaireStockageClient storageLocal = new GestionnaireStockageClient(requireContext());
        GestionnaireStockageClient storageApi = new GestionnaireStockageClient(
                requireContext(),
                GestionnaireStockageClient.API_CLIENTS_FILE
        );

        List<Client> clientsLocaux = storageLocal.loadClients();
        List<Client> clientsApi = storageApi.loadClients();

        // Charge toutes les commandes pour identifier quels clients ont des commandes
        GestionnaireStockageCommande commandeStorage = new GestionnaireStockageCommande(requireContext());
        List<Commande> toutesCommandes = commandeStorage.loadCommandes();

        // Crée une liste de tous les clients à envoyer (avec ou sans commandes)
        List<Client> clientsAEnvoyer = new ArrayList<>();

        // 1. D'abord ajouter tous les clients LOCAUX (fromApi=false) sans commandes
        if (clientsLocaux != null && !clientsLocaux.isEmpty()) {
            for (Client clientLocal : clientsLocaux) {
                if (!clientLocal.isFromApi()) {
                    // Vérifie si ce client a des commandes
                    boolean aDesCommandes = false;
                    if (toutesCommandes != null && !toutesCommandes.isEmpty()) {
                        for (Commande cmd : toutesCommandes) {
                            if (cmd.getClient() != null && cmd.getClient().getNom().equals(clientLocal.getNom())) {
                                aDesCommandes = true;
                                break;
                            }
                        }
                    }

                    // Ajoute le client local qu'il ait des commandes ou non
                    if (!clientsAEnvoyer.contains(clientLocal)) {
                        clientsAEnvoyer.add(clientLocal);
                        Log.d(LISTE_ATTENTE, "Client local ajouté: " + clientLocal.getNom() +
                                " (avec commandes: " + aDesCommandes + ")");
                    }
                }
            }
        }

        // 2. Ensuite ajoute les clients avec commandes qui ne sont pas encore dans la liste
        //    (cela concerne principalement les clients API qui ont des commandes)
        if (toutesCommandes != null && !toutesCommandes.isEmpty()) {
            for (Commande cmd : toutesCommandes) {
                if (cmd.getClient() != null) {
                    String nomClient = cmd.getClient().getNom();

                    // Cherche le client correspondant (local ou API)
                    Client clientComplet = null;

                    // D'abord chercher dans les clients locaux
                    for (Client c : clientsLocaux) {
                        if (c.getNom().equals(nomClient)) {
                            clientComplet = c;
                            break;
                        }
                    }

                    // Si pas trouvé, chercher dans les clients API
                    if (clientComplet == null) {
                        for (Client c : clientsApi) {
                            if (c.getNom().equals(nomClient)) {
                                clientComplet = c;
                                break;
                            }
                        }
                    }

                    // Ajoute le client s'il n'est pas déjà dans la liste
                    if (clientComplet != null && !clientsAEnvoyer.contains(clientComplet)) {
                        clientsAEnvoyer.add(clientComplet);
                        Log.d(LISTE_ATTENTE, "Client avec commandes ajouté: " + clientComplet.getNom());
                    }
                }
            }
        }

        if (clientsAEnvoyer.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Aucun client ni commande à envoyer", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(LISTE_ATTENTE, "Nombre total de clients à envoyer: " + clientsAEnvoyer.size());

        // Envoi chaque client + ses commandes séquentiellement
        ClientApiRepository clientRepo = new ClientApiRepository(requireContext());
        CommandeApiRepository commandeRepo = new CommandeApiRepository(requireContext());

        envoyerClientEtCommandesRecursif(clientsAEnvoyer, 0, clientRepo, commandeRepo,
                storageLocal, commandeStorage, progressDialog, rapport);
    }

    /**
     * Envoie les clients un par un avec leurs commandes de manière récursive.
     * Les clients provenant de l'API ne sont pas envoyés (ils existent déjà dans Dolibarr),
     * seules leurs commandes sont envoyées.
     *
     * @param  :
     *         clients La liste de tous les clients à traiter (locaux + API)
     *         index L'index du client actuel dans la liste
     *         clientRepo Le repository pour envoyer les clients
     *         commandeRepo Le repository pour envoyer les commandes
     *         clientStorage Le gestionnaire de stockage pour les clients locaux
     *         commandeStorage Le gestionnaire de stockage pour les commandes locales
     *         progressDialog Le dialogue de progression à mettre à jour
     *         rapport Le rapport de synchronisation à mettre à jour avec les résultats
     *
     */
    private void envoyerClientEtCommandesRecursif(List<Client> clients, int index,
                                                  ClientApiRepository clientRepo,
                                                  CommandeApiRepository commandeRepo,
                                                  GestionnaireStockageClient clientStorage,
                                                  GestionnaireStockageCommande commandeStorage,
                                                  ProgressDialog progressDialog,
                                                  RapportSynchronisation rapport) {
        // Tous les clients ont été traités
        if (index >= clients.size()) {
            Log.d(LISTE_ATTENTE, "Tous les clients et commandes traités. Re-synchronisation...");
            resynchroniserClients(progressDialog, rapport);
            return;
        }

        Client client = clients.get(index);
        Log.d(LISTE_ATTENTE, "Traitement du client " + (index + 1) + "/" + clients.size() + ": " + client.getNom());

        progressDialog.setMessage("Traitement du client " + client.getNom() + " (" + (index + 1) + "/" + clients.size() + ")...");

        // Vérifier si le client provient de l'API (existe déjà dans Dolibarr)
        if (client.isFromApi()) {
            Log.d(LISTE_ATTENTE, VALIDE_CLIENT + client.getNom() + " provient de l'API (ID: " + client.getId() + ") - Pas d'envoi nécessaire");

            // Le client existe déjà dans Dolibarr, on utilise directement son ID
            // 1. Envoye les commandes de ce client
            envoyerCommandesDuClient(client, commandeRepo, commandeStorage, rapport, () -> {
                // 2. Pas de suppression du client car il provient de l'API (on le garde)
                Log.d(LISTE_ATTENTE, "✅ Commandes du client API " + client.getNom() + " traitées (client conservé)");

                // 3. Passe au client suivant
                envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                        clientStorage, commandeStorage, progressDialog, rapport);
            });
        } else {
            // Client local : il faut l'envoyer vers Dolibarr
            Log.d(LISTE_ATTENTE, "Envoi du client local " + client.getNom() + " vers Dolibarr...");

            // 1. Envoye le client vers Dolibarr + historique
            clientRepo.envoyerClient(client, new ClientApiRepository.ClientEnvoiCallback() {
                @Override
                public void onSuccess(String dolibarrId) {
                    Log.d(LISTE_ATTENTE, VALIDE_CLIENT + client.getNom() + " envoyé ! ID Dolibarr: " + dolibarrId);

                    rapport.ajouterClientReussi(client.getNom());

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

                    // 2. Envoye les commandes de ce client
                    envoyerCommandesDuClient(clientAvecId, commandeRepo, commandeStorage, rapport, () -> {
                        // 3. Supprime le client du stockage local après tout (avec ses commandes)
                        ServiceClient serviceClient = new ServiceClient(requireContext());
                        boolean supprime = serviceClient.deleteClient(client);

                        if (supprime) {
                            Log.d(LISTE_ATTENTE, VALIDE_CLIENT + client.getNom() + " supprimé du stockage local");
                        } else {
                            Log.w(LISTE_ATTENTE, "⚠️ Erreur suppression du client local: " + client.getNom());
                        }

                        // 4. Passe au client suivant
                        envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                                clientStorage, commandeStorage, progressDialog, rapport);
                    });
                }

                @Override
                public void onError(String message) {
                    Log.e(LISTE_ATTENTE, "❌ Erreur envoi module natif " + client.getNom() + ": " + message);

                    // Vérifier si c'est une erreur de connexion OU si l'appareil n'est pas connecté
                    boolean erreurConnexion = estErreurConnexion(message) || !estConnecteAInternet();

                    if (erreurConnexion) {
                        // Pas de tentative d'envoi vers l'historique si pas de connexion
                        Log.w(LISTE_ATTENTE, "⚠️ Pas de connexion - Pas d'envoi vers l'historique");

                        rapport.ajouterClientEchoue(client.getNom(),
                                "Pas de connexion internet (historique non enregistré)");

                        // Le client reste en local
                        Log.d(LISTE_ATTENTE, "⚠️ Client " + client.getNom() + " conservé en local");

                        // Passer au client suivant
                        envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                                clientStorage, commandeStorage, progressDialog, rapport);
                    } else {
                        // Erreur de validation : tenter d'envoyer vers l'historique avec update_date = "Non"
                        Log.d(LISTE_ATTENTE, "📤 Envoi dans l'historique malgré l'échec du module natif...");

                        clientRepo.envoyerClientVersHistorique(client, "Non", new ClientApiRepository.ClientHistoriqueCallback() {
                            @Override
                            public void onSuccess(String historiqueId) {
                                Log.d(LISTE_ATTENTE, "✅ Client " + client.getNom() + " enregistré dans l'historique (update_date=Non)");

                                rapport.ajouterClientEchoue(client.getNom(),
                                    simplifierMessageErreur(message) + " (enregistré dans l'historique pour correction)");

                                ServiceClient serviceClient = new ServiceClient(requireContext());
                                boolean supprime = serviceClient.deleteClient(client);

                                if (supprime) {
                                    Log.d(LISTE_ATTENTE, "✅ Client " + client.getNom() + " supprimé du stockage local (historique OK)");
                                } else {
                                    Log.w(LISTE_ATTENTE, "⚠️ Erreur suppression du client local: " + client.getNom());
                                }

                                // Passer au client suivant
                                envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                                        clientStorage, commandeStorage, progressDialog, rapport);
                            }

                            @Override
                            public void onError(String historiqueError) {
                                Log.e(LISTE_ATTENTE, "❌ Erreur envoi historique " + client.getNom() + ": " + historiqueError);

                                // Double échec : module natif + historique
                                // Vérifier si c'est aussi une erreur de connexion pour l'historique
                                if (estErreurConnexion(historiqueError)) {
                                    rapport.ajouterClientEchoue(client.getNom(),
                                        "Pas de connexion internet (historique non enregistré)");
                                } else {
                                    rapport.ajouterClientEchoue(client.getNom(),
                                        simplifierMessageErreur(message) + " (historique non enregistré)");
                                }

                                // Le client reste en local
                                Log.d(LISTE_ATTENTE, "⚠️ Client " + client.getNom() + " conservé en local");

                                // Continue avec le client suivant même en cas d'erreur
                                envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                                        clientStorage, commandeStorage, progressDialog, rapport);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Envoie toutes les commandes d'un client vers Dolibarr (module natif + historique).
     * Flux :
     * 1. Envoyer la commande vers le module natif → récupérer l'ID Dolibarr
     * 2. Envoyer vers l'historique avec l'ID Dolibarr
     * 3. Supprimer la commande locale
     */
    private void envoyerCommandesDuClient(Client client,
                                          CommandeApiRepository commandeRepo,
                                          GestionnaireStockageCommande commandeStorage,
                                          RapportSynchronisation rapport,
                                          Runnable onTermine) {
        // Charge toutes les commandes
        List<Commande> toutesCommandes = commandeStorage.loadCommandes();

        if (toutesCommandes == null || toutesCommandes.isEmpty()) {
            Log.d(LISTE_ATTENTE, "Aucune commande pour le client " + client.getNom());
            onTermine.run();
            return;
        }

        // Filtre les commandes de ce client (basé sur le nom du client)
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
            Log.d(LISTE_ATTENTE, "Aucune commande pour le client " + client.getNom());
            onTermine.run();
            return;
        }

        Log.d(LISTE_ATTENTE, "Envoi de " + commandesDuClient.size() + " commande(s) pour " + client.getNom());

        // Envoye les commandes une par une (module natif + historique)
        envoyerCommandesRecursif(commandesDuClient, 0, commandeRepo, commandeStorage, rapport, onTermine);
    }

    /**
     * Envoie les commandes une par une de manière récursive.
     * Flux pour chaque commande :
     * 1. Envoyer vers le module natif Dolibarr → récupérer l'ID
     * 2. Envoyer vers l'historique avec l'ID Dolibarr
     * 3. Supprimer du stockage local
     */
    private void envoyerCommandesRecursif(List<Commande> commandes, int index,
                                          CommandeApiRepository repo,
                                          GestionnaireStockageCommande storage,
                                          RapportSynchronisation rapport,
                                          Runnable onTermine) {
        if (index >= commandes.size()) {
            Log.d(LISTE_ATTENTE, "Toutes les commandes du client envoyées");
            onTermine.run();
            return;
        }

        Commande commande = commandes.get(index);
        Log.d(LISTE_ATTENTE, "Envoi commande " + (index + 1) + "/" + commandes.size() +
                " - " + commande.getLignesCommande().size() + " ligne(s)");

        // Étape 1 : Envoye vers le module natif Dolibarr
        Log.d(LISTE_ATTENTE, "📤 Étape 1/2 : Envoi vers le module natif Dolibarr...");

        repo.envoyerCommandeVersModuleNatif(commande, new CommandeApiRepository.CommandeNativeEnvoiCallback() {
            @Override
            public void onSuccess(String dolibarrCommandeId) {
                Log.d(LISTE_ATTENTE, VALIDE_COMMANDE + commande.getId() + " créée dans Dolibarr ! ID: " + dolibarrCommandeId);

                // Étape 2 : Envoye vers l'historique avec l'ID Dolibarr
                Log.d(LISTE_ATTENTE, "📤 Étape 2/2 : Envoi vers l'historique avec ID Dolibarr...");

                repo.envoyerCommandeVersHistoriqueAvecId(commande, dolibarrCommandeId, new CommandeApiRepository.CommandeEnvoiCallback() {
                    @Override
                    public void onSuccess(String historiqueId) {
                        Log.d(LISTE_ATTENTE, VALIDE_COMMANDE + commande.getId() + " envoyée vers l'historique !");

                        rapport.ajouterCommandeReussie(commande.getId());

                        // Étape 3 : Supprime la commande du stockage local
                        boolean supprime = storage.deleteCommande(commande.getId());

                        if (supprime) {
                            Log.d(LISTE_ATTENTE, VALIDE_COMMANDE + commande.getId() + " supprimée du stockage local");
                        } else {
                            Log.w(LISTE_ATTENTE, "⚠️ Erreur suppression de la commande locale: " + commande.getId());
                        }

                        // Envoi la commande suivante
                        envoyerCommandesRecursif(commandes, index + 1, repo, storage, rapport, onTermine);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(LISTE_ATTENTE, "❌ Erreur envoi historique commande " + commande.getId() + ": " + message);

                        rapport.ajouterCommandeEchouee(commande.getId(), simplifierMessageErreur(message));

                        // Continue avec la commande suivante même en cas d'erreur
                        envoyerCommandesRecursif(commandes, index + 1, repo, storage, rapport, onTermine);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e(LISTE_ATTENTE, "❌ Erreur envoi module natif commande " + commande.getId() + ": " + message);

                // Vérifier si c'est une erreur de connexion OU si l'appareil n'est pas connecté
                boolean erreurConnexion = estErreurConnexion(message) || !estConnecteAInternet();

                if (erreurConnexion) {
                    // Pas de tentative d'envoi vers l'historique si pas de connexion
                    Log.w(LISTE_ATTENTE, "⚠️ Pas de connexion - Pas d'envoi vers l'historique");

                    rapport.ajouterCommandeEchouee(commande.getId(),
                            "Pas de connexion internet (historique non enregistré)");

                    // La commande reste en local
                    Log.d(LISTE_ATTENTE, "⚠️ Commande " + commande.getId() + " conservée en local");

                    // Continuer avec la commande suivante
                    envoyerCommandesRecursif(commandes, index + 1, repo, storage, rapport, onTermine);
                } else {
                    // Erreur de validation : tenter d'envoyer vers l'historique avec update_date = "Non"
                    Log.d(LISTE_ATTENTE, "📤 Envoi dans l'historique malgré l'échec du module natif (update_date=Non)...");

                    repo.envoyerCommandeVersHistoriqueSansId(commande, new CommandeApiRepository.CommandeEnvoiCallback() {
                        @Override
                        public void onSuccess(String historiqueId) {
                            Log.d(LISTE_ATTENTE, "✅ Commande " + commande.getId() + " enregistrée dans l'historique (update_date=Non)");

                            rapport.ajouterCommandeEchouee(commande.getId(),
                                    simplifierMessageErreur(message) + " (lignes enregistrées dans l'historique pour correction)");

                            boolean supprime = storage.deleteCommande(commande.getId());

                            if (supprime) {
                                Log.d(LISTE_ATTENTE, "✅ Commande " + commande.getId() + " supprimée du stockage local (historique OK)");
                            } else {
                                Log.w(LISTE_ATTENTE, "⚠️ Erreur suppression de la commande locale: " + commande.getId());
                            }

                            // Continuer avec la commande suivante
                            envoyerCommandesRecursif(commandes, index + 1, repo, storage, rapport, onTermine);
                        }

                        @Override
                        public void onError(String historiqueError) {
                            Log.e(LISTE_ATTENTE, "❌ Erreur envoi historique commande " + commande.getId() + ": " + historiqueError);

                            // Double échec : module natif + historique
                            // Vérifier si c'est aussi une erreur de connexion pour l'historique
                            if (estErreurConnexion(historiqueError)) {
                                rapport.ajouterCommandeEchouee(commande.getId(),
                                        "Pas de connexion internet (historique non enregistré)");
                            } else {
                                rapport.ajouterCommandeEchouee(commande.getId(),
                                        simplifierMessageErreur(message) + " (historique non enregistré)");
                            }

                            // La commande reste en local
                            Log.d(LISTE_ATTENTE, "⚠️ Commande " + commande.getId() + " conservée en local");

                            // Continuer avec la commande suivante
                            envoyerCommandesRecursif(commandes, index + 1, repo, storage, rapport, onTermine);
                        }
                    });
                }
            }
        });
    }


    /**
     * Re-synchronise les clients depuis l'API Dolibarr après l'envoi.
     */
    private void resynchroniserClients(ProgressDialog progressDialog, RapportSynchronisation rapport) {
        progressDialog.setMessage("Récupération des clients depuis Dolibarr...");

        ClientApiRepository repo = new ClientApiRepository(requireContext());

        // Crée un gestionnaire pour les clients API
        GestionnaireStockageClient storageApi = new GestionnaireStockageClient(
                requireContext(),
                GestionnaireStockageClient.API_CLIENTS_FILE
        );

        repo.synchroniserDepuisApi(new ClientApiRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                Log.d(LISTE_ATTENTE, "✅ " + clients.size() + " clients récupérés depuis l'API");

                // Sauvegarde dans le fichier API
                storageApi.saveClients(clients);

                progressDialog.dismiss();

                afficherRapportSynchronisation(rapport);
            }

            @Override
            public void onError(String message) {
                Log.e(LISTE_ATTENTE, "❌ Erreur synchronisation: " + message);

                progressDialog.dismiss();

                // Converti le message d'erreur technique en message convivial
                String messageConvivial = convertirErreurEnMessageConvivial(message);

                afficherRapportAvecErreurSync(rapport, messageConvivial);
            }
        });
    }

    /**
     * Affiche le rapport de synchronisation dans une fenêtre.
     */
    private void afficherRapportSynchronisation(RapportSynchronisation rapport) {
        String titre;
        int icone;

        if (rapport.aToutReussi()) {
            titre = "✅ Synchronisation réussie";
            icone = android.R.drawable.ic_dialog_info;
        } else if (rapport.aDesErreurs()) {
            titre = "⚠️ Synchronisation partielle";
            icone = android.R.drawable.ic_dialog_alert;
        } else {
            titre = "ℹ️ Rapport de synchronisation";
            icone = android.R.drawable.ic_dialog_info;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(titre)
                .setIcon(icone)
                .setMessage(rapport.genererRapportDetaille())
                .setPositiveButton("OK", (dialog, which) -> naviguerVersAccueil())
                .setCancelable(false)
                .show();
    }

    /**
     * Affiche le rapport avec une erreur de resynchronisation.
     */
    private void afficherRapportAvecErreurSync(RapportSynchronisation rapport, String erreurSync) {
        StringBuilder message = new StringBuilder();
        message.append(rapport.genererRapportDetaille());
        message.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        message.append("⚠️ ATTENTION :\n");
        message.append("La resynchronisation des clients a échoué.\n\n");
        message.append(erreurSync);

        new AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Synchronisation avec avertissement")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(message.toString())
                .setPositiveButton("OK", (dialog, which) -> naviguerVersAccueil())
                .setNegativeButton("Réessayer", (dialog, which) -> envoyerToutVersDolibarr())
                .setCancelable(false)
                .show();
    }

    /**
     * Simplifie un message d'erreur technique pour le rendre plus lisible.
     */
    private String simplifierMessageErreur(String message) {
        if (message == null) return "Erreur inconnue";

        // Extraire uniquement le message principal sans les détails techniques
        if (message.contains(":")) {
            String[] parties = message.split(":");
            return parties[parties.length - 1].trim();
        }

        return message.length() > 100 ? message.substring(0, 100) + "..." : message;
    }

    /**
     * Navigue vers la page d'accueil (Home fragment).
     * Utilise le BottomNavigationView de l'activité parente.
     */
    private void naviguerVersAccueil() {
        if (getActivity() != null) {
            // Récupérer le BottomNavigationView depuis l'activité
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    getActivity().findViewById(R.id.bottomNavigation);

            if (bottomNav != null) {
                // Sélectionner l'item "Home" du menu
                bottomNav.setSelectedItemId(R.id.nav_home);
                Log.d(LISTE_ATTENTE, "🏠 Navigation vers la page d'accueil");
            } else {
                Log.w(LISTE_ATTENTE, "⚠️ BottomNavigationView non trouvé");
            }
        }
    }

    /**
     * Vérifie si un message d'erreur correspond à une erreur de connexion réseau.
     *
     * @param message Message d'erreur à analyser
     * @return true si c'est une erreur de connexion, false sinon
     */
    private boolean estErreurConnexion(String message) {
        if (message == null) {
            return false;
        }

        // Convertir en minuscules pour une comparaison plus souple
        String messageLower = message.toLowerCase();

        // Détection des erreurs de connexion courantes
        return messageLower.contains("unknownhostexception") ||
               messageLower.contains("unable to resolve host") ||
               messageLower.contains("sockettimeoutexception") ||
               messageLower.contains("timeout") ||
               messageLower.contains("no address associated with hostname") ||
               messageLower.contains("network is unreachable") ||
               messageLower.contains("connection refused") ||
               messageLower.contains("failed to connect") ||
               messageLower.contains("no internet") ||
               messageLower.contains("pas de connexion") ||
               messageLower.contains("connectexception") ||
               messageLower.contains("econnrefused") ||
               messageLower.contains("enetunreach") ||
               messageLower.contains("ehostunreach") ||
               messageLower.contains("network error") ||
               messageLower.contains("erreur réseau") ||
               messageLower.contains("no network") ||
               messageLower.contains("offline");
    }

    /**
     * Vérifie si l'appareil est connecté à Internet.
     *
     * @return true si connecté, false sinon
     */
    private boolean estConnecteAInternet() {
        if (getContext() == null) {
            return false;
        }

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Convertit un message d'erreur technique en message convivial pour l'utilisateur.
     * Détecte les types d'erreurs courants et fournit des explications de base.
     */
    private String convertirErreurEnMessageConvivial(String message) {
        if (message == null) {
            return "Une erreur inconnue s'est produite lors de la synchronisation.";
        }

        // Erreur de connexion réseau
        if (message.contains("UnknownHostException") || message.contains("Unable to resolve host")) {
            return "❌ Pas de connexion internet\n\n" +
                   "Vérifiez votre connexion réseau et réessayez.";
        }

        // Timeout
        if (message.contains("SocketTimeoutException") || message.contains("timeout")) {
            return "⏱️ Délai d'attente dépassé\n\n" +
                   "Le serveur met trop de temps à répondre. Vérifiez votre connexion ou réessayez plus tard.";
        }

        // Erreur d'authentification
        if (message.contains("401") || message.contains("Unauthorized")) {
            return "🔒 Erreur d'authentification\n\n" +
                   "Vos identifiants sont peut-être expirés. Reconnectez-vous.";
        }

        // Erreur serveur
        if (message.contains("500") || message.contains("Internal Server Error")) {
            return "🔧 Erreur du serveur Dolibarr\n\n" +
                   "Le serveur a rencontré une erreur. Contactez votre administrateur.";
        }

        // Message par défaut avec simplification
        return "❌ Erreur de synchronisation\n\n" + simplifierMessageErreur(message);
    }

    /**
     * Adapte pour le ViewPager2 - Gère les 2 onglets (Clients et Commandes)
     */
    private static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new TableauClientsFragment();
            } else {
                return new TableauCommandesFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // 2 onglets : Clients et Commandes
        }
    }
}
