package com.example.dolorders.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dolorders.Commande; // Ton modèle
import com.example.dolorders.R;
import com.example.dolorders.ui.adapters.CommandesAttenteAdapter; // L'adapter créé juste avant
import java.util.ArrayList;
import java.util.List;

public class TabCommandesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_commandes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_commandes_attente);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // TODO: Plus tard, ici on chargera les données depuis le JSON
        // Pour l'instant, données factices pour voir le rendu
        List<Commande> fakeList = new ArrayList<>();
        // Ajoute ici quelques fausses commandes si tu veux tester l'affichage immédiatement

        CommandesAttenteAdapter adapter = new CommandesAttenteAdapter(fakeList);
        recyclerView.setAdapter(adapter);
    }
}