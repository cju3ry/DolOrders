package com.example.dolorders.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolorders.Client;
import com.example.dolorders.R;
import com.example.dolorders.data.storage.ClientStorageManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClientsFragment extends Fragment {

    private ClientsFragmentViewModel viewModel;

    private RecyclerView listeClients;
    private ClientAdapter clientAdapter;

    private MaterialButton btnFiltre, btnAjoutClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ClientsFragmentViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews(view);
        setupRecyclerView();      // ✅ AJOUT
        // observeViewModel();
        // setupListeners();
    }

    private void setupViews(View view) {
        listeClients = view.findViewById(R.id.listeClient);
        btnFiltre = view.findViewById(R.id.btn_filtrer_clients);
        btnAjoutClient = view.findViewById(R.id.btn_ajouter_client);

        // ❌ À supprimer : ces vues ne sont pas dans fragment_client.xml
        // txtNom = view.findViewById(R.id.txtNom);
        // txtTelephone = view.findViewById(R.id.txtTelephone);
        // txtVille = view.findViewById(R.id.txtVille);
    }

    private void setupRecyclerView() {
        // 1) LayoutManager
        listeClients.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2) Données temporaires (remplacées plus tard par JSON/API)
        List<Client> clients = new ArrayList<>();
        clients.add(new Client.Builder()
                .setId("1")
                .setNom("Dupont")
                .setAdresse("adresse")
                .setCodePostal("12000")
                .setVille("Paris")
                .setAdresseMail("mail@mail.com")
                .setTelephone("0600000000")
                .setUtilisateur("utilisateur")
                .setDateSaisie(new Date())
                .build()
        );
        clients.add(new Client.Builder()
                .setId("2")
                .setNom("Martin")
                .setAdresse("adresse")
                .setCodePostal("12000")
                .setVille("Lyon")
                .setAdresseMail("mail@mail.com")
                .setTelephone("0611111111")
                .setUtilisateur("utilisateur")
                .setDateSaisie(new Date())
                .build()
        );
        clients.add(new Client.Builder()
                .setId("3")
                .setNom("Durand")
                .setAdresse("adresse")
                .setCodePostal("12000")
                .setVille("Toulouse")
                .setAdresseMail("mail@mail.com")
                .setTelephone("0622222222")
                .setUtilisateur("utilisateur")
                .setDateSaisie(new Date())
                .build()
        );

        // 3) Adapter
        clientAdapter = new ClientAdapter(clients);
        listeClients.setAdapter(clientAdapter);
    }
}
