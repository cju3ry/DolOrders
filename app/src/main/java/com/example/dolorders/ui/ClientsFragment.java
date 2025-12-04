package com.example.dolorders.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dolorders.Client;
import com.example.dolorders.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;

public class ClientsFragment extends Fragment {

    private ClientsFragmentViewModel viewModel;
    private TextInputEditText editTextNom, editTextAdresse, editTextCodePostal, editTextVille, editTextEmail, editTextTelephone;
    private MaterialButton btnAnnuler, btnValider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ClientsFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        observeViewModel();
        setupListeners();
    }

    private void setupViews(View view) {
        editTextNom = view.findViewById(R.id.edit_text_nom);
        editTextAdresse = view.findViewById(R.id.edit_text_adresse);
        editTextCodePostal = view.findViewById(R.id.edit_text_code_postal);
        editTextVille = view.findViewById(R.id.edit_text_ville);
        editTextEmail = view.findViewById(R.id.edit_text_email);
        editTextTelephone = view.findViewById(R.id.edit_text_telephone);
        btnAnnuler = view.findViewById(R.id.btn_annuler);
        btnValider = view.findViewById(R.id.btn_valider);
    }

    private void observeViewModel() {
        viewModel.getNom().observe(getViewLifecycleOwner(), s -> { if (!s.equals(editTextNom.getText().toString())) editTextNom.setText(s); });
        viewModel.getAdresse().observe(getViewLifecycleOwner(), s -> { if (!s.equals(editTextAdresse.getText().toString())) editTextAdresse.setText(s); });
        viewModel.getCodePostal().observe(getViewLifecycleOwner(), s -> { if (!s.equals(editTextCodePostal.getText().toString())) editTextCodePostal.setText(s); });
        viewModel.getVille().observe(getViewLifecycleOwner(), s -> { if (!s.equals(editTextVille.getText().toString())) editTextVille.setText(s); });
        viewModel.getEmail().observe(getViewLifecycleOwner(), s -> { if (!s.equals(editTextEmail.getText().toString())) editTextEmail.setText(s); });
        viewModel.getTelephone().observe(getViewLifecycleOwner(), s -> { if (!s.equals(editTextTelephone.getText().toString())) editTextTelephone.setText(s); });
    }

    private void setupListeners() {
        editTextNom.addTextChangedListener(createTextWatcher(viewModel::setNom));
        editTextAdresse.addTextChangedListener(createTextWatcher(viewModel::setAdresse));
        editTextCodePostal.addTextChangedListener(createTextWatcher(viewModel::setCodePostal));
        editTextVille.addTextChangedListener(createTextWatcher(viewModel::setVille));
        editTextEmail.addTextChangedListener(createTextWatcher(viewModel::setEmail));
        editTextTelephone.addTextChangedListener(createTextWatcher(viewModel::setTelephone));

        btnAnnuler.setOnClickListener(v -> showCancelConfirmationDialog());

        btnValider.setOnClickListener(v -> {
            // Validation côté UI
            if (!isFormulaireValide()) {
                return; // Arrête l'exécution si le formulaire est invalide
            }

            // Création de l'objet
            try {
                Client nouveauClient = new Client.Builder()
                        .setNom(viewModel.getNom().getValue())
                        .setAdresse(viewModel.getAdresse().getValue())
                        .setCodePostal(viewModel.getCodePostal().getValue())
                        .setVille(viewModel.getVille().getValue())
                        .setAdresseMail(viewModel.getEmail().getValue())
                        .setTelephone(viewModel.getTelephone().getValue())
                        .setUtilisateur("UtilisateurActuel") // TODO à remplacer par la gestion de l'utilisateur connecté
                        .setDateSaisie(new Date())
                        .build();

                Toast.makeText(getContext(), "Client '" + nouveauClient.getNom() + "' ajouté !", Toast.LENGTH_SHORT).show();

                // TODO: Ajouter la logique pour sauvegarder le client (ex: viewModel.sauvegarderClient(nouveauClient))

                // Vide le ViewModel et retourne à l'accueil
                viewModel.clear();
                navigateToHome();

            } catch (IllegalStateException e) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Erreur de validation")
                        .setMessage("Une erreur inattendue est survenue lors de la création du client : " + e.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    /**
     * Valide les champs du formulaire et affiche des erreurs sur les champs invalides.
     * @return true si tous les champs sont valides, false sinon.
     */
    private boolean isFormulaireValide() {
        // Nettoie les erreurs précédentes
        editTextNom.setError(null);
        editTextAdresse.setError(null);
        editTextCodePostal.setError(null);
        editTextVille.setError(null);
        editTextEmail.setError(null);
        editTextTelephone.setError(null);

        String nom = viewModel.getNom().getValue();
        if (nom == null || nom.trim().isEmpty()) {
            editTextNom.setError("Le nom du client est requis");
            editTextNom.requestFocus();
            return false;
        }

        String adresse = viewModel.getAdresse().getValue();
        if (adresse == null || adresse.trim().isEmpty()) {
            editTextAdresse.setError("L'adresse est requise");
            editTextAdresse.requestFocus();
            return false;
        }

        String codePostal = viewModel.getCodePostal().getValue();
        if (codePostal == null || !codePostal.matches("\\d{5}")) {
            editTextCodePostal.setError("Le code postal doit contenir 5 chiffres");
            editTextCodePostal.requestFocus();
            return false;
        }

        String ville = viewModel.getVille().getValue();
        if (ville == null || ville.trim().isEmpty()) {
            editTextVille.setError("La ville est requise");
            editTextVille.requestFocus();
            return false;
        }

        String email = viewModel.getEmail().getValue();
        if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("L'adresse e-mail n'est pas valide");
            editTextEmail.requestFocus();
            return false;
        }

        String telephone = viewModel.getTelephone().getValue();
        if (telephone == null || !telephone.matches("\\d{10}")) {
            editTextTelephone.setError("Le téléphone doit contenir 10 chiffres");
            editTextTelephone.requestFocus();
            return false;
        }

        return true;
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

    private TextWatcher createTextWatcher(TextUpdate action) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { action.update(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    @FunctionalInterface
    interface TextUpdate {
        void update(String text);
    }
}
