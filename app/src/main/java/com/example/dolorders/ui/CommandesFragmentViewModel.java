package com.example.dolorders.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dolorders.Client;
import com.example.dolorders.Produit;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommandesFragmentViewModel extends ViewModel {

    private final MutableLiveData<Client> clientSelectionne = new MutableLiveData<>();
    private final MutableLiveData<String> date = new MutableLiveData<>();
    private final MutableLiveData<String> remise = new MutableLiveData<>();
    private final MutableLiveData<Map<Produit, Integer>> articles = new MutableLiveData<>(new LinkedHashMap<>());
    private final MutableLiveData<List<Client>> listeClients = new MutableLiveData<>();
    private final MutableLiveData<List<Produit>> listeProduits = new MutableLiveData<>();

    // --- Getters ---
    public LiveData<Client> getClientSelectionne() { return clientSelectionne; }
    public LiveData<String> getDate() { return date; }
    public LiveData<String> getRemise() { return remise; }
    public LiveData<Map<Produit, Integer>> getArticles() { return articles; }
    public LiveData<List<Client>> getListeClients() { return listeClients; }
    public LiveData<List<Produit>> getListeProduits() { return listeProduits; }

    public void setClientSelectionne(Client client) {
        this.clientSelectionne.setValue(client);
    }

    public void setDate(String date) {
        this.date.setValue(date);
    }

    public void setRemise(String remise) {
        this.remise.setValue(remise);
    }

    public void addArticle(Produit produit) {
        Map<Produit, Integer> panierActuel = articles.getValue();
        if (panierActuel == null) panierActuel = new LinkedHashMap<>();

        int quantite = panierActuel.getOrDefault(produit, 0);
        panierActuel.put(produit, quantite + 1);
        articles.setValue(panierActuel); // Déclenche l'update
    }

    public void removeArticle(Produit produit) {
        Map<Produit, Integer> panierActuel = articles.getValue();
        if (panierActuel != null) {
            panierActuel.remove(produit);
            articles.setValue(panierActuel);
        }
    }

    public void updateQuantity(Produit produit, int nouvelleQuantite) {
        Map<Produit, Integer> panierActuel = articles.getValue();
        if (panierActuel != null && panierActuel.containsKey(produit)) {
            if (nouvelleQuantite > 0) {
                panierActuel.put(produit, nouvelleQuantite);
            } else {
                panierActuel.remove(produit); // Supprime si la quantité est 0 ou moins
            }
            articles.setValue(panierActuel);
        }
    }

    public void clear() {
        clientSelectionne.setValue(null);
        date.setValue(null);
        remise.setValue(null);
        articles.setValue(new LinkedHashMap<>());
    }

    // --- Chargement des données de test ---
    public void chargerDonneesDeTest() {
        Client.Builder client = new Client.Builder()
                .setId("001")
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("test@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(new Date());
        List<Client> clientsFactices = new ArrayList<>();
        clientsFactices.add(client.build());
        listeClients.setValue(clientsFactices);

        // Produits factices
        List<Produit> produitsFactices = new ArrayList<>();
        produitsFactices.add(new Produit(101, "Stylo Bleu", 1.50, "Fournitures"));
        produitsFactices.add(new Produit(102, "Cahier A4", 3.20, "Papeterie"));
        produitsFactices.add(new Produit(103, "Clavier USB", 25.00, "Informatique"));
        produitsFactices.add(new Produit(104, "Souris sans fil", 18.50, "Informatique"));
        listeProduits.setValue(produitsFactices);
    }
}
