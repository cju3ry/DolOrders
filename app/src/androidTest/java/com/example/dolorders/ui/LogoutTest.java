package com.example.dolorders.ui;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.dolorders.LoginActivity;
import com.example.dolorders.MainActivity;
import com.example.dolorders.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class LogoutTest {

    private SharedPreferences securePrefs;
    private Context appContext;

    // Valeurs de test
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_API_KEY = "8tx6JpQ69itXzK9bQbEK9qW17gmAZ41K";
    private static final String TEST_URL = "https://dolibarr.test/api";

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setup() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Supprimer les anciennes prefs si il y en avait déja
        appContext.deleteSharedPreferences("secure_prefs_crypto");

        MasterKey masterKey = new MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        // Créer les prefs cryptées
        securePrefs = EncryptedSharedPreferences.create(
                appContext,
                "secure_prefs_crypto",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        // Nettoyer les prefs avant chaque test
        securePrefs.edit().clear().commit();
    }

    /** Méthode pour initialiser un faux login */
    private void setupFakeLogin() {
        securePrefs.edit()
                .putString("username", TEST_USERNAME)
                .putString("api_key", TEST_API_KEY)
                .putString("base_url", TEST_URL)
                .putBoolean("is_logged_in", true)
                .commit();
    }

    /** Méthode générique pour se déconnecter et vérifier les prefs */
    private void logoutAndCheckPrefs() {
        // Ouvre le menu
        openActionBarOverflowOrOptionsMenu(appContext);

        // Clique sur "Se déconnecter"
        onView(withText("Se déconnecter")).perform(click());

        // Vérifie que l'on est bien sur la page de connexion
        onView(withId(R.id.login_layout)).check(matches(isDisplayed()));


        // Vérifie que les prefs sont bien vidées
        assertNull(securePrefs.getString("username", null));
        assertNull(securePrefs.getString("api_key", null));
        assertNull(securePrefs.getString("base_url", null));
        assertFalse(securePrefs.getBoolean("is_logged_in", false));
    }

    @Test
    public void testLogoutFromHome() {
        setupFakeLogin();
        ActivityScenario.launch(MainActivity.class);
        logoutAndCheckPrefs();
    }

    @Test
    public void testLogoutFromClients() {
        setupFakeLogin();
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.nav_clients)).perform(click());
        logoutAndCheckPrefs();
    }

    @Test
    public void testLogoutFromCommandes() {
        setupFakeLogin();
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.nav_commandes)).perform(click());
        logoutAndCheckPrefs();
    }
}
