package com.example.dolorders.data.stockage.produit;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.dolorders.objet.Produit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de stockage local pour les produits.
 * Utilise SharedPreferences et Gson pour sauvegarder/charger les produits en JSON.
 */
public class ProduitStorageManager {

    private static final String PREF_NAME = "produits_storage";
    private static final String KEY_PRODUITS = "produits_list";

    private final SharedPreferences preferences;
    private final Gson gson;

    /**
     * Constructeur.
     *
     * @param context Contexte Android nécessaire pour SharedPreferences
     */
    public ProduitStorageManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Configuration Gson avec l'adapter personnalisé
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Produit.class, new ProduitTypeAdapter());
        gson = builder.create();
    }

    /**
     * Sauvegarde la liste des produits dans SharedPreferences.
     *
     * @param produits Liste des produits à sauvegarder
     */
    public void saveProduits(List<Produit> produits) {
        if (produits == null) {
            produits = new ArrayList<>();
        }

        String json = gson.toJson(produits);
        preferences.edit().putString(KEY_PRODUITS, json).apply();
    }

    /**
     * Charge la liste des produits depuis SharedPreferences.
     *
     * @return Liste des produits, ou liste vide si aucun produit sauvegardé
     */
    public List<Produit> loadProduits() {
        String json = preferences.getString(KEY_PRODUITS, null);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<Produit>>(){}.getType();
            List<Produit> produits = gson.fromJson(json, type);
            return produits != null ? produits : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Supprime tous les produits du stockage.
     */
    public void clearProduits() {
        preferences.edit().remove(KEY_PRODUITS).apply();
    }

    /**
     * Vérifie si des produits sont présents dans le stockage.
     *
     * @return true si des produits existent, false sinon
     */
    public boolean hasProduits() {
        return preferences.contains(KEY_PRODUITS) && !loadProduits().isEmpty();
    }
}

