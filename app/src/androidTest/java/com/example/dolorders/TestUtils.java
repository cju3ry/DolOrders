package com.example.dolorders;

import android.view.View;
import com.google.android.material.textfield.TextInputLayout;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TestUtils {

    /**
     * Renvoie un Matcher qui vérifie si un TextInputLayout affiche un texte d'erreur spécifique.
     * @param expectedErrorText Le message d'erreur attendu.
     * @return Un Matcher pour la vérification avec Espresso.
     */
    public static Matcher<View> withError(final String expectedErrorText) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                // Vérifie si la vue est bien un TextInputLayout
                if (!(view.getParent().getParent() instanceof TextInputLayout)) {
                    return false;
                }
                // Récupère le message d'erreur du TextInputLayout parent
                CharSequence error = ((TextInputLayout) view.getParent().getParent()).getError();
                if (error == null) {
                    return false;
                }
                String hint = error.toString();
                // Compare le message d'erreur avec celui attendu
                return expectedErrorText.equals(hint);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with error: " + expectedErrorText);
            }
        };
    }
}
