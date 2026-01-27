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
import java.util.Date;
import java.util.List;

public class TabCommandesFragment extends Fragment {

    private GestionnaireStockageCommande commandeStorage;
    private CommandesAttenteAdapteur adapter;
    private List<Commande> listeCommandes;
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

        listeCommandes = new ArrayList<>();
        dialog = new CommandeFormDialogFragment();

        // Initialisation de l'adapter
        adapter = new CommandesAttenteAdapteur(listeCommandes, new CommandesAttenteAdapteur.OnCommandeActionListener() {
            @Override
            public void onEdit(Commande commande) {
                // L'adapter nous dit qu'il faut éditer cette commande
                ouvrirPopupModification(commande);
            }

            @Override
            public void onDelete(Commande commande) {
                // L'adapter nous dit qu'il faut supprimer cette commande
                confirmerSuppression(commande);
            }
        });

        recyclerView.setAdapter(adapter);
        // chargerCommandes() est appelé dans onResume, pas besoin de le faire ici.
    }

    @Override
    public void onResume() {
        super.onResume();
        // onResume est le meilleur endroit pour rafraîchir la liste,
        // car il est appelé à chaque fois que le fragment redevient visible.
        chargerCommandes();
    }

    private void chargerCommandes() {
        List<Commande> chargement = commandeStorage.loadCommandes();
        listeCommandes.clear();
        if (chargement != null) {
            listeCommandes.addAll(chargement);
        }
        if (adapter != null) {
            // Utiliser notifyDataSetChanged ici est acceptable car on recharge toute la liste.
            adapter.notifyDataSetChanged();
        }
    }

    private void confirmerSuppression(Commande commande) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation de suppression")
                .setMessage("Supprimer la commande de " + commande.getClient().getNom() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // 1. On essaie de supprimer du fichier
                    boolean success = commandeStorage.deleteCommande(commande.getId());

                    if (success) {
                        // 2. Si la suppression a réussi, on met à jour la liste et l'UI
                        int index = trouverIndexCommandeParId(commande.getId());
                        if (index != -1) {
                            listeCommandes.remove(index);
                            // Notifie l'adapter que l'élément à cette position a été retiré
                            adapter.notifyItemRemoved(index);
                        }
                        Toast.makeText(getContext(), "Commande supprimée", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Erreur lors de la suppression de la commande", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Cette méthode est la nouvelle version, alignée sur votre logique "Client"
    private void ouvrirPopupModification(Commande commande) {
        // Crée une instance du dialogue en lui passant la commande à modifier
        dialog = CommandeFormDialogFragment.newInstance();
        dialog.setCommandeInitiale(commande); // On passe la commande initiale pour le pré-remplissage
        dialog.setListeProduits(getListeProduitsDisponibles()); // On fournit la liste des produits

        int index = trouverIndexCommandeParId(commande.getId());
        if (index == -1) {
            Toast.makeText(getContext(), "Erreur: Commande introuvable dans la liste.", Toast.LENGTH_SHORT).show();
            return; // On ne peut pas modifier une commande qui n'est pas dans la liste
        }

        // On écoute le résultat du dialogue
        dialog.setOnCommandeEditedListener((dateModifiee, lignesModifiees) -> {
            try {
                // On reconstruit l'objet Commande avec les nouvelles informations
                Commande updatedCommande = new Commande.Builder()
                        .setId(commande.getId()) // L'ID ne change pas
                        .setClient(commande.getClient()) // Le client ne change pas
                        .setDateCommande(dateModifiee) // La nouvelle date
                        .setLignesCommande(lignesModifiees) // Les nouvelles lignes de commande
                        .setUtilisateur(commande.getUtilisateur()) // L'utilisateur ne change pas
                        .build();

                // On sauvegarde la commande modifiée dans le fichier
                boolean success = commandeStorage.modifierCommande(updatedCommande);

                if (success) {
                    // Si la sauvegarde a réussi, on met à jour la liste et l'UI
                    listeCommandes.set(index, updatedCommande);
                    // Notifie l'adapter que juste cet item a changé (plus performant)
                    adapter.notifyItemChanged(index);
                    Toast.makeText(getContext(), "Commande mise à jour !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Erreur lors de la sauvegarde de la commande", Toast.LENGTH_SHORT).show();
                }

            } catch (IllegalStateException ex) {
                // Attrape les erreurs du Commande.Builder (ex: panier vide)
                Toast.makeText(requireContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // On affiche le dialogue
        dialog.show(getParentFragmentManager(), "EditCommandeDialog");
    }

    private int trouverIndexCommandeParId(String commandeId) {
        if (commandeId == null) return -1;
        for (int i = 0; i < listeCommandes.size(); i++) {
            if (commandeId.equals(listeCommandes.get(i).getId())) {
                return i;
            }
        }
        return -1; // Retourne -1 si non trouvé
    }

    // Méthode utilitaire pour fournir la liste des produits disponibles au dialogue.
    // À l'avenir, cela pourrait venir d'un ProduitStorageManager.
    private List<Produit> getListeProduitsDisponibles() {
        List<Produit> produits = new ArrayList<>();
        produits.add(new Produit(101, "Stylo Bleu", 1.50));
        produits.add(new Produit(102, "Cahier A4", 3.20));
        produits.add(new Produit(103, "Clavier USB", 25.00));
        produits.add(new Produit(104, "Souris sans fil", 18.50));
        return produits;
    }
}
