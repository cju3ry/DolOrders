package com.example.dolorders.ui.adapteur;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dolorders.R;
import com.example.dolorders.objet.Produit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter personnalisé pour afficher les produits dans un AutoCompleteTextView.
 * Supporte le filtrage par nom et description.
 */
public class ProduitAdapter extends ArrayAdapter<Produit> implements Filterable {

    private final List<Produit> produitsComplet;  // Liste originale complète
    private List<Produit> produitsFiltres;        // Liste filtrée affichée

    public ProduitAdapter(@NonNull Context context, @NonNull List<Produit> produits) {
        super(context, 0, produits);
        this.produitsComplet = new ArrayList<>(produits);
        this.produitsFiltres = new ArrayList<>(produits);
    }

    @Override
    public int getCount() {
        return produitsFiltres.size();
    }

    @Nullable
    @Override
    public Produit getItem(int position) {
        return produitsFiltres.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_produit_dropdown, parent, false);
        }

        TextView tvNom = convertView.findViewById(R.id.tv_produit_nom);
        TextView tvDescription = convertView.findViewById(R.id.tv_produit_description);

        Produit produit = getItem(position);
        if (produit != null) {
            // Afficher : Nom - Prix€
            String displayText = String.format(Locale.FRANCE, "%s - %.2f€",
                    produit.getLibelle(), produit.getPrixUnitaire());
            tvNom.setText(displayText);

            // Afficher la description si elle existe et n'est pas vide
            if (produit.getDescription() != null && !produit.getDescription().trim().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(produit.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Produit> suggestions = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    // Pas de filtre, afficher tous les produits
                    suggestions.addAll(produitsComplet);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    // Filtrer par libellé ou description
                    for (Produit produit : produitsComplet) {
                        boolean matchLibelle = produit.getLibelle() != null &&
                                produit.getLibelle().toLowerCase().contains(filterPattern);

                        boolean matchDescription = produit.getDescription() != null &&
                                produit.getDescription().toLowerCase().contains(filterPattern);

                        if (matchLibelle || matchDescription) {
                            suggestions.add(produit);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                produitsFiltres.clear();
                if (results.values != null) {
                    produitsFiltres.addAll((List<Produit>) results.values);
                }
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                // Affiche le libellé du produit dans le champ de texte après sélection
                return resultValue instanceof Produit
                        ? ((Produit) resultValue).getLibelle()
                        : "";
            }
        };
    }

    /**
     * Met à jour la liste complète des produits et rafraîchit l'affichage.
     */
    public void updateProduits(List<Produit> nouveauxProduits) {
        produitsComplet.clear();
        produitsFiltres.clear();

        if (nouveauxProduits != null) {
            produitsComplet.addAll(nouveauxProduits);
            produitsFiltres.addAll(nouveauxProduits);
        }

        notifyDataSetChanged();
    }
}

