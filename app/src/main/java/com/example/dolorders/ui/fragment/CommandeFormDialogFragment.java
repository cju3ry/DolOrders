package com.example.dolorders.ui.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommandeFormDialogFragment extends DialogFragment {

    private Commande commandeInitiale;
    private List<LigneCommande> lignesEditees;
    private List<Produit> tousLesProduits;
    private ProduitAdapter produitAdapter;
    // New variable to track the modified date. Initialize with original date.
    private Date dateModifiee;


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
            this.dateModifiee = commande.getDateCommande(); // Init date
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

        // 1. Remplissage Info Client et Date
        if (commandeInitiale != null) {
            if (commandeInitiale.getClient() != null) {
                edtClientNom.setText(commandeInitiale.getClient().getNom());
            }
        }

        // Update date field with current stored date
        updateDateField(edtDateCommande);

        // Desactivation Client (Fixe)
        edtClientNom.setEnabled(false);

        // Activation Date (Modifiable)
        edtDateCommande.setEnabled(true);
        edtDateCommande.setFocusable(false); // No keyboard
        edtDateCommande.setClickable(true);  // Click triggers picker
        edtDateCommande.setOnClickListener(view -> showDatePicker(edtDateCommande));

        // 2. Configuration Ajout Article avec ProduitAdapter personnalisé
        if (tousLesProduits != null && !tousLesProduits.isEmpty()) {
            produitAdapter = new ProduitAdapter(requireContext(), tousLesProduits);
            autoCompleteAjoutArticle.setAdapter(produitAdapter);
            autoCompleteAjoutArticle.setThreshold(1); // Déclenche la recherche après 1 caractère

            autoCompleteAjoutArticle.setOnItemClickListener((parent, view, position, id) -> {
                Produit produitSelectionne = (Produit) parent.getItemAtPosition(position);
                ajouterArticle(produitSelectionne, containerLignes, tvTotalFinal, tvNbArticles);
                autoCompleteAjoutArticle.setText("", false);
            });
        } else {
            // Aucun produit disponible
            autoCompleteAjoutArticle.setEnabled(false);
            autoCompleteAjoutArticle.setHint("Aucun produit disponible");
        }

        // 3. Affichage initial liste
        rafraichirListeArticles(containerLignes, tvTotalFinal, tvNbArticles);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(v)
                .setTitle("Modifier la commande")
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Enregistrer", null); // On va gérer le clic manuellement

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialogInterface -> {
            android.widget.Button btnEnregistrer = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            verifierEtMettreAJourBoutonEnregistrer(btnEnregistrer, containerLignes);

            btnEnregistrer.setOnClickListener(view -> {
                // Vérification des erreurs sur tous les champs quantité/remise (fonctionnalité du Bloc 2)
                boolean hasError = false;
                for (int i = 0; i < containerLignes.getChildCount(); i++) {
                    View row = containerLignes.getChildAt(i);
                    EditText etQty = row.findViewById(R.id.edit_text_quantite_article);
                    EditText etRem = row.findViewById(R.id.edit_text_remise_ligne);
                    if ((etQty != null && etQty.getError() != null) || (etRem != null && etRem.getError() != null)) {
                        hasError = true;
                        break;
                    }
                }
                if (hasError) {
                    Toast.makeText(requireContext(),
                            "Corrigez les champs en erreur avant d'enregistrer",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (toutesLesLignesSontValidees()) {
                    if (listener != null) {
                        listener.onCommandeEdited(dateModifiee, lignesEditees);
                    }
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(requireContext(),
                            "Veuillez valider toutes les lignes avant d'enregistrer",
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        return alertDialog;
    }

    private void updateDateField(EditText edt) {
        if (dateModifiee != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            edt.setText(sdf.format(dateModifiee));
        }
    }

    private void showDatePicker(EditText edt) {
        Calendar c = Calendar.getInstance();
        c.setTime(dateModifiee != null ? dateModifiee : new Date());

        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            dateModifiee = newDate.getTime();
            updateDateField(edt);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void ajouterArticle(Produit produit, LinearLayout container, TextView tvTotal, TextView tvNb) {
        for (LigneCommande ligne : lignesEditees) {
            if (ligne.getProduit().getId().equals(produit.getId())) {
                return;
            }
        }
        lignesEditees.add(new LigneCommande(produit, 1, 0.0));
        rafraichirListeArticles(container, tvTotal, tvNb);
    }

    private void rafraichirListeArticles(LinearLayout container, TextView tvTotal, TextView tvNb) {
        container.removeAllViews();
        double total = 0.0;

        for (LigneCommande ligne : lignesEditees) {
            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_article_commande, container, false);

            TextView tvLibelle = row.findViewById(R.id.text_libelle_article);
            TextView tvPU = row.findViewById(R.id.text_prix_unitaire);
            TextView tvTotalLigne = row.findViewById(R.id.text_total_ligne);
            EditText etQty = row.findViewById(R.id.edit_text_quantite_article);
            EditText etRem = row.findViewById(R.id.edit_text_remise_ligne);
            View btnDelete = row.findViewById(R.id.btn_delete_article);
            View btnValiderLigne = row.findViewById(R.id.btn_valider_ligne);

            tvLibelle.setText(ligne.getProduit().getLibelle());
            tvPU.setText(String.format(Locale.FRANCE, "%.2f", ligne.getProduit().getPrixUnitaire()));
            tvTotalLigne.setText(String.format(Locale.FRANCE, "%.2f €", ligne.getMontantLigne()));

            // Store object in tag
            row.setTag(ligne);

            // Gérer l'état visuel selon si la ligne est validée ou non
            mettreAJourEtatVisuel(row, ligne, etQty, etRem, btnValiderLigne);

            if (!etQty.hasFocus()) etQty.setText(String.valueOf(ligne.getQuantite()));
            if (!etRem.hasFocus()) {
                if(ligne.getRemise() == (long) ligne.getRemise())
                    etRem.setText(String.valueOf((long)ligne.getRemise()));
                else
                    etRem.setText(String.valueOf(ligne.getRemise()));
            }

            // Bouton valider/dévalider la ligne
            btnValiderLigne.setOnClickListener(v -> {
                LigneCommande current = (LigneCommande) row.getTag();
                toggleValidationLigne(row, current, container, tvTotal, tvNb);

                // Mettre à jour le bouton Enregistrer après chaque validation/dévalidation
                verifierEtMettreAJourBoutonEnregistrer(getBoutonEnregistrer(), container);
            });

            btnDelete.setOnClickListener(v -> {
                lignesEditees.remove(ligne);
                rafraichirListeArticles(container, tvTotal, tvNb);
            });

            // --- Text Watchers ---
            etQty.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (etQty.hasFocus() && s.length() > 0) {
                        try {
                            int newQ = Integer.parseInt(s.toString());

                            // Validation : la quantité doit être positive
                            if (newQ <= 0) {
                                etQty.setError("Quantité min : 1");
                                return;
                            } else {
                                etQty.setError(null);
                            }

                            LigneCommande current = (LigneCommande) row.getTag();

                            // Ne pas mettre à jour si la ligne est validée
                            if (current.isValidee()) return;

                            if (newQ != current.getQuantite()) {
                                updateLigneLocale(row, current, newQ, current.getRemise(), current.isValidee());
                                tvTotalLigne.setText(String.format(Locale.FRANCE, "%.2f €", ((LigneCommande)row.getTag()).getMontantLigne()));
                                recalculerTotalGlobal(tvTotal);
                            }
                        } catch (NumberFormatException e) { }
                    }
                }
            });

            etRem.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (etRem.hasFocus() && s.length() > 0) {
                        try {
                            double newR = Double.parseDouble(s.toString());

                            // Validation : la remise doit être entre 0 et 100%
                            if (newR > 100) {
                                etRem.setError("Remise max : 100%");
                                return;
                            } else if (newR < 0) {
                                etRem.setError("Remise min : 0%");
                                return;
                            } else {
                                etRem.setError(null);
                            }

                            LigneCommande current = (LigneCommande) row.getTag();

                            // Ne pas mettre à jour si la ligne est validée
                            if (current.isValidee()) return;

                            newR = Double.parseDouble(s.toString());
                            if (newR != current.getRemise()) {
                                updateLigneLocale(row, current, current.getQuantite(), newR, current.isValidee());
                                tvTotalLigne.setText(String.format(Locale.FRANCE, "%.2f €", ((LigneCommande)row.getTag()).getMontantLigne()));
                                recalculerTotalGlobal(tvTotal);
                            }
                        } catch (NumberFormatException e) { }
                    }
                }
            });

            total += ligne.getMontantLigne();
            container.addView(row);
        }

        tvTotal.setText(String.format(Locale.FRANCE, "%.2f €", total));
        tvNb.setText(lignesEditees.size() + " articles");

        // Mettre à jour le bouton Enregistrer après rafraîchissement
        verifierEtMettreAJourBoutonEnregistrer(getBoutonEnregistrer(), container);
    }

    // UPDATED Helper method: Accepts 'row' view directly and preserves validation state
    private void updateLigneLocale(View row, LigneCommande oldLigne, int newQty, double newRemise, boolean validee) {
        int index = lignesEditees.indexOf(oldLigne);
        if (index != -1) {
            LigneCommande newLigne = new LigneCommande(oldLigne.getProduit(), newQty, newRemise, validee);
            lignesEditees.set(index, newLigne);

            // Update tag directly on the passed view
            if (row != null) {
                row.setTag(newLigne);
            }
        }
    }

    /**
     * Bascule l'état de validation d'une ligne.
     */
    private void toggleValidationLigne(View row, LigneCommande ligne, LinearLayout container, TextView tvTotal, TextView tvNb) {
        int index = lignesEditees.indexOf(ligne);
        if (index != -1) {
            // Créer une nouvelle ligne avec l'état de validation inversé
            LigneCommande nouvelleLigne = new LigneCommande(
                ligne.getProduit(),
                ligne.getQuantite(),
                ligne.getRemise(),
                !ligne.isValidee()
            );

            lignesEditees.set(index, nouvelleLigne);
            row.setTag(nouvelleLigne);

            // Mettre à jour l'état visuel
            EditText etQty = row.findViewById(R.id.edit_text_quantite_article);
            EditText etRem = row.findViewById(R.id.edit_text_remise_ligne);
            View btnValiderLigne = row.findViewById(R.id.btn_valider_ligne);

            mettreAJourEtatVisuel(row, nouvelleLigne, etQty, etRem, btnValiderLigne);
        }
    }

    /**
     * Met à jour l'apparence visuelle d'une ligne selon son état de validation.
     */
    private void mettreAJourEtatVisuel(View row, LigneCommande ligne, EditText etQty, EditText etRem, View btnValiderLigne) {
        boolean estValidee = ligne.isValidee();

        if (estValidee) {
            // Ligne validée : icône crayon orange, fond vert, champs désactivés
            ((android.widget.ImageButton) btnValiderLigne).setImageResource(R.drawable.ic_edit);
            ((android.widget.ImageButton) btnValiderLigne).setColorFilter(
                getResources().getColor(android.R.color.holo_orange_dark, null));

            etQty.setEnabled(false);
            etRem.setEnabled(false);
            etQty.setAlpha(0.6f);
            etRem.setAlpha(0.6f);

            row.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, null));
            row.getBackground().setAlpha(50);

        } else {
            // Ligne non validée : icône coche bleue, fond normal, champs activés
            ((android.widget.ImageButton) btnValiderLigne).setImageResource(R.drawable.ic_check);
            ((android.widget.ImageButton) btnValiderLigne).setColorFilter(
                getResources().getColor(R.color.blue_dolibarr, null));

            etQty.setEnabled(true);
            etRem.setEnabled(true);
            etQty.setAlpha(1.0f);
            etRem.setAlpha(1.0f);

            row.setBackgroundResource(R.drawable.border_bottom);
        }
    }

    private void recalculerTotalGlobal(TextView tvTotal) {
        double total = 0.0;
        for (LigneCommande l : lignesEditees) {
            total += l.getMontantLigne();
        }
        tvTotal.setText(String.format(Locale.FRANCE, "%.2f €", total));
    }

    /**
     * Vérifie si toutes les lignes de la commande sont validées.
     */
    private boolean toutesLesLignesSontValidees() {
        if (lignesEditees == null || lignesEditees.isEmpty()) {
            return false; // Pas de lignes = pas validé
        }

        for (LigneCommande ligne : lignesEditees) {
            if (!ligne.isValidee()) {
                return false; // Au moins une ligne non validée
            }
        }

        return true; // Toutes les lignes sont validées
    }

    /**
     * Récupère le bouton Enregistrer du dialogue (si disponible).
     */
    private android.widget.Button getBoutonEnregistrer() {
        if (getDialog() instanceof AlertDialog) {
            return ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        }
        return null;
    }

    /**
     * Vérifie l'état des lignes et met à jour le bouton Enregistrer.
     */
    private void verifierEtMettreAJourBoutonEnregistrer(android.widget.Button btnEnregistrer, LinearLayout container) {
        if (btnEnregistrer == null) return;

        boolean toutValidees = toutesLesLignesSontValidees();

        btnEnregistrer.setEnabled(toutValidees);

        // Changer visuellement le bouton selon l'état
        if (toutValidees) {
            btnEnregistrer.setAlpha(1.0f);
            btnEnregistrer.setText("Enregistrer");
        } else {
            btnEnregistrer.setAlpha(0.5f);
            int nbNonValidees = 0;
            for (LigneCommande ligne : lignesEditees) {
                if (!ligne.isValidee()) nbNonValidees++;
            }
            btnEnregistrer.setText("Enregistrer (" + nbNonValidees + " ligne(s) à valider)");
        }
    }
}

