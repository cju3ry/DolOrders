package com.example.dolorders.ui.fragment;

import android.annotation.SuppressLint;
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
import com.example.dolorders.service.ServiceClient;
import com.example.dolorders.ui.adapteur.ClientAdapteur;
import com.example.dolorders.ui.util.NavigationUtils;
import com.example.dolorders.ui.viewModel.ClientsFragmentViewModel;
import com.example.dolorders.ui.viewModel.CommandesFragmentViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ClientsFragment extends Fragment {

    private ClientsFragmentViewModel viewModel;

    private final List<Client> clientsSource = new ArrayList<>();
    private final List<Client> clientsDisplayed = new ArrayList<>();

    private RecyclerView listeClients;
    private ClientAdapteur clientAdapteur;

    private ServiceClient serviceClient;

    private MaterialButton btnFiltre;
    private MaterialButton btnAjoutClient;

    // Champs de filtre mémorisés
    private String filtreNom = "";
    private String filtreAdresse = "";
    private String filtreCP = "";
    private String filtreVille = "";
    private String filtreTel = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ClientsFragmentViewModel.class);
        serviceClient = new ServiceClient(requireContext());
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
        //TODO a modifier
        // 1) LayoutManager
        listeClients.setLayoutManager(new LinearLayoutManager(getContext()));

        GestionnaireStockageClient storageManager =
                new GestionnaireStockageClient(requireContext());

        // 2) Données temporaires (remplacées plus tard par JSON/API)
        clientsSource.clear();
        clientsSource.addAll(storageManager.loadClients());

        clientsDisplayed.clear();
        clientsDisplayed.addAll(clientsSource);

        // 3) Adapter
        clientAdapteur = new ClientAdapteur(clientsDisplayed, new ClientAdapteur.OnClientActionListener() {
            @Override
            public void onDetails(Client client) {
                ClientFormulaireFragment dialog = ClientFormulaireFragment.newInstance(
                        ClientFormulaireFragment.MODE_DETAILS, client
                );
                dialog.show(getParentFragmentManager(), "client_details");
            }

            @Override
            public void onModifier(Client client) {
                // On capture l'index avant d'ouvrir le dialog
                int index = clientsDisplayed.indexOf(client);
                if (index < 0) return;

                ClientFormulaireFragment dialog = ClientFormulaireFragment.newInstance(
                        ClientFormulaireFragment.MODE_EDIT, client
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
                        clientAdapteur.notifyItemChanged(index);

                        // (optionnel) si tu veux aussi “sauvegarder” ailleurs (ViewModel/API), c’est ici.
                        boolean modiffier = storageManager.modifierClient(updated);

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

            @Override
            public void onNouvelleCommande(Client client) {
                CommandesFragmentViewModel commandesVM =
                        new ViewModelProvider(requireActivity()).get(CommandesFragmentViewModel.class);

                commandesVM.setFromListeClients();
                commandesVM.startNouvelleCommandePour(client);

                BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_commandes);
                }
            }
        });
        listeClients.setAdapter(clientAdapteur);
    }

    private void setupListeners() {
        btnAjoutClient.setOnClickListener(v ->
                createNewClient());

        btnFiltre.setOnClickListener(v -> showFilterDialog());

    }

    private void createNewClient() {
        NavigationUtils.navigateToClientAjout(this);
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_filter_clients, null, false);

        com.google.android.material.textfield.TextInputEditText edtNom = dialogView.findViewById(R.id.filtreNom);
        com.google.android.material.textfield.TextInputEditText edtAdresse = dialogView.findViewById(R.id.filtreAdresse);
        com.google.android.material.textfield.TextInputEditText edtCP = dialogView.findViewById(R.id.filtreCodePostal);
        com.google.android.material.textfield.TextInputEditText edtVille = dialogView.findViewById(R.id.filtreVille);
        com.google.android.material.textfield.TextInputEditText edtTel = dialogView.findViewById(R.id.filtreTelephone);

        // Remplir les champs avec les valeurs précédentes
        edtNom.setText(filtreNom);
        edtAdresse.setText(filtreAdresse);
        edtCP.setText(filtreCP);
        edtVille.setText(filtreVille);
        edtTel.setText(filtreTel);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtrer les clients")
                .setView(dialogView)
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .setNeutralButton("Réinitialiser", (d, w) ->
                        resetFilter())
                .setPositiveButton("Appliquer", (d, w) -> {
                    // Mémoriser les valeurs saisies
                    filtreNom = edtNom.getText().toString();
                    filtreAdresse = edtAdresse.getText().toString();
                    filtreCP = edtCP.getText().toString();
                    filtreVille = edtVille.getText().toString();
                    filtreTel = edtTel.getText().toString();
                    applyFilter(filtreNom, filtreAdresse, filtreCP, filtreVille, filtreTel);
                })
                .show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyFilter(String nom, String adresse, String cp, String ville, String tel) {
        clientsDisplayed.clear();
        clientsDisplayed.addAll(serviceClient.filter(nom, adresse, cp, ville, tel));
        clientAdapteur.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetFilter() {
        filtreNom = "";
        filtreAdresse = "";
        filtreCP = "";
        filtreVille = "";
        filtreTel = "";
        GestionnaireStockageClient storageManager =
                new GestionnaireStockageClient(requireContext());
        clientsDisplayed.clear();
        clientsDisplayed.addAll(storageManager.loadClients());
        clientAdapteur.notifyDataSetChanged();
    }


    private void observeViewModel() {
        viewModel.getClientCree().observe(getViewLifecycleOwner(), client -> {
            if (client == null) return;

            clientsSource.add(client);
            clientAdapteur.notifyItemInserted(clientsSource.size() - 1);
            listeClients.scrollToPosition(clientsSource.size() - 1);

            // éviter un doublon si re-émission
            viewModel.consommerClientCree();
        });
    }

}
