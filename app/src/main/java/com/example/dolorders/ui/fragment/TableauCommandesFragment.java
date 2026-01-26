package com.example.dolorders.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolorders.R;
import com.example.dolorders.data.stockage.commande.GestionnaireStockageCommande;
import com.example.dolorders.objet.Commande;
import com.example.dolorders.objet.Produit;
import com.example.dolorders.ui.CommandeFormDialogFragment;
import com.example.dolorders.ui.adapteur.CommandesAttenteAdapteur;

import java.util.ArrayList;
import java.util.List;

public class TabCommandesFragment extends Fragment {

    private GestionnaireStockageCommande commandeStorage;
    private CommandesAttenteAdapteur adapter;
    private List<Commande> commandes;
    private CommandeFormDialogFragment dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_commandes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commandeStorage = new GestionnaireStockageCommande(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.recycler_commandes_attente);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        commandes = new ArrayList<>();
        dialog = new CommandeFormDialogFragment();

        // Initialisation de l'adapter
        adapter = new CommandesAttenteAdapteur(commandes, new CommandesAttenteAdapteur.OnCommandeActionListener() {
            @Override
            public void onEdit(Commande commande) {
                ouvrirPopupModification(commande);
            }

            @Override
            public void onDelete(Commande commande) {
                confirmerSuppression(commande);
            }
        });

        recyclerView.setAdapter(adapter);

        chargerCommandes();
    }

    @Override
    public void onResume() {
        super.onResume();
        chargerCommandes();
    }

    private void chargerCommandes() {
        List<Commande> chargement = commandeStorage.loadCommandes();
        commandes.clear();
        if (chargement != null) {
            commandes.addAll(chargement);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void confirmerSuppression(Commande commande) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Suppression")
                .setMessage("Supprimer la commande de " + commande.getClient().getNom() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {

                    /*
                    boolean success = commandeStorage.deleteCommande(commande);

                    if (success) {
                        commandes.remove(commande);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Commande supprimée", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    }
                    */
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void ouvrirPopupModification(Commande commande) {
        dialog = CommandeFormDialogFragment.newInstance();
        dialog.setCommandeInitiale(commande);

        // IMPORTANT : On fournit la liste des produits pour l'ajout
        dialog.setListeProduits(getListeProduitsDisponibles());

        dialog.setOnCommandeEditedListener((date, lignes) -> {
            try {
                // Reconstruire la commande
                Commande updatedCommande = new Commande.Builder()
                        .setId(commande.getId())
                        .setClient(commande.getClient()) // Client inchangé
                        .setDateCommande(date)           // Date inchangée (ou modifiée si tu as activé le champ)
                        .setLignesCommande(lignes)       // Nouvelles lignes
                        .setUtilisateur(commande.getUtilisateur())
                        .build();

                // Sauvegarde
                // boolean success = commandeStorage.updateCommande(updatedCommande);

                /*
                if (success) {
                    // Mise à jour locale
                    int index = trouverIndexCommande(commande);
                    if (index != -1) {
                        commandes.set(index, updatedCommande);
                        adapter.notifyItemChanged(index);
                    }
                    Toast.makeText(getContext(), "Commande mise à jour !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Erreur sauvegarde commande", Toast.LENGTH_SHORT).show();
                }
                */

            } catch (Exception e) {
                Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        dialog.show(getParentFragmentManager(), "EditCommande");
    }

    private int trouverIndexCommande(Commande c) {
        for (int i = 0; i < commandes.size(); i++) {
            if (commandes.get(i).getId().equals(c.getId())) {
                return i;
            }
        }
        return -1;
    }

    // Méthode helper pour avoir les produits (copie de celle du ViewModel, ou à récupérer d'un Repository)
    private List<Produit> getListeProduitsDisponibles() {
        List<Produit> produits = new ArrayList<>();
        // À terme, ça devrait venir d'un ProduitStorageManager
        produits.add(new Produit(101, "Stylo Bleu", 1.50));
        produits.add(new Produit(102, "Cahier A4", 3.20));
        produits.add(new Produit(103, "Clavier USB", 25.00));
        produits.add(new Produit(104, "Souris sans fil", 18.50));
        // ... ajoute tes autres produits ici
        return produits;
    }
}