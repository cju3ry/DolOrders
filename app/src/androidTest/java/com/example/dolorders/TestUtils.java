package com.example.dolorders;

import android.view.View;
import android.view.ViewParent;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class TestUtils {

    /**
     * Matcher qui vérifie le texte d'erreur d'un TextInputLayout parent,
     * quelle que soit la profondeur de l'enfant (EditText ou AutoCompleteTextView).
     */
    public static Matcher<View> hasTextInputLayoutErrorText(final String expectedErrorText) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                // On remonte la hiérarchie des vues pour trouver le TextInputLayout parent.
                ViewParent parent = view.getParent();
                while (parent != null && !(parent instanceof TextInputLayout)) {
                    parent = parent.getParent();
                }

                // Si on a trouvé un TextInputLayout, on vérifie son erreur.
                if (parent instanceof TextInputLayout) {
                    TextInputLayout textInputLayout = (TextInputLayout) parent;
                    CharSequence error = textInputLayout.getError();
                    return error != null && expectedErrorText.equals(error.toString());
                }

                // Si aucun TextInputLayout n'est trouvé, le test échoue.
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has parent TextInputLayout with error text: " + expectedErrorText);
            }
        };
    }
}
