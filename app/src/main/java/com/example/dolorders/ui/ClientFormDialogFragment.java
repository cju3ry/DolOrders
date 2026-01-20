package com.example.dolorders.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dolorders.Client;
import com.example.dolorders.R;
import com.google.android.material.textfield.TextInputEditText;

public class ClientFormDialogFragment extends DialogFragment {

    public static final int MODE_DETAILS = 0;
    public static final int MODE_EDIT = 1;

    private static final String ARG_MODE = "arg_mode";

    // On passe les champs (simple et robuste)
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

    public static ClientFormDialogFragment newInstance(int mode, Client client) {
        ClientFormDialogFragment frag = new ClientFormDialogFragment();
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

        // MODE_DETAILS => champs non éditables
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
        } else {
            builder.setTitle("Modifier le client")
                    .setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Enregistrer", (dialog, which) -> {
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
                    });
        }

        return builder.create();
    }

    private static void setEditable(TextInputEditText editText, boolean editable) {
        editText.setEnabled(editable);
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        if (!editable) {
            // optionnel: éviter curseur/clavier
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
