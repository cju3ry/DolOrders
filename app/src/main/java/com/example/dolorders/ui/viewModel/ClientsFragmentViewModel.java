package com.example.dolorders.ui.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.objet.Client;
import com.example.dolorders.repository.ClientApiRepository;

import java.util.ArrayList;
import java.util.List;

public class ClientsFragmentViewModel extends ViewModel {

    private static final String TAG = "ClientsFragmentVM";

    private final MutableLiveData<Client> clientCree = new MutableLiveData<>();
    private final MutableLiveData<List<Client>> listeClients = new MutableLiveData<>();

    private ClientApiRepository clientApiRepository;
    private GestionnaireStockageClient clientApiStorageManager;
    private GestionnaireStockageClient clientStorageManager;

    public LiveData<Client> getClientCree() {
        return clientCree;
    }

    public void publierClientCree(Client client) {
        clientCree.setValue(client);
    }

    // Optionnel: éviter rediffusion après rotation
    public void consommerClientCree() {
        clientCree.setValue(null);
    }

    public LiveData<List<Client>> getListeClients() {
        return listeClients;
    }

    /**
     * Charge tous les clients (locaux + API) fusionnés.
     * Utilisé pour afficher la liste complète dans le tableau des clients.
     *
     * @param context Le contexte nécessaire pour initialiser les storage managers
     */
    public void chargerTousLesClients(Context context) {
        if (clientStorageManager == null) {
            clientStorageManager = new GestionnaireStockageClient(context);
        }
        if (clientApiStorageManager == null) {
            clientApiStorageManager = new GestionnaireStockageClient(context, GestionnaireStockageClient.API_CLIENTS_FILE);
        }

        // Charger les clients locaux
        List<Client> clientsLocaux = clientStorageManager.loadClients();

        // Charger les clients de l'API
        List<Client> clientsApi = clientApiStorageManager.loadClients();

        // Fusionner les deux listes
        List<Client> tousLesClients = new ArrayList<>();
        tousLesClients.addAll(clientsLocaux);
        tousLesClients.addAll(clientsApi);

        Log.d(TAG, "Clients chargés : " + clientsLocaux.size() + " locaux + " +
              clientsApi.size() + " API = " + tousLesClients.size() + " total");

        listeClients.postValue(tousLesClients);
    }

    /**
     * Synchronise les clients avec l'API Dolibarr et les sauvegarde dans le cache API.
     * Utilisé lors de la synchronisation manuelle depuis l'accueil.
     *
     * @param context Le contexte nécessaire pour initialiser le repository
     */
    public void synchroniserClientsDepuisApi(Context context) {
        if (clientApiRepository == null) {
            clientApiRepository = new ClientApiRepository(context);
        }
        if (clientApiStorageManager == null) {
            clientApiStorageManager = new GestionnaireStockageClient(context, GestionnaireStockageClient.API_CLIENTS_FILE);
        }

        // Appeler l'API via le Repository
        clientApiRepository.synchroniserDepuisApi(new ClientApiRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                Log.d(TAG, "Clients synchronisés depuis l'API : " + clients.size());

                // Sauvegarder dans le cache API
                clientApiStorageManager.saveClients(clients);

                // Recharger tous les clients pour mettre à jour l'affichage
                chargerTousLesClients(context);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Erreur lors de la synchronisation des clients : " + message);

                // En cas d'erreur API, charger depuis le cache si disponible
                chargerTousLesClients(context);
            }
        });
    }
}
