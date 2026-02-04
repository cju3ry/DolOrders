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
import com.example.dolorders.service.ServiceClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class ListeAttenteFragment extends Fragment {

    private static final String VALIDE_CLIENT = "✅ Client ";
    private static final String VALIDE_COMMANDE = "✅ Commande ";
    private static final String LISTE_ATTENTE = "ListeAttente";


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

        // Charger TOUS les clients (locaux + API)
        GestionnaireStockageClient storageLocal = new GestionnaireStockageClient(requireContext());
        GestionnaireStockageClient storageApi = new GestionnaireStockageClient(
                requireContext(),
                GestionnaireStockageClient.API_CLIENTS_FILE
        );

        List<Client> clientsLocaux = storageLocal.loadClients();
        List<Client> clientsApi = storageApi.loadClients();

        // Charger toutes les commandes pour identifier quels clients ont des commandes
        GestionnaireStockageCommande commandeStorage = new GestionnaireStockageCommande(requireContext());
        List<Commande> toutesCommandes = commandeStorage.loadCommandes();

        // Créer une liste de tous les clients à envoyer (avec ou sans commandes)
        List<Client> clientsAEnvoyer = new ArrayList<>();

        // 1. D'abord ajouter tous les clients LOCAUX (fromApi=false) sans commandes
        if (clientsLocaux != null && !clientsLocaux.isEmpty()) {
            for (Client clientLocal : clientsLocaux) {
                if (!clientLocal.isFromApi()) {
                    // Vérifier si ce client a des commandes
                    boolean aDesCommandes = false;
                    if (toutesCommandes != null && !toutesCommandes.isEmpty()) {
                        for (Commande cmd : toutesCommandes) {
                            if (cmd.getClient() != null && cmd.getClient().getNom().equals(clientLocal.getNom())) {
                                aDesCommandes = true;
                                break;
                            }
                        }
                    }

                    // Ajouter le client local qu'il ait des commandes ou non
                    if (!clientsAEnvoyer.contains(clientLocal)) {
                        clientsAEnvoyer.add(clientLocal);
                        Log.d(LISTE_ATTENTE, "Client local ajouté: " + clientLocal.getNom() +
                                " (avec commandes: " + aDesCommandes + ")");
                    }
                }
            }
        }

        // 2. Ensuite ajouter les clients avec commandes qui ne sont pas encore dans la liste
        //    (cela concerne principalement les clients API qui ont des commandes)
        if (toutesCommandes != null && !toutesCommandes.isEmpty()) {
            for (Commande cmd : toutesCommandes) {
                if (cmd.getClient() != null) {
                    String nomClient = cmd.getClient().getNom();

                    // Chercher le client correspondant (local ou API)
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

                    // Ajouter le client s'il n'est pas déjà dans la liste
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

        // Envoyer chaque client + ses commandes séquentiellement
        ClientApiRepository clientRepo = new ClientApiRepository(requireContext());
        CommandeApiRepository commandeRepo = new CommandeApiRepository(requireContext());

        envoyerClientEtCommandesRecursif(clientsAEnvoyer, 0, clientRepo, commandeRepo,
                storageLocal, commandeStorage, progressDialog);
    }

    /**
     * Envoie les clients un par un avec leurs commandes de manière récursive.
     * Les clients provenant de l'API ne sont pas envoyés (ils existent déjà dans Dolibarr),
     * seules leurs commandes sont envoyées.
     */
    private void envoyerClientEtCommandesRecursif(List<Client> clients, int index,
                                                  ClientApiRepository clientRepo,
                                                  CommandeApiRepository commandeRepo,
                                                  GestionnaireStockageClient clientStorage,
                                                  GestionnaireStockageCommande commandeStorage,
                                                  ProgressDialog progressDialog) {
        // Tous les clients ont été traités
        if (index >= clients.size()) {
            Log.d(LISTE_ATTENTE, "Tous les clients et commandes traités. Re-synchronisation...");
            resynchroniserClients(progressDialog);
            return;
        }

        Client client = clients.get(index);
        Log.d(LISTE_ATTENTE, "Traitement du client " + (index + 1) + "/" + clients.size() + ": " + client.getNom());

        progressDialog.setMessage("Traitement du client " + client.getNom() + " (" + (index + 1) + "/" + clients.size() + ")...");

        // Vérifier si le client provient de l'API (existe déjà dans Dolibarr)
        if (client.isFromApi()) {
            Log.d(LISTE_ATTENTE, VALIDE_CLIENT + client.getNom() + " provient de l'API (ID: " + client.getId() + ") - Pas d'envoi nécessaire");

            // Le client existe déjà dans Dolibarr, on utilise directement son ID
            // 1. Envoyer les commandes de ce client
            envoyerCommandesDuClient(client, commandeRepo, commandeStorage, () -> {
                // 2. Pas de suppression du client car il provient de l'API (on le garde)
                Log.d(LISTE_ATTENTE, "✅ Commandes du client API " + client.getNom() + " traitées (client conservé)");

                // 3. Passer au client suivant
                envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                        clientStorage, commandeStorage, progressDialog);
            });
        } else {
            // Client local : il faut l'envoyer vers Dolibarr
            Log.d(LISTE_ATTENTE, "Envoi du client local " + client.getNom() + " vers Dolibarr...");

            // 1. Envoyer le client vers Dolibarr + historique
            clientRepo.envoyerClient(client, new ClientApiRepository.ClientEnvoiCallback() {
                @Override
                public void onSuccess(String dolibarrId) {
                    Log.d(LISTE_ATTENTE, VALIDE_CLIENT + client.getNom() + " envoyé ! ID Dolibarr: " + dolibarrId);

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
                        // 3. Supprimer le client du stockage local après tout (avec ses commandes)
                        ServiceClient serviceClient = new ServiceClient(requireContext());
                        boolean supprime = serviceClient.deleteClient(client);

                        if (supprime) {
                            Log.d(LISTE_ATTENTE, VALIDE_CLIENT + client.getNom() + " supprimé du stockage local");
                        } else {
                            Log.w(LISTE_ATTENTE, "⚠️ Erreur suppression du client local: " + client.getNom());
                        }

                        // 4. Passer au client suivant
                        envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                                clientStorage, commandeStorage, progressDialog);
                    });
                }

                @Override
                public void onError(String message) {
                    Log.e(LISTE_ATTENTE, "❌ Erreur envoi " + client.getNom() + ": " + message);

                    Toast.makeText(getContext(),
                            "Erreur : " + client.getNom() + " - " + message,
                            Toast.LENGTH_LONG).show();

                    // Continuer avec le client suivant même en cas d'erreur
                    envoyerClientEtCommandesRecursif(clients, index + 1, clientRepo, commandeRepo,
                            clientStorage, commandeStorage, progressDialog);
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
                                          Runnable onTermine) {
        // Charger toutes les commandes
        List<Commande> toutesCommandes = commandeStorage.loadCommandes();

        if (toutesCommandes == null || toutesCommandes.isEmpty()) {
            Log.d(LISTE_ATTENTE, "Aucune commande pour le client " + client.getNom());
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
            Log.d(LISTE_ATTENTE, "Aucune commande pour le client " + client.getNom());
            onTermine.run();
            return;
        }

        Log.d(LISTE_ATTENTE, "Envoi de " + commandesDuClient.size() + " commande(s) pour " + client.getNom());

        // Envoyer les commandes une par une (module natif + historique)
        envoyerCommandesRecursif(commandesDuClient, 0, commandeRepo, commandeStorage, onTermine);
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
                                          Runnable onTermine) {
        if (index >= commandes.size()) {
            Log.d(LISTE_ATTENTE, "Toutes les commandes du client envoyées");
            onTermine.run();
            return;
        }

        Commande commande = commandes.get(index);
        Log.d(LISTE_ATTENTE, "Envoi commande " + (index + 1) + "/" + commandes.size() +
                " - " + commande.getLignesCommande().size() + " ligne(s)");

        // Étape 1 : Envoyer vers le module natif Dolibarr
        Log.d(LISTE_ATTENTE, "📤 Étape 1/2 : Envoi vers le module natif Dolibarr...");

        repo.envoyerCommandeVersModuleNatif(commande, new CommandeApiRepository.CommandeNativeEnvoiCallback() {
            @Override
            public void onSuccess(String dolibarrCommandeId) {
                Log.d(LISTE_ATTENTE, VALIDE_COMMANDE + commande.getId() + " créée dans Dolibarr ! ID: " + dolibarrCommandeId);

                // Étape 2 : Envoyer vers l'historique avec l'ID Dolibarr
                Log.d(LISTE_ATTENTE, "📤 Étape 2/2 : Envoi vers l'historique avec ID Dolibarr...");

                repo.envoyerCommandeVersHistoriqueAvecId(commande, dolibarrCommandeId, new CommandeApiRepository.CommandeEnvoiCallback() {
                    @Override
                    public void onSuccess(String historiqueId) {
                        Log.d(LISTE_ATTENTE, VALIDE_COMMANDE + commande.getId() + " envoyée vers l'historique !");

                        // Étape 3 : Supprimer la commande du stockage local
                        boolean supprime = storage.deleteCommande(commande.getId());

                        if (supprime) {
                            Log.d(LISTE_ATTENTE, VALIDE_COMMANDE + commande.getId() + " supprimée du stockage local");
                        } else {
                            Log.w(LISTE_ATTENTE, "⚠️ Erreur suppression de la commande locale: " + commande.getId());
                        }

                        // Envoyer la commande suivante
                        envoyerCommandesRecursif(commandes, index + 1, repo, storage, onTermine);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(LISTE_ATTENTE, "❌ Erreur envoi historique commande " + commande.getId() + ": " + message);

                        Toast.makeText(getContext(),
                                "Erreur historique : " + message,
                                Toast.LENGTH_SHORT).show();

                        // Continuer avec la commande suivante même en cas d'erreur
                        envoyerCommandesRecursif(commandes, index + 1, repo, storage, onTermine);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e(LISTE_ATTENTE, "❌ Erreur envoi module natif commande " + commande.getId() + ": " + message);

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
    private void resynchroniserClients(ProgressDialog progressDialog) {
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
                Log.d(LISTE_ATTENTE, "✅ " + clients.size() + " clients récupérés depuis l'API");

                // Sauvegarder dans le fichier API
                storageApi.saveClients(clients);

                progressDialog.dismiss();

                Toast.makeText(getContext(),
                        "✅ Synchronisation terminée ! " + clients.size() + " clients récupérés",
                        Toast.LENGTH_LONG).show();

                // Naviguer vers la page d'accueil au lieu de rafraîchir les fragments
                naviguerVersAccueil();
            }

            @Override
            public void onError(String message) {
                Log.e(LISTE_ATTENTE, "❌ Erreur synchronisation: " + message);

                progressDialog.dismiss();

                // Convertir le message d'erreur technique en message convivial
                String messageConvivial = convertirErreurEnMessageConvivial(message);

                // Afficher un dialogue d'erreur au lieu d'un simple Toast
                new AlertDialog.Builder(requireContext())
                        .setTitle("❌ Erreur de synchronisation")
                        .setMessage(messageConvivial)
                        .setPositiveButton("OK", (dialog, which) ->
                                // Naviguer vers la page d'accueil même en cas d'erreur
                                naviguerVersAccueil())
                        .setNegativeButton("Réessayer", (dialog, which) ->
                                // Réessayer en relançant tout le processus
                                envoyerToutVersDolibarr())
                        .setCancelable(false)
                        .show();
            }
        });
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
     * Convertit un message d'erreur technique en message convivial pour l'utilisateur.
     * Détecte les types d'erreurs courants (connexion, timeout, authentification, etc.)
     * et retourne un message clair avec des conseils d'action.
     */
    private String convertirErreurEnMessageConvivial(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return "Une erreur inconnue s'est produite";
        }

        String lowerMessage = errorMessage.toLowerCase();

        // Détection des problèmes de connexion Internet
        if (lowerMessage.contains("unknownhostexception") ||
                lowerMessage.contains("unable to resolve host")) {
            return "🔍 Impossible de contacter le serveur Dolibarr.\n\n" +
                    "Veuillez vérifier :\n" +
                    "• Votre connexion Internet (point rouge en haut = déconnecté)\n" +
                    "• L'URL de connexion au serveur\n" +
                    "• L'état serveur";
        }

        if (lowerMessage.contains("timeout") || lowerMessage.contains("timed out")) {
            return "⏱️ Le serveur met trop de temps à répondre.\n\n" +
                    "Vérifiez :\n" +
                    "• Votre connexion Internet\n" +
                    "• Le serveur Dolibarr n'est pas surchargé";
        }

        if (lowerMessage.contains("no connection") ||
                lowerMessage.contains("no internet") ||
                lowerMessage.contains("network unavailable")) {
            return "📡 Aucune connexion Internet détectée.\n\n" +
                    "Actions :\n" +
                    "• Activez le WiFi ou les données mobiles\n" +
                    "• Vérifiez le point rouge en haut de l'écran";
        }

        if (lowerMessage.contains("connection refused")) {
            return "🚫 Connexion refusée par le serveur.\n\n" +
                    "Vérifiez :\n" +
                    "• L'URL du serveur Dolibarr\n" +
                    "• Le serveur est bien démarré";
        }

        // Erreurs d'authentification
        if (lowerMessage.contains("401") || lowerMessage.contains("unauthorized")) {
            return "🔐 Authentification échouée.\n\n" +
                    "Votre clé API est peut-être invalide ou expirée.\n" +
                    "Reconnectez-vous pour rafraîchir vos identifiants.";
        }

        // Erreurs serveur
        if (lowerMessage.contains("404") || lowerMessage.contains("not found")) {
            return "❓ Ressource introuvable sur le serveur.\n\n" +
                    "Vérifiez que l'URL du serveur Dolibarr est correcte.";
        }

        if (lowerMessage.contains("500") || lowerMessage.contains("internal server")) {
            return "⚠️ Erreur interne du serveur Dolibarr.\n\n" +
                    "Contactez l'administrateur du serveur.";
        }

        if (lowerMessage.contains("503") || lowerMessage.contains("service unavailable")) {
            return "🔧 Serveur temporairement indisponible.\n\n" +
                    "Réessayez dans quelques instants.";
        }

        // Si le message est court et ne contient pas de termes techniques, on le garde
        if (errorMessage.length() < 100 && !errorMessage.contains("Exception") &&
                !errorMessage.contains("Error") && !errorMessage.contains("error")) {
            return "❌ " + errorMessage;
        }

        // Message générique pour les autres cas
        return "❌ Erreur de communication avec le serveur.\n\n" +
                "Vérifiez votre connexion Internet et réessayez.\n\n" +
                "Détail technique : " + errorMessage;
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
