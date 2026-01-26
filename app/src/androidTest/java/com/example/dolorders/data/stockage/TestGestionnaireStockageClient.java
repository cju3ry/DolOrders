package com.example.dolorders.data.stockage;

import android.content.Context;

import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;

/**
 * Version de test de ClientStorageManager qui utilise un fichier différent.
 * Permet de séparer les données de test des données de production.
 */
public class TestGestionnaireStockageClient extends GestionnaireStockageClient {

    private static final String TEST_FILE_NAME = "test_clients_data.json";

    public TestGestionnaireStockageClient(Context context) {
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

