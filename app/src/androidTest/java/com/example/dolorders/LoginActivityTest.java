// Emplacement : app/src/androidTest/java/com/example/dolorders/LoginActivityTest.java
package com.example.dolorders;

// Imports statiques pour un code plus lisible
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Classe de test pour LoginActivity.
 * Se concentre sur la validation des champs de saisie.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    // Cette règle lance LoginActivity avant chaque test et gère son cycle de vie.
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * Teste qu'un message d'erreur s'affiche si le champ 'Identifiant' est vide.
     */
    @Test
    public void emptyUsername_showsValidationError() {
        // 1. Laisser le champ identifiant vide, mais remplir les autres
        onView(withId(R.id.etUrl)).perform(typeText("http://test.url"));
        onView(withId(R.id.etPassword)).perform(typeText("un_mot_de_passe"), closeSoftKeyboard());

        // 2. Cliquer sur le bouton de connexion
        onView(withId(R.id.btnLogin)).perform(click());

        // 3. Vérifier que le champ 'etUsername' affiche le message d'erreur attendu
        // La chaîne "Identifiant requis" doit correspondre exactement à celle de votre code.
        onView(withId(R.id.etUsername)).check(matches(hasErrorText("Identifiant requis")));
    }

    /**
     * Teste qu'un message d'erreur s'affiche si le champ 'Mot de passe' est vide.
     */
    @Test
    public void emptyPassword_showsValidationError() {
        // 1. Laisser le champ mot de passe vide, mais remplir les autres
        onView(withId(R.id.etUrl)).perform(typeText("http://test.url"));
        onView(withId(R.id.etUsername)).perform(typeText("un_identifiant"), closeSoftKeyboard());

        // 2. Cliquer sur le bouton de connexion
        onView(withId(R.id.btnLogin)).perform(click());

        // 3. Vérifier que le champ 'etPassword' affiche le message d'erreur attendu
        // La chaîne "Mot de passe requis" doit correspondre exactement à celle de votre code.
        onView(withId(R.id.etPassword)).check(matches(hasErrorText("Mot de passe requis")));
    }

    /**
     * Teste qu'un message d'erreur s'affiche si le champ 'URL' est vide.
     */
    @Test
    public void emptyUrl_showsValidationError() {
        // 1. Laisser le champ URL vide, mais remplir les autres
        onView(withId(R.id.etUsername)).perform(typeText("un_identifiant"));
        onView(withId(R.id.etPassword)).perform(typeText("un_mot_de_passe"), closeSoftKeyboard());

        // 2. Cliquer sur le bouton de connexion
        onView(withId(R.id.btnLogin)).perform(click());

        // 3. Vérifier que le champ 'etUrl' affiche le message d'erreur attendu
        // La chaîne "URL requise" doit correspondre exactement à celle de votre code.
        onView(withId(R.id.etUrl)).check(matches(hasErrorText("URL requise")));
    }
}
