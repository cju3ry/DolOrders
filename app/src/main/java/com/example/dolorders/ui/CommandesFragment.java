package com.example.dolorders.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
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
        // Clients
        viewModel.getListeClients().observe(getViewLifecycleOwner(), clients -> {
            ArrayAdapter<Client> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, clients);
            autoCompleteClient.setAdapter(adapter);
        });

        // Produits
        viewModel.getListeProduits().observe(getViewLifecycleOwner(), produits -> {
            ArrayAdapter<Produit> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, produits);
            autoCompleteArticle.setAdapter(adapter);
        });

        // Client sélectionné
        viewModel.getClientSelectionne().observe(getViewLifecycleOwner(), client -> {
            if (client != null) {
                autoCompleteClient.setText(client.toString(), false);
                autoCompleteClient.setEnabled(false); // Bloqué
                autoCompleteClient.setTextColor(Color.BLACK);
                autoCompleteClient.setError(null);

                tvClientAdresse.setText(String.format("Adresse : %s, %s %s", client.getAdresse(), client.getCodePostal(), client.getVille()));
                tvClientTel.setText(String.format("Tél : %s", client.getTelephone()));

                layoutInfosClient.setVisibility(View.VISIBLE);
                containerDetailsCommande.setVisibility(View.VISIBLE);
                btnValider.setEnabled(true);
            } else {
                autoCompleteClient.setEnabled(true); // Débloqué
                autoCompleteClient.setText("", false);

                layoutInfosClient.setVisibility(View.GONE);
                containerDetailsCommande.setVisibility(View.GONE);
            }
        });

        // Date
        viewModel.getDate().observe(getViewLifecycleOwner(), date -> editTextDate.setText(date));

        // Articles
        viewModel.getLignesCommande().observe(getViewLifecycleOwner(), this::updateArticlesListView);
    }

    private void setupListeners() {
        // Client
        autoCompleteClient.setOnItemClickListener((parent, view, position, id) -> {
            Client client = (Client) parent.getItemAtPosition(position);
            viewModel.setClientSelectionne(client);
            autoCompleteClient.clearFocus();
            fermerClavier(view);
        });

        // Date
        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // Article
        autoCompleteArticle.setOnClickListener(v -> autoCompleteArticle.showDropDown());
        autoCompleteArticle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoCompleteArticle.showDropDown();
        });

        autoCompleteArticle.setOnItemClickListener((parent, view, position, id) -> {
            Produit produit = (Produit) parent.getItemAtPosition(position);
            viewModel.addArticle(produit);
            autoCompleteArticle.setText("", false);
            autoCompleteArticle.setError(null);
            autoCompleteArticle.postDelayed(() -> autoCompleteArticle.showDropDown(), 100);
        });

        // Boutons
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

        // Compteur (inchangé)
        int count = (lignes != null) ? lignes.size() : 0;
        String texteCompteur = (count <= 1) ? count + " article" : count + " articles différents";
        tvNbArticles.setText(texteCompteur);

        if (lignes == null) return;

        // Si le nombre de lignes change, on refait tout (plus simple)
        if (layoutArticlesSelectionnes.getChildCount() != lignes.size()) {
            layoutArticlesSelectionnes.removeAllViews();
            for (LigneCommande ligne : lignes) {
                ajouterVueLigne(ligne);
            }
        } else {
            // Sinon on met à jour intelligemment
            for (int i = 0; i < lignes.size(); i++) {
                View row = layoutArticlesSelectionnes.getChildAt(i);
                LigneCommande nouvelleDonnee = lignes.get(i);

                // On récupère l'ancienne donnée stockée sur la vue
                LigneCommande ancienneDonnee = (LigneCommande) row.getTag();

                // On vérifie si c'est bien le même produit (par ID)
                if (ancienneDonnee != null && ancienneDonnee.getProduit().getId() == nouvelleDonnee.getProduit().getId()) {
                    mettreAJourVueLigne(row, nouvelleDonnee);
                } else {
                    // L'ordre a changé ou c'est pas le bon produit -> On refait tout par sécurité
                    layoutArticlesSelectionnes.removeAllViews();
                    for (LigneCommande l : lignes) ajouterVueLigne(l);
                    return;
                }
            }
        }
    }

    // Méthode pour créer une nouvelle ligne (Appelée seulement à l'ajout)
    private void ajouterVueLigne(LigneCommande ligne) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_article_commande, layoutArticlesSelectionnes, false);

        ImageButton btnDel = row.findViewById(R.id.btn_delete_article);
        EditText etQty = row.findViewById(R.id.edit_text_quantite_article);
        EditText etRem = row.findViewById(R.id.edit_text_remise_ligne);

        // On stocke l'objet ligne ACTUEL dans la vue
        row.setTag(ligne);

        btnDel.setOnClickListener(v -> {
            // Pour supprimer, on prend aussi la version à jour
            LigneCommande current = (LigneCommande) row.getTag();
            viewModel.removeLigne(current);
        });

        etQty.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etQty.getTag() == null && s.length() > 0) {
                    try {
                        // On récupère la donnée À JOUR depuis le tag
                        LigneCommande currentLigne = (LigneCommande) row.getTag();

                        int newQ = Integer.parseInt(s.toString());
                        // On compare et on update
                        if (newQ != currentLigne.getQuantite()) {
                            viewModel.updateLigne(currentLigne, newQ, currentLigne.getRemise());
                        }
                    } catch (NumberFormatException e) { }
                }
            }
        });

        etRem.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etRem.getTag() == null) {
                    try {
                        // On récupère la donnée À JOUR depuis le tag
                        LigneCommande currentLigne = (LigneCommande) row.getTag();

                        double newR = 0.0;
                        if (s.length() > 0) newR = Double.parseDouble(s.toString());

                        if (newR != currentLigne.getRemise()) {
                            viewModel.updateLigne(currentLigne, currentLigne.getQuantite(), newR);
                        }
                    } catch (NumberFormatException e) { }
                }
            }
        });

        mettreAJourVueLigne(row, ligne);
        layoutArticlesSelectionnes.addView(row);
    }

    // Méthode pour mettre à jour une ligne existante SANS voler le focus
    private void mettreAJourVueLigne(View row, LigneCommande ligne) {
        // On met à jour le tag avec la nouvelle version de l'objet
        row.setTag(ligne);

        TextView tvLibelle = row.findViewById(R.id.text_libelle_article);
        TextView tvPU = row.findViewById(R.id.text_prix_unitaire);
        TextView tvTotal = row.findViewById(R.id.text_total_ligne);
        EditText etQty = row.findViewById(R.id.edit_text_quantite_article);
        EditText etRem = row.findViewById(R.id.edit_text_remise_ligne);

        tvLibelle.setText(ligne.getProduit().getLibelle());
        tvPU.setText(String.format(Locale.FRANCE, "%.2f", ligne.getProduit().getPrixUnitaire()));
        tvTotal.setText(String.format(Locale.FRANCE, "%.2f €", ligne.getMontantLigne()));

        if (!etQty.hasFocus()) {
            etQty.setTag("UPDATING");
            etQty.setText(String.valueOf(ligne.getQuantite()));
            etQty.setTag(null);
        }

        if (!etRem.hasFocus()) {
            etRem.setTag("UPDATING");
            if(ligne.getRemise() == (long) ligne.getRemise())
                etRem.setText(String.format(Locale.US, "%d", (long)ligne.getRemise()));
            else
                etRem.setText(String.format(Locale.US, "%.1f", ligne.getRemise()));
            etRem.setTag(null);
        }
    }

    private void updateTotal() {
        double total = viewModel.getTotal();
        tvTotalFinal.setText(String.format(Locale.FRANCE, "%.2f €", total));
    }

    private boolean isFormulaireValide() {
        boolean estValide = true;
        if (viewModel.getClientSelectionne().getValue() == null) {
            autoCompleteClient.setError("Client requis");
            estValide = false;
        }
        List<LigneCommande> lignes = viewModel.getLignesCommande().getValue();
        if (lignes == null || lignes.isEmpty()) {
            autoCompleteArticle.setError("Article requis");
            estValide = false;
        }
        if (viewModel.getDate().getValue() == null) {
            editTextDate.setError("Date requise");
            estValide = false;
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
            Commande cmd = new Commande.Builder()
                    .setId("CMD-" + System.currentTimeMillis())
                    .setClient(client)
                    .setDateCommande(dateCommande)
                    .setLignesCommande(lignes)
                    .setRemiseGlobale(0.0)
                    .setUtilisateur("Admin")
                    .build();

            Toast.makeText(getContext(), "Commande validée : " + String.format("%.2f €", cmd.getMontantTotal()), Toast.LENGTH_LONG).show();
            viewModel.clear();
            navigateToHome();
        } catch (Exception e) {
            new AlertDialog.Builder(requireContext()).setMessage(e.getMessage()).setPositiveButton("OK", null).show();
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Annuler")
                .setMessage("Tout effacer ?")
                .setPositiveButton("Oui", (d, w) -> { viewModel.clear(); navigateToHome(); })
                .setNegativeButton("Non", null)
                .show();
    }

    private void navigateToHome() {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (v, y, m, d) -> {
            c.set(y, m, d);
            viewModel.setDate(new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(c.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void fermerClavier(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}