package com.example.dolorders.data.storage;

import android.content.Context;

/**
 * Version de test de ClientStorageManager qui utilise un fichier différent.
 * Permet de séparer les données de test des données de production.
 */
public class TestClientStorageManager extends ClientStorageManager {

    private static final String TEST_FILE_NAME = "test_clients_data.json";

    public TestClientStorageManager(Context context) {
        super(context);
    }

    /**
     * Surcharge pour utiliser un nom de fichier de test.
     * Cela permet d'isoler les tests des données de production.
     */
    @Override
    protected String getFileName() {
        return TEST_FILE_NAME;
    }
}

