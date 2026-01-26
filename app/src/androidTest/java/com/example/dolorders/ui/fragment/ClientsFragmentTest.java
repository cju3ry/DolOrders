// java
package com.example.dolorders.ui.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dolorders.R;
import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.objet.Client;
import com.example.dolorders.ui.viewModel.ClientsFragmentViewModel;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ClientsFragmentTest {

    private FragmentScenario<ClientsFragment> scenario;

    @Before
    public void setup() {
        // 1) On seed le stockage utilisé par ClientService.filter() et resetFilter()
        seedLocalStorageWithKnownClients();

        // 2) On lance le fragment après le seed
        scenario = FragmentScenario.launchInContainer(
                ClientsFragment.class,
                (Bundle) null,
                R.style.Theme_DolOrders, // adapte si besoin
                (FragmentFactory) null
        );
    }

    private void seedLocalStorageWithKnownClients() {
        GestionnaireStockageClient storage = new GestionnaireStockageClient(
                ApplicationProvider.getApplicationContext()
        );

        storage.clearClients();

        List<Client> seeded = new ArrayList<>();
        Date seedDate = new GregorianCalendar(2026, Calendar.JANUARY, 23).getTime();

        seeded.add(new Client.Builder()
                .setId("c1")
                .setNom("DUPONT")
                .setAdresse("1 rue de Test")
                .setCodePostal("75000")
                .setVille("Paris")
                .setTelephone("0102030405")
                .setAdresseMail("dupont@test.fr")
                .setUtilisateur("ui-test")
                .setDateSaisie(seedDate)
                .build());

        seeded.add(new Client.Builder()
                .setId("c2")
                .setNom("MARTIN")
                .setAdresse("10 avenue République")
                .setCodePostal("69000")
                .setVille("Lyon")
                .setTelephone("0607080910")
                .setAdresseMail("martin@test.fr")
                .setUtilisateur("ui-test")
                .setDateSaisie(seedDate)
                .build());

        seeded.add(new Client.Builder()
                .setId("c3")
                .setNom("DURAND")
                .setAdresse("5 place Bellecour")
                .setCodePostal("69002")
                .setVille("Lyon")
                .setTelephone("0611111111")
                .setAdresseMail("durand@test.fr")
                .setUtilisateur("ui-test")
                .setDateSaisie(seedDate)
                .build());

        boolean ok = storage.saveClients(seeded);
        if (!ok)
            throw new AssertionError("Impossible de seed le fichier clients_data.json pour les tests.");
    }

    @Test
    public void fragment_afficheRecyclerView_etBoutons() {
        onView(withId(R.id.listeClient)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_filtrer_clients)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_ajouter_client)).check(matches(isDisplayed()));
    }

    @Test
    public void ajoutClient_viaViewModel_ajouteUneLigneDansRecyclerView() {
        // Given: on pousse un client via le ViewModel observé par le fragment
        scenario.onFragment(fragment -> {
            // utiliser le scope du fragment pour récupérer le même ViewModel que le fragment
            ClientsFragmentViewModel vm =
                    new ViewModelProvider(fragment).get(ClientsFragmentViewModel.class);

            Client c = new Client.Builder()
                    .setId("test-1")
                    .setNom("DUPONT")
                    .setAdresse("1 rue de Test")
                    .setCodePostal("75000")
                    .setVille("Paris")
                    .setTelephone("0102030405")
                    .setAdresseMail("dupont@test.fr")
                    .setUtilisateur("ui-test")
                    .setDateSaisie(new GregorianCalendar(2026, Calendar.JANUARY, 23).getTime())
                    .build();

            vm.publierClientCree(c);
        });

        // Petite attente pour laisser le LiveData notifier l'UI (simple et efficace en UI tests)
        SystemClock.sleep(300);

        // Then: le RecyclerView doit contenir au moins 1 item
        onView(withId(R.id.listeClient)).check(new RecyclerViewItemCountAtLeastAssertion(1));
    }

    @Test
    public void filtre_ouvreDialog_etAfficheChamps() {
        onView(withId(R.id.btn_filtrer_clients)).perform(click());

        onView(withText("Filtrer les clients")).check(matches(isDisplayed()));
        onView(withId(R.id.filtreNom)).check(matches(isDisplayed()));
        onView(withId(R.id.filtreAdresse)).check(matches(isDisplayed()));
        onView(withId(R.id.filtreCodePostal)).check(matches(isDisplayed()));
        onView(withId(R.id.filtreVille)).check(matches(isDisplayed()));
        onView(withId(R.id.filtreTelephone)).check(matches(isDisplayed()));

        onView(withText("Annuler")).check(matches(isDisplayed()));
        onView(withText("Réinitialiser")).check(matches(isDisplayed()));
        onView(withText("Appliquer")).check(matches(isDisplayed()));
    }

    @Test
    public void filtre_appliquerPuisReset_retablitLaListe() {
        // On s'assure qu'il y a au moins 1 client au départ via le ViewModel
        scenario.onFragment(fragment -> {
            ClientsFragmentViewModel vm =
                    new ViewModelProvider(fragment).get(ClientsFragmentViewModel.class);

            Client c = new Client.Builder()
                    .setId("test-2")
                    .setNom("MARTIN")
                    .setAdresse("2 rue de Test")
                    .setCodePostal("69000")
                    .setVille("Lyon")
                    .setTelephone("0607080910")
                    .setAdresseMail("martin@test.fr")
                    .setUtilisateur("ui-test")
                    .setDateSaisie(new GregorianCalendar(2026, Calendar.JANUARY, 23).getTime())
                    .build();

            vm.publierClientCree(c);
        });
        SystemClock.sleep(300);

        // Vérif initiale
        onView(withId(R.id.listeClient)).check(new RecyclerViewItemCountAtLeastAssertion(1));

        // Ouvrir filtre
        onView(withId(R.id.btn_filtrer_clients)).perform(click());

        // Mettre un critère improbable (supposé ne matcher personne)
        onView(withId(R.id.filtreNom)).perform(replaceText("___NO_MATCH___"), closeSoftKeyboard());

        // Appliquer
        onView(withText("Appliquer")).perform(click());
        SystemClock.sleep(300);

        // Attendu: liste vide (si ton filter(...) renvoie bien 0 résultats quand aucun match)
        onView(withId(R.id.listeClient)).check(new RecyclerViewItemCountAssertion(0));

        // Réouvrir filtre et reset
        onView(withId(R.id.btn_filtrer_clients)).perform(click());
        onView(withText("Réinitialiser")).perform(click());
        SystemClock.sleep(300);

        // Attendu: la liste revient (au moins 1)
        onView(withId(R.id.listeClient)).check(new RecyclerViewItemCountAtLeastAssertion(1));
    }

    @Test
    public void boutonAjouterClient_remplaceParClientsAjoutFragment() {
        onView(withId(R.id.btn_ajouter_client)).perform(click());
        SystemClock.sleep(300);

        // Vérification "structurelle" : le Fragment courant est ClientsAjoutFragment
        scenario.onFragment(fragment -> {
            // Le fragment visible après replace est dans le parent FragmentManager.
            // On récupère le dernier fragment ajouté.
            int count = fragment.getParentFragmentManager().getFragments().size();
            if (count == 0) throw new AssertionError("Aucun fragment trouvé après navigation.");

            androidx.fragment.app.Fragment top =
                    fragment.getParentFragmentManager().getFragments().get(count - 1);

            if (!(top instanceof ClientsAjoutFragment)) {
                throw new AssertionError("Fragment attendu: ClientsAjoutFragment, obtenu: " + top.getClass().getName());
            }
        });
    }

    // -----------------------------
    // Helpers RecyclerView assertions
    // -----------------------------

    private static class RecyclerViewItemCountAssertion implements androidx.test.espresso.ViewAssertion {
        private final int expectedCount;

        RecyclerViewItemCountAssertion(int expectedCount) {
            this.expectedCount = expectedCount;
        }

        @Override
        public void check(View view, androidx.test.espresso.NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) throw noViewFoundException;
            RecyclerView rv = (RecyclerView) view;
            RecyclerView.Adapter<?> adapter = rv.getAdapter();
            if (adapter == null) throw new AssertionError("RecyclerView adapter est null");
            if (adapter.getItemCount() != expectedCount) {
                throw new AssertionError("ItemCount attendu=" + expectedCount + " obtenu=" + adapter.getItemCount());
            }
        }
    }

    private static class RecyclerViewItemCountAtLeastAssertion implements androidx.test.espresso.ViewAssertion {
        private final int minCount;

        RecyclerViewItemCountAtLeastAssertion(int minCount) {
            this.minCount = minCount;
        }

        @Override
        public void check(View view, androidx.test.espresso.NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) throw noViewFoundException;
            RecyclerView rv = (RecyclerView) view;
            RecyclerView.Adapter<?> adapter = rv.getAdapter();
            if (adapter == null) throw new AssertionError("RecyclerView adapter est null");
            int count = adapter.getItemCount();
            if (count < minCount) {
                throw new AssertionError("ItemCount attendu >= " + minCount + " obtenu=" + count);
            }
        }
    }

    // (Optionnel) matcher utile si tu veux cibler un item spécifique plus tard
    @NonNull
    private static Matcher<View> withRecyclerViewSizeAtLeast(int minSize) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                if (!(view instanceof RecyclerView)) return false;
                RecyclerView rv = (RecyclerView) view;
                return rv.getAdapter() != null && rv.getAdapter().getItemCount() >= minSize;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("RecyclerView avec taille >= " + minSize);
            }
        };
    }
}
