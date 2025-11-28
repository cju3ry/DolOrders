package com.example.dolorders.ui;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dolorders.MainActivity;
import com.example.dolorders.R;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

/**
 * Tests d'instrumentation pour CommandesFragment.
 * Ces tests valident le comportement de l'UI et la logique de validation de la vue.
 */
@RunWith(AndroidJUnit4.class)
public class CommandesFragmentTest {

    @Before
    public void setup() {
        // Lance l'activité et navigue vers le fragment des commandes
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.nav_commandes)).perform(click());
    }

    @Test
    public void lesChampsSontAffichesCorrectement() {
        onView(withId(R.id.auto_complete_client)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_date)).check(matches(isDisplayed()));
        onView(withId(R.id.auto_complete_article)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_remise)).check(matches(isDisplayed()));
        onView(withId(R.id.layout_articles_selectionnes)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_annuler)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_valider)).check(matches(isDisplayed()));
        // Le bouton valider doit être désactivé au départ
        onView(withId(R.id.btn_valider)).check(matches(not(isDisplayed())));
    }

    @Test
    public void validation_afficheErreurSiClientEstVide() {
        // On clique sur valider sans rien remplir
        onView(withId(R.id.btn_valider)).perform(click());
        // On vérifie que le champ client a la bonne erreur
        onView(withId(R.id.auto_complete_client)).check(matches(hasErrorText("Veuillez sélectionner un client")));
    }

    @Test
    public void validation_afficheErreurSiArticlesSontVides() {
        // On sélectionne un client pour passer la première validation
        onView(withId(R.id.auto_complete_client)).perform(typeText("Dupont"));
        // On suppose que l'auto-complétion fait son travail, pour un test plus robuste il faudrait utiliser onData(...)
        onView(withId(R.id.btn_valider)).perform(click());
        // On vérifie que le champ article a la bonne erreur
        onView(withId(R.id.auto_complete_article)).check(matches(hasErrorText("Veuillez ajouter au moins un article")));
    }

    @Test
    public void validation_afficheErreurSiDateEstVide() {
        // On remplit le client
        onView(withId(R.id.auto_complete_client)).perform(typeText("Dupont"));
        // On ajoute un article
        onView(withId(R.id.auto_complete_article)).perform(typeText("Stylo"));

        // On vide la date (au cas où elle est pré-remplie)
        onView(withId(R.id.edit_text_date)).perform(typeText(""));

        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_date)).check(matches(hasErrorText("La date de la commande est requise")));
    }

    @Test
    public void validation_afficheErreurSiFormatDateEstInvalide() {
        // On remplit le client et un article
        onView(withId(R.id.auto_complete_client)).perform(typeText("Dupont"));
        onView(withId(R.id.auto_complete_article)).perform(typeText("Stylo"));
        // On saisit une date invalide
        onView(withId(R.id.edit_text_date)).perform(typeText("32/01/2025"));

        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_date)).check(matches(hasErrorText("Le format de la date est invalide (jj/mm/aaaa)")));
    }

    @Test
    public void validation_afficheErreurSiRemiseEstNegative() {
        // On remplit tout ce qui est nécessaire
        onView(withId(R.id.auto_complete_client)).perform(typeText("Dupont"));
        onView(withId(R.id.auto_complete_article)).perform(typeText("Stylo"));

        // On choisit une date valide via le date picker pour éviter les problèmes de format
        onView(withId(R.id.edit_text_date)).perform(click());
        onView(withId(android.R.id.button1)).perform(click()); // "OK" sur le date picker

        // On met une remise négative
        onView(withId(R.id.edit_text_remise)).perform(typeText("-5"));

        onView(withId(R.id.btn_valider)).perform(click());
        onView(withId(R.id.edit_text_remise)).check(matches(hasErrorText("La remise ne peut pas être négative")));
    }
}
