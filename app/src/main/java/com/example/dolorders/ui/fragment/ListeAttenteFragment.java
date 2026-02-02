package com.example.dolorders.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.dolorders.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ListeAttenteFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liste_attente, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        Button btnEnvoyer = view.findViewById(R.id.btn_envoyer_dolibarr);

        // Configuration de l'adapter (Seulement 2 onglets maintenant)
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Liaison TabLayout <-> ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("CLIENTS");
            } else if (position == 1) {
                tab.setText("COMMANDES");
            }
        }).attach();

        // Gestion du bouton Envoyer (Action globale)
        btnEnvoyer.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Synchronisation")
                        .setMessage("Voulez-vous envoyer toutes les données en attente vers Dolibarr ?")
                        .setPositiveButton("Envoyer", (dialog, which) ->
                                // TODO: Appeler ton futur JsonManager / ApiManager ici
                                Toast.makeText(getContext(), "Envoi en cours...", Toast.LENGTH_SHORT).show())
                        .setNegativeButton("Annuler", null)
                        .show());
    }

    // Adapter interne réduit à 2 onglets
    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new TableauClientsFragment();
            }
            return new TableauCommandesFragment();
        }

        @Override
        public int getItemCount() {
            return 2; // Uniquement Clients et Commandes
        }
    }
}