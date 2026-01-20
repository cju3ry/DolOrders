package com.example.dolorders.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dolorders.Client;
import com.example.dolorders.Commande;
import com.example.dolorders.LigneCommande;
import com.example.dolorders.Produit;
import com.example.dolorders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommandesFragment extends Fragment {

    private CommandesFragmentViewModel viewModel;

    // Vues
    private AutoCompleteTextView autoCompleteClient, autoCompleteArticle;
    private TextInputEditText editTextDate;
    private LinearLayout layoutArticlesSelectionnes;
    private MaterialButton btnAnnuler, btnValider;

    // Infos clients & Totaux
    private TextView tvClientAdresse, tvClientTel;
    private LinearLayout layoutInfosClient, containerDetailsCommande;
    private TextView tvTotalFinal, tvNbArticles;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CommandesFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_commandes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        observeViewModel();
        setupListeners();

        if (viewModel.getClientSelectionne().getValue() == null) {
            viewModel.chargerDonneesDeTest();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            viewModel.setDate(sdf.format(new Date()));
        }
    }

    private void setupViews(View view) {
        autoCompleteClient = view.findViewById(R.id.auto_complete_client);
        autoCompleteArticle = view.findViewById(R.id.auto_complete_article);
        editTextDate = view.findViewById(R.id.edit_text_date);

        layoutArticlesSelectionnes = view.findViewById(R.id.layout_articles_selectionnes);
        btnAnnuler = view.findViewById(R.id.btn_annuler);
        btnValider = view.findViewById(R.id.btn_valider);

        tvClientAdresse = view.findViewById(R.id.tv_client_adresse);
        tvClientTel = view.findViewById(R.id.tv_client_tel);
        layoutInfosClient = view.findViewById(R.id.layout_infos_client);
        containerDetailsCommande = view.findViewById(R.id.container_details_commande);

        tvTotalFinal = view.findViewById(R.id.tv_total_final);
        tvNbArticles = view.findViewById(R.id.tv_nb_articles);
    }

    private void observeViewModel() {
        viewModel.getListeClients().observe(getViewLifecycleOwner(), clients -> {
            ArrayAdapter<Client> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, clients);
            autoCompleteClient.setAdapter(adapter);
        });

        viewModel.getListeProduits().observe(getViewLifecycleOwner(), produits -> {
            ArrayAdapter<Produit> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, produits);
            autoCompleteArticle.setAdapter(adapter);
        });

        viewModel.getClientSelectionne().observe(getViewLifecycleOwner(), client -> {
            if (client != null) {
                if (!autoCompleteClient.getText().toString().equals(client.toString())) {
                    autoCompleteClient.setText(client.toString(), false);
                }
                autoCompleteClient.setError(null);
                tvClientAdresse.setText(String.format("Adresse : %s, %s %s", client.getAdresse(), client.getCodePostal(), client.getVille()));
                tvClientTel.setText(String.format("Tél : %s", client.getTelephone()));

                layoutInfosClient.setVisibility(View.VISIBLE);
                containerDetailsCommande.setVisibility(View.VISIBLE);
                btnValider.setEnabled(true);

            } else {
                if (!autoCompleteClient.hasFocus()) {
                    autoCompleteClient.setText("", false);
                }
                layoutInfosClient.setVisibility(View.GONE);
                containerDetailsCommande.setVisibility(View.GONE);
            }
        });

        viewModel.getDate().observe(getViewLifecycleOwner(), date -> editTextDate.setText(date));

        // Observation de la liste des articles
        viewModel.getLignesCommande().observe(getViewLifecycleOwner(), this::updateArticlesListView);
    }

    private void setupListeners() {
        // --- CLIENT ---
        autoCompleteClient.setOnItemClickListener((parent, view, position, id) -> {
            Client client = (Client) parent.getItemAtPosition(position);
            viewModel.setClientSelectionne(client);
            autoCompleteClient.clearFocus();
            fermerClavier(view);
        });

        autoCompleteClient.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (autoCompleteClient.hasFocus()) {
                    viewModel.setClientSelectionne(null);
                }
            }
        });

        // --- DATE ---
        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // Afficher la liste complète au CLIC
        autoCompleteArticle.setOnClickListener(v -> autoCompleteArticle.showDropDown());

        // Afficher la liste complète au FOCUS
        autoCompleteArticle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteArticle.showDropDown();
            }
        });

        // Sélection d'un article
        autoCompleteArticle.setOnItemClickListener((parent, view, position, id) -> {
            Produit produit = (Produit) parent.getItemAtPosition(position);
            viewModel.addArticle(produit);
            autoCompleteArticle.setText("", false); // Vider le champ pour la prochaine saisie
            autoCompleteArticle.setError(null);
            autoCompleteArticle.postDelayed(() -> autoCompleteArticle.showDropDown(), 100);
        });


        // --- BOUTONS ---
        btnAnnuler.setOnClickListener(v -> showCancelConfirmationDialog());

        btnValider.setOnClickListener(v -> {
            fermerClavier(v);
            if (isFormulaireValide()) {
                enregistrerCommande();
            }
        });
    }

    private void updateArticlesListView(List<LigneCommande> lignes) {
        layoutArticlesSelectionnes.removeAllViews();
        updateTotal();

        // Compteur
        int count = (lignes != null) ? lignes.size() : 0;
        String texteCompteur;
        if (count == 0) {
            texteCompteur = "0 article";
        } else if (count == 1) {
            texteCompteur = "1 article";
        } else {
            texteCompteur = count + " articles différents";
        }
        tvNbArticles.setText(texteCompteur);

        if (lignes == null || lignes.isEmpty()) {
            return;
        }

        for (LigneCommande ligne : lignes) {
            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_article_commande, layoutArticlesSelectionnes, false);

            TextView tvLibelle = row.findViewById(R.id.text_libelle_article);
            TextView tvPU = row.findViewById(R.id.text_prix_unitaire);
            TextView tvTotal = row.findViewById(R.id.text_total_ligne);
            EditText etQty = row.findViewById(R.id.edit_text_quantite_article);
            EditText etRem = row.findViewById(R.id.edit_text_remise_ligne);
            ImageButton btnDel = row.findViewById(R.id.btn_delete_article);

            tvLibelle.setText(ligne.getProduit().getLibelle());
            tvPU.setText(String.format(Locale.FRANCE, "%.2f", ligne.getProduit().getPrixUnitaire()));
            tvTotal.setText(String.format(Locale.FRANCE, "%.2f €", ligne.getMontantLigne()));

            etQty.setTag("UPDATING");
            etQty.setText(String.valueOf(ligne.getQuantite()));
            etQty.setTag(null);

            etRem.setTag("UPDATING");
            if(ligne.getRemise() == (long) ligne.getRemise())
                etRem.setText(String.format(Locale.US, "%d", (long)ligne.getRemise()));
            else
                etRem.setText(String.format(Locale.US, "%.1f", ligne.getRemise()));
            etRem.setTag(null);

            btnDel.setOnClickListener(v -> viewModel.removeLigne(ligne));

            etQty.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (etQty.getTag() == null && s.length() > 0) {
                        try {
                            int newQ = Integer.parseInt(s.toString());
                            if (newQ != ligne.getQuantite()) {
                                viewModel.updateLigne(ligne, newQ, ligne.getRemise());
                            }
                        } catch (NumberFormatException e) { }
                    }
                }
            });

            etRem.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (etRem.getTag() == null) {
                        double newR = 0.0;
                        try {
                            if (s.length() > 0) newR = Double.parseDouble(s.toString());
                        } catch (NumberFormatException e) { }

                        if (newR != ligne.getRemise()) {
                            viewModel.updateLigne(ligne, ligne.getQuantite(), newR);
                        }
                    }
                }
            });

            layoutArticlesSelectionnes.addView(row);
        }
    }

    private void updateTotal() {
        double total = viewModel.getTotal();
        tvTotalFinal.setText(String.format(Locale.FRANCE, "%.2f €", total));
    }

    private boolean isFormulaireValide() {
        autoCompleteClient.setError(null);
        autoCompleteArticle.setError(null);
        editTextDate.setError(null);

        boolean estValide = true;

        if (viewModel.getClientSelectionne().getValue() == null) {
            autoCompleteClient.setError("Veuillez sélectionner un client");
            if (estValide) autoCompleteClient.requestFocus();
            estValide = false;
        }

        List<LigneCommande> lignes = viewModel.getLignesCommande().getValue();
        if (lignes == null || lignes.isEmpty()) {
            autoCompleteArticle.setError("Veuillez ajouter au moins un article");
            if (estValide) autoCompleteArticle.requestFocus();
            estValide = false;
        }

        String dateStr = viewModel.getDate().getValue();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            editTextDate.setError("La date est requise");
            if (estValide) editTextDate.requestFocus();
            estValide = false;
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                sdf.setLenient(false);
                sdf.parse(dateStr);
            } catch (ParseException e) {
                editTextDate.setError("Format invalide");
                if (estValide) editTextDate.requestFocus();
                estValide = false;
            }
        }
        return estValide;
    }

    private void enregistrerCommande() {
        Client client = viewModel.getClientSelectionne().getValue();
        List<LigneCommande> lignes = viewModel.getLignesCommande().getValue();

        Date dateCommande;
        try {
            dateCommande = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(viewModel.getDate().getValue());
        } catch (ParseException e) { return; }

        try {
            Commande nouvelleCommande = new Commande.Builder()
                    .setId("CMD-" + System.currentTimeMillis())
                    .setClient(client)
                    .setDateCommande(dateCommande)
                    .setLignesCommande(lignes)
                    .setRemiseGlobale(0.0) // Remise fixée à 0 car supprimée de l'UI
                    .setUtilisateur("Admin")
                    .build();

            Toast.makeText(getContext(),
                    "Commande validée ! Total : " + String.format(Locale.FRANCE, "%.2f €", nouvelleCommande.getMontantTotal()),
                    Toast.LENGTH_LONG).show();

            viewModel.clear();
            navigateToHome();

        } catch (IllegalStateException e) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Erreur de validation")
                    .setMessage(e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Annuler la saisie")
                .setMessage("Voulez-vous vraiment annuler ?")
                .setPositiveButton("Oui", (d, w) -> {
                    viewModel.clear();
                    navigateToHome();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void navigateToHome() {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            calendar.setTime(sdf.parse(viewModel.getDate().getValue()));
        } catch (Exception e) { }

        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                    viewModel.setDate(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void fermerClavier(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}