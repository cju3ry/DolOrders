package com.example.dolorders.ui.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    public void addArticle(Produit produit) {
        List<LigneCommande> currentList = lignesCommande.getValue();
        if (currentList == null) currentList = new ArrayList<>();

        // Vérifier si le produit existe déjà
        for (LigneCommande ligne : currentList) {
            if (ligne.getProduit().getId().equals(produit.getId())) {
                return;
            }
        }

        // Si pas trouvé, on l'ajoute avec quantité 1
        List<LigneCommande> newList = new ArrayList<>(currentList);
        newList.add(new LigneCommande(produit, 1, 0.0));
        lignesCommande.setValue(newList);
    }

    public void removeLigne(LigneCommande ligneToDelete) {
        List<LigneCommande> currentList = lignesCommande.getValue();
        if (currentList != null) {
            List<LigneCommande> newList = new ArrayList<>(currentList);
            // Suppression basée sur l'égalité des objets ou ID
            newList.removeIf(l -> l.getProduit().getId().equals(ligneToDelete.getProduit().getId()));
            lignesCommande.setValue(newList);
        }
    }

    public void updateLigne(LigneCommande oldLigne, int newQty, double newRemise) {
        List<LigneCommande> currentList = lignesCommande.getValue();
        if (currentList != null) {
            List<LigneCommande> newList = new ArrayList<>();
            for (LigneCommande l : currentList) {
                if (l.getProduit().getId().equals(oldLigne.getProduit().getId())) {
                    if (newQty > 0 && newRemise >= 0 && newRemise <= 100) {
                        try {
                            // Conserver l'état validé lors de la mise à jour
                        newList.add(new LigneCommande(l.getProduit(), newQty, newRemise, l.isValidee()));
                        } catch (IllegalArgumentException e) {
                            // Si la création échoue, on garde l'ancienne ligne
                            newList.add(l);
                        }
                    } else {
                        // Valeurs invalides, on garde l'ancienne ligne
                        newList.add(l);
                    }
                } else {
                    newList.add(l);
                }
            }
            lignesCommande.setValue(newList);
        }
    }

    /**
     * Bascule l'état de validation d'une ligne (validée ↔ non validée).
     * Une ligne validée ne peut plus être modifiée jusqu'à ce qu'elle soit dévalidée.
     */
    public void toggleValidationLigne(LigneCommande ligneToToggle) {
        List<LigneCommande> currentList = lignesCommande.getValue();
        if (currentList != null) {
            List<LigneCommande> newList = new ArrayList<>();
            for (LigneCommande l : currentList) {
                if (l.getProduit().getId().equals(ligneToToggle.getProduit().getId())) {
                    // Inverser l'état de validation
                    newList.add(new LigneCommande(l.getProduit(), l.getQuantite(), l.getRemise(), !l.isValidee()));
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

    public void startNouvelleCommandePour(Client client) {
        clientSelectionne.setValue(client);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        date.setValue(sdf.format(new Date()));
        lignesCommande.setValue(new java.util.ArrayList<>());
    }

    /**
     * Charge les produits depuis le cache local uniquement (pas d'appel API).
     * Utilisé au démarrage du fragment pour avoir les produits immédiatement disponibles.
     * Pour synchroniser depuis l'API, utiliser chargerProduits().
     *
     * @param context Le contexte nécessaire pour initialiser le storage manager
     */
    public void chargerProduitsDepuisCache(Context context) {
        if (produitStorageManager == null) {
            produitStorageManager = new ProduitStorageManager(context);
        }

        // Charger directement depuis le fichier local (pas de Repository)
        List<Produit> produitsCache = produitStorageManager.loadProduits();

        if (produitsCache != null && !produitsCache.isEmpty()) {
            Log.d(TAG, "Produits chargés depuis le cache : " + produitsCache.size());
            listeProduits.postValue(produitsCache);
        } else {
            Log.d(TAG, "Aucun produit en cache. Utilisez 'Synchroniser les produits' depuis l'accueil.");
            listeProduits.postValue(new ArrayList<>());
        }
    }

    /**
     * Synchronise les produits avec l'API et les sauvegarde dans le cache.
     * Utilisé lors de la synchronisation manuelle depuis l'accueil.
     *
     * @param context Le contexte nécessaire pour initialiser le repository
     */
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


    public void setListeClients(List<Client> clients) {
        this.listeClients.setValue(clients);
    }
}
