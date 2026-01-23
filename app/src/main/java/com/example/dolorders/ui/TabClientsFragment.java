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
    private List<Client> ListeClients;
    private ClientStorageManager clientStorageManager;

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

        // --- Données factices ---
        ListeClients = new ArrayList<>();
        ListeClients = clientStorageManager.loadClients();

        // Initialisation de l'adapter avec le Listener
        adapter = new ClientsAttenteAdapter(ListeClients, new ClientsAttenteAdapter.OnClientActionListener() {
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
                    ListeClients.remove(client);
                    adapter.notifyDataSetChanged();
                    // TODO: Supprimer aussi du fichier JSON
                    Toast.makeText(getContext(), "Client supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void modifierClient(Client client) {
        // Préparer le fragment de destination
        ClientsFragment clientsFragment = new ClientsFragment();

        // Transmettre les données via un Bundle
        Bundle args = new Bundle();
        args.putString("nom", client.getNom());
        args.putString("adresse", client.getAdresse());
        args.putString("codePostal", client.getCodePostal());
        args.putString("ville", client.getVille());
        args.putString("email", client.getAdresseMail());
        args.putString("telephone", client.getTelephone());
        // On pourrait passer l'ID aussi pour savoir que c'est une modif et pas une création
        clientsFragment.setArguments(args);

        // Remplacer le fragment actuel par le ClientsFragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, clientsFragment)
                .addToBackStack(null)
                .commit();

        // Mettre à jour visuellement le BottomNavigation pour qu'il souligne "Clients"
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_clients);
        }
    }
}