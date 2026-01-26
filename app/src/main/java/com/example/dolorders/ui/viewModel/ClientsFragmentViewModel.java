package com.example.dolorders.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dolorders.objet.Client;

public class ClientsFragmentViewModel extends ViewModel {
    private final MutableLiveData<Client> clientCree = new MutableLiveData<>();

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

}
