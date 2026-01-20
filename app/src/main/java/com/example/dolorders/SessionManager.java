package com.example.dolorders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {

    private static final String PREF_NAME = "DolOrdersPrefs";

    /**
     * MÉTHODE DE DÉCONNEXION
     * Efface toutes les données cryptées et redirige vers LoginActivity
     *
     * Utilisation dans une autre activité (ex: MainActivity):
     * LoginActivity.logout(this);
     */
    public static void logout(AppCompatActivity activity) {
        try {
            // Clé maître pour le cryptage
            MasterKey masterKey = new MasterKey.Builder(activity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // SharedPreferences cryptées exactement comme dans LoginActivity
            SharedPreferences securePrefs = EncryptedSharedPreferences.create(
                    activity,
                    "secure_prefs_crypto",  // <== IMPORTANT
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Sauvegarde l'URL avant d'effacer (pour la réutiliser à la prochaine connexion)
            String lastUrl = securePrefs.getString("base_url", null);
            android.util.Log.d("LOGOUT_DEBUG", "URL sauvegardée avant effacement: " + lastUrl);

            // Efface toutes les données cryptées
            securePrefs.edit().clear().apply();

            // Restaure l'URL dans les SharedPreferences normales (non sensible)
            if (lastUrl != null && !lastUrl.isEmpty()) {
                SharedPreferences normalPrefs = activity.getSharedPreferences("DolOrdersPrefs", Context.MODE_PRIVATE);
                normalPrefs.edit().putString("last_used_url", lastUrl).apply();
                android.util.Log.d("LOGOUT_DEBUG", "URL restaurée pour la prochaine connexion");
            }

            android.util.Log.d("LOGOUT_DEBUG", "Données cryptées effacées avec succès");

            // Redirection vers LoginActivity
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();

            Toast.makeText(activity, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        } catch (GeneralSecurityException | IOException e) {
            android.util.Log.e("LOGOUT_DEBUG", "Erreur lors de la déconnexion", e);
            Toast.makeText(activity, "Erreur lors de la déconnexion", Toast.LENGTH_LONG).show();
        }
    }

}
