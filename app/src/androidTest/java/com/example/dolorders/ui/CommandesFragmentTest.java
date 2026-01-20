package com.example.dolorders.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dolorders.MainActivity;
import com.example.dolorders.R;
import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CommandesFragmentTest {

    private ActivityScenario<MainActivity> activityScenario;

    @Before
    public void setup() {
        activityScenario = ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.nav_commandes)).perform(click());
    }

    @After
    public void tearDown() {
        if (activityScenario != null) {
            activityScenario.close();
        }
    }

    public static Matcher<View> hasTextInputLayoutError(final String expectedErrorText) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }
                CharSequence error = ((TextInputLayout) view).getError();
                // On gère le cas où l'erreur est null
                if (error == null) {
                    return expectedErrorText == null;
                }
                return expectedErrorText.equals(error.toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with error: " + expectedErrorText);
            }
        };
    }

//    @Test
//    public void lesChampsSontAffichesCorrectement() {
//        onView(withId(R.id.auto_complete_client)).check(matches(isDisplayed()));
//        onView(withId(R.id.edit_text_date)).check(matches(isDisplayed()));
//        onView(withId(R.id.auto_complete_article)).check(matches(isDisplayed()));
//        onView(withId(R.id.edit_text_remise)).check(matches(isDisplayed()));
//        onView(withId(R.id.btn_valider)).check(matches(isDisplayed()));
//    }

    @Test
    public void validation_afficheErreurSiClientEstVide() {
        // attente de 500 ms pour s'assurer que le fragment est bien chargé
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        onView(withId(R.id.btn_valider)).perform(click());

        onView(withId(R.id.auto_complete_client))
                .check(matches(hasErrorText("Veuillez sélectionner un client")));
    }
        }

//    @Test
//    public void validation_afficheErreurSiArticlesSontVides() {
//
//        onView(withId(R.id.auto_complete_client)).perform(ViewActions.replaceText("Dupont"), ViewActions.closeSoftKeyboard());
//
//        onView(withText("Dupont")).perform(click());
//
//        onView(withId(R.id.btn_valider)).perform(click());
//
//        onView(withId(R.id.auto_complete_article))
//                .check(matches(hasErrorText("Veuillez ajouter au moins un article")));
//    }
}
