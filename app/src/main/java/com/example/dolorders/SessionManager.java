package com.example.dolorders;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SessionManager {

    private static final String PREF_NAME = "DolOrdersPrefs";

    public static void logout(Context context) {

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        Toast.makeText(context, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
    }
}
