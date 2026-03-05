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
        // Charge UNIQUEMENT les clients EN ATTENTE (locaux, pas encore envoyés à Dolibarr)
        GestionnaireStockageClient gestionnaireClientLocal = new GestionnaireStockageClient(requireContext());
        int nbClientsEnAttente = gestionnaireClientLocal.loadClients().size();

        // Charge UNIQUEMENT les commandes EN ATTENTE (locales, pas encore envoyées)
        GestionnaireStockageCommande gestionnaireCommande = new GestionnaireStockageCommande(requireContext());
        final int nbCommandesEnAttente = gestionnaireCommande.loadCommandes().size();

        updateStats(nbClientsEnAttente, nbCommandesEnAttente);

        // Bouton de synchronisation des clients
        btnSyncClients.setOnClickListener(v -> {
            btnSyncClients.setEnabled(false);
            btnSyncClients.setText("Synchronisation...");

            // Réinitialise les états avant de lancer la synchronisation
            clientsViewModel.consommerErreur();
            clientsViewModel.consommerSucces();

            // Crée et afficher le ProgressDialog avec bouton Annuler
            android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(requireContext());
            progressDialog.setTitle("Synchronisation des clients");
            progressDialog.setMessage("Récupération des clients depuis Dolibarr...");
            progressDialog.setCancelable(true);

            // Variables pour stocker les observers (pour pouvoir les supprimer)
            final androidx.lifecycle.Observer<String>[] erreurObserverHolder = new androidx.lifecycle.Observer[1];
            final androidx.lifecycle.Observer<Boolean>[] succesObserverHolder = new androidx.lifecycle.Observer[1];

            // Fonction pour nettoyer les observers
            Runnable cleanupObservers = () -> {
                if (erreurObserverHolder[0] != null) {
                    clientsViewModel.getErreurSynchronisation().removeObserver(erreurObserverHolder[0]);
                }
                if (succesObserverHolder[0] != null) {
                    clientsViewModel.getSynchronisationReussie().removeObserver(succesObserverHolder[0]);
                }
            };

            progressDialog.setButton(android.app.ProgressDialog.BUTTON_NEGATIVE, "Annuler", (dialog, which) -> {
                cleanupObservers.run();
                dialog.dismiss();
                btnSyncClients.setEnabled(true);
                btnSyncClients.setText("Synchroniser les clients");
                Toast.makeText(requireContext(), "Synchronisation annulée", Toast.LENGTH_SHORT).show();
            });
            progressDialog.setOnCancelListener(dialog -> {
                cleanupObservers.run();
                btnSyncClients.setEnabled(true);
                btnSyncClients.setText("Synchroniser les clients");
            });
            progressDialog.show();

            // Observe pour les ERREURS - usage unique
            erreurObserverHolder[0] = erreur -> {
                if (erreur != null && !erreur.isEmpty()) {
                    // Nettoye les observers
                    cleanupObservers.run();
                    clientsViewModel.consommerErreur();

                    // Ferme le ProgressDialog
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    btnSyncClients.setEnabled(true);
                    btnSyncClients.setText("Synchroniser les clients");

                    // Converti l'erreur technique en message convivial
                    String messageConvivial = convertirErreurEnMessageConvivial(erreur);

                    // Affiche un dialogue d'erreur - juste OK
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("❌ Erreur de synchronisation clients")
                            .setMessage(messageConvivial)
                            .setPositiveButton("OK", null)
                            .show();
                }
            };

            // Observe pour le SUCCÈS - usage unique
            succesObserverHolder[0] = reussie -> {
                if (reussie != null && reussie) {
                    // Nettoyer les observers
                    cleanupObservers.run();
                    clientsViewModel.consommerSucces();

                    // Ferme le ProgressDialog
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    btnSyncClients.setEnabled(true);
                    btnSyncClients.setText("Synchroniser les clients");

                    // Récupérer le nombre de clients synchronisés
                    Integer nbClients = clientsViewModel.getNombreClientsSynchronises().getValue();
                    int nbClientsTotal = nbClients != null ? nbClients : 0;

                    Toast.makeText(requireContext(),
                            "✅ " + nbClientsTotal + " client(s) synchronisé(s) avec succès !",
                            Toast.LENGTH_LONG).show();
                }
            };

            // Attache les observers AVANT de lancer la synchronisation
            clientsViewModel.getErreurSynchronisation().observe(getViewLifecycleOwner(), erreurObserverHolder[0]);
            clientsViewModel.getSynchronisationReussie().observe(getViewLifecycleOwner(), succesObserverHolder[0]);

            // Lance la synchronisation
            clientsViewModel.synchroniserClientsDepuisApi(requireContext());
        });

        // Bouton de synchronisation des produits
        btnSyncProduits.setOnClickListener(v -> {
            btnSyncProduits.setEnabled(false);
            btnSyncProduits.setText("Synchronisation...");

            // Réinitialise les états avant de lancer la synchronisation
            commandesViewModel.consommerErreur();
            commandesViewModel.consommerSucces();

            // Crée et afficher le ProgressDialog avec bouton Annuler
            android.app.ProgressDialog progressDialogProduits = new android.app.ProgressDialog(requireContext());
            progressDialogProduits.setTitle("Synchronisation des produits");
            progressDialogProduits.setMessage("Récupération des produits depuis Dolibarr...");
            progressDialogProduits.setCancelable(true);

            // Variables pour stocker les observers (pour pouvoir les supprimer)
            final androidx.lifecycle.Observer<String>[] erreurObserverHolder = new androidx.lifecycle.Observer[1];
            final androidx.lifecycle.Observer<Boolean>[] succesObserverHolder = new androidx.lifecycle.Observer[1];

            // Fonction pour nettoyer les observers
            Runnable cleanupObservers = () -> {
                if (erreurObserverHolder[0] != null) {
                    commandesViewModel.getErreurSynchronisation().removeObserver(erreurObserverHolder[0]);
                }
                if (succesObserverHolder[0] != null) {
                    commandesViewModel.getSynchronisationReussie().removeObserver(succesObserverHolder[0]);
                }
            };

            progressDialogProduits.setButton(android.app.ProgressDialog.BUTTON_NEGATIVE, "Annuler", (dialog, which) -> {
                cleanupObservers.run();
                dialog.dismiss();
                btnSyncProduits.setEnabled(true);
                btnSyncProduits.setText("Synchroniser les produits");
                Toast.makeText(requireContext(), "Synchronisation annulée", Toast.LENGTH_SHORT).show();
            });
            progressDialogProduits.setOnCancelListener(dialog -> {
                cleanupObservers.run();
                btnSyncProduits.setEnabled(true);
                btnSyncProduits.setText("Synchroniser les produits");
            });
            progressDialogProduits.show();

            // Observe pour les ERREURS - usage unique
            erreurObserverHolder[0] = erreur -> {
                if (erreur != null && !erreur.isEmpty()) {
                    // Nettoye les observers
                    cleanupObservers.run();
                    commandesViewModel.consommerErreur();

                    // Ferme le ProgressDialog
                    if (progressDialogProduits.isShowing()) {
                        progressDialogProduits.dismiss();
                    }

                    btnSyncProduits.setEnabled(true);
                    btnSyncProduits.setText("Synchroniser les produits");

                    // Convertir l'erreur technique en message convivial
                    String messageConvivial = convertirErreurEnMessageConvivial(erreur);

                    // Affiche un dialogue d'erreur - juste OK
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("❌ Erreur de synchronisation produits")
                            .setMessage(messageConvivial)
                            .setPositiveButton("OK", null)
                            .show();
                }
            };

            // Observe pour le SUCCÈS - usage unique
            succesObserverHolder[0] = reussie -> {
                if (reussie != null && reussie) {
                    // Nettoyer les observers
                    cleanupObservers.run();
                    commandesViewModel.consommerSucces();

                    // Ferme le ProgressDialog
                    if (progressDialogProduits.isShowing()) {
                        progressDialogProduits.dismiss();
                    }

                    btnSyncProduits.setEnabled(true);
                    btnSyncProduits.setText("Synchroniser les produits");

                    // Récupére le nombre de produits synchronisés
                    Integer nbProduits = commandesViewModel.getNombreProduitsSynchronises().getValue();
                    int nbProduitsTotal = nbProduits != null ? nbProduits : 0;

                    Toast.makeText(requireContext(),
                            "✅ " + nbProduitsTotal + " produit(s) synchronisé(s) avec succès !",
                            Toast.LENGTH_LONG).show();
                }
            };

            // Attache les observers AVANT de lancer la synchronisation
            commandesViewModel.getErreurSynchronisation().observe(getViewLifecycleOwner(), erreurObserverHolder[0]);
            commandesViewModel.getSynchronisationReussie().observe(getViewLifecycleOwner(), succesObserverHolder[0]);

            // Lance la synchronisation
            commandesViewModel.chargerProduits(requireContext());
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
