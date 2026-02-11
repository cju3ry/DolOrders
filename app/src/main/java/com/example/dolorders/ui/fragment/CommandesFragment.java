package com.example.dolorders.ui.fragment;

import android.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dolorders.R;
import com.example.dolorders.activity.LoginActivity;
import com.example.dolorders.data.stockage.commande.GestionnaireStockageCommande;
import com.example.dolorders.objet.Client;
import com.example.dolorders.objet.Commande;
import com.example.dolorders.objet.LigneCommande;
import com.example.dolorders.objet.Produit;
import com.example.dolorders.ui.adapteur.ProduitAdapter;
import com.example.dolorders.ui.viewModel.CommandesFragmentViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommandesFragment extends Fragment {

    private static final String REGEX_MONTANT = "%.2f €";

    private CommandesFragmentViewModel viewModel;
    private GestionnaireStockageCommande commandeStorage;
    private ProduitAdapter produitAdapter;

    // Vues
    private AutoCompleteTextView autoCompleteClient;
    private AutoCompleteTextView autoCompleteArticle;
    private LinearLayout layoutArticlesSelectionnes;
    private MaterialButton btnAnnuler;
    private MaterialButton btnValider;

    private TextView tvClientAdresse;
    private TextView tvClientTel;
    private LinearLayout layoutInfosClient;
    private LinearLayout containerDetailsCommande;
    private TextView tvTotalFinal;
    private TextView tvNbArticles;
    private String nomUtilisateur;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(CommandesFragmentViewModel.class);
        commandeStorage = new GestionnaireStockageCommande(requireContext());
        nomUtilisateur = LoginActivity.getUsername(requireContext());
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
        setupListeners();

        viewModel.chargerTousLesClients(requireContext());
        viewModel.chargerProduitsDepuisCache(requireContext());
        observeViewModel();
    }

    private void setupViews(View view) {
        autoCompleteClient = view.findViewById(R.id.auto_complete_client);
        autoCompleteArticle = view.findViewById(R.id.auto_complete_article);
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
            // Trier les clients par ordre alphabétique avant de les afficher
            List<Client> sortedClients = new ArrayList<>(clients);
            sortedClients.sort((c1, c2) -> c1.getNom().compareToIgnoreCase(c2.getNom()));

            ArrayAdapter<Client> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, sortedClients);
            autoCompleteClient.setAdapter(adapter);
        });

        viewModel.getListeProduits().observe(getViewLifecycleOwner(), produits -> {
            if (produits != null) {
                if (produitAdapter == null) {
                    produitAdapter = new ProduitAdapter(requireContext(), new ArrayList<>());
                    autoCompleteArticle.setAdapter(produitAdapter);
                    autoCompleteArticle.setThreshold(1);
                }
                produitAdapter.updateProduits(produits);
            }
        });

        viewModel.getClientSelectionne().observe(getViewLifecycleOwner(), client -> {
            if (client != null) {
                autoCompleteClient.setText(client.toString(), false);
                tvClientAdresse.setText(String.format("Adresse : %s, %s %s", client.getAdresse(), client.getCodePostal(), client.getVille()));
                tvClientTel.setText(String.format("Tél : %s", client.getTelephone()));
                layoutInfosClient.setVisibility(View.VISIBLE);
                containerDetailsCommande.setVisibility(View.VISIBLE);
                btnValider.setEnabled(true);
            } else {
                autoCompleteClient.setEnabled(true);
                layoutInfosClient.setVisibility(View.GONE);
                containerDetailsCommande.setVisibility(View.GONE);
            }
        });

        viewModel.getLignesCommande().observe(getViewLifecycleOwner(), this::updateArticlesListView);
    }

    private void setupListeners() {
        autoCompleteClient.setOnItemClickListener((parent, view, position, id) -> {
            Client client = (Client) parent.getItemAtPosition(position);
            viewModel.setClientSelectionne(client);
            autoCompleteClient.clearFocus();
            fermerClavier(view);
        });


        autoCompleteArticle.setOnClickListener(v -> autoCompleteArticle.showDropDown());
        autoCompleteArticle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoCompleteArticle.showDropDown();
        });

        // --- LISTENER SELECTION ARTICLE : OUVRE POP-UP ---
        autoCompleteArticle.setOnItemClickListener((parent, view, position, id) -> {
            Produit produit = (Produit) parent.getItemAtPosition(position);

            // Ouvre la pop-up pour configurer le produit
            ouvrirPopupConfigArticle(produit, null);

            autoCompleteArticle.setText("", false);
            autoCompleteArticle.dismissDropDown();
            autoCompleteArticle.clearFocus();

            // Forcer la réinitialisation du filtre de l'adaptateur
            if (autoCompleteArticle.getAdapter() instanceof android.widget.Filterable) {
                ((android.widget.Filterable) autoCompleteArticle.getAdapter()).getFilter().filter("");
            }
        });

        btnAnnuler.setOnClickListener(v -> showCancelConfirmationDialog());
        btnValider.setOnClickListener(v -> {
            fermerClavier(v);
            if (isFormulaireValide()) {
                enregistrerCommande();
            }
        });
    }

    private void updateArticlesListView(List<LigneCommande> lignes) {
        updateTotal();

        int count = (lignes != null) ? lignes.size() : 0;
        String texteCompteur = (count <= 1) ? count + " article" : count + " articles différents";
        tvNbArticles.setText(texteCompteur);

        if (lignes == null) return;

        // Recharger toute la liste
        layoutArticlesSelectionnes.removeAllViews();
        for (LigneCommande ligne : lignes) {
            ajouterVueLigne(ligne);
        }
    }

    private void ajouterVueLigne(LigneCommande ligne) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_article_commande, layoutArticlesSelectionnes, false);

        TextView tvLibelle = row.findViewById(R.id.text_libelle_article);
        TextView tvPU = row.findViewById(R.id.text_prix_unitaire);
        TextView tvTotal = row.findViewById(R.id.text_total_ligne);
        TextView tvQty = row.findViewById(R.id.text_quantite_article);
        TextView tvRem = row.findViewById(R.id.text_remise_ligne);

        ImageButton btnEdit = row.findViewById(R.id.btn_edit_article);
        ImageButton btnDel = row.findViewById(R.id.btn_delete_article);
        View container = row.findViewById(R.id.container_ligne);

        // Affichage des données (Lecture Seule)
        tvLibelle.setText(ligne.getProduit().getLibelle());
        tvPU.setText(String.format(Locale.FRANCE, "%.2f", ligne.getProduit().getPrixUnitaire()));
        tvQty.setText(String.valueOf(ligne.getQuantite()));

        if (ligne.getRemise() == (long) ligne.getRemise())
            tvRem.setText(String.format(Locale.US, "%d%%", (long) ligne.getRemise()));
        else
            tvRem.setText(String.format(Locale.US, "%.1f%%", ligne.getRemise()));

        tvTotal.setText(String.format(Locale.FRANCE, REGEX_MONTANT, ligne.getMontantLigne()));

        btnDel.setOnClickListener(v -> viewModel.removeLigne(ligne));

        // Ouvre la pop-up avec les données existantes
        View.OnClickListener editAction = v -> ouvrirPopupConfigArticle(ligne.getProduit(), ligne);
        btnEdit.setOnClickListener(editAction);
        container.setOnClickListener(editAction);

        layoutArticlesSelectionnes.addView(row);
    }

    // --- POP-UP DE CONFIGURATION ---
    private void ouvrirPopupConfigArticle(Produit produit, @Nullable LigneCommande ligneExistante) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_config_article, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Vues de la pop-up
        TextView tvTitre = view.findViewById(R.id.tv_titre_produit);
        TextView tvPU = view.findViewById(R.id.tv_prix_unitaire);
        TextView tvTotal = view.findViewById(R.id.tv_total_ligne_config);
        TextInputEditText edtQty = view.findViewById(R.id.edt_quantite);
        TextInputEditText edtRem = view.findViewById(R.id.edt_remise);
        MaterialButton btnAnnulerConfig = view.findViewById(R.id.btn_annuler_config);
        MaterialButton btnValiderConfig = view.findViewById(R.id.btn_valider_config);

        // Initialisation
        tvTitre.setText(produit.getLibelle());
        tvPU.setText(String.format(Locale.FRANCE, "Prix unitaire : %.2f €", produit.getPrixUnitaire()));

        if (ligneExistante != null) {
            edtQty.setText(String.valueOf(ligneExistante.getQuantite()));
            if (ligneExistante.getRemise() == (long) ligneExistante.getRemise())
                edtRem.setText(String.valueOf((long) ligneExistante.getRemise()));
            else
                edtRem.setText(String.valueOf(ligneExistante.getRemise()));
        }

        // Calcul dynamique du total dans la pop-up
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
                    tvTotal.setText(String.format(Locale.FRANCE, REGEX_MONTANT, total));
                } catch (Exception e) {
                    tvTotal.setText("-");
                }
            }
        };
        edtQty.addTextChangedListener(watcher);
        edtRem.addTextChangedListener(watcher);
        // Force le calcul initial
        watcher.afterTextChanged(null);

        btnAnnulerConfig.setOnClickListener(v -> dialog.dismiss());

        btnValiderConfig.setOnClickListener(v -> {
            try {
                String sQty = edtQty.getText().toString();
                String sRem = edtRem.getText().toString();

                int qty = sQty.isEmpty() ? 0 : Integer.parseInt(sQty);
                double rem = sRem.isEmpty() ? 0.0 : Double.parseDouble(sRem);

                if (qty <= 0) {
                    edtQty.setError("Min 1");
                    return;
                }
                if (rem < 0 || rem > 100) {
                    edtRem.setError("0-100%");
                    return;
                }

                // Ajoute ou remplace la ligne dans le ViewModel
                LigneCommande nouvelleLigne = new LigneCommande(produit, qty, rem, true);
                viewModel.addLigne(nouvelleLigne);

                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Format invalide", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateTotal() {
        double total = viewModel.getTotal();
        tvTotalFinal.setText(String.format(Locale.FRANCE, REGEX_MONTANT, total));
    }

    private boolean isFormulaireValide() {
        boolean estValide = true;
        if (viewModel.getClientSelectionne().getValue() == null) {
            autoCompleteClient.setError("Client requis");
            estValide = false;
        }
        List<LigneCommande> lignes = viewModel.getLignesCommande().getValue();
        if (lignes == null || lignes.isEmpty()) {
            autoCompleteArticle.setError("Au moins un article requis");
            estValide = false;
        }
        return estValide;
    }

    private void enregistrerCommande() {
        Client client = viewModel.getClientSelectionne().getValue();
        List<LigneCommande> lignes = viewModel.getLignesCommande().getValue();

        // La date de commande est fixée au moment de la création (maintenant)
        Date dateCommande = new Date();

        try {
            Commande cmd = new Commande.Builder()
                    .setId("CMD-" + System.currentTimeMillis())
                    .setClient(client)
                    .setDateCommande(dateCommande)
                    .setLignesCommande(lignes)
                    .setUtilisateur(nomUtilisateur)
                    .build();

            boolean saved = commandeStorage.addCommande(cmd);

            if (saved) {
                Toast.makeText(getContext(), "Commande enregistrée : " + String.format(REGEX_MONTANT, cmd.getMontantTotal()), Toast.LENGTH_LONG).show();
                viewModel.clear();
                navigateToOriginFragment();
            } else {
                Toast.makeText(getContext(), "Erreur enregistrement", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(requireContext()).setMessage(e.getMessage()).setPositiveButton("OK", null).show();
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Annuler")
                .setMessage("Tout effacer ?")
                .setPositiveButton("Oui", (d, w) -> {
                    viewModel.clear();
                    navigateToOriginFragment();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void navigateToOriginFragment() {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            if (viewModel.consumeFromAccueil()) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else if (viewModel.consumeFromListeClients()) {
                bottomNav.setSelectedItemId(R.id.nav_clients);
            } else {
                bottomNav.setSelectedItemId(R.id.nav_commandes);
            }
        }
    }


    private void fermerClavier(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}