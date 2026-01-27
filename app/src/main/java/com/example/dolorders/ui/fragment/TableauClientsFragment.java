package com.example.dolorders.ui.fragment;

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

import com.example.dolorders.R;
import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.objet.Client;
import com.example.dolorders.ui.adapteur.ClientsAttenteAdapteur;
import com.example.dolorders.ui.viewModel.ClientsFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class TableauClientsFragment extends Fragment {

    private ClientsAttenteAdapteur adapter;
    private List<Client> listeClients;
    private GestionnaireStockageClient gestionnaireStockageClient;
    private ClientFormulaireFragment dialog;

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

        gestionnaireStockageClient = new GestionnaireStockageClient(requireContext());
        dialog = new ClientFormulaireFragment();

        // --- Données factices ---
        listeClients = new ArrayList<>();
        listeClients = gestionnaireStockageClient.loadClients();

        // Initialisation de l'adapter avec le Listener
        adapter = new ClientsAttenteAdapteur(listeClients, new ClientsAttenteAdapteur.OnClientActionListener() {
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

                    // 1. On essaie d'abord de supprimer du fichier
                    boolean success = gestionnaireStockageClient.deleteClient(client);

                    if (success) {
                        // 2. Si ça a marché dans le fichier, on met à jour l'écran
                        listeClients.remove(client);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Client supprimé définitivement", Toast.LENGTH_SHORT).show();
                    } else {
                        // 3. Sinon, on avertit l'utilisateur (probablement que le client n'était pas dans le fichier)
                        Toast.makeText(getContext(), "Erreur : Client introuvable dans le fichier (ID: " + client.getId() + ")", Toast.LENGTH_LONG).show();

                        // Optionnel : Si c'était juste une donnée en mémoire (fake), on le supprime quand même de la vue
                        // listeClients.remove(client);
                        // adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void modifierClient(Client client) {
        dialog = ClientFormulaireFragment.newInstance(
                ClientFormulaireFragment.MODE_EDIT, client
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

                // Si tu veux aussi “sauvegarder” ailleurs
                boolean modiffier = gestionnaireStockageClient.modifierClient(updated);

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