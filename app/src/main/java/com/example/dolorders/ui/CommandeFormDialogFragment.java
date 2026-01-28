package com.example.dolorders.ui;

import android.app.DatePickerDialog; // Import for DatePicker
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dolorders.objet.Commande;
import com.example.dolorders.objet.LigneCommande;
import com.example.dolorders.objet.Produit;
import com.example.dolorders.R;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
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

        // 2. Configuration Ajout Article
        if (tousLesProduits != null) {
            ArrayAdapter<Produit> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, tousLesProduits);
            autoCompleteAjoutArticle.setAdapter(adapter);

            autoCompleteAjoutArticle.setOnItemClickListener((parent, view, position, id) -> {
                Produit produitSelectionne = (Produit) parent.getItemAtPosition(position);
                ajouterArticle(produitSelectionne, containerLignes, tvTotalFinal, tvNbArticles);
                autoCompleteAjoutArticle.setText("", false);
            });
        }

        // 3. Affichage initial liste
        rafraichirListeArticles(containerLignes, tvTotalFinal, tvNbArticles);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(v)
                .setTitle("Modifier la commande")
                .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Enregistrer", null); // On gère le clic plus bas

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                // Vérification des erreurs sur tous les champs quantité/remise
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
                    android.widget.Toast.makeText(requireContext(), "Corrigez les champs en erreur avant d'enregistrer", android.widget.Toast.LENGTH_LONG).show();
                    return;
                }
                if (listener != null) {
                    listener.onCommandeEdited(dateModifiee, lignesEditees);
                }
                dialog.dismiss();
            });
        });
        return dialog;
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
            if (ligne.getProduit().getId() == produit.getId()) {
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

            tvLibelle.setText(ligne.getProduit().getLibelle());
            tvPU.setText(String.format(Locale.FRANCE, "%.2f", ligne.getProduit().getPrixUnitaire()));
            tvTotalLigne.setText(String.format(Locale.FRANCE, "%.2f €", ligne.getMontantLigne()));

            // Store object in tag
            row.setTag(ligne);

            if (!etQty.hasFocus()) etQty.setText(String.valueOf(ligne.getQuantite()));
            if (!etRem.hasFocus()) {
                if(ligne.getRemise() == (long) ligne.getRemise())
                    etRem.setText(String.valueOf((long)ligne.getRemise()));
                else
                    etRem.setText(String.valueOf(ligne.getRemise()));
            }

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
                            if (newQ != current.getQuantite()) {
                                // CRASH FIX: Pass 'row' directly instead of finding it
                                updateLigneLocale(row, current, newQ, current.getRemise());
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
                            if (newR != current.getRemise()) {
                                // CRASH FIX: Pass 'row' directly
                                updateLigneLocale(row, current, current.getQuantite(), newR);
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
    }

    // UPDATED Helper method: Accepts 'row' view directly
    private void updateLigneLocale(View row, LigneCommande oldLigne, int newQty, double newRemise) {
        int index = lignesEditees.indexOf(oldLigne);
        if (index != -1) {
            LigneCommande newLigne = new LigneCommande(oldLigne.getProduit(), newQty, newRemise);
            lignesEditees.set(index, newLigne);

            // Update tag directly on the passed view
            if (row != null) {
                row.setTag(newLigne);
            }
        }
    }

    private void recalculerTotalGlobal(TextView tvTotal) {
        double total = 0.0;
        for (LigneCommande l : lignesEditees) {
            total += l.getMontantLigne();
        }
        tvTotal.setText(String.format(Locale.FRANCE, "%.2f €", total));
    }
}

