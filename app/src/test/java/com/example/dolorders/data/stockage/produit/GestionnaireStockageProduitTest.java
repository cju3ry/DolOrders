package com.example.dolorders.data.stockage.produit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.dolorders.objet.Produit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests unitaires pour les composants de stockage des produits.
 * Ces tests se concentrent sur la logique métier sans dépendances Android.
 * Structure similaire à GestionnaireStockageClientTest.
 */
public class GestionnaireStockageProduitTest {

    private Gson gson;
    private Produit produitTest1;
    private Produit produitTest2;
    private Produit produitTest3;

    @Before
    public void setUp() {
        // Configuration de Gson avec l'adaptateur personnalisé
        gson = new GsonBuilder()
                .registerTypeAdapter(Produit.class, new ProduitTypeAdapter())
                .create();

        // Création de produits de test
        produitTest1 = new Produit("1", "Evian 1.5L", "Eau minérale naturelle", 1.50, 20.0);
        produitTest2 = new Produit("2", "Contrex 1L", "Eau riche en minéraux", 2.30, 20.0);
        produitTest3 = new Produit("3", "Pain Bio", "Pain complet", 3.50, 5.5); // TVA réduite
    }

    // ==================== TESTS DE CRÉATION ====================

    /**
     * Test : Vérifie que le produit est bien créé avec toutes ses données
     */
    @Test
    public void creationProduit_AvecToutesLesDonnees_Reussit() {
        assertNotNull("Le produit ne devrait pas être null", produitTest1);
        assertEquals("1", produitTest1.getId());
        assertEquals("Evian 1.5L", produitTest1.getLibelle());
        assertEquals("Eau minérale naturelle", produitTest1.getDescription());
        assertEquals(1.50, produitTest1.getPrixUnitaire(), 0.01);
        assertEquals(20.0, produitTest1.getTauxTva(), 0.01);
    }

    /**
     * Test : Vérifie que plusieurs produits peuvent être créés
     */
    @Test
    public void creationProduits_Plusieurs_Reussit() {
        assertNotNull(produitTest1);
        assertNotNull(produitTest2);
        assertNotNull(produitTest3);
        assertEquals("Evian 1.5L", produitTest1.getLibelle());
        assertEquals("Contrex 1L", produitTest2.getLibelle());
        assertEquals("Pain Bio", produitTest3.getLibelle());
    }

    // ==================== TESTS DE SÉRIALISATION ====================

    /**
     * Test : Sérialisation d'un produit en JSON
     */
    @Test
    public void serialisation_ProduitEnJson_Contient_ChampsPrincipaux() {
        String json = gson.toJson(produitTest1);

        assertNotNull("Le JSON ne devrait pas être null", json);
        assertFalse("Le JSON ne devrait pas être vide", json.isEmpty());
        assertTrue("Le JSON devrait contenir l'id", json.contains("\"id\":\"1\""));
        assertTrue("Le JSON devrait contenir le libelle", json.contains("Evian"));
        assertTrue("Le JSON devrait contenir le prix", json.contains("1.5"));
    }

    /**
     * Test : Sérialisation de plusieurs produits
     */
    @Test
    public void serialisation_PlusieursProduits_Reussit() {
        String json1 = gson.toJson(produitTest1);
        String json2 = gson.toJson(produitTest2);
        String json3 = gson.toJson(produitTest3);

        assertNotNull("Le JSON du premier produit ne devrait pas être null", json1);
        assertNotNull("Le JSON du deuxième produit ne devrait pas être null", json2);
        assertNotNull("Le JSON du troisième produit ne devrait pas être null", json3);
        assertTrue("Le premier JSON devrait contenir Evian", json1.contains("Evian"));
        assertTrue("Le deuxième JSON devrait contenir Contrex", json2.contains("Contrex"));
        assertTrue("Le troisième JSON devrait contenir Pain", json3.contains("Pain"));
    }

    /**
     * Test : Sérialisation avec null retourne "null"
     */
    @Test
    public void serialisation_ProduitNull_RetourneNull() {
        String json = gson.toJson(null, Produit.class);
        assertEquals("null", json);
    }

    /**
     * Test : Vérifie que la TVA est correctement sérialisée
     */
    @Test
    public void serialisation_AvecTva_ContientTauxTva() {
        String json = gson.toJson(produitTest3);

        assertTrue("Le JSON devrait contenir le taux de TVA réduit", json.contains("5.5"));
    }

    // ==================== TESTS DE DÉSÉRIALISATION ====================

    /**
     * Test : Désérialisation d'un JSON simple en Produit
     */
    @Test
    public void deserialisation_JsonSimpleEnProduit_Reussit() {
        String json = "{" +
                "\"id\":\"123\"," +
                "\"libelle\":\"TestProduit\"," +
                "\"description\":\"Description test\"," +
                "\"prixUnitaire\":15.99," +
                "\"tauxTva\":20.0" +
                "}";

        Produit produit = gson.fromJson(json, Produit.class);

        assertNotNull("Le produit ne devrait pas être null", produit);
        assertEquals("ID", "123", produit.getId());
        assertEquals("Libellé", "TestProduit", produit.getLibelle());
        assertEquals("Description", "Description test", produit.getDescription());
        assertEquals("Prix", 15.99, produit.getPrixUnitaire(), 0.01);
        assertEquals("TVA", 20.0, produit.getTauxTva(), 0.01);
    }

    /**
     * Test : Désérialisation avec TVA réduite
     */
    @Test
    public void deserialisation_AvecTvaReduite_Reussit() {
        String json = "{" +
                "\"id\":\"1\"," +
                "\"libelle\":\"Livre\"," +
                "\"description\":\"Roman\"," +
                "\"prixUnitaire\":12.50," +
                "\"tauxTva\":5.5" +
                "}";

        Produit produit = gson.fromJson(json, Produit.class);

        assertEquals("TVA réduite", 5.5, produit.getTauxTva(), 0.01);
    }

    /**
     * Test : Désérialisation sans TVA utilise valeur par défaut
     */
    @Test
    public void deserialisation_SansTva_UtiliseValeurParDefaut() {
        String json = "{" +
                "\"id\":\"1\"," +
                "\"libelle\":\"Test\"," +
                "\"description\":\"Desc\"," +
                "\"prixUnitaire\":10.0" +
                "}";

        Produit produit = gson.fromJson(json, Produit.class);

        assertEquals("TVA par défaut devrait être 20%", 20.0, produit.getTauxTva(), 0.01);
    }

    // ==================== TESTS CYCLE COMPLET ====================

    /**
     * Test : Cycle complet sérialisation/désérialisation
     */
    @Test
    public void cycleComplet_SerialisationDeserialisation_PreserveLesDonnees() {
        String json = gson.toJson(produitTest1);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertNotNull("Le produit reconstitué ne devrait pas être null", produitReconstitue);
        assertEquals("ID", produitTest1.getId(), produitReconstitue.getId());
        assertEquals("Libellé", produitTest1.getLibelle(), produitReconstitue.getLibelle());
        assertEquals("Description", produitTest1.getDescription(), produitReconstitue.getDescription());
        assertEquals("Prix", produitTest1.getPrixUnitaire(), produitReconstitue.getPrixUnitaire(), 0.01);
        assertEquals("TVA", produitTest1.getTauxTva(), produitReconstitue.getTauxTva(), 0.01);
    }

    /**
     * Test : Cycle complet avec TVA réduite
     */
    @Test
    public void cycleComplet_AvecTvaReduite_PreserveTva() {
        String json = gson.toJson(produitTest3);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("TVA réduite préservée", 5.5, produitReconstitue.getTauxTva(), 0.01);
    }

    /**
     * Test : Cycle complet avec prix zéro
     */
    @Test
    public void cycleComplet_AvecPrixZero_PreservePrix() {
        Produit produitGratuit = new Produit("0", "Echantillon", "Gratuit", 0.0, 0.0);

        String json = gson.toJson(produitGratuit);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Prix zéro préservé", 0.0, produitReconstitue.getPrixUnitaire(), 0.01);
        assertEquals("TVA zéro préservée", 0.0, produitReconstitue.getTauxTva(), 0.01);
    }

    /**
     * Test : Cycle complet avec ID préservé
     */
    @Test
    public void cycleComplet_AvecId_PreserveId() {
        Produit produitAvecId = new Produit("PROD-12345", "Test", "Description", 10.0);

        String json = gson.toJson(produitAvecId);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("L'ID devrait être préservé", "PROD-12345", produitReconstitue.getId());
    }

    // ==================== TESTS CARACTÈRES SPÉCIAUX ====================

    /**
     * Test : Désérialisation avec caractères spéciaux
     */
    @Test
    public void deserialisation_AvecCaracteresSpeciaux_Reussit() {
        String json = "{" +
                "\"id\":\"1\"," +
                "\"libelle\":\"Café éthiopien\"," +
                "\"description\":\"Arôme corsé & intense\"," +
                "\"prixUnitaire\":12.50," +
                "\"tauxTva\":20.0" +
                "}";

        Produit produit = gson.fromJson(json, Produit.class);

        assertEquals("Café éthiopien", produit.getLibelle());
        assertEquals("Arôme corsé & intense", produit.getDescription());
    }

    /**
     * Test : Cycle complet avec caractères spéciaux
     */
    @Test
    public void cycleComplet_AvecCaracteresSpeciaux_Reussit() {
        Produit produitSpecial = new Produit("1", "Thé vert façon Japonaise", "Infusion légère & parfumée", 8.99, 5.5);

        String json = gson.toJson(produitSpecial);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Thé vert façon Japonaise", produitReconstitue.getLibelle());
        assertEquals("Infusion légère & parfumée", produitReconstitue.getDescription());
    }

    // ==================== TESTS CAS LIMITES ====================

    /**
     * Test : Désérialisation avec description vide
     */
    @Test
    public void deserialisation_AvecDescriptionVide_Reussit() {
        String json = "{" +
                "\"id\":\"1\"," +
                "\"libelle\":\"Produit\"," +
                "\"description\":\"\"," +
                "\"prixUnitaire\":10.0," +
                "\"tauxTva\":20.0" +
                "}";

        Produit produit = gson.fromJson(json, Produit.class);

        assertEquals("", produit.getDescription());
    }

    /**
     * Test : Désérialisation avec champs inconnus ignorés
     */
    @Test
    public void deserialisation_AvecChampsInconnus_IgnoreLesChamps() {
        String json = "{" +
                "\"id\":\"1\"," +
                "\"libelle\":\"Test\"," +
                "\"description\":\"Desc\"," +
                "\"prixUnitaire\":10.0," +
                "\"tauxTva\":20.0," +
                "\"champInconnu\":\"valeur\"," +
                "\"autreChamp\":123" +
                "}";

        Produit produit = gson.fromJson(json, Produit.class);

        assertNotNull("Le produit devrait être créé malgré les champs inconnus", produit);
        assertEquals("Test", produit.getLibelle());
    }

    /**
     * Test : Cycle complet avec prix très grand
     */
    @Test
    public void cycleComplet_AvecPrixTresGrand_Reussit() {
        Produit produitCher = new Produit("1", "Produit Luxe", "Très cher", 999999.99, 20.0);

        String json = gson.toJson(produitCher);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Prix très grand préservé", 999999.99, produitReconstitue.getPrixUnitaire(), 0.01);
    }

    /**
     * Test : Cycle complet avec prix décimal précis
     */
    @Test
    public void cycleComplet_AvecPrixDecimal_Reussit() {
        Produit produitDecimal = new Produit("1", "Produit", "Desc", 1.234567, 20.0);

        String json = gson.toJson(produitDecimal);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Prix décimal préservé", 1.234567, produitReconstitue.getPrixUnitaire(), 0.000001);
    }

    /**
     * Test : Cycle complet avec description longue
     */
    @Test
    public void cycleComplet_AvecDescriptionLongue_Reussit() {
        String longueDescription = "A".repeat(1000);
        Produit produitLongDesc = new Produit("1", "Produit", longueDescription, 10.0);

        String json = gson.toJson(produitLongDesc);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Description longue préservée", longueDescription, produitReconstitue.getDescription());
    }

    // ==================== TESTS LISTE DE PRODUITS ====================

    /**
     * Test : Sérialisation/désérialisation d'une liste de produits
     */
    @Test
    public void cycleComplet_ListeProduits_Reussit() {
        List<Produit> produits = new ArrayList<>();
        produits.add(produitTest1);
        produits.add(produitTest2);
        produits.add(produitTest3);

        // Sérialisation manuelle de chaque produit
        List<String> jsons = new ArrayList<>();
        for (Produit p : produits) {
            jsons.add(gson.toJson(p));
        }

        // Désérialisation
        List<Produit> produitsReconstitues = new ArrayList<>();
        for (String json : jsons) {
            produitsReconstitues.add(gson.fromJson(json, Produit.class));
        }

        assertEquals("Même nombre de produits", produits.size(), produitsReconstitues.size());
        assertEquals("Premier produit", produitTest1.getLibelle(), produitsReconstitues.get(0).getLibelle());
        assertEquals("Deuxième produit", produitTest2.getLibelle(), produitsReconstitues.get(1).getLibelle());
        assertEquals("Troisième produit", produitTest3.getLibelle(), produitsReconstitues.get(2).getLibelle());
    }

    /**
     * Test : Vérifie que les différentes TVA sont préservées dans une liste
     */
    @Test
    public void cycleComplet_ListeAvecDifferentesTva_PreserveTva() {
        List<Produit> produits = new ArrayList<>();
        produits.add(new Produit("1", "Standard", "", 10.0, 20.0));
        produits.add(new Produit("2", "Réduite", "", 10.0, 5.5));
        produits.add(new Produit("3", "Super réduite", "", 10.0, 2.1));

        for (int i = 0; i < produits.size(); i++) {
            String json = gson.toJson(produits.get(i));
            Produit produitReconstitue = gson.fromJson(json, Produit.class);
            assertEquals("TVA du produit " + i,
                    produits.get(i).getTauxTva(),
                    produitReconstitue.getTauxTva(),
                    0.01);
        }
    }
}

