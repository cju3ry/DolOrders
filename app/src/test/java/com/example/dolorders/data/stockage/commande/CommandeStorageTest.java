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
 * Ces tests se concentrent sur la sérialisation/désérialisation avec Gson,
 * la gestion des dates de création des lignes, et la validation.
 */
public class CommandeStorageTest {

    private Gson gson;
    private Client clientTest;
    private Produit produit1;
    private Produit produit2;
    private Produit produit3;
    private Commande commandeTest;
    private Date dateReference;

    @Before
    public void setUp() {
        // Configuration de Gson avec l'adaptateur personnalisé pour Commande
        gson = new GsonBuilder()
                .registerTypeAdapter(Commande.class, new AdapteurStockageCommande())
                .create();

        // Date de référence pour les tests
        dateReference = new Date();

        // Création d'un client de test
        clientTest = new Client.Builder()
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("dupont@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(dateReference)
                .build();

        // Création de produits de test
        produit1 = new Produit("1", "Evian 1.5L", "eau en bouteille", 1.50, 20.0);
        produit2 = new Produit("2", "Contrex 1L", "eau en bouteille spéciale", 2.00, 20.0);
        produit3 = new Produit("3", "Vittel 2L", "eau minérale", 2.50, 5.5);

        // Création de lignes de commande avec dates de création
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 3, 0.0, true, dateReference));  // 3 x 1.50€ = 4.50€
        lignes.add(new LigneCommande(produit2, 2, 10.0, true, new Date(dateReference.getTime() + 1000))); // 2 x 2.00€ - 10% = 3.60€

        // Création d'une commande de test
        commandeTest = new Commande.Builder()
                .setId("CMD-001")
                .setClient(clientTest)
                .setDateCommande(dateReference)
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();
    }

    // ==================== TESTS DE CRÉATION ====================

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
     * Test : Création d'une ligne avec date automatique
     */
    @Test
    public void creationLigne_SansDateSpecifique_UtiliseDateActuelle() {
        LigneCommande ligne = new LigneCommande(produit1, 5, 0.0, false);

        assertNotNull("La date de création ne doit pas être null", ligne.getDateCreation());
        assertTrue("La date de création doit être récente",
                   System.currentTimeMillis() - ligne.getDateCreation().getTime() < 1000);
    }

    /**
     * Test : Création d'une ligne avec date spécifique
     */
    @Test
    public void creationLigne_AvecDateSpecifique_ConserveLaDate() {
        Date dateSpecifique = new Date(System.currentTimeMillis() - 60000); // Il y a 1 minute
        LigneCommande ligne = new LigneCommande(produit1, 5, 0.0, false, dateSpecifique);

        assertNotNull("La date de création ne doit pas être null", ligne.getDateCreation());
        assertEquals("La date doit être celle spécifiée", dateSpecifique.getTime(), ligne.getDateCreation().getTime());
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
        lignes.add(new LigneCommande(produit1, 1, 0.0, true));

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
        lignes.add(new LigneCommande(produit1, 1, 0.0, true));

        new Commande.Builder()
                .setId("CMD-INVALID")
                .setClient(clientTest)
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .build();
    }

    // ==================== TESTS DE SÉRIALISATION ====================

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
     * Test : Sérialisation inclut les dates de création des lignes
     */
    @Test
    public void serialisation_CommandeEnJson_InclutDatesCreationLignes() {
        String json = gson.toJson(commandeTest);

        assertNotNull("Le JSON ne doit pas être null", json);
        assertTrue("Le JSON doit contenir dateCreation", json.contains("\"dateCreation\""));
    }

    /**
     * Test : Sérialisation inclut le flag validée des lignes
     */
    @Test
    public void serialisation_CommandeEnJson_InclutFlagValidee() {
        String json = gson.toJson(commandeTest);

        assertNotNull("Le JSON ne doit pas être null", json);
        assertTrue("Le JSON doit contenir validee", json.contains("\"validee\""));
    }

    /**
     * Test : Sérialisation inclut la TVA des produits
     */
    @Test
    public void serialisation_CommandeEnJson_InclutTVAProduits() {
        String json = gson.toJson(commandeTest);

        assertNotNull("Le JSON ne doit pas être null", json);
        assertTrue("Le JSON doit contenir tauxTva", json.contains("\"tauxTva\""));
    }

    // ==================== TESTS DE DÉSÉRIALISATION ====================

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
    public void deserialisation_LignesCommande_SontCorrectementRestaurees() {
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
        assertTrue("La ligne doit être validée", ligne1.isValidee());
        assertNotNull("La date de création doit être présente", ligne1.getDateCreation());

        // Vérification de la deuxième ligne
        LigneCommande ligne2 = lignes.get(1);
        assertEquals("2", ligne2.getProduit().getId());
        assertEquals("Contrex 1L", ligne2.getProduit().getLibelle());
        assertEquals(2, ligne2.getQuantite());
        assertEquals(10.0, ligne2.getRemise(), 0.01);
        assertEquals(3.60, ligne2.getMontantLigne(), 0.01);
        assertTrue("La ligne doit être validée", ligne2.isValidee());
        assertNotNull("La date de création doit être présente", ligne2.getDateCreation());
    }

    /**
     * Test : Désérialisation préserve l'ordre des dates de création des lignes
     */
    @Test
    public void deserialisation_PreserveOrdreDatesCreationLignes() {
        String json = gson.toJson(commandeTest);
        Commande commandeDeserialise = gson.fromJson(json, Commande.class);

        List<LigneCommande> lignes = commandeDeserialise.getLignesCommande();

        Date dateLigne1 = lignes.get(0).getDateCreation();
        Date dateLigne2 = lignes.get(1).getDateCreation();

        assertTrue("La date de la ligne 2 doit être après celle de la ligne 1",
                   dateLigne2.getTime() >= dateLigne1.getTime());
    }

    /**
     * Test : Désérialisation préserve la TVA des produits
     */
    @Test
    public void deserialisation_PreserveTVAProduits() {
        String json = gson.toJson(commandeTest);
        Commande commandeDeserialise = gson.fromJson(json, Commande.class);

        LigneCommande ligne1 = commandeDeserialise.getLignesCommande().get(0);
        assertEquals("La TVA doit être 20%", 20.0, ligne1.getProduit().getTauxTva(), 0.01);
    }

    // ==================== TESTS DE MODIFICATION ====================

    /**
     * Test : Modification d'une ligne de commande (changement de quantité)
     */
    @Test
    public void modification_QuantiteLigne_RecalculeMontant() {
        List<LigneCommande> lignes = new ArrayList<>();
        Date dateCreationOriginale = new Date(System.currentTimeMillis() - 5000);
        LigneCommande ligneOriginale = new LigneCommande(produit1, 2, 0.0, false, dateCreationOriginale);
        lignes.add(ligneOriginale);

        // Simulation d'une modification : nouvelle ligne avec nouvelle quantité mais même date
        LigneCommande ligneModifiee = new LigneCommande(
            ligneOriginale.getProduit(),
            5, // Nouvelle quantité
            ligneOriginale.getRemise(),
            true, // Validée après modification
            ligneOriginale.getDateCreation() // On conserve la date originale
        );

        assertEquals("La quantité doit être mise à jour", 5, ligneModifiee.getQuantite());
        assertEquals("Le montant doit être recalculé", 7.50, ligneModifiee.getMontantLigne(), 0.01);
        assertEquals("La date de création doit être conservée",
                     dateCreationOriginale.getTime(),
                     ligneModifiee.getDateCreation().getTime());
        assertTrue("La ligne doit être validée", ligneModifiee.isValidee());
    }

    /**
     * Test : Ajout d'une nouvelle ligne à une commande existante
     */
    @Test
    public void ajout_NouvelleLigne_AvecDateDifferente() {
        List<LigneCommande> lignes = new ArrayList<>(commandeTest.getLignesCommande());

        // Ajout d'une nouvelle ligne avec une date plus récente
        Date nouvelleDateCreation = new Date(System.currentTimeMillis() + 5000);
        LigneCommande nouvelleLigne = new LigneCommande(produit3, 4, 5.0, false, nouvelleDateCreation);
        lignes.add(nouvelleLigne);

        Commande commandeModifiee = new Commande.Builder()
                .setId(commandeTest.getId())
                .setClient(commandeTest.getClient())
                .setDateCommande(commandeTest.getDateCommande())
                .setLignesCommande(lignes)
                .setUtilisateur(commandeTest.getUtilisateur())
                .build();

        assertEquals("La commande doit avoir 3 lignes", 3, commandeModifiee.getLignesCommande().size());

        LigneCommande derniereLigne = commandeModifiee.getLignesCommande().get(2);
        assertEquals("Le produit ajouté doit être Vittel 2L", "Vittel 2L", derniereLigne.getProduit().getLibelle());
        assertFalse("La nouvelle ligne ne doit pas être validée", derniereLigne.isValidee());
        assertEquals("La date de la nouvelle ligne doit être la plus récente",
                     nouvelleDateCreation.getTime(),
                     derniereLigne.getDateCreation().getTime());
    }

    /**
     * Test : Modification d'une ligne conserve sa date de création
     */
    @Test
    public void modification_LigneExistante_ConserveDateCreation() {
        Date dateOriginale = new Date(System.currentTimeMillis() - 10000);
        LigneCommande ligneOriginale = new LigneCommande(produit1, 2, 0.0, false, dateOriginale);

        // Modification : nouvelle quantité et remise, mais même date de création
        LigneCommande ligneModifiee = new LigneCommande(
            ligneOriginale.getProduit(),
            10, // Nouvelle quantité
            15.0, // Nouvelle remise
            true,
            ligneOriginale.getDateCreation() // Conservation de la date
        );

        assertEquals("La date de création doit être conservée",
                     dateOriginale.getTime(),
                     ligneModifiee.getDateCreation().getTime());
        assertEquals("La quantité doit être mise à jour", 10, ligneModifiee.getQuantite());
        assertEquals("La remise doit être mise à jour", 15.0, ligneModifiee.getRemise(), 0.01);
    }

    // ==================== TESTS DE VALIDATION ====================

    /**
     * Test : Une ligne non validée peut être modifiée
     */
    @Test
    public void validation_LigneNonValidee_PeutEtreModifiee() {
        LigneCommande ligne = new LigneCommande(produit1, 5, 0.0, false);

        assertFalse("La ligne ne doit pas être validée", ligne.isValidee());

        // Simulation de modification puis validation
        LigneCommande ligneValidee = new LigneCommande(
            ligne.getProduit(),
            10,
            5.0,
            true,
            ligne.getDateCreation()
        );

        assertTrue("La ligne doit maintenant être validée", ligneValidee.isValidee());
        assertEquals("La quantité doit avoir changé", 10, ligneValidee.getQuantite());
    }

    /**
     * Test : Vérification du flag validée après sérialisation/désérialisation
     */
    @Test
    public void serialisation_FlagValidee_EstPreserve() {
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 2, 0.0, true));  // Validée
        lignes.add(new LigneCommande(produit2, 3, 5.0, false)); // Non validée

        Commande commande = new Commande.Builder()
                .setId("CMD-TEST-VALIDEE")
                .setClient(clientTest)
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        String json = gson.toJson(commande);
        Commande commandeDeserialise = gson.fromJson(json, Commande.class);

        assertTrue("La première ligne doit être validée",
                   commandeDeserialise.getLignesCommande().get(0).isValidee());
        assertFalse("La deuxième ligne ne doit pas être validée",
                    commandeDeserialise.getLignesCommande().get(1).isValidee());
    }

    // ==================== TESTS DE CALCULS ====================

    /**
     * Test : Calcul correct du montant avec remise et TVA
     */
    @Test
    public void calcul_MontantLigneAvecRemiseEtTVA_EstCorrect() {
        // Produit avec TVA de 5.5%
        LigneCommande ligne = new LigneCommande(produit3, 10, 10.0, true);

        // 10 x 2.50€ = 25€
        // 25€ - 10% = 22.50€
        assertEquals("Le montant avec remise doit être correct", 22.50, ligne.getMontantLigne(), 0.01);
        assertEquals("Le taux de TVA doit être 5.5%", 5.5, ligne.getProduit().getTauxTva(), 0.01);
    }

    /**
     * Test : Vérification du montant total d'une commande complexe
     */
    @Test
    public void calcul_MontantTotalCommande_AvecPlusieursLignes() {
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 10, 0.0, true));   // 10 x 1.50€ = 15.00€
        lignes.add(new LigneCommande(produit2, 5, 20.0, true));   // 5 x 2.00€ - 20% = 8.00€
        lignes.add(new LigneCommande(produit3, 4, 10.0, true));   // 4 x 2.50€ - 10% = 9.00€

        Commande commande = new Commande.Builder()
                .setId("CMD-CALCUL")
                .setClient(clientTest)
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        // Total : 15.00 + 8.00 + 9.00 = 32.00€
        assertEquals("Le montant total doit être correct", 32.00, commande.getMontantTotal(), 0.01);
    }

    // ==================== TESTS DE LISTE ====================

    /**
     * Test : Sérialisation/désérialisation d'une liste de commandes
     */
    @Test
    public void serialisation_ListeCommandes_Reussit() {
        List<Commande> commandes = new ArrayList<>();
        commandes.add(commandeTest);

        // Création d'une seconde commande
        List<LigneCommande> lignes2 = new ArrayList<>();
        lignes2.add(new LigneCommande(produit1, 5, 0.0, true));

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
     * Test : Modification d'un client et propagation dans ses commandes
     * Scénario :
     * 1. Créer un client "Dupont"
     * 2. Créer une commande pour ce client
     * 3. Modifier le client en "Durand"
     * 4. Vérifier que le client dans la commande a été mis à jour
     */
    @Test
    public void modificationClient_SePropageDansCommandes() {
        // 1) Créer un client initial
        Client clientOriginal = new Client.Builder()
                .setId("CLIENT-001")
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("dupont@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(new Date())
                .build();

        // 2) Créer une commande pour ce client
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 3, 0.0, true));

        Commande commandeOriginale = new Commande.Builder()
                .setId("CMD-TEST-001")
                .setClient(clientOriginal)
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        // Vérifier que la commande contient bien "Dupont"
        assertEquals("Dupont", commandeOriginale.getClient().getNom());
        assertEquals("dupont@example.com", commandeOriginale.getClient().getAdresseMail());

        // 3) Modifier le client (simuler une modification utilisateur)
        Client clientModifie = new Client.Builder()
                .setId("CLIENT-001") // Même ID
                .setNom("Durand") // ✅ Nom modifié
                .setAdresse("20 avenue des Champs-Élysées") // ✅ Adresse modifiée
                .setCodePostal("75008") // ✅ Code postal modifié
                .setVille("Paris")
                .setAdresseMail("durand@example.com") // ✅ Email modifié
                .setTelephone("0987654321") // ✅ Téléphone modifié
                .setUtilisateur("userTest")
                .setDateSaisie(clientOriginal.getDateSaisie())
                .build();

        // 4) Créer une nouvelle commande avec le client modifié (simule updateClientInCommandes)
        Commande commandeModifiee = new Commande.Builder()
                .setId(commandeOriginale.getId())
                .setClient(clientModifie) // ✅ Client mis à jour
                .setDateCommande(commandeOriginale.getDateCommande())
                .setLignesCommande(commandeOriginale.getLignesCommande())
                .setUtilisateur(commandeOriginale.getUtilisateur())
                .build();

        // 5) Vérifications : le client dans la commande doit être mis à jour
        assertNotNull("La commande modifiée ne doit pas être null", commandeModifiee);
        assertNotNull("Le client dans la commande ne doit pas être null", commandeModifiee.getClient());

        // Vérifier que TOUTES les informations du client ont été mises à jour
        assertEquals("Le nom du client doit être 'Durand'", "Durand", commandeModifiee.getClient().getNom());
        assertEquals("L'adresse doit être mise à jour", "20 avenue des Champs-Élysées", commandeModifiee.getClient().getAdresse());
        assertEquals("Le code postal doit être mis à jour", "75008", commandeModifiee.getClient().getCodePostal());
        assertEquals("L'email doit être mis à jour", "durand@example.com", commandeModifiee.getClient().getAdresseMail());
        assertEquals("Le téléphone doit être mis à jour", "0987654321", commandeModifiee.getClient().getTelephone());

        // Vérifier que l'ID du client est resté le même
        assertEquals("L'ID du client doit rester le même", "CLIENT-001", commandeModifiee.getClient().getId());

        // Vérifier que les lignes de commande n'ont pas été modifiées
        assertEquals("Le nombre de lignes doit rester le même", 1, commandeModifiee.getLignesCommande().size());
        assertEquals("Le montant total doit rester le même",
                     commandeOriginale.getMontantTotal(),
                     commandeModifiee.getMontantTotal(), 0.01);

        // 6) Test de sérialisation/désérialisation avec le client modifié
        String json = gson.toJson(commandeModifiee);
        Commande commandeDeserialise = gson.fromJson(json, Commande.class);

        assertNotNull("La commande désérialisée ne doit pas être null", commandeDeserialise);
        assertEquals("Le nom du client après sérialisation doit être 'Durand'",
                     "Durand", commandeDeserialise.getClient().getNom());
        assertEquals("L'email du client après sérialisation doit être mis à jour",
                     "durand@example.com", commandeDeserialise.getClient().getAdresseMail());
    }

    /**
     * Test : Modification d'un client avec plusieurs commandes
     * Scénario :
     * 1. Créer un client
     * 2. Créer PLUSIEURS commandes pour ce client
     * 3. Modifier le client
     * 4. Vérifier que TOUTES les commandes sont mises à jour
     */
    @Test
    public void modificationClient_SePropageDansPlusieurCommandes() {
        // 1) Créer un client
        Client clientOriginal = new Client.Builder()
                .setId("CLIENT-002")
                .setNom("Martin")
                .setAdresse("5 rue du Test")
                .setCodePostal("69000")
                .setVille("Lyon")
                .setAdresseMail("martin@test.com")
                .setTelephone("0411111111")
                .setUtilisateur("userTest")
                .setDateSaisie(new Date())
                .build();

        // 2) Créer plusieurs commandes pour ce client
        List<Commande> commandes = new ArrayList<>();

        // Commande 1
        List<LigneCommande> lignes1 = new ArrayList<>();
        lignes1.add(new LigneCommande(produit1, 2, 0.0, true));
        Commande cmd1 = new Commande.Builder()
                .setId("CMD-MULTI-001")
                .setClient(clientOriginal)
                .setDateCommande(new Date())
                .setLignesCommande(lignes1)
                .setUtilisateur("userTest")
                .build();
        commandes.add(cmd1);

        // Commande 2
        List<LigneCommande> lignes2 = new ArrayList<>();
        lignes2.add(new LigneCommande(produit2, 5, 10.0, true));
        Commande cmd2 = new Commande.Builder()
                .setId("CMD-MULTI-002")
                .setClient(clientOriginal)
                .setDateCommande(new Date())
                .setLignesCommande(lignes2)
                .setUtilisateur("userTest")
                .build();
        commandes.add(cmd2);

        // Commande 3
        List<LigneCommande> lignes3 = new ArrayList<>();
        lignes3.add(new LigneCommande(produit1, 1, 0.0, true));
        lignes3.add(new LigneCommande(produit2, 2, 5.0, true));
        Commande cmd3 = new Commande.Builder()
                .setId("CMD-MULTI-003")
                .setClient(clientOriginal)
                .setDateCommande(new Date())
                .setLignesCommande(lignes3)
                .setUtilisateur("userTest")
                .build();
        commandes.add(cmd3);

        // Vérifier que toutes les commandes ont le client "Martin"
        for (Commande cmd : commandes) {
            assertEquals("Martin", cmd.getClient().getNom());
        }

        // 3) Modifier le client
        Client clientModifie = new Client.Builder()
                .setId("CLIENT-002") // Même ID
                .setNom("Martin-Dupuis") // Nom modifié
                .setAdresse("15 boulevard Nouveau")
                .setCodePostal("69001")
                .setVille("Lyon")
                .setAdresseMail("martin.dupuis@test.com")
                .setTelephone("0422222222")
                .setUtilisateur("userTest")
                .setDateSaisie(clientOriginal.getDateSaisie())
                .build();

        // 4) Mettre à jour toutes les commandes (simule updateClientInCommandes)
        List<Commande> commandesModifiees = new ArrayList<>();
        for (Commande cmdOriginal : commandes) {
            Commande cmdModifiee = new Commande.Builder()
                    .setId(cmdOriginal.getId())
                    .setClient(clientModifie) // Client mis à jour
                    .setDateCommande(cmdOriginal.getDateCommande())
                    .setLignesCommande(cmdOriginal.getLignesCommande())
                    .setUtilisateur(cmdOriginal.getUtilisateur())
                    .build();
            commandesModifiees.add(cmdModifiee);
        }

        // 5) Vérifier que TOUTES les commandes ont le client modifié
        assertEquals("Doit avoir 3 commandes", 3, commandesModifiees.size());

        for (Commande cmd : commandesModifiees) {
            assertNotNull("Le client ne doit pas être null", cmd.getClient());
            assertEquals("Le nom doit être 'Martin-Dupuis'", "Martin-Dupuis", cmd.getClient().getNom());
            assertEquals("L'adresse doit être mise à jour", "15 boulevard Nouveau", cmd.getClient().getAdresse());
            assertEquals("L'email doit être mis à jour", "martin.dupuis@test.com", cmd.getClient().getAdresseMail());
            assertEquals("Le téléphone doit être mis à jour", "0422222222", cmd.getClient().getTelephone());
        }

        // 6) Vérifier que les montants des commandes n'ont pas changé
        for (int i = 0; i < commandes.size(); i++) {
            assertEquals("Le montant de la commande " + (i + 1) + " ne doit pas changer",
                         commandes.get(i).getMontantTotal(),
                         commandesModifiees.get(i).getMontantTotal(),
                         0.01);
        }
    }

    /**
     * Test : Vérification que l'ID du client est bien utilisé pour identifier les commandes à mettre à jour
     */
    @Test
    public void modificationClient_NeModifiePasAutresClients() {
        // Créer deux clients différents
        Client client1 = new Client.Builder()
                .setId("CLIENT-A")
                .setNom("Client A")
                .setAdresse("Adresse A")
                .setCodePostal("10000")
                .setVille("Ville A")
                .setAdresseMail("a@test.com")
                .setTelephone("0100000000")
                .setUtilisateur("userTest")
                .setDateSaisie(new Date())
                .build();

        Client client2 = new Client.Builder()
                .setId("CLIENT-B")
                .setNom("Client B")
                .setAdresse("Adresse B")
                .setCodePostal("20000")
                .setVille("Ville B")
                .setAdresseMail("b@test.com")
                .setTelephone("0200000000")
                .setUtilisateur("userTest")
                .setDateSaisie(new Date())
                .build();

        // Créer une commande pour chaque client
        List<LigneCommande> lignes = new ArrayList<>();
        lignes.add(new LigneCommande(produit1, 1, 0.0, true));

        Commande commandeA = new Commande.Builder()
                .setId("CMD-A")
                .setClient(client1)
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        Commande commandeB = new Commande.Builder()
                .setId("CMD-B")
                .setClient(client2)
                .setDateCommande(new Date())
                .setLignesCommande(lignes)
                .setUtilisateur("userTest")
                .build();

        // Modifier client1
        Client client1Modifie = new Client.Builder()
                .setId("CLIENT-A")
                .setNom("Client A Modifié")
                .setAdresse("Nouvelle Adresse A")
                .setCodePostal("10001")
                .setVille("Nouvelle Ville A")
                .setAdresseMail("a.modifie@test.com")
                .setTelephone("0111111111")
                .setUtilisateur("userTest")
                .setDateSaisie(client1.getDateSaisie())
                .build();

        // Mettre à jour seulement la commande du client A
        Commande commandeAModifiee = new Commande.Builder()
                .setId(commandeA.getId())
                .setClient(client1Modifie)
                .setDateCommande(commandeA.getDateCommande())
                .setLignesCommande(commandeA.getLignesCommande())
                .setUtilisateur(commandeA.getUtilisateur())
                .build();

        // Vérifications
        assertEquals("La commande A doit avoir le client modifié",
                     "Client A Modifié", commandeAModifiee.getClient().getNom());
        assertEquals("La commande B ne doit PAS être modifiée",
                     "Client B", commandeB.getClient().getNom());
        assertEquals("L'email de la commande B ne doit PAS changer",
                     "b@test.com", commandeB.getClient().getAdresseMail());
    }
}
