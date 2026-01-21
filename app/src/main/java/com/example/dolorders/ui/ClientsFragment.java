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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.dolorders.data.storage.ClientStorageManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClientsFragment extends Fragment {

    private ClientsFragmentViewModel viewModel;

    private final List<Client> clientsSource = new ArrayList<>();
    private final List<Client> clientsDisplayed = new ArrayList<>();

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
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void setupViews(View view) {
        listeClients = view.findViewById(R.id.listeClient);
        btnFiltre = view.findViewById(R.id.btn_filtrer_clients);
        btnAjoutClient = view.findViewById(R.id.btn_ajouter_client);
    }

    private void setupRecyclerView() {
        // 1) LayoutManager
        listeClients.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2) Données temporaires (remplacées plus tard par JSON/API)
        clientsSource.clear();
        clientsSource.add(new Client.Builder()
                .setId("1")
                .setNom("Dupont")
                .setAdresse("adresse")
                .setCodePostal("12000")
                .setVille("Paris")
                .setAdresseMail("mail@mail.com")
                .setTelephone("0600000000")
                .setUtilisateur("utilisateur")
                .setDateSaisie(new Date())
                .setFromApi(true)
                .build()
        );
        clientsSource.add(new Client.Builder()
                .setId("2")
                .setNom("Martin")
                .setAdresse("2 Rue du Test")
                .setCodePostal("12000")
                .setVille("Rodez")
                .setAdresseMail("mail.martin@gmail.com")
                .setTelephone("0611111111")
                .setUtilisateur("utilisateur")
                .setDateSaisie(new Date())
                .setFromApi(false)
                .build()
        );
        clientsSource.add(new Client.Builder()
                .setId("3")
                .setNom("Durand")
                .setAdresse("adresse")
                .setCodePostal("12000")
                .setVille("Toulouse")
                .setAdresseMail("mail@mail.com")
                .setTelephone("0622222222")
                .setUtilisateur("utilisateur")
                .setDateSaisie(new Date())
                .setFromApi(false)
                .build()
        );

        clientsDisplayed.clear();
        clientsDisplayed.addAll(clientsSource);

        // 3) Adapter
        clientAdapter = new ClientAdapter(clientsDisplayed, new ClientAdapter.OnClientActionListener() {
            @Override
            public void onDetails(Client client) {
                ClientFormDialogFragment dialog = ClientFormDialogFragment.newInstance(
                        ClientFormDialogFragment.MODE_DETAILS, client
                );
                dialog.show(getParentFragmentManager(), "client_details");
            }

            @Override
            public void onModifier(Client client) {
                // On capture l'index avant d'ouvrir le dialog
                int index = clientsDisplayed.indexOf(client);
                if (index < 0) return;

                ClientFormDialogFragment dialog = ClientFormDialogFragment.newInstance(
                        ClientFormDialogFragment.MODE_EDIT, client
                );

                dialog.setOnClientEditedListener((nom, adresse, cp, ville, tel, mail) -> {
                    try {
                        // 1) Construire un NOUVEAU client (car champs final)
                        Client updated = new Client.Builder()
                                .setId(client.getId())                 // conserve
                                .setNom(nom)
                                .setAdresse(adresse)
                                .setCodePostal(cp)
                                .setVille(ville)
                                .setTelephone(tel)
                                .setAdresseMail(mail)
                                .setUtilisateur(client.getUtilisateur()) // conserve
                                .setDateSaisie(client.getDateSaisie())   // conserve
                                .build();

                        // 2) Remplacer dans la liste
                        clientsDisplayed.set(index, updated);

                        // 3) Notifier l’adapter
                        clientAdapter.notifyItemChanged(index);

                        // (optionnel) si tu veux aussi “sauvegarder” ailleurs (ViewModel/API), c’est ici.

                    } catch (IllegalStateException ex) {
                        android.widget.Toast.makeText(requireContext(),
                                ex.getMessage(),
                                android.widget.Toast.LENGTH_LONG).show();
                    }
                });

                dialog.show(getParentFragmentManager(), "client_edit");
            }

            @Override
            public void onNouvelleCommande(Client client) {
                CommandesFragmentViewModel commandesVM =
                        new ViewModelProvider(requireActivity()).get(CommandesFragmentViewModel.class);

                commandesVM.startNouvelleCommandePour(client);

                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_commandes);
                }
            }
        });
        listeClients.setAdapter(clientAdapter);
    }

    private void setupListeners() {
        btnAjoutClient.setOnClickListener(v -> {
            // (Optionnel mais propre) vider le formulaire avant d’ouvrir l’écran d’ajout
            ClientsAjoutFragmentViewModel ajoutVM =
                    new ViewModelProvider(requireActivity()).get(ClientsAjoutFragmentViewModel.class);
            ajoutVM.clear(); // :contentReference[oaicite:1]{index=1}

            // Trouver le container qui héberge ce Fragment, puis remplacer par ClientsAjoutFragment
            View parent = (View) requireView().getParent();
            int containerId = parent != null ? parent.getId() : View.NO_ID;

            if (containerId == View.NO_ID) {
                // Fallback : si jamais le parent n'a pas d'ID, tu devras mettre ici l'ID du container de ton activity_main.xml
                // ex: containerId = R.id.nav_host_fragment_activity_main;
                throw new IllegalStateException("Impossible de trouver l'id du container parent pour la navigation.");
            }

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(containerId, new ClientsAjoutFragment())
                    .addToBackStack("clients_ajout")
                    .commit();
        });

        btnFiltre.setOnClickListener(v -> showFilterDialog());

    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_filter_clients, null, false);

        com.google.android.material.textfield.TextInputEditText edtNom = dialogView.findViewById(R.id.filtreNom);
        com.google.android.material.textfield.TextInputEditText edtAdresse = dialogView.findViewById(R.id.filtreAdresse);
        com.google.android.material.textfield.TextInputEditText edtCP = dialogView.findViewById(R.id.filtreCodePostal);
        com.google.android.material.textfield.TextInputEditText edtVille = dialogView.findViewById(R.id.filtreVille);
        com.google.android.material.textfield.TextInputEditText edtTel = dialogView.findViewById(R.id.filtreTelephone);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtrer les clients")
                .setView(dialogView)
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .setNeutralButton("Réinitialiser", (d, w) -> resetFilter())
                .setPositiveButton("Appliquer", (d, w) -> {
                    ClientFilter f = new ClientFilter(
                            ClientFilter.textOf(edtNom),
                            ClientFilter.textOf(edtAdresse),
                            ClientFilter.textOf(edtCP),
                            ClientFilter.textOf(edtVille),
                            ClientFilter.textOf(edtTel)
                    );
                    applyFilter(f);
                })
                .show();
    }

    private void applyFilter(ClientFilter f) {
        clientsDisplayed.clear();

        for (Client c : clientsSource) {
            if (ClientFilter.matches(c, f)) {
                clientsDisplayed.add(c);
            }
        }
        clientAdapter.notifyDataSetChanged();
    }

    private void resetFilter() {
        clientsDisplayed.clear();
        clientsDisplayed.addAll(clientsSource);
        clientAdapter.notifyDataSetChanged();
    }


    private void observeViewModel() {
        viewModel.getClientCree().observe(getViewLifecycleOwner(), client -> {
            if (client == null) return;

            clientsSource.add(client);
            clientAdapter.notifyItemInserted(clientsSource.size() - 1);
            listeClients.scrollToPosition(clientsSource.size() - 1);

            // éviter un doublon si re-émission
            viewModel.consommerClientCree();
        });
    }
}
