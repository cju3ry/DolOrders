package com.example.dolorders.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dolorders.R;
import com.example.dolorders.objet.Client;
import com.google.android.material.textfield.TextInputEditText;

public class ClientFormulaireFragment extends DialogFragment {

    public static final int MODE_DETAILS = 0;
    public static final int MODE_EDIT = 1;

    private static final String ARG_MODE = "arg_mode";
    private static final String ARG_NOM = "arg_nom";
    private static final String ARG_ADRESSE = "arg_adresse";
    private static final String ARG_CP = "arg_cp";
    private static final String ARG_VILLE = "arg_ville";
    private static final String ARG_TEL = "arg_tel";
    private static final String ARG_EMAIL = "arg_email";

    public interface OnClientEditedListener {
        void onClientEdited(String nom, String adresse, String cp, String ville, String tel, String mail);
    }

    private OnClientEditedListener editedListener;

    public void setOnClientEditedListener(OnClientEditedListener listener) {
        this.editedListener = listener;
    }

    public static ClientFormulaireFragment newInstance(int mode, Client client) {
        ClientFormulaireFragment frag = new ClientFormulaireFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode);

        args.putString(ARG_NOM, safe(client.getNom()));
        args.putString(ARG_ADRESSE, safe(client.getAdresse()));
        args.putString(ARG_CP, safe(client.getCodePostal()));
        args.putString(ARG_VILLE, safe(client.getVille()));
        args.putString(ARG_TEL, safe(client.getTelephone()));
        args.putString(ARG_EMAIL, safe(client.getAdresseMail()));

        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_client_form, null, false);

        Bundle b = getArguments();
        int mode = b != null ? b.getInt(ARG_MODE, MODE_DETAILS) : MODE_DETAILS;

        // Récupération des champs
        TextInputEditText edtNom = v.findViewById(R.id.edtNom);
        TextInputEditText edtAdresse = v.findViewById(R.id.edtAdresse);
        TextInputEditText edtCodePostal = v.findViewById(R.id.edtCodePostal);
        TextInputEditText edtVille = v.findViewById(R.id.edtVille);
        TextInputEditText edtTelephone = v.findViewById(R.id.edtTelephone);
        TextInputEditText edtEmail = v.findViewById(R.id.edtEmail);

        // Pré-remplissage
        if (b != null) {
            edtNom.setText(b.getString(ARG_NOM, ""));
            edtAdresse.setText(b.getString(ARG_ADRESSE, ""));
            edtCodePostal.setText(b.getString(ARG_CP, ""));
            edtVille.setText(b.getString(ARG_VILLE, ""));
            edtTelephone.setText(b.getString(ARG_TEL, ""));
            edtEmail.setText(b.getString(ARG_EMAIL, ""));
        }

        // Configuration lecture seule / édition
        boolean editable = (mode == MODE_EDIT);
        setEditable(edtNom, editable);
        setEditable(edtAdresse, editable);
        setEditable(edtCodePostal, editable);
        setEditable(edtVille, editable);
        setEditable(edtTelephone, editable);
        setEditable(edtEmail, editable);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(v);

        if (mode == MODE_DETAILS) {
            builder.setTitle("Détails du client")
                    .setPositiveButton("Fermer", (dialog, which) -> dialog.dismiss());
            return builder.create();
        } else {
            builder.setTitle("Modifier le client")
                    .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                    // IMPORTANT : On met null ici pour surcharger le comportement dans onShow
                    // sinon la boîte de dialogue se ferme automatiquement quoi qu'il arrive.
                    .setPositiveButton("Enregistrer", null);
        }

        AlertDialog dialog = builder.create();

        // On surcharge le bouton "Enregistrer" une fois la boîte affichée pour gérer la validation
        dialog.setOnShowListener(d -> {
            Button button = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                // 1. Reset des erreurs
                edtNom.setError(null);
                edtAdresse.setError(null);
                edtCodePostal.setError(null);
                edtVille.setError(null);
                edtTelephone.setError(null);
                edtEmail.setError(null);

                boolean isValid = true;
                View focusView = null;

                // 2. Validations (Du bas vers le haut pour focus le premier champ visible)

                // Email
                String email = textOf(edtEmail);
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    edtEmail.setError("Email invalide");
                    focusView = edtEmail;
                    isValid = false;
                }

                // Téléphone
                String tel = textOf(edtTelephone);
                if (!tel.matches("\\d{10}")) {
                    edtTelephone.setError("10 chiffres requis");
                    focusView = edtTelephone;
                    isValid = false;
                }

                // Ville
                if (textOf(edtVille).isEmpty()) {
                    edtVille.setError("Ville requise");
                    focusView = edtVille;
                    isValid = false;
                }

                // Code Postal
                String cp = textOf(edtCodePostal);
                if (!cp.matches("\\d{5}")) {
                    edtCodePostal.setError("5 chiffres requis");
                    focusView = edtCodePostal;
                    isValid = false;
                }

                // Adresse
                if (textOf(edtAdresse).isEmpty()) {
                    edtAdresse.setError("Adresse requise");
                    focusView = edtAdresse;
                    isValid = false;
                }

                // Nom
                if (textOf(edtNom).isEmpty()) {
                    edtNom.setError("Nom requis");
                    focusView = edtNom;
                    isValid = false;
                }

                // 3. Action
                if (isValid) {
                    if (editedListener != null) {
                        editedListener.onClientEdited(
                                textOf(edtNom),
                                textOf(edtAdresse),
                                textOf(edtCodePostal),
                                textOf(edtVille),
                                textOf(edtTelephone),
                                textOf(edtEmail)
                        );
                    }
                    dialog.dismiss();
                } else {
                    // Gestion du focus et clavier
                    if (focusView != null) {
                        focusView.requestFocus();
                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                }
            });
        });

        return dialog;
    }

    private static void setEditable(TextInputEditText editText, boolean editable) {
        editText.setEnabled(editable);
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        if (!editable) {
            editText.setCursorVisible(false);
            editText.setKeyListener(null);
        }
    }

    private static String textOf(TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString().trim() : "";
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}