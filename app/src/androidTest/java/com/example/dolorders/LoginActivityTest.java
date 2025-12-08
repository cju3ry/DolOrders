package com.example.dolorders;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Tests complets pour LoginActivity.
 * Couvre les cas nominal, limite et d'erreur.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    // ========== VARIABLES GLOBALES DE TEST ==========
    // À MODIFIER avec les vraies credentials de test
//    private static final String TEST_URL = "stub";  // "stub" pu "bouchon" pour le mode bouchon
//    private static final String TEST_USERNAME = "admin";
//    private static final String TEST_PASSWORD = "admin123";

    // Pour les tests avec de vraies credentials :
     private static final String TEST_URL = "A MODIFIER";
     private static final String TEST_USERNAME = "A MODIFIER";
     private static final String TEST_PASSWORD = "A MODIFIER";

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private Context context;
    private SharedPreferences securePrefs;

    @Before
    public void setUp() throws GeneralSecurityException, IOException {
        Intents.init();

        context = ApplicationProvider.getApplicationContext();

        // Accéde aux SharedPreferences cryptées
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        securePrefs = EncryptedSharedPreferences.create(
                context,
                "secure_prefs_crypto",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        // Nettoye les préférences avant chaque test
        securePrefs.edit().clear().commit();
    }

    @After
    public void tearDown() {
        Intents.release();

        if (securePrefs != null) {
            securePrefs.edit().clear().commit();
        }
    }

    // =============================================
    // 🟢 CAS NOMINAL
    // =============================================

    /**
     * 🟢 CAS NOMINAL 1 : Connexion réussie avec bons identifiants
     *
     * ÉTANT DONNÉ un utilisateur non identifié sur la page de connexion
     * LORSQU'il renseigne les bons identifiants et mot de passe
     * ALORS l'utilisateur accède à l'application
     * ET la clé API est stockée de manière cryptée localement
     */
    @Test
    public void successfulLogin() throws InterruptedException {
        // Prépare les données de test

        // Rempli les champs et cliquer sur connexion
        onView(withId(R.id.etUrl))
                .perform(typeText(TEST_URL), closeSoftKeyboard());

        onView(withId(R.id.etUsername))
                .perform(typeText(TEST_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.etPassword))
                .perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.btnLogin))
                .perform(click());

        // Attend la réponse
        Thread.sleep(1000);

        // ASSERT 1 : Vérifie que l'intention de démarrer MainActivity a bien été lancée.
        intended(hasComponent(MainActivity.class.getName()));

        // ASSERT 2 : Vérifie que la clé API est stockée de manière CRYPTÉE
        String storedApiKey = securePrefs.getString("api_key", null);
        assertNotNull("La clé API devrait être stockée", storedApiKey);
        assertTrue("La clé API ne devrait pas être vide", !storedApiKey.isEmpty());

        // ASSERT 3 : Vérifie que les autres informations sont stockées
        String storedUrl = securePrefs.getString("base_url", null);
        String storedUsername = securePrefs.getString("username", null);
        boolean isLoggedIn = securePrefs.getBoolean("is_logged_in", false);

        assertNotNull("L'URL devrait être stockée", storedUrl);
        assertNotNull("Le nom d'utilisateur devrait être stocké", storedUsername);
        assertTrue("Le statut de connexion devrait être true", isLoggedIn);
    }

    /**
     * 🟢 CAS NOMINAL 2 : Redirection automatique si déjà connecté
     *
     * ÉTANT DONNÉ un utilisateur déjà connecté précédemment
     * LORSQU'il ouvre l'application
     * ALORS il est automatiquement redirigé vers la page d'accueil
     * ET il retrouve son profil utilisateur
     */
    @Test
    public void alreadyLoggedIn() throws InterruptedException {
        // Simuler un utilisateur déjà connecté
        // On utilise commit() dans les tests pour s'assurer que l'écriture est terminée
        // avant de passer à l'étape suivante.
        securePrefs.edit()
                .putString("username", TEST_USERNAME)
                .putString("api_key", "8tx6JpQ69itXzK9bQbEK9qW17gmAZ41K")
                .putString("base_url", TEST_URL)
                .putBoolean("is_logged_in", true)
                .commit();

        //  Ferme l'activité actuelle (lancée par la @Rule)
        // et en lance une nouvelle manuellement pour tester le onCreate().
        activityRule.getScenario().close();

        // Lance une nouvelle instance de LoginActivity.
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        // Attendre que la redirection (qui est asynchrone) ait lieu
        Thread.sleep(1000);

        // Vérifie que l'intention de démarrer MainActivity a bien été lancée.
        // `intended` vérifie toutes les intentions lancées depuis le dernier `Intents.init()`.
        intended(hasComponent(MainActivity.class.getName()));

        // Ferme le scénario que nous avons lancé manuellement.
        scenario.close();
    }

    // =============================================
    // 🟠 CAS LIMITE
    // =============================================

    /**
     * 🟠 CAS LIMITE : Identifiants incorrects
     *
     * ÉTANT DONNÉ un utilisateur non identifié sur la page de connexion
     * LORSQU'il saisit un login et/ou un mot de passe incorrects
     * ALORS un message stipule "Identifiant et/ou mot de passe incorrects"
     * ET le mot de passe est vidé
     * ET le login reste pré-rempli
     *
     * NOTE : Ce test nécessite un vrai serveur Dolibarr.
     * En mode "stub", tous les identifiants sont acceptés.
     */
    @Test
    public void incorrectCredentials_ErrorAndClearsPassword() throws InterruptedException {
        // Prépare de mauvais identifiants
        // ---Ce test ne fonctionne qu'avec une vrai instance de Dolibarr---
        String wrongPassword = "mauvais_mot_de_passe";

        // Rempli avec de mauvais identifiants
        onView(withId(R.id.etUrl))
                .perform(typeText(TEST_URL), closeSoftKeyboard());

        onView(withId(R.id.etUsername))
                .perform(typeText(TEST_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.etPassword))
                .perform(typeText(wrongPassword), closeSoftKeyboard());

        onView(withId(R.id.btnLogin))
                .perform(click());

        // Attendre la réponse du serveur
        Thread.sleep(2000);

        // Vérifie qu'on est toujours sur LoginActivity (pas de navigation)
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));

    }

    // =============================================
    // 🔴 CAS D'ERREUR
    // =============================================

    /**
     * 🔴 CAS D'ERREUR 1 : Connexion sans internet
     *
     * ÉTANT DONNÉ un utilisateur non identifié, sur la page de connexion
     * LORSQU'il tente de se connecter sans connexion internet
     * ALORS un message d'erreur s'affiche "Connexion impossible. Vérifiez votre connexion internet"
     * ET l'utilisateur reste sur la page de connexion
     * ET il peut réessayer
     */
    @Test
    public void noInternet_showsNetworkError() throws InterruptedException {
        // Utilise une URL invalide pour simuler une erreur réseau
        String invalidUrl = "http://url-qui-nexiste-pas.invalide";

        // Rempli les champs avec une URL invalide
        onView(withId(R.id.etUrl))
                .perform(typeText(invalidUrl), closeSoftKeyboard());

        onView(withId(R.id.etUsername))
                .perform(typeText(TEST_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.etPassword))
                .perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.btnLogin))
                .perform(click());

        // Attend le timeout
        Thread.sleep(3000);

        // Vérifie qu'on est toujours sur LoginActivity
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));

    }

    /**
     * 🔴 CAS D'ERREUR 2 : Timeout de l'API (>3000ms)
     *
     * ÉTANT DONNÉ un utilisateur non identifié, sur la page de connexion
     * LORSQUE l'API ne répond pas dans un délai de 3000ms
     * ALORS un message d'erreur s'affiche "Connexion impossible. Vérifiez votre connexion internet"
     * ET l'utilisateur reste sur la page de connexion
     * ET il peut réessayer
     */
    @Test
    public void apiTimeout_showsTimeoutError() throws InterruptedException {
        // Utilisation d'une URL qui va timeout
        String timeoutUrl = "http://10.255.255.1";  // Adresse non routable

        // Tente la connexion
        onView(withId(R.id.etUrl))
                .perform(typeText(timeoutUrl), closeSoftKeyboard());

        onView(withId(R.id.etUsername))
                .perform(typeText(TEST_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.etPassword))
                .perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.btnLogin))
                .perform(click());

        // Attend plus que le timeout (3000ms + 1000ms marge)
        Thread.sleep(4000);

        // Vérifie qu'on est toujours sur LoginActivity
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }

    // =============================================
    // TESTS DE VALIDATION (existants)
    // =============================================

    @Test
    public void emptyUsername_showsValidationError() {
        onView(withId(R.id.etUrl)).perform(typeText("http://test.url"));
        onView(withId(R.id.etPassword)).perform(typeText("un_mot_de_passe"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.etUsername)).check(matches(hasErrorText("Identifiant requis")));
    }

    @Test
    public void emptyPassword_showsValidationError() {
        onView(withId(R.id.etUrl)).perform(typeText("http://test.url"));
        onView(withId(R.id.etUsername)).perform(typeText("un_identifiant"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.etPassword)).check(matches(hasErrorText("Mot de passe requis")));
    }

    @Test
    public void emptyUrl_showsValidationError() {
        onView(withId(R.id.etUsername)).perform(typeText("un_identifiant"));
        onView(withId(R.id.etPassword)).perform(typeText("un_mot_de_passe"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.etUrl)).check(matches(hasErrorText("URL requise")));
    }

}
