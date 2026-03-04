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
 * Tests unitaires pour les composants de stockage des commandes.
 * Ces tests se concentrent sur la logique métier sans dépendances Android.
 * Structure similaire à GestionnaireStockageClientTest.
 */
public class GestionnaireStockageCommandeTest {

    private Gson gson;
    private Client clientTest1;
    private Client clientTest2;
    private Produit produit1;
    private Produit produit2;
    private Produit produit3;
    private Commande commandeTest1;
    private Commande commandeTest2;
    private Date dateReference;

    @Before
    public void setUp() {
        // Configuration de Gson avec l'adaptateur personnalisé
        gson = new GsonBuilder()
                .registerTypeAdapter(Commande.class, new AdapteurStockageCommande())
                .create();

        // Date de référence pour les tests
        dateReference = new Date(1700000000000L);

        // Création de clients de test
        clientTest1 = new Client.Builder()
                .setId("CLI-001")
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("dupont@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(dateReference)
                .build();

        clientTest2 = new Client.Builder()
                .setId("CLI-002")
                .setNom("Martin")
                .setAdresse("25 avenue des Champs")
                .setCodePostal("69001")
                .setVille("Lyon")
                .setAdresseMail("martin@example.com")
                .setTelephone("0987654321")
                .setUtilisateur("userTest")
                .setDateSaisie(dateReference)
                .build();

        // Création de produits de test
        produit1 = new Produit("1", "Evian 1.5L", "Eau minérale naturelle", 1.50, 20.0);
        produit2 = new Produit("2", "Contrex 1L", "Eau riche en minéraux", 2.00, 20.0);
        produit3 = new Produit("3", "Pain Bio", "Pain complet", 3.50, 5.5);

        // Création de commandes de test
        List<LigneCommande> lignes1 = new ArrayList<>();
        lignes1.add(new LigneCommande(produit1, 3, 0.0, true, dateReference));
        lignes1.add(new LigneCommande(produit2, 2, 10.0, true, dateReference));

        commandeTest1 = new Commande.Builder()
                .setId("CMD-001")
                .setClient(clientTest1)
                .setDateCommande(dateReference)
                .setLignesCommande(lignes1)
                .setUtilisateur("userTest")
                .build();

        List<LigneCommande> lignes2 = new ArrayList<>();
        lignes2.add(new LigneCommande(produit3, 5, 0.0, true, dateReference));

        commandeTest2 = new Commande.Builder()
                .setId("CMD-002")
                .setClient(clientTest2)
                .setDateCommande(dateReference)
                .setLignesCommande(lignes2)
                .setUtilisateur("userTest")
                .build();
    }

    // ==================== TESTS DE CRÉATION ====================

    /**
     * Test : Vérifie que la commande est bien créée avec toutes ses données
     */
    @Test
    public void creationCommande_AvecToutesLesDonnees_Reussit() {
        assertNotNull("La commande ne devrait pas être null", commandeTest1);
        assertEquals("CMD-001", commandeTest1.getId());
        assertEquals(clientTest1, commandeTest1.getClient());
        assertEquals(2, commandeTest1.getLignesCommande().size());
        assertEquals("userTest", commandeTest1.getUtilisateur());
    }

    /**
     * Test : Vérifie que plusieurs commandes peuvent être créées
     */
    @Test
    public void creationCommandes_Plusieurs_Reussit() {
        assertNotNull(commandeTest1);
        assertNotNull(commandeTest2);
        assertEquals("CMD-001", commandeTest1.getId());
        assertEquals("CMD-002", commandeTest2.getId());
    }

    /**
     * Test : Vérifie le calcul du montant total
     */
    @Test
    public void creationCommande_CalculMontantTotal_Correct() {
        // Commande 1: (3 x 1.50) + (2 x 2.00 - 10%) = 4.50 + 3.60 = 8.10€
        assertEquals(8.10, commandeTest1.getMontantTotal(), 0.01);

        // Commande 2: 5 x 3.50 = 17.50€
        assertEquals(17.50, commandeTest2.getMontantTotal(), 0.01);
    }

    // ==================== TESTS DE SÉRIALISATION ====================

    /**
     * Test : Sérialisation d'une commande en JSON
     */
    @Test
    public void serialisation_CommandeEnJson_Contient_ChampsPrincipaux() {
        String json = gson.toJson(commandeTest1);

        assertNotNull("Le JSON ne devrait pas être null", json);
        assertFalse("Le JSON ne devrait pas être vide", json.isEmpty());
        assertTrue("Le JSON devrait contenir l'id", json.contains("CMD-001"));
        assertTrue("Le JSON devrait contenir le client", json.contains("client"));
        assertTrue("Le JSON devrait contenir les lignes", json.contains("lignesCommande"));
        assertTrue("Le JSON devrait contenir l'utilisateur", json.contains("userTest"));
    }

    /**
     * Test : Sérialisation de plusieurs commandes
     */
    @Test
    public void serialisation_PlusieursCommandes_Reussit() {
        String json1 = gson.toJson(commandeTest1);
        String json2 = gson.toJson(commandeTest2);

        assertNotNull("Le JSON de la première commande ne devrait pas être null", json1);
        assertNotNull("Le JSON de la deuxième commande ne devrait pas être null", json2);
        assertTrue("Le premier JSON devrait contenir CMD-001", json1.contains("CMD-001"));
        assertTrue("Le deuxième JSON devrait contenir CMD-002", json2.contains("CMD-002"));
    }

    /**
     * Test : Sérialisation avec null retourne "null"
     */
    @Test
    public void serialisation_CommandeNull_RetourneNull() {
        String json = gson.toJson(null, Commande.class);
        assertEquals("null", json);
    }

    /**
     * Test : Vérifie que les dates sont correctement sérialisées
     */
    @Test
    public void serialisation_AvecDates_ContientTimestamp() {
        String json = gson.toJson(commandeTest1);

        assertTrue("Le JSON devrait contenir le timestamp de la date commande",
                json.contains(String.valueOf(dateReference.getTime())));
    }

    // ==================== TESTS DE DÉSÉRIALISATION ====================

    /**
     * Test : Désérialisation d'un JSON simple en Commande
     */
    @Test
    public void deserialisation_JsonSimpleEnCommande_Reussit() {
        // Création d'un JSON complet de commande
        String json = gson.toJson(commandeTest1);

        Commande commande = gson.fromJson(json, Commande.class);

        assertNotNull("La commande ne devrait pas être null", commande);
        assertEquals("ID", "CMD-001", commande.getId());
        assertNotNull("Le client ne devrait pas être null", commande.getClient());
        assertEquals("Nombre de lignes", 2, commande.getLignesCommande().size());
    }

    // ==================== TESTS CYCLE COMPLET ====================

    /**
     * Test : Cycle complet sérialisation/désérialisation
     */
    @Test
    public void cycleComplet_SerialisationDeserialisation_PreserveLesDonnees() {
        String json = gson.toJson(commandeTest1);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        assertNotNull("La commande reconstituée ne devrait pas être null", commandeReconstituee);
        assertEquals("ID", commandeTest1.getId(), commandeReconstituee.getId());
        assertEquals("Client nom", commandeTest1.getClient().getNom(), commandeReconstituee.getClient().getNom());
        assertEquals("Nombre de lignes",
                commandeTest1.getLignesCommande().size(),
                commandeReconstituee.getLignesCommande().size());
        assertEquals("Montant total",
                commandeTest1.getMontantTotal(),
                commandeReconstituee.getMontantTotal(), 0.01);
        assertEquals("Utilisateur", commandeTest1.getUtilisateur(), commandeReconstituee.getUtilisateur());
    }

    /**
     * Test : Cycle complet avec date préservée
     */
    @Test
    public void cycleComplet_AvecDate_PreserveDate() {
        String json = gson.toJson(commandeTest1);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        assertEquals("Date commande préservée",
                commandeTest1.getDateCommande().getTime(),
                commandeReconstituee.getDateCommande().getTime());
    }

    /**
     * Test : Cycle complet avec ID préservé
     */
    @Test
    public void cycleComplet_AvecId_PreserveId() {
        String json = gson.toJson(commandeTest1);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        assertEquals("L'ID devrait être préservé", "CMD-001", commandeReconstituee.getId());
    }

    /**
     * Test : Cycle complet préserve les informations du client
     */
    @Test
    public void cycleComplet_PreserveInfosClient() {
        String json = gson.toJson(commandeTest1);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        Client clientReconstitue = commandeReconstituee.getClient();
        assertNotNull("Le client reconstitué ne devrait pas être null", clientReconstitue);
        assertEquals("Nom client", "Dupont", clientReconstitue.getNom());
        assertEquals("Adresse client", "10 rue de la Paix", clientReconstitue.getAdresse());
        assertEquals("Ville client", "Paris", clientReconstitue.getVille());
    }

    /**
     * Test : Cycle complet préserve les lignes de commande
     */
    @Test
    public void cycleComplet_PreserveLignesCommande() {
        String json = gson.toJson(commandeTest1);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        List<LigneCommande> lignes = commandeReconstituee.getLignesCommande();
        assertEquals("Nombre de lignes", 2, lignes.size());

        // Vérification première ligne
        LigneCommande ligne1 = lignes.get(0);
        assertEquals("Quantité ligne 1", 3, ligne1.getQuantite());
        assertEquals("Produit ligne 1", "Evian 1.5L", ligne1.getProduit().getLibelle());

        // Vérification deuxième ligne
        LigneCommande ligne2 = lignes.get(1);
        assertEquals("Quantité ligne 2", 2, ligne2.getQuantite());
        assertEquals("Remise ligne 2", 10.0, ligne2.getRemise(), 0.01);
    }

    /**
     * Test : Cycle complet préserve les produits dans les lignes
     */
    @Test
    public void cycleComplet_PreserveProduitsLignes() {
        String json = gson.toJson(commandeTest1);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        Produit produitReconstitue = commandeReconstituee.getLignesCommande().get(0).getProduit();
        assertNotNull("Le produit reconstitué ne devrait pas être null", produitReconstitue);
        assertEquals("ID produit", "1", produitReconstitue.getId());
        assertEquals("Libellé produit", "Evian 1.5L", produitReconstitue.getLibelle());
        assertEquals("Prix produit", 1.50, produitReconstitue.getPrixUnitaire(), 0.01);
        assertEquals("TVA produit", 20.0, produitReconstitue.getTauxTva(), 0.01);
    }

    /**
     * Test : Cycle complet préserve le flag validée des lignes
     */
    @Test
    public void cycleComplet_PreserveFlagValideeLignes() {
        String json = gson.toJson(commandeTest1);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        for (LigneCommande ligne : commandeReconstituee.getLignesCommande()) {
            assertTrue("Le flag validée devrait être préservé", ligne.isValidee());
        }
    }

    /**
     * Test : Cycle complet avec commande à une seule ligne
     */
    @Test
    public void cycleComplet_CommandeUneLigne_Reussit() {
        String json = gson.toJson(commandeTest2);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        assertNotNull(commandeReconstituee);
        assertEquals("CMD-002", commandeReconstituee.getId());
        assertEquals(1, commandeReconstituee.getLignesCommande().size());
        assertEquals(17.50, commandeReconstituee.getMontantTotal(), 0.01);
    }

    // ==================== TESTS CAS LIMITES ====================

    /**
     * Test : Cycle complet avec caractères spéciaux dans le nom du client
     */
    @Test
    public void cycleComplet_AvecCaracteresSpeciaux_Reussit() {
        Client clientSpecial = new Client.Builder()
                .setId("CLI-SP")
                .setNom("Dupönt-André")
                .setAdresse("10 rue de l'Église")
                .setCodePostal("75002")
                .setVille("Aix-en-Provence")
                .setAdresseMail("dupont@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(dateReference)
                .build();

        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 1, 0.0, true, dateReference));

        Commande commandeSpeciale = new Commande.Builder()
                .setId("CMD-SPECIAL")
                .setClient(clientSpecial)
                .setDateCommande(dateReference)
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        String json = gson.toJson(commandeSpeciale);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        assertEquals("Dupönt-André", commandeReconstituee.getClient().getNom());
        assertEquals("10 rue de l'Église", commandeReconstituee.getClient().getAdresse());
        assertEquals("Aix-en-Provence", commandeReconstituee.getClient().getVille());
    }

    /**
     * Test : Cycle complet avec grande quantité
     */
    @Test
    public void cycleComplet_AvecGrandeQuantite_Reussit() {
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 10000, 0.0, true, dateReference));

        Commande commandeGrande = new Commande.Builder()
                .setId("CMD-GRANDE")
                .setClient(clientTest1)
                .setDateCommande(dateReference)
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        String json = gson.toJson(commandeGrande);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        assertEquals(10000, commandeReconstituee.getLignesCommande().get(0).getQuantite());
        assertEquals(15000.0, commandeReconstituee.getMontantTotal(), 0.01);
    }

    /**
     * Test : Cycle complet avec remise maximale (100%)
     */
    @Test
    public void cycleComplet_AvecRemise100Pourcent_Reussit() {
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 5, 100.0, true, dateReference));

        Commande commandeGratuite = new Commande.Builder()
                .setId("CMD-GRATUIT")
                .setClient(clientTest1)
                .setDateCommande(dateReference)
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        String json = gson.toJson(commandeGratuite);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        assertEquals(100.0, commandeReconstituee.getLignesCommande().get(0).getRemise(), 0.01);
        assertEquals(0.0, commandeReconstituee.getMontantTotal(), 0.01);
    }

    /**
     * Test : Cycle complet avec plusieurs lignes et remises différentes
     */
    @Test
    public void cycleComplet_AvecPlusieursLignesRemises_Reussit() {
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 2, 0.0, true, dateReference));   // 3.00€
        lignes.add(new LigneCommande(produit2, 3, 25.0, true, dateReference));  // 4.50€ (6.00 - 25%)
        lignes.add(new LigneCommande(produit3, 1, 50.0, true, dateReference));  // 1.75€ (3.50 - 50%)

        Commande commandeMultiple = new Commande.Builder()
                .setId("CMD-MULTI")
                .setClient(clientTest1)
                .setDateCommande(dateReference)
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        String json = gson.toJson(commandeMultiple);
        Commande commandeReconstituee = gson.fromJson(json, Commande.class);

        assertEquals(3, commandeReconstituee.getLignesCommande().size());
        // Total: 3.00 + 4.50 + 1.75 = 9.25€
        assertEquals(9.25, commandeReconstituee.getMontantTotal(), 0.01);
    }

    // ==================== TESTS LISTE DE COMMANDES ====================

    /**
     * Test : Sérialisation/désérialisation de plusieurs commandes
     */
    @Test
    public void cycleComplet_ListeCommandes_Reussit() {
        List<Commande> commandes = new ArrayList<>();
        commandes.add(commandeTest1);
        commandes.add(commandeTest2);

        // Sérialisation de chaque commande
        List<String> jsons = new ArrayList<>();
        for (Commande c : commandes) {
            jsons.add(gson.toJson(c));
        }

        // Désérialisation
        List<Commande> commandesReconstituees = new ArrayList<>();
        for (String json : jsons) {
            commandesReconstituees.add(gson.fromJson(json, Commande.class));
        }

        assertEquals("Même nombre de commandes", commandes.size(), commandesReconstituees.size());
        assertEquals("Première commande", "CMD-001", commandesReconstituees.get(0).getId());
        assertEquals("Deuxième commande", "CMD-002", commandesReconstituees.get(1).getId());
    }
}

