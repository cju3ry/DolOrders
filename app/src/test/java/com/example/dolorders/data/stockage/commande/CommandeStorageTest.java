package com.example.dolorders.data.stockage.commande;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.dolorders.objet.Client;
import com.example.dolorders.objet.Commande;
import com.example.dolorders.objet.LigneCommande;
import com.example.dolorders.objet.Produit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tests unitaires pour le stockage des commandes.
 * Ces tests se concentrent sur la sérialisation/désérialisation avec Gson.
 */
public class CommandeStorageTest {

    private Gson gson;
    private Client clientTest;
    private Produit produit1;
    private Produit produit2;
    private Commande commandeTest;

    @Before
    public void setUp() {
        // Configuration de Gson avec l'adaptateur personnalisé pour Commande
        gson = new GsonBuilder()
                .registerTypeAdapter(Commande.class, new AdapteurStockageCommande())
                .create();

        // Création d'un client de test
        Date maintenant = new Date();
        clientTest = new Client.Builder()
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("dupont@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(maintenant)
                .build();

        // Création de produits de test
        produit1 = new Produit(1, "Evian 1.5L", 1.50);
        produit2 = new Produit(2, "Contrex 1L", 2.00);

        // Création de lignes de commande
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 3, 0.0));  // 3 x 1.50€ = 4.50€
        lignes.add(new LigneCommande(produit2, 2, 10.0)); // 2 x 2.00€ - 10% = 3.60€

        // Création d'une commande de test
        commandeTest = new Commande.Builder()
                .setId("CMD-001")
                .setClient(clientTest)
                .setDateCommande(maintenant)
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();
    }

    /**
     * Test : Vérifie que la commande est bien créée avec toutes ses données
     */
    @Test
    public void creationCommande_AvecToutesLesDonnees_Reussit() {
        assertNotNull("La commande ne devrait pas être null", commandeTest);
        assertEquals("CMD-001", commandeTest.getId());
        assertEquals(clientTest, commandeTest.getClient());
        assertEquals(2, commandeTest.getLignesCommande().size());
        assertEquals("userTest", commandeTest.getUtilisateur());

        // Vérification du montant total (4.50 + 3.60 = 8.10€)
        assertEquals(8.10, commandeTest.getMontantTotal(), 0.01);
    }

    /**
     * Test : Sérialisation d'une commande en JSON
     */
    @Test
    public void serialisation_CommandeEnJson_ContienetChampsPrincipaux() {
        String json = gson.toJson(commandeTest);

        assertNotNull("Le JSON ne doit pas être null", json);
        assertFalse("Le JSON ne doit pas être vide", json.isEmpty());

        // Vérification de la présence des champs principaux
        assertTrue("Le JSON doit contenir l'id", json.contains("\"id\":\"CMD-001\""));
        assertTrue("Le JSON doit contenir le client", json.contains("\"client\""));
        assertTrue("Le JSON doit contenir les lignes de commande", json.contains("\"lignesCommande\""));
        assertTrue("Le JSON doit contenir l'utilisateur", json.contains("\"utilisateur\":\"userTest\""));
    }

    /**
     * Test : Désérialisation d'une commande depuis JSON
     */
    @Test
    public void deserialisation_JsonEnCommande_Reussit() {
        // Sérialisation puis désérialisation
        String json = gson.toJson(commandeTest);
        Commande commandeDeserialise = gson.fromJson(json, Commande.class);

        // Vérifications
        assertNotNull("La commande désérialisée ne doit pas être null", commandeDeserialise);
        assertEquals("CMD-001", commandeDeserialise.getId());
        assertEquals("userTest", commandeDeserialise.getUtilisateur());
        assertEquals(2, commandeDeserialise.getLignesCommande().size());

        // Vérification du client
        assertNotNull("Le client ne doit pas être null", commandeDeserialise.getClient());
        assertEquals("Dupont", commandeDeserialise.getClient().getNom());
        assertEquals("dupont@example.com", commandeDeserialise.getClient().getAdresseMail());

        // Vérification du montant total
        assertEquals(8.10, commandeDeserialise.getMontantTotal(), 0.01);
    }

    /**
     * Test : Vérification des lignes de commande après désérialisation
     */
    @Test
    public void deserialisation_LignesCommande_SontCorrectement_Restaurees() {
        String json = gson.toJson(commandeTest);
        Commande commandeDeserialise = gson.fromJson(json, Commande.class);

        List<LigneCommande> lignes = commandeDeserialise.getLignesCommande();

        assertNotNull("Les lignes de commande ne doivent pas être null", lignes);
        assertEquals("Doit avoir 2 lignes", 2, lignes.size());

        // Vérification de la première ligne
        LigneCommande ligne1 = lignes.get(0);
        assertEquals("1", ligne1.getProduit().getId());
        assertEquals("Evian 1.5L", ligne1.getProduit().getLibelle());
        assertEquals(3, ligne1.getQuantite());
        assertEquals(0.0, ligne1.getRemise(), 0.01);
        assertEquals(4.50, ligne1.getMontantLigne(), 0.01);

        // Vérification de la deuxième ligne
        LigneCommande ligne2 = lignes.get(1);
        assertEquals("2", ligne2.getProduit().getId());
        assertEquals("Contrex 1L", ligne2.getProduit().getLibelle());
        assertEquals(2, ligne2.getQuantite());
        assertEquals(10.0, ligne2.getRemise(), 0.01);
        assertEquals(3.60, ligne2.getMontantLigne(), 0.01);
    }

    /**
     * Test : Sérialisation/désérialisation d'une liste de commandes
     */
    @Test
    public void serialisation_ListeCommandes_Reussit() {
        List<Commande> commandes = new ArrayList<>();
        commandes.add(commandeTest);

        // Création d'une seconde commande
        List<LigneCommande> lignes2 = new ArrayList<>();
        lignes2.add(new LigneCommande(produit1, 5, 0.0));

        Commande commande2 = new Commande.Builder()
                .setId("CMD-002")
                .setClient(clientTest)
                .setDateCommande(new Date())
                .setLignesCommande(lignes2)
                .setUtilisateur("userTest")
                .build();

        commandes.add(commande2);

        // Sérialisation
        String json = gson.toJson(commandes);

        assertNotNull("Le JSON ne doit pas être null", json);
        assertTrue("Le JSON doit contenir CMD-001", json.contains("CMD-001"));
        assertTrue("Le JSON doit contenir CMD-002", json.contains("CMD-002"));
    }

    /**
     * Test : Vérification de la remise globale
     */
    @Test
    public void creationCommande_CalculCorrect() {
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 10, 0.0)); // 10 x 1.50€ = 15.00€

        Commande commande = new Commande.Builder()
                .setId("CMD-003")
                .setClient(clientTest)
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        assertEquals(15, commande.getMontantTotal(), 0.01);

        // Sérialisation/désérialisation
        String json = gson.toJson(commande);
        Commande commandeDeserialise = gson.fromJson(json, Commande.class);

        assertEquals(15, commandeDeserialise.getMontantTotal(), 0.01);
    }

    /**
     * Test : Vérification qu'une commande sans lignes lève une exception
     */
    @Test(expected = IllegalStateException.class)
    public void creationCommande_SansLignes_LeveException() {
        new Commande.Builder()
                .setId("CMD-INVALID")
                .setClient(clientTest)
                .setDateCommande(new Date())
                .setLignesCommande(new ArrayList<>())
                .setUtilisateur("userTest")
                .build();
    }

    /**
     * Test : Vérification qu'une commande sans client lève une exception
     */
    @Test(expected = IllegalStateException.class)
    public void creationCommande_SansClient_LeveException() {
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 1, 0.0));

        new Commande.Builder()
                .setId("CMD-INVALID")
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();
    }

    /**
     * Test : Vérification qu'une commande sans utilisateur lève une exception
     */
    @Test(expected = IllegalStateException.class)
    public void creationCommande_SansUtilisateur_LeveException() {
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 1, 0.0));

        new Commande.Builder()
                .setId("CMD-INVALID")
                .setClient(clientTest)
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .build();
    }
}

