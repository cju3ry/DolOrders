package com.example.dolorders.ui.fragment;

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

import com.example.dolorders.R;
import com.example.dolorders.activity.LoginActivity;
import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommandesFragment extends Fragment {

    private static final String REGEX_MONTANT = "%.2f €";

    private static final String REGEX_DATE = "dd/MM/yyyy";
    private CommandesFragmentViewModel viewModel;
    private GestionnaireStockageCommande commandeStorage;
    private ProduitAdapter produitAdapter;

    // Vues
    private AutoCompleteTextView autoCompleteClient;
    private AutoCompleteTextView autoCompleteArticle;
    private TextInputEditText editTextDate;
    private LinearLayout layoutArticlesSelectionnes;
    private MaterialButton btnAnnuler;
    private MaterialButton btnValider;

    // Infos clients & Totaux
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
        GestionnaireStockageClient gestionnaireStockageClient = new GestionnaireStockageClient(requireContext());
        List<Client> listeClients = gestionnaireStockageClient.loadClients();

        viewModel.setListeClients(listeClients);

        // Charger les produits depuis le fichier local (pas d'appel API automatique)
        // Si le fichier existe, les produits seront disponibles immédiatement
        // Pour synchroniser depuis l'API, l'utilisateur doit cliquer sur "Synchroniser les produits" dans l'accueil
        viewModel.chargerProduitsDepuisCache(requireContext());

        observeViewModel();
        if (viewModel.getClientSelectionne().getValue() == null) {
            SimpleDateFormat sdf = new SimpleDateFormat(REGEX_DATE, Locale.FRANCE);
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

        // Produits - Utilisation du ProduitAdapter personnalisé
        viewModel.getListeProduits().observe(getViewLifecycleOwner(), produits -> {
            if (produits != null) {
                if (produitAdapter == null) {
                    produitAdapter = new ProduitAdapter(requireContext(), new ArrayList<>());
                    autoCompleteArticle.setAdapter(produitAdapter);
                    autoCompleteArticle.setThreshold(1); // Déclenche la recherche après 1 caractère
                }
                produitAdapter.updateProduits(produits);
            }
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

            // Fermer la liste déroulante après la sélection
            autoCompleteArticle.dismissDropDown();
            autoCompleteArticle.clearFocus();
        });

        // Boutons
        btnAnnuler.setOnClickListener(v -> showCancelConfirmationDialog());

        btnValider.setOnClickListener(v -> {
            fermerClavier(v);
            // Vérification des erreurs sur tous les champs quantité/remise
            boolean hasError = false;
            for (int i = 0; i < layoutArticlesSelectionnes.getChildCount(); i++) {
                View row = layoutArticlesSelectionnes.getChildAt(i);
                EditText etQty = row.findViewById(R.id.edit_text_quantite_article);
                EditText etRem = row.findViewById(R.id.edit_text_remise_ligne);
                if ((etQty != null && etQty.getError() != null) || (etRem != null && etRem.getError() != null)) {
                    hasError = true;
                    break;
                }
            }
            if (hasError) {
                Toast.makeText(requireContext(), "Corrigez les champs en erreur avant d'enregistrer", Toast.LENGTH_LONG).show();
                return;
            }
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

        // Mettre à jour le bouton Valider selon l'état des lignes
        mettreAJourBoutonValider(lignes);

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
                if (ancienneDonnee != null && ancienneDonnee.getProduit().getId().equals(nouvelleDonnee.getProduit().getId())) {
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
        ImageButton btnValiderLigne = row.findViewById(R.id.btn_valider_ligne);
        EditText etQty = row.findViewById(R.id.edit_text_quantite_article);
        EditText etRem = row.findViewById(R.id.edit_text_remise_ligne);

        // On stocke l'objet ligne ACTUEL dans la vue
        row.setTag(ligne);

        // Bouton supprimer
        btnDel.setOnClickListener(v -> {
            LigneCommande current = (LigneCommande) row.getTag();
            viewModel.removeLigne(current);
        });

        // Bouton valider/dévalider la ligne
        btnValiderLigne.setOnClickListener(v -> {
            LigneCommande current = (LigneCommande) row.getTag();
            viewModel.toggleValidationLigne(current);
        });

        etQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etQty.getTag() == null && s.length() > 0) {
                    try {
                        LigneCommande currentLigne = (LigneCommande) row.getTag();

                        // Ne pas mettre à jour si la ligne est validée
                        if (currentLigne.isValidee()) return;

                        int newQ = Integer.parseInt(s.toString());

                        // Validation : la quantité doit être positive
                        if (newQ <= 0) {
                            etQty.setError("Quantité min : 1");
                            return;
                        } else {
                            etQty.setError(null);
                        }

                        // On compare et on update
                        if (newQ != currentLigne.getQuantite()) {
                            viewModel.updateLigne(currentLigne, newQ, currentLigne.getRemise());
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer les saisies invalides
                    }
                }
            }
        });

        etRem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etRem.getTag() == null) {
                    try {
                        LigneCommande currentLigne = (LigneCommande) row.getTag();

                        // Ne pas mettre à jour si la ligne est validée
                        if (currentLigne.isValidee()) return;

                        double newR = 0.0;
                        if (s.length() > 0) {
                            newR = Double.parseDouble(s.toString());

                            // Validation : la remise ne peut pas dépasser 100%
                            if (newR > 100) {
                                etRem.setError("Remise max : 100%");
                                return;
                            } else if (newR < 0) {
                                etRem.setError("Remise min : 0%");
                                return;
                            } else {
                                etRem.setError(null);
                            }
                        }

                        if (newR != currentLigne.getRemise()) {
                            viewModel.updateLigne(currentLigne, currentLigne.getQuantite(), newR);
                        }
                    } catch (NumberFormatException e) {
                        // Ignorer les saisies invalides
                    }
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
        ImageButton btnValiderLigne = row.findViewById(R.id.btn_valider_ligne);
        ImageButton btnDel = row.findViewById(R.id.btn_delete_article);

        tvLibelle.setText(ligne.getProduit().getLibelle());
        tvPU.setText(String.format(Locale.FRANCE, "%.2f", ligne.getProduit().getPrixUnitaire()));
        tvTotal.setText(String.format(Locale.FRANCE, REGEX_MONTANT, ligne.getMontantLigne()));

        // Gérer l'état visuel selon si la ligne est validée ou non
        boolean estValidee = ligne.isValidee();

        // Changer l'icône du bouton de validation
        if (estValidee) {
            // Ligne validée : afficher icône "éditer" pour pouvoir dévalider
            btnValiderLigne.setImageResource(R.drawable.ic_edit);
            btnValiderLigne.setColorFilter(getResources().getColor(android.R.color.holo_orange_dark, null));

            // Désactiver les champs d'édition
            etQty.setEnabled(false);
            etRem.setEnabled(false);
            etQty.setAlpha(0.6f);
            etRem.setAlpha(0.6f);

            // Changer le fond de la ligne pour indiquer qu'elle est validée
            row.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, null));
            row.getBackground().setAlpha(50);
        } else {
            // Ligne non validée : afficher icône "coche" pour valider
            btnValiderLigne.setImageResource(R.drawable.ic_check);
            btnValiderLigne.setColorFilter(getResources().getColor(R.color.blue_dolibarr, null));

            // Activer les champs d'édition
            etQty.setEnabled(true);
            etRem.setEnabled(true);
            etQty.setAlpha(1.0f);
            etRem.setAlpha(1.0f);

            // Remettre le fond normal
            row.setBackgroundResource(R.drawable.border_bottom);
        }

        if (!etQty.hasFocus()) {
            etQty.setTag("UPDATING");
            etQty.setText(String.valueOf(ligne.getQuantite()));
            etQty.setTag(null);
        }

        if (!etRem.hasFocus()) {
            etRem.setTag("UPDATING");
            if (ligne.getRemise() == (long) ligne.getRemise())
                etRem.setText(String.format(Locale.US, "%d", (long) ligne.getRemise()));
            else
                etRem.setText(String.format(Locale.US, "%.1f", ligne.getRemise()));
            etRem.setTag(null);
        }
    }

    private void updateTotal() {
        double total = viewModel.getTotal();
        tvTotalFinal.setText(String.format(Locale.FRANCE, REGEX_MONTANT, total));
    }

    /**
     * Met à jour le bouton Valider selon l'état des lignes de commande.
     * Affiche le nombre de lignes à valider et change l'apparence du bouton.
     */
    private void mettreAJourBoutonValider(List<LigneCommande> lignes) {
        if (lignes == null || lignes.isEmpty()) {
            btnValider.setEnabled(true);
            btnValider.setAlpha(1.0f);
            btnValider.setText("Valider la commande");
            return;
        }

        // Compter les lignes non validées
        int nbNonValidees = 0;
        for (LigneCommande ligne : lignes) {
            if (!ligne.isValidee()) {
                nbNonValidees++;
            }
        }

        if (nbNonValidees > 0) {
            // Il reste des lignes à valider
            btnValider.setEnabled(true); // Reste cliquable pour afficher le Toast
            btnValider.setAlpha(0.6f);
            btnValider.setText("Valider (" + nbNonValidees + " ligne(s) à valider)");
        } else {
            // Toutes les lignes sont validées
            btnValider.setEnabled(true);
            btnValider.setAlpha(1.0f);
            btnValider.setText("Valider la commande");
        }
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
        } else {
            // Vérifier que toutes les lignes sont validées
            int nbNonValidees = 0;
            for (LigneCommande ligne : lignes) {
                if (!ligne.isValidee()) {
                    nbNonValidees++;
                }
            }

            if (nbNonValidees > 0) {
                Toast.makeText(requireContext(),
                    "Veuillez valider toutes les lignes (" + nbNonValidees + " ligne(s) non validée(s))",
                    Toast.LENGTH_LONG).show();
                estValide = false;
            }
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
            dateCommande = new SimpleDateFormat(REGEX_DATE, Locale.FRANCE).parse(viewModel.getDate().getValue());
        } catch (ParseException e) {
            return;
        }

        try {
            Commande cmd = new Commande.Builder()
                    .setId("CMD-" + System.currentTimeMillis())
                    .setClient(client)
                    .setDateCommande(dateCommande)
                    .setLignesCommande(lignes)
                    .setUtilisateur(nomUtilisateur)
                    .build();

            // Enregistrement de la commande en local
            boolean saved = commandeStorage.addCommande(cmd);

            if (saved) {
                Toast.makeText(getContext(),
                        "Commande enregistrée : " + String.format(REGEX_MONTANT, cmd.getMontantTotal()),
                        Toast.LENGTH_LONG).show();
                viewModel.clear();
                navigateToHome();
            } else {
                Toast.makeText(getContext(),
                        "Erreur lors de l'enregistrement de la commande",
                        Toast.LENGTH_LONG).show();
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
                    navigateToHome();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void navigateToHome() {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);

        if (bottomNav == null) {
            return;
        }

        boolean fromListeClients = viewModel.consumeFromListeClients();
        boolean fromAccueil = viewModel.consumeFromAccueil();

        if (fromListeClients) {
            bottomNav.setSelectedItemId(R.id.nav_clients);
        } else if (fromAccueil) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        } else {
            bottomNav.setSelectedItemId(R.id.nav_commandes);
        }
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (v, y, m, d) -> {
            c.set(y, m, d);
            viewModel.setDate(new SimpleDateFormat(REGEX_DATE, Locale.FRANCE).format(c.getTime()));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void fermerClavier(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}