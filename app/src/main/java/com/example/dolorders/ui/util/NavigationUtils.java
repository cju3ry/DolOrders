package com.example.dolorders.ui.util;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dolorders.ui.fragment.ClientsAjoutFragment;
import com.example.dolorders.ui.viewModel.ClientsAjoutFragmentViewModel;

public class NavigationUtils {

    private NavigationUtils() {
        // Constructeur privé pour empêcher l'instanciation
    }

    /**
     * Navigate to ClientsAjoutFragment from any fragment
     * Clears the form before navigating
     */
    public static void navigateToClientAjout(Fragment fragment) {
        // Vider le formulaire avant d'ouvrir l'écran d'ajout
        ClientsAjoutFragmentViewModel ajoutVM =
                new ViewModelProvider(fragment.requireActivity()).get(ClientsAjoutFragmentViewModel.class);
        ajoutVM.clear();

        // Trouver le container qui héberge ce Fragment
        View parent = (View) fragment.requireView().getParent();
        int containerId = parent != null ? parent.getId() : View.NO_ID;

        if (containerId == View.NO_ID) {
            throw new IllegalStateException("Impossible de trouver l'id du container parent pour la navigation.");
        }

        // Navigation vers ClientsAjoutFragment
        fragment.getParentFragmentManager()
                .beginTransaction()
                .replace(containerId, new ClientsAjoutFragment())
                .addToBackStack("clients_ajout")
                .commit();
    }
}
