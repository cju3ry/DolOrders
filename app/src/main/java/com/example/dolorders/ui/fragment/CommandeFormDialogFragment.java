package com.example.dolorders.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dolorders.R;
import com.example.dolorders.objet.Commande;
import com.example.dolorders.objet.LigneCommande;
import com.example.dolorders.objet.Produit;
import com.example.dolorders.ui.adapteur.ProduitAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommandeFormDialogFragment extends DialogFragment {

    private Commande commandeInitiale;
    private List<LigneCommande> lignesEditees;
    private List<Produit> tousLesProduits;
    private Date dateModifiee;

    private static final String REGEX_PRIX = "%.2f €";

    public interface OnCommandeEditedListener {
        void onCommandeEdited(Date dateCommande, List<LigneCommande> lignes);
    }

    private OnCommandeEditedListener listener;

    public void setOnCommandeEditedListener(OnCommandeEditedListener listener) {
        this.listener = listener;
    }

    public void setCommandeInitiale(Commande commande) {
        this.commandeInitiale = commande;
        if (commande != null) {
            this.lignesEditees = new ArrayList<>(commande.getLignesCommande());
            this.dateModifiee = commande.getDateCommande();
        } else {
            this.lignesEditees = new ArrayList<>();
            this.dateModifiee = new Date();
        }
    }

    public void setListeProduits(List<Produit> produits) {
        this.tousLesProduits = produits;
    }

    public static CommandeFormDialogFragment newInstance() {
        return new CommandeFormDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_commande_form, null, false);

        TextInputEditText edtClientNom = v.findViewById(R.id.edtClientNom);
        TextInputEditText edtDateCommande = v.findViewById(R.id.edtDateCommande);
        AutoCompleteTextView autoCompleteAjoutArticle = v.findViewById(R.id.autoCompleteAjoutArticle);
        LinearLayout containerLignes = v.findViewById(R.id.containerLignesCommande);
        TextView tvTotalFinal = v.findViewById(R.id.tvDialogTotalFinal);
        TextView tvNbArticles = v.findViewById(R.id.tvDialogNbArticles);

        if (commandeInitiale != null && commandeInitiale.getClient() != null) {
            edtClientNom.setText(commandeInitiale.getClient().getNom());
        }
        updateDateField(edtDateCommande);

        // Champs non modifiables en mode édition
        edtClientNom.setEnabled(false);
        edtDateCommande.setEnabled(false);  // Date non modifiable

        if (tousLesProduits != null) {
            ProduitAdapter adapter = new ProduitAdapter(requireContext(), tousLesProduits);
            autoCompleteAjoutArticle.setAdapter(adapter);
            autoCompleteAjoutArticle.setThreshold(1);

            autoCompleteAjoutArticle.setOnItemClickListener((parent, view, position, id) -> {
                Produit produitSelectionne = (Produit) parent.getItemAtPosition(position);

                // Ouvre pop-up config
                ouvrirPopupConfigArticle(produitSelectionne, null, containerLignes, tvTotalFinal, tvNbArticles);

                // Vider le champ ET réinitialiser le filtre pour éviter que le filtre persiste
                autoCompleteAjoutArticle.setText("", false);
                autoCompleteAjoutArticle.dismissDropDown();
                autoCompleteAjoutArticle.clearFocus();

                // Forcer la réinitialisation du filtre de l'adaptateur
                if (autoCompleteAjoutArticle.getAdapter() instanceof Filterable) {
                    ((Filterable) autoCompleteAjoutArticle.getAdapter()).getFilter().filter("");
                }
            });
        }

        rafraichirListeArticles(containerLignes, tvTotalFinal, tvNbArticles);

        return new AlertDialog.Builder(requireContext())
                .setView(v)
                .setTitle("Modifier la commande")
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    if (listener != null) {
                        listener.onCommandeEdited(dateModifiee, lignesEditees);
                    }
                })
                .create();
    }

    private void rafraichirListeArticles(LinearLayout container, TextView tvTotal, TextView tvNb) {
        container.removeAllViews();
        double total = 0.0;

        for (LigneCommande ligne : lignesEditees) {
            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_article_commande, container, false);

            TextView tvLibelle = row.findViewById(R.id.text_libelle_article);
            TextView tvPU = row.findViewById(R.id.text_prix_unitaire);
            TextView tvTotalLigne = row.findViewById(R.id.text_total_ligne);
            TextView tvQty = row.findViewById(R.id.text_quantite_article);
            TextView tvRem = row.findViewById(R.id.text_remise_ligne);
            ImageButton btnEdit = row.findViewById(R.id.btn_edit_article);
            ImageButton btnDel = row.findViewById(R.id.btn_delete_article);
            View rowContainer = row.findViewById(R.id.container_ligne);

            tvLibelle.setText(ligne.getProduit().getLibelle());
            tvPU.setText(String.format(Locale.FRANCE, "%.2f", ligne.getProduit().getPrixUnitaire()));
            tvTotalLigne.setText(String.format(Locale.FRANCE, REGEX_PRIX, ligne.getMontantLigne()));
            tvQty.setText(String.valueOf(ligne.getQuantite()));

            if (ligne.getRemise() == (long) ligne.getRemise())
                tvRem.setText(String.format(Locale.US, "%d%%", (long) ligne.getRemise()));
            else
                tvRem.setText(String.format(Locale.US, "%.1f%%", ligne.getRemise()));

            btnDel.setOnClickListener(v -> {
                // Pop-up de confirmation avant suppression
                new AlertDialog.Builder(requireContext())
                        .setTitle("Supprimer le produit")
                        .setMessage("Êtes-vous sûr de vouloir supprimer \"" + ligne.getProduit().getLibelle() + "\" de la commande ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            lignesEditees.remove(ligne);
                            rafraichirListeArticles(container, tvTotal, tvNb);
                        })
                        .setNegativeButton("Annuler", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            });

            View.OnClickListener editAction = v ->
                    ouvrirPopupConfigArticle(ligne.getProduit(), ligne, container, tvTotal, tvNb);
            btnEdit.setOnClickListener(editAction);
            rowContainer.setOnClickListener(editAction);

            total += ligne.getMontantLigne();
            container.addView(row);
        }

        tvTotal.setText(String.format(Locale.FRANCE, REGEX_PRIX, total));
        tvNb.setText(lignesEditees.size() + " articles");
    }

    private void ouvrirPopupConfigArticle(Produit produit, @Nullable LigneCommande ligneExistante,
                                          LinearLayout container, TextView tvTotal, TextView tvNb) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_config_article, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Vues dialog
        TextView tvTitre = view.findViewById(R.id.tv_titre_produit);
        TextView tvPU = view.findViewById(R.id.tv_prix_unitaire);
        TextView tvTotalConfig = view.findViewById(R.id.tv_total_ligne_config);
        TextInputEditText edtQty = view.findViewById(R.id.edt_quantite);
        TextInputEditText edtRem = view.findViewById(R.id.edt_remise);
        MaterialButton btnAnnuler = view.findViewById(R.id.btn_annuler_config);
        MaterialButton btnValider = view.findViewById(R.id.btn_valider_config);

        tvTitre.setText(produit.getLibelle());
        tvPU.setText(String.format(Locale.FRANCE, "Prix unitaire : %.2f €", produit.getPrixUnitaire()));

        if (ligneExistante != null) {
            edtQty.setText(String.valueOf(ligneExistante.getQuantite()));
            if (ligneExistante.getRemise() == (long) ligneExistante.getRemise())
                edtRem.setText(String.valueOf((long) ligneExistante.getRemise()));
            else
                edtRem.setText(String.valueOf(ligneExistante.getRemise()));
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Pas utilisé : nécessaire pour implémenter TextWatcher
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Pas utilisé : nécessaire pour implémenter TextWatcher
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String sQty = edtQty.getText().toString();
                    String sRem = edtRem.getText().toString();
                    int qty = sQty.isEmpty() ? 0 : Integer.parseInt(sQty);
                    double rem = sRem.isEmpty() ? 0.0 : Double.parseDouble(sRem);
                    double total = (produit.getPrixUnitaire() * qty) * (1 - rem / 100);
                    tvTotalConfig.setText(String.format(Locale.FRANCE, REGEX_PRIX, total));
                } catch (Exception e) {
                    tvTotalConfig.setText("-");
                }
            }
        };
        edtQty.addTextChangedListener(watcher);
        edtRem.addTextChangedListener(watcher);
        watcher.afterTextChanged(null);

        btnAnnuler.setOnClickListener(v -> dialog.dismiss());
        btnValider.setOnClickListener(v -> {
            // 1. Reset erreurs
            edtQty.setError(null);
            edtRem.setError(null);

            boolean erreur = false;
            View focusView = null;

            try {
                String sQty = edtQty.getText().toString();
                String sRem = edtRem.getText().toString();

                int qty = sQty.isEmpty() ? 0 : Integer.parseInt(sQty);
                double rem = sRem.isEmpty() ? 0.0 : Double.parseDouble(sRem);

                // 2. Vérifications (Bas vers Haut)

                // Remise
                if (rem < 0 || rem > 100) {
                    edtRem.setError("Doit être entre 0 et 100%");
                    focusView = edtRem;
                    erreur = true;
                }

                // Quantité
                if (qty <= 0) {
                    edtQty.setError("Min 1");
                    focusView = edtQty;
                    erreur = true;
                }

                // 3. Gestion Erreur & Focus
                if (erreur) {
                    if (focusView != null) {
                        focusView.requestFocus();
                        // Ouvrir le clavier
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(focusView, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                    return;
                }

                // Validation réussie
                LigneCommande newLine = new LigneCommande(produit, qty, rem, true);

                if (ligneExistante != null) {
                    int index = lignesEditees.indexOf(ligneExistante);
                    if (index != -1) lignesEditees.set(index, newLine);
                } else {
                    lignesEditees.add(newLine);
                }

                rafraichirListeArticles(container, tvTotal, tvNb);
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Erreur saisie", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateDateField(EditText edt) {
        if (dateModifiee != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            edt.setText(sdf.format(dateModifiee));
        }
    }
}