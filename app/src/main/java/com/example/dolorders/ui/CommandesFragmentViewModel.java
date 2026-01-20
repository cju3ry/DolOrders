package com.example.dolorders.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dolorders.Client;
import com.example.dolorders.LigneCommande;
import com.example.dolorders.Produit;

import java.util.ArrayList;
import java.util.List;

public class CommandesFragmentViewModel extends ViewModel {

    private final MutableLiveData<List<LigneCommande>> lignesCommande = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Client> clientSelectionne = new MutableLiveData<>();
    private final MutableLiveData<String> date = new MutableLiveData<>();
    private final MutableLiveData<List<Client>> listeClients = new MutableLiveData<>();
    private final MutableLiveData<List<Produit>> listeProduits = new MutableLiveData<>();

    // --- Getters ---
    public LiveData<List<LigneCommande>> getLignesCommande() { return lignesCommande; }
    public LiveData<Client> getClientSelectionne() { return clientSelectionne; }
    public LiveData<String> getDate() { return date; }
    public LiveData<List<Client>> getListeClients() { return listeClients; }
    public LiveData<List<Produit>> getListeProduits() { return listeProduits; }

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
                    if (newQty > 0) {
                        newList.add(new LigneCommande(l.getProduit(), newQty, newRemise));
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

    public void chargerDonneesDeTest() {
        Client.Builder clientBuilder = new Client.Builder()
                .setId("001")
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("test@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(new java.util.Date());
        List<Client> clientsFactices = new ArrayList<>();
        clientsFactices.add(clientBuilder.build());
        listeClients.setValue(clientsFactices);

        List<Produit> produitsFactices = new ArrayList<>();
        produitsFactices.add(new Produit(101, "Stylo Bleu", 1.50));
        produitsFactices.add(new Produit(102, "Cahier A4", 3.20));
        produitsFactices.add(new Produit(103, "Clavier USB", 25.00));
        produitsFactices.add(new Produit(104, "Souris sans fil", 18.50));
        listeProduits.setValue(produitsFactices);
    }
}