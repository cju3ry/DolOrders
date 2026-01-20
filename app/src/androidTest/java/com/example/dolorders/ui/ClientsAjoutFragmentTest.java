package com.example.dolorders.ui;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.dolorders.MainActivity;
import com.example.dolorders.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// Importations statiques pour Espresso
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ClientsAjoutFragmentTest {

    /**
     * On lance la MainActivity directement. Elle se chargera de créer et d'afficher
     * le ClientsFragment, avec le bon thème et le bon contexte.
     */
    @Before
    public void setup() {
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.nav_clients)).perform(click());
    }

    @Test
    public void lesChampsEtBoutonsSontAffichesCorrectement() {
        onView(withId(R.id.edit_text_nom)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_adresse)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_code_postal)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_ville)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_telephone)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_annuler)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_valider)).check(matches(isDisplayed()));
    }

    @Test
    public void validation_afficheErreurSiNomEstVide() {
        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_nom)).check(matches(ViewMatchers.hasErrorText("Le nom du client est requis")));
    }

    @Test
    public void validation_afficheErreurSiAdresseEstVide() {
        // On remplit le nom pour passer à la validation suivante
        onView(withId(R.id.edit_text_nom)).perform(typeText("Client Test"));
        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_adresse)).check(matches(ViewMatchers.hasErrorText("L'adresse est requise")));
    }

    @Test
    public void validation_afficheErreurSiCodePostalEstInvalide() {
        onView(withId(R.id.edit_text_nom)).perform(typeText("Client Test"));
        onView(withId(R.id.edit_text_adresse)).perform(typeText("123 rue du Test"));
        onView(withId(R.id.edit_text_code_postal)).perform(typeText("123")); // CP invalide
        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_code_postal)).check(matches(ViewMatchers.hasErrorText("Le code postal doit contenir 5 chiffres")));
    }

    @Test
    public void validation_afficheErreurSiVilleEstVide() {
        onView(withId(R.id.edit_text_nom)).perform(typeText("Client Test"));
        onView(withId(R.id.edit_text_adresse)).perform(typeText("123 rue du Test"));
        onView(withId(R.id.edit_text_code_postal)).perform(typeText("69001"));
        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_ville)).check(matches(ViewMatchers.hasErrorText("La ville est requise")));
    }

    @Test
    public void validation_afficheErreurSiEmailEstInvalide() {
        onView(withId(R.id.edit_text_nom)).perform(typeText("Client Test"));
        onView(withId(R.id.edit_text_adresse)).perform(typeText("123 rue du Test"));
        onView(withId(R.id.edit_text_code_postal)).perform(typeText("69001"));
        onView(withId(R.id.edit_text_ville)).perform(typeText("Lyon"));
        onView(withId(R.id.edit_text_email)).perform(typeText("email-invalide")); // Email invalide
        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_email)).check(matches(ViewMatchers.hasErrorText("L'adresse e-mail n'est pas valide")));
    }

    @Test
    public void validation_afficheErreurSiTelephoneEstInvalide() {
        onView(withId(R.id.edit_text_nom)).perform(typeText("Client Test"));
        onView(withId(R.id.edit_text_adresse)).perform(typeText("123 rue du Test"));
        onView(withId(R.id.edit_text_code_postal)).perform(typeText("69001"));
        onView(withId(R.id.edit_text_ville)).perform(typeText("Lyon"));
        onView(withId(R.id.edit_text_email)).perform(typeText("test@test.com"));
        onView(withId(R.id.edit_text_telephone)).perform(typeText("010203")); // Tel invalide
        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_telephone)).check(matches(ViewMatchers.hasErrorText("Le téléphone doit contenir 10 chiffres")));
    }

    @Test
    public void validation_reussitAvecTousLesChampsValides() {
        onView(withId(R.id.edit_text_nom)).check(matches(isDisplayed()));

        onView(withId(R.id.edit_text_nom)).perform(typeText("Martin SARL"), closeSoftKeyboard());
        onView(withId(R.id.edit_text_adresse)).perform(typeText("123 Avenue du Test"), closeSoftKeyboard());
        onView(withId(R.id.edit_text_code_postal)).perform(typeText("69001"), closeSoftKeyboard());
        onView(withId(R.id.edit_text_ville)).perform(typeText("Lyon"), closeSoftKeyboard());
        onView(withId(R.id.edit_text_email)).perform(typeText("contact@martin.fr"), closeSoftKeyboard());
        onView(withId(R.id.edit_text_telephone)).perform(typeText("0401020304"), closeSoftKeyboard());

        onView(withId(R.id.btn_valider)).perform(click());

        onView(withId(R.id.nav_home)).check(matches(isDisplayed()));
    }

    @Test
    public void boutonAnnuler_afficheLaBoiteDeDialogueDeConfirmation() {
        onView(withId(R.id.btn_annuler)).perform(click());
        onView(withText("Annuler la saisie")).check(matches(isDisplayed()));
        onView(withText("Voulez-vous vraiment annuler ? Toutes les données saisies seront perdues.")).check(matches(isDisplayed()));
        onView(withText("Oui, annuler")).check(matches(isDisplayed()));
        onView(withText("Non")).check(matches(isDisplayed()));
    }
}