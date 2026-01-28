package com.example.dolorders.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dolorders.objet.Client;
import com.example.dolorders.objet.LigneCommande;
import com.example.dolorders.objet.Produit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommandesFragmentViewModel extends ViewModel {

    private final MutableLiveData<List<LigneCommande>> lignesCommande = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Client> clientSelectionne = new MutableLiveData<>();
    private final MutableLiveData<String> date = new MutableLiveData<>();
    private final MutableLiveData<List<Client>> listeClients = new MutableLiveData<>();
    private final MutableLiveData<List<Produit>> listeProduits = new MutableLiveData<>();
    private final MutableLiveData<Boolean> fromAccueil = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> fromListeClients = new MutableLiveData<>(false);

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
            if (ligne.getProduit().getId() == produit.getId()) {
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
            newList.removeIf(l -> l.getProduit().getId() == ligneToDelete.getProduit().getId());
            lignesCommande.setValue(newList);
        }
    }

    public void updateLigne(LigneCommande oldLigne, int newQty, double newRemise) {
        List<LigneCommande> currentList = lignesCommande.getValue();
        if (currentList != null) {
            List<LigneCommande> newList = new ArrayList<>();
            for (LigneCommande l : currentList) {
                if (l.getProduit().getId() == oldLigne.getProduit().getId()) {
                    if (newQty > 0 && newRemise >= 0 && newRemise <= 100) {
                        try {
                            newList.add(new LigneCommande(l.getProduit(), newQty, newRemise));
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

    public void chargerProduitsDeTest() {
        List<Produit> produitsFactices = new ArrayList<>();
        produitsFactices.add(new Produit(101, "Stylo Bleu", 1.50));
        produitsFactices.add(new Produit(102, "Cahier A4", 3.20));
        produitsFactices.add(new Produit(103, "Clavier USB", 25.00));
        produitsFactices.add(new Produit(104, "Souris sans fil", 18.50));
        listeProduits.setValue(produitsFactices);
    }

    public void setListeClients(List<Client> clients) {
        this.listeClients.setValue(clients);
    }
}
