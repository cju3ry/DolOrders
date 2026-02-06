package com.example.dolorders.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
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
import com.example.dolorders.ui.adapteur.ClientAdapteur;
import com.example.dolorders.ui.util.NavigationUtils;
import com.example.dolorders.ui.viewModel.ClientsFragmentViewModel;
import com.example.dolorders.ui.viewModel.CommandesFragmentViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class ClientsFragment extends Fragment {

    private ClientsFragmentViewModel viewModel;

    private final List<Client> clientsSource = new ArrayList<>();
    private final List<Client> clientsDisplayed = new ArrayList<>();

    private RecyclerView listeClients;
    private ClientAdapteur clientAdapteur;

    private ChipGroup chipGroupActiveFilters;
    private HorizontalScrollView filtersScroll;
    private BadgeDrawable filtreBadge;


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
        updateActiveFiltersUI();
    }

    private void setupViews(View view) {
        listeClients = view.findViewById(R.id.listeClient);
        btnFiltre = view.findViewById(R.id.btn_filtrer_clients);
        btnAjoutClient = view.findViewById(R.id.btn_ajouter_client);

        chipGroupActiveFilters = view.findViewById(R.id.chipGroup_active_filters);
        filtersScroll = view.findViewById(R.id.filters_scroll);

        // Badge sur le bouton "Filtrer"
        filtreBadge = BadgeDrawable.create(requireContext());
        filtreBadge.setBackgroundColor(Color.parseColor("#D32F2F")); // rouge (change si tu veux)
        filtreBadge.setBadgeTextColor(Color.WHITE);
        filtreBadge.setVisible(false);
        BadgeUtils.attachBadgeDrawable(filtreBadge, btnFiltre);

    }

    private void setupRecyclerView() {
        // 1) LayoutManager
        listeClients.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2) Charger tous les clients (locaux + API) via le ViewModel
        viewModel.chargerTousLesClients(requireContext());

        // Les données seront chargées via l'observer dans observeViewModel()
        clientsSource.clear();
        clientsDisplayed.clear();

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
                // Vérifier si le client provient de l'API (on ne peut pas le modifier)
                if (client.isFromApi()) {
                    Toast.makeText(getContext(),
                            "Impossible de modifier un client synchronisé depuis Dolibarr",
                            Toast.LENGTH_LONG).show();
                    return;
                }

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
                                .setFromApi(false) // Client local, pas de l'API
                                .build();

                        // 2) Modifier dans le gestionnaire de stockage
                        GestionnaireStockageClient storageManager = new GestionnaireStockageClient(requireContext());
                        boolean modiffier = storageManager.modifierClient(updated);

                        if (modiffier) {
                            Toast.makeText(getContext(), "Client '" + updated.getNom() + "' modifié et enregistré localement !", Toast.LENGTH_SHORT)
                                    .show();

                            // 3) Recharger tous les clients via le ViewModel
                            viewModel.chargerTousLesClients(requireContext());
                            viewModel.publierClientCree(updated);
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

        // Filtrer sur clientsSource (qui contient locaux + API)
        List<Client> filtered = clientsSource.stream()
                .filter(c -> (nom == null || nom.isEmpty() || c.getNom().toLowerCase().contains(nom.toLowerCase())))
                .filter(c -> (adresse == null || adresse.isEmpty() || c.getAdresse().toLowerCase().contains(adresse.toLowerCase())))
                .filter(c -> (cp == null || cp.isEmpty() || c.getCodePostal().toLowerCase().contains(cp.toLowerCase())))
                .filter(c -> (ville == null || ville.isEmpty() || c.getVille().toLowerCase().contains(ville.toLowerCase())))
                .filter(c -> (tel == null || tel.isEmpty() || c.getTelephone().toLowerCase().contains(tel.toLowerCase())))
                .collect(java.util.stream.Collectors.toList());

        clientsDisplayed.addAll(filtered);
        clientAdapteur.notifyDataSetChanged();

        updateActiveFiltersUI();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetFilter() {
        filtreNom = "";
        filtreAdresse = "";
        filtreCP = "";
        filtreVille = "";
        filtreTel = "";

        // Recharger tous les clients (locaux + API) via le ViewModel
        clientsDisplayed.clear();
        clientsDisplayed.addAll(clientsSource);
        clientAdapteur.notifyDataSetChanged();
        updateActiveFiltersUI();
    }

    private void updateActiveFiltersUI() {
        if (chipGroupActiveFilters == null || filtersScroll == null || filtreBadge == null) return;

        chipGroupActiveFilters.removeAllViews();

        int count = 0;

        count += addFilterChipIfNotEmpty("Nom", filtreNom, () -> {
            filtreNom = "";
            applyFilter(filtreNom, filtreAdresse, filtreCP, filtreVille, filtreTel);
        });

        count += addFilterChipIfNotEmpty("Adresse", filtreAdresse, () -> {
            filtreAdresse = "";
            applyFilter(filtreNom, filtreAdresse, filtreCP, filtreVille, filtreTel);
        });

        count += addFilterChipIfNotEmpty("CP", filtreCP, () -> {
            filtreCP = "";
            applyFilter(filtreNom, filtreAdresse, filtreCP, filtreVille, filtreTel);
        });

        count += addFilterChipIfNotEmpty("Ville", filtreVille, () -> {
            filtreVille = "";
            applyFilter(filtreNom, filtreAdresse, filtreCP, filtreVille, filtreTel);
        });

        count += addFilterChipIfNotEmpty("Téléphone", filtreTel, () -> {
            filtreTel = "";
            applyFilter(filtreNom, filtreAdresse, filtreCP, filtreVille, filtreTel);
        });

        // Affichage/masquage de la zone chips
        filtersScroll.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

        // Badge sur le bouton
        filtreBadge.setVisible(count > 0);
        if (count > 0) {
            filtreBadge.setNumber(count);
        }
    }

    /**
     * Ajoute une chip "Label: valeur" si valeur non vide.
     *
     * @return 1 si ajoutée, 0 sinon.
     */
    private int addFilterChipIfNotEmpty(String label, String value, Runnable onRemove) {
        if (value == null) return 0;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return 0;

        Chip chip = new Chip(requireContext());
        chip.setText(label + " : " + trimmed);
        chip.setCloseIconVisible(true);
        chip.setClickable(false);
        chip.setCheckable(false);

        chip.setOnCloseIconClickListener(v -> {
            onRemove.run();
            updateActiveFiltersUI(); // refresh chips + badge
        });

        chipGroupActiveFilters.addView(chip);
        return 1;
    }


    private void observeViewModel() {
        // Observer la liste complète des clients (locaux + API)
        viewModel.getListeClients().observe(getViewLifecycleOwner(), clients -> {
            if (clients != null) {
                clientsSource.clear();
                clientsSource.addAll(clients);

                clientsDisplayed.clear();
                clientsDisplayed.addAll(clients);

                clientAdapteur.notifyDataSetChanged();

                applyFilter(filtreNom, filtreAdresse, filtreCP, filtreVille, filtreTel);
                updateActiveFiltersUI();
            }
        });

        // Observer la création d'un nouveau client pour recharger la liste
        viewModel.getClientCree().observe(getViewLifecycleOwner(), client -> {
            if (client != null) {
                // Recharger tous les clients quand un nouveau client est créé
                viewModel.chargerTousLesClients(requireContext());
                viewModel.consommerClientCree(); // Consommer l'événement
            }
        });
    }

}
