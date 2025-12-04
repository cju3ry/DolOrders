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
import com.example.dolorders.Produit;
import com.example.dolorders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandesFragment extends Fragment {

    private CommandesFragmentViewModel viewModel;

    // Vues
    private AutoCompleteTextView autoCompleteClient, autoCompleteArticle;
    private TextInputEditText editTextDate, editTextRemise;
    private LinearLayout layoutArticlesSelectionnes;
    private MaterialButton btnAnnuler, btnValider;

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
        editTextRemise = view.findViewById(R.id.edit_text_remise);
        layoutArticlesSelectionnes = view.findViewById(R.id.layout_articles_selectionnes);
        btnAnnuler = view.findViewById(R.id.btn_annuler);
        btnValider = view.findViewById(R.id.btn_valider);
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
                autoCompleteClient.setText(client.toString(), false);
                autoCompleteClient.setError(null);
            } else {
                autoCompleteClient.setText("", false);
            }
        });

        viewModel.getDate().observe(getViewLifecycleOwner(), date -> editTextDate.setText(date));

        viewModel.getRemise().observe(getViewLifecycleOwner(), remise -> {
            if (remise != null && !remise.equals(editTextRemise.getText().toString())) {
                editTextRemise.setText(remise);
            }
        });

        viewModel.getArticles().observe(getViewLifecycleOwner(), this::updateArticlesListView);
    }

    private void setupListeners() {
        autoCompleteClient.setOnItemClickListener((parent, view, position, id) -> {
            Client client = (Client) parent.getItemAtPosition(position);
            viewModel.setClientSelectionne(client);
        });

        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        autoCompleteArticle.setOnItemClickListener((parent, view, position, id) -> {
            Produit produit = (Produit) parent.getItemAtPosition(position);
            viewModel.addArticle(produit);
            autoCompleteArticle.setText("", false);
            autoCompleteArticle.setError(null);
        });

        editTextRemise.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { viewModel.setRemise(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAnnuler.setOnClickListener(v -> showCancelConfirmationDialog());

        btnValider.setOnClickListener(v -> {
            fermerClavier(v);
            if (isFormulaireValide()) {
                enregistrerCommande();
            }
        });
    }

    private void updateArticlesListView(Map<Produit, Integer> articles) {
        layoutArticlesSelectionnes.removeAllViews();
        if (articles.isEmpty()) {
            return;
        }

        btnValider.setEnabled(viewModel.getClientSelectionne().getValue() != null);

        List<Produit> produitsList = new ArrayList<>(articles.keySet());

        for (Produit produit : produitsList) {
            Integer quantite = articles.get(produit);
            if (quantite == null) continue;

            View articleView = LayoutInflater.from(getContext()).inflate(R.layout.item_article_commande, layoutArticlesSelectionnes, false);

            TextView textLibelle = articleView.findViewById(R.id.text_libelle_article);
            EditText editTextQuantite = articleView.findViewById(R.id.edit_text_quantite_article);
            TextView textPrix = articleView.findViewById(R.id.text_prix_article);
            ImageButton btnDelete = articleView.findViewById(R.id.btn_delete_article);
            ImageButton btnMinus = articleView.findViewById(R.id.btn_minus);
            ImageButton btnPlus = articleView.findViewById(R.id.btn_plus);

            textLibelle.setText(produit.getNom());
            editTextQuantite.setText(String.valueOf(quantite));
            textPrix.setText(String.format(Locale.FRANCE, "%.2f €/u", produit.getPrixUnitaire()));

            btnDelete.setOnClickListener(v -> viewModel.removeArticle(produit));
            btnMinus.setOnClickListener(v -> viewModel.updateQuantity(produit, quantite - 1));
            btnPlus.setOnClickListener(v -> viewModel.updateQuantity(produit, quantite + 1));

            editTextQuantite.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0) {
                        try {
                            int newQuantity = Integer.parseInt(s.toString());
                            if (newQuantity != quantite) {
                                viewModel.updateQuantity(produit, newQuantity);
                            }
                        } catch (NumberFormatException e) { /* Ignoré */ }
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            layoutArticlesSelectionnes.addView(articleView);
        }
    }

    private boolean isFormulaireValide() {
        autoCompleteClient.setError(null);
        autoCompleteArticle.setError(null);
        editTextDate.setError(null);
        editTextRemise.setError(null);

        boolean estValide = true;

        // Vérification du client
        if (viewModel.getClientSelectionne().getValue() == null) {
            autoCompleteClient.setError("Veuillez sélectionner un client");
            if (estValide) {
                autoCompleteClient.requestFocus();
            }
            estValide = false;
        }

        // Vérification des articles
        Map<Produit, Integer> articles = viewModel.getArticles().getValue();
        if (articles == null || articles.isEmpty()) {
            autoCompleteArticle.setError("Veuillez ajouter au moins un article");
            if (estValide) {
                autoCompleteArticle.requestFocus();
            }
            estValide = false;
        }

        // Vérification de la date
        String dateStr = viewModel.getDate().getValue();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            editTextDate.setError("La date de la commande est requise"); // Changé
            if (estValide) {
                editTextDate.requestFocus();
            }
            estValide = false;
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                sdf.setLenient(false);
                sdf.parse(dateStr);
            } catch (ParseException e) {
                editTextDate.setError("Le format de la date est invalide (jj/mm/aaaa)"); // Changé
                if (estValide) {
                    editTextDate.requestFocus();
                }
                estValide = false;
            }
        }

        // Vérification sur la remise
        String remiseStr = editTextRemise.getText().toString();
        if (!remiseStr.isEmpty()) {
            try {
                double remise = Double.parseDouble(remiseStr);
                if (remise < 0) {
                    editTextRemise.setError("La remise ne peut pas être négative");
                    if (estValide) {
                        editTextRemise.requestFocus();
                    }
                    estValide = false;
                }
            } catch (NumberFormatException e) {
                editTextRemise.setError("La valeur de la remise est invalide");
                if (estValide) {
                    editTextRemise.requestFocus();
                }
                estValide = false;
            }
        }

        return estValide;
    }

    private void enregistrerCommande() {
        Client client = viewModel.getClientSelectionne().getValue();
        Map<Produit, Integer> articles = viewModel.getArticles().getValue();
        Date dateCommande = null;
        try {
            dateCommande = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(viewModel.getDate().getValue());
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Erreur de date inattendue", Toast.LENGTH_SHORT).show();
            return;
        }

        double remise = 0.0;
        String remiseStr = editTextRemise.getText().toString();
        if (!remiseStr.isEmpty()) {
            remise = Double.parseDouble(remiseStr);
        }

        try {
            Commande nouvelleCommande = new Commande.Builder()
                    .setClient(client)
                    .setDateCommande(dateCommande)
                    .setProduitsEtQuantites(articles)
                    .setRemise(remise)
                    .setUtilisateur("Admin") // TODO: Remplacer par l'utilisateur connecté
                    .build();

            Toast.makeText(getContext(), "Commande validée ! Montant total : " + String.format(Locale.FRANCE, "%.2f €", nouvelleCommande.getMontantTotal()), Toast.LENGTH_LONG).show();

            viewModel.clear();
            navigateToHome();

        } catch (IllegalStateException e) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Erreur de validation Modèle")
                    .setMessage(e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Annuler la saisie")
                .setMessage("Voulez-vous vraiment annuler ? Toutes les données saisies seront perdues.")
                .setPositiveButton("Oui, annuler", (dialog, which) -> {
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
                    editTextDate.setError(null);
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
}
