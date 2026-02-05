package com.example.dolorders.ui.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.data.stockage.produit.ProduitStorageManager;
import com.example.dolorders.objet.Client;
import com.example.dolorders.objet.LigneCommande;
import com.example.dolorders.objet.Produit;
import com.example.dolorders.repository.ProduitRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommandesFragmentViewModel extends ViewModel {

    private static final String TAG = "CommandesFragmentVM";

    private final MutableLiveData<List<LigneCommande>> lignesCommande = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Client> clientSelectionne = new MutableLiveData<>();
    private final MutableLiveData<String> date = new MutableLiveData<>();
    private final MutableLiveData<List<Client>> listeClients = new MutableLiveData<>();
    private final MutableLiveData<List<Produit>> listeProduits = new MutableLiveData<>();
    private final MutableLiveData<Boolean> fromAccueil = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> fromListeClients = new MutableLiveData<>(false);

    private ProduitRepository produitRepository;
    private ProduitStorageManager produitStorageManager;
    private GestionnaireStockageClient clientStorageManager;
    private GestionnaireStockageClient clientApiStorageManager;

    // --- Getters ---
    public LiveData<List<LigneCommande>> getLignesCommande() {
        return lignesCommande;
    }

    public LiveData<Client> getClientSelectionne() {
        return clientSelectionne;
    }

    public LiveData<String> getDate() {
        return date;
    }

    public LiveData<List<Client>> getListeClients() {
        return listeClients;
    }

    public LiveData<List<Produit>> getListeProduits() {
        return listeProduits;
    }

    public LiveData<Boolean> getFromAccueil() {
        return fromAccueil;
    }

    public LiveData<Boolean> getFromListeClients() {
        return fromListeClients;
    }

    public void setFromAccueil() {
        fromAccueil.setValue(true);
        fromListeClients.setValue(false);
    }

    public void setFromListeClients() {
        fromListeClients.setValue(true);
        fromAccueil.setValue(false);
    }

    public boolean consumeFromAccueil() {
        Boolean value = fromAccueil.getValue();
        fromAccueil.setValue(false);
        return value != null && value;
    }

    public boolean consumeFromListeClients() {
        Boolean value = fromListeClients.getValue();
        fromListeClients.setValue(false);
        return value != null && value;
    }

    public void setClientSelectionne(Client client) {
        this.clientSelectionne.setValue(client);
    }

    public void setDate(String date) {
        this.date.setValue(date);
    }

    /**
     * Ajoute ou remplace une ligne de commande complète (venant de la pop-up).
     */
    public void addLigne(LigneCommande nouvelleLigne) {
        List<LigneCommande> currentList = lignesCommande.getValue();
        if (currentList == null) currentList = new ArrayList<>();

        List<LigneCommande> newList = new ArrayList<>(currentList);

        // On cherche si le produit existe déjà pour le remplacer, sinon on ajoute
        boolean found = false;
        for (int i = 0; i < newList.size(); i++) {
            if (newList.get(i).getProduit().getId().equals(nouvelleLigne.getProduit().getId())) {
                newList.set(i, nouvelleLigne);
                found = true;
                break;
            }
        }

        if (!found) {
            newList.add(nouvelleLigne);
        }

        lignesCommande.setValue(newList);
    }

    public void removeLigne(LigneCommande ligneToDelete) {
        List<LigneCommande> currentList = lignesCommande.getValue();
        if (currentList != null) {
            List<LigneCommande> newList = new ArrayList<>(currentList);
            newList.removeIf(l -> l.getProduit().getId().equals(ligneToDelete.getProduit().getId()));
            lignesCommande.setValue(newList);
        }
    }

    public void startNouvelleCommandePour(Client client) {
        clientSelectionne.setValue(client);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        date.setValue(sdf.format(new Date()));
        lignesCommande.setValue(new java.util.ArrayList<>());
    }

    public void chargerProduits(Context context) {
        if (produitRepository == null) {
            produitRepository = new ProduitRepository(context);
        }
        if (produitStorageManager == null) {
            produitStorageManager = new ProduitStorageManager(context);
        }

        // Appeler l'API via le Repository (qui s'occupe uniquement de l'API)
        produitRepository.synchroniserDepuisApi(new ProduitRepository.ProduitCallback() {
            @Override
            public void onSuccess(List<Produit> produits) {
                Log.d(TAG, "Produits synchronisés depuis l'API : " + produits.size());

                // Sauvegarder dans le cache
                produitStorageManager.saveProduits(produits);

                // Mettre à jour le LiveData
                listeProduits.postValue(produits);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Erreur lors de la synchronisation des produits : " + message);

                // En cas d'erreur API, charger depuis le cache si disponible
                List<Produit> produitsCache = produitStorageManager.loadProduits();
                if (produitsCache != null && !produitsCache.isEmpty()) {
                    Log.d(TAG, "Fallback sur le cache : " + produitsCache.size() + " produits");
                    listeProduits.postValue(produitsCache);
                } else {
                    listeProduits.postValue(new ArrayList<>());
                }
            }
        });
    }

    public void updateLigne(LigneCommande oldLigne, int newQty, double newRemise) {
        List<LigneCommande> currentList = lignesCommande.getValue();
        if (currentList != null) {
            List<LigneCommande> newList = new ArrayList<>();
            for (LigneCommande l : currentList) {
                if (l.getProduit().getId().equals(oldLigne.getProduit().getId())) {
                    if (newQty > 0 && newRemise >= 0 && newRemise <= 100) {
                        // On crée une nouvelle ligne validée par défaut
                        newList.add(new LigneCommande(l.getProduit(), newQty, newRemise, true));
                    } else {
                        newList.add(l);
                    }
                } else {
                    newList.add(l);
                }
            }
            lignesCommande.setValue(newList);
        }
    }

    public double getTotal() {
        List<LigneCommande> list = lignesCommande.getValue();
        double total = 0.0;
        if (list != null) {
            for (LigneCommande l : list) {
                total += l.getMontantLigne();
            }
        }
        return total;
    }

    public void clear() {
        setClientSelectionne(null);
        setDate(null);
        lignesCommande.setValue(new ArrayList<>());
    }

    public void chargerProduitsDepuisCache(Context context) {
        if (produitStorageManager == null) {
            produitStorageManager = new ProduitStorageManager(context);
        }
        List<Produit> produitsCache = produitStorageManager.loadProduits();
        if (produitsCache != null && !produitsCache.isEmpty()) {
            Log.d(TAG, "Produits chargés depuis le cache : " + produitsCache.size());
            listeProduits.postValue(produitsCache);
        } else {
            listeProduits.postValue(new ArrayList<>());
        }
    }

    public void chargerTousLesClients(Context context) {
        if (clientStorageManager == null) {
            clientStorageManager = new GestionnaireStockageClient(context);
        }
        if (clientApiStorageManager == null) {
            clientApiStorageManager = new GestionnaireStockageClient(context, GestionnaireStockageClient.API_CLIENTS_FILE);
        }
        List<Client> clientsLocaux = clientStorageManager.loadClients();
        List<Client> clientsApi = clientApiStorageManager.loadClients();
        List<Client> tousLesClients = new ArrayList<>();
        tousLesClients.addAll(clientsLocaux);
        tousLesClients.addAll(clientsApi);
        listeClients.setValue(tousLesClients);
    }
}