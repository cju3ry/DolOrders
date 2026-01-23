package com.example.dolorders.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolorders.Client;
import com.example.dolorders.R;
import com.example.dolorders.data.storage.ClientStorageManager;
import com.example.dolorders.ui.adapters.ClientsAttenteAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TabClientsFragment extends Fragment {

    private ClientsAttenteAdapter adapter;
    private List<Client> listeClients;
    private ClientStorageManager clientStorageManager;
    private ClientFormDialogFragment dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_clients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_clients_attente);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        clientStorageManager = new ClientStorageManager(requireContext());
        dialog = new ClientFormDialogFragment();

        // --- Données factices ---
        listeClients = new ArrayList<>();
        listeClients = clientStorageManager.loadClients();

        // Initialisation de l'adapter avec le Listener
        adapter = new ClientsAttenteAdapter(listeClients, new ClientsAttenteAdapter.OnClientActionListener() {
            @Override
            public void onEdit(Client client) {
                modifierClient(client);
            }

            @Override
            public void onDelete(Client client) {
                supprimerClient(client);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void supprimerClient(Client client) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Suppression")
                .setMessage("Voulez-vous vraiment supprimer " + client.getNom() + " de la liste d'attente ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // Suppression de la liste visuelle
                    listeClients.remove(client);
                    adapter.notifyDataSetChanged();
                    // TODO: Supprimer aussi du fichier JSON
                    Toast.makeText(getContext(), "Client supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void modifierClient(Client client) {
        dialog = ClientFormDialogFragment.newInstance(
                ClientFormDialogFragment.MODE_EDIT, client
        );

        int index = listeClients.indexOf(client);

        dialog.setOnClientEditedListener((nom, adresse, cp, ville, tel, mail) -> {
            try {
                // Construire un NOUVEAU client
                Client updated = new Client.Builder()
                        .setId(client.getId())
                        .setNom(nom)
                        .setAdresse(adresse)
                        .setCodePostal(cp)
                        .setVille(ville)
                        .setTelephone(tel)
                        .setAdresseMail(mail)
                        .setUtilisateur(client.getUtilisateur())
                        .setDateSaisie(client.getDateSaisie())
                        .build();

                // Remplacer dans la liste
                listeClients.set(index, updated);

                //notifier l'adaptateur
                adapter.notifyItemChanged(index);

                // Si tu veux aussi “sauvegarder” ailleurs (ViewModel/API)
                boolean modiffier = clientStorageManager.modifierClient(updated);

                if (modiffier) {
                    Toast.makeText(getContext(), "Client '" + updated.getNom() + "' modifié et enregistré localement !", Toast.LENGTH_SHORT)
                            .show();

                    ClientsFragmentViewModel clientsVM = new ViewModelProvider(requireActivity())
                            .get(ClientsFragmentViewModel.class);

                    clientsVM.publierClientCree(updated);
                } else {
                    Toast.makeText(getContext(),
                            "Client '" + updated.getNom() + "' modifié et enregistré localement a échoué",
                            Toast.LENGTH_SHORT).show();
                }

            } catch (IllegalStateException ex) {
                android.widget.Toast.makeText(requireContext(),
                        ex.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });

        dialog.show(getParentFragmentManager(), "client_edit");
    }
}