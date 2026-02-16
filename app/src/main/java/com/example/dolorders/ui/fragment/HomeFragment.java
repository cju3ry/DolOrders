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
import com.example.dolorders.ui.viewModel.ClientsFragmentViewModel;
import com.example.dolorders.ui.viewModel.CommandesFragmentViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private TextView textClients;

    private TextView textCommandes;

    private TextView textTotal;

    private android.app.ProgressDialog progressDialogClients;
    private android.app.ProgressDialog progressDialogProduits;

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
        MaterialButton btnSyncClients = view.findViewById(R.id.btnSyncClients);
        MaterialButton btnSyncProduits = view.findViewById(R.id.btnSyncProduits);
        CommandesFragmentViewModel commandesViewModel = new ViewModelProvider(requireActivity()).get(CommandesFragmentViewModel.class);
        ClientsFragmentViewModel clientsViewModel = new ViewModelProvider(requireActivity()).get(ClientsFragmentViewModel.class);

        // Récupération réelle des données
        // Charger UNIQUEMENT les clients EN ATTENTE (locaux, pas encore envoyés à Dolibarr)
        GestionnaireStockageClient gestionnaireClientLocal = new GestionnaireStockageClient(requireContext());
        int nbClientsEnAttente = gestionnaireClientLocal.loadClients().size();

        // Charger UNIQUEMENT les commandes EN ATTENTE (locales, pas encore envoyées)
        GestionnaireStockageCommande gestionnaireCommande = new GestionnaireStockageCommande(requireContext());
        final int nbCommandesEnAttente = gestionnaireCommande.loadCommandes().size();

        updateStats(nbClientsEnAttente, nbCommandesEnAttente);

        // Bouton de synchronisation des clients
        btnSyncClients.setOnClickListener(v -> {
            btnSyncClients.setEnabled(false);
            btnSyncClients.setText("Synchronisation...");

            // Créer et afficher le ProgressDialog (fermer l'ancien s'il existe)
            if (progressDialogClients != null && progressDialogClients.isShowing()) {
                progressDialogClients.dismiss();
            }
            progressDialogClients = new android.app.ProgressDialog(requireContext());
            progressDialogClients.setTitle("Synchronisation des clients");
            progressDialogClients.setMessage("Récupération des clients depuis Dolibarr...");
            progressDialogClients.setCancelable(false);
            progressDialogClients.show();

            clientsViewModel.synchroniserClientsDepuisApi(requireContext());
        });

        // Observer les ERREURS de synchronisation des clients (UNE SEULE FOIS)
        clientsViewModel.getErreurSynchronisation().observe(getViewLifecycleOwner(), erreur -> {
            if (erreur != null && !erreur.isEmpty()) {
                // Fermer le ProgressDialog
                if (progressDialogClients != null && progressDialogClients.isShowing()) {
                    progressDialogClients.dismiss();
                }

                btnSyncClients.setEnabled(true);
                btnSyncClients.setText("Synchroniser les clients");

                // Convertir l'erreur technique en message convivial
                String messageConvivial = convertirErreurEnMessageConvivial(erreur);

                // Afficher un dialogue d'erreur
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("❌ Erreur de synchronisation clients")
                        .setMessage(messageConvivial)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Réessayer", (dialog, which) -> {
                            clientsViewModel.consommerErreur();
                            btnSyncClients.performClick();
                        })
                        .show();

                clientsViewModel.consommerErreur();
            }
        });

        // Observer le SUCCÈS de la synchronisation des clients (UNE SEULE FOIS)
        clientsViewModel.getSynchronisationReussie().observe(getViewLifecycleOwner(), reussie -> {
            if (reussie != null && reussie) {
                // Fermer le ProgressDialog
                if (progressDialogClients != null && progressDialogClients.isShowing()) {
                    progressDialogClients.dismiss();
                }

                btnSyncClients.setEnabled(true);
                btnSyncClients.setText("Synchroniser les clients");

                // Récupérer le nombre de clients synchronisés depuis le LiveData dédié
                Integer nbClients = clientsViewModel.getNombreClientsSynchronises().getValue();
                int nbClientsTotal = nbClients != null ? nbClients : 0;

                Toast.makeText(requireContext(),
                        "✅ " + nbClientsTotal + " client(s) synchronisé(s) avec succès !",
                        Toast.LENGTH_LONG).show();

                // Rafraîchir les stats après synchronisation
                // Note: Les stats affichent les clients EN ATTENTE (locaux uniquement)
                // donc on ne met pas à jour ici car les clients synchronisés ne sont plus "en attente"
                // Les stats seront mises à jour au prochain rechargement du fragment

                clientsViewModel.consommerSucces();
            }
        });

        // Bouton de synchronisation des produits
        btnSyncProduits.setOnClickListener(v -> {
            btnSyncProduits.setEnabled(false);
            btnSyncProduits.setText("Synchronisation...");

            // Créer et afficher le ProgressDialog (fermer l'ancien s'il existe)
            if (progressDialogProduits != null && progressDialogProduits.isShowing()) {
                progressDialogProduits.dismiss();
            }
            progressDialogProduits = new android.app.ProgressDialog(requireContext());
            progressDialogProduits.setTitle("Synchronisation des produits");
            progressDialogProduits.setMessage("Récupération des produits depuis Dolibarr...");
            progressDialogProduits.setCancelable(false);
            progressDialogProduits.show();

            commandesViewModel.chargerProduits(requireContext());
        });

        // Observer les ERREURS de synchronisation des produits (UNE SEULE FOIS)
        commandesViewModel.getErreurSynchronisation().observe(getViewLifecycleOwner(), erreur -> {
            if (erreur != null && !erreur.isEmpty()) {
                // Fermer le ProgressDialog
                if (progressDialogProduits != null && progressDialogProduits.isShowing()) {
                    progressDialogProduits.dismiss();
                }

                btnSyncProduits.setEnabled(true);
                btnSyncProduits.setText("Synchroniser les produits");

                // Convertir l'erreur technique en message convivial
                String messageConvivial = convertirErreurEnMessageConvivial(erreur);

                // Afficher un dialogue d'erreur
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("❌ Erreur de synchronisation produits")
                        .setMessage(messageConvivial)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Réessayer", (dialog, which) -> {
                            commandesViewModel.consommerErreur();
                            btnSyncProduits.performClick();
                        })
                        .show();

                commandesViewModel.consommerErreur();
            }
        });

        // Observer le SUCCÈS de la synchronisation des produits (UNE SEULE FOIS)
        commandesViewModel.getSynchronisationReussie().observe(getViewLifecycleOwner(), reussie -> {
            if (reussie != null && reussie) {
                // Fermer le ProgressDialog
                if (progressDialogProduits != null && progressDialogProduits.isShowing()) {
                    progressDialogProduits.dismiss();
                }

                btnSyncProduits.setEnabled(true);
                btnSyncProduits.setText("Synchroniser les produits");

                // Récupérer le nombre de produits synchronisés depuis le LiveData dédié
                Integer nbProduits = commandesViewModel.getNombreProduitsSynchronises().getValue();
                int nbProduitsTotal = nbProduits != null ? nbProduits : 0;

                Toast.makeText(requireContext(),
                    "✅ " + nbProduitsTotal + " produit(s) synchronisé(s) avec succès !",
                    Toast.LENGTH_LONG).show();

                commandesViewModel.consommerSucces();
            }
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

    /**
     * Convertit un message d'erreur technique en message convivial pour l'utilisateur.
     * Utilise la même logique que ListeAttenteFragment pour la cohérence.
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
                    "• L'état du serveur";
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
}
