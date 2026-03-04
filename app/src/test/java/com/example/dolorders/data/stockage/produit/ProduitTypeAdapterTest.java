package com.example.dolorders.data.stockage.produit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.dolorders.objet.Produit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests unitaires pour ProduitTypeAdapter.
 * Vérifie que la sérialisation/désérialisation JSON des produits fonctionne correctement.
 */
public class ProduitTypeAdapterTest {

    private Gson gson;
    private Produit produitTest;

    @Before
    public void setUp() {
        // Configuration de Gson avec l'adaptateur personnalisé
        gson = new GsonBuilder()
                .registerTypeAdapter(Produit.class, new ProduitTypeAdapter())
                .create();

        // Création d'un produit de test
        produitTest = new Produit("123", "Evian 1.5L", "Eau minérale naturelle", 1.50, 20.0);
    }

    // ==================== Tests de sérialisation ====================

    @Test
    public void serialisation_ProduitEnJson_Reussit() {
        String json = gson.toJson(produitTest);

        assertNotNull("Le JSON ne devrait pas être null", json);
        assertTrue("Le JSON doit contenir l'id", json.contains("\"id\":\"123\""));
        assertTrue("Le JSON doit contenir le libelle", json.contains("\"libelle\":\"Evian 1.5L\""));
        assertTrue("Le JSON doit contenir la description", json.contains("\"description\":\"Eau minérale naturelle\""));
    }

    @Test
    public void serialisation_ProduitNull_RetourneNull() {
        String json = gson.toJson(null, Produit.class);

        assertEquals("Le JSON devrait être 'null'", "null", json);
    }

    @Test
    public void serialisation_InclutTauxTva() {
        Produit produitAvecTva = new Produit("1", "Test", "Desc", 10.0, 5.5);

        String json = gson.toJson(produitAvecTva);

        assertTrue("Le JSON doit contenir le taux de TVA", json.contains("\"tauxTva\":5.5"));
    }

    // ==================== Tests de désérialisation ====================

    @Test
    public void deserialisation_JsonEnProduit_Reussit() {
        String json = "{" +
                "\"id\":\"456\"," +
                "\"libelle\":\"Contrex 1L\"," +
                "\"description\":\"Eau riche en minéraux\"," +
                "\"prixUnitaire\":2.30," +
                "\"tauxTva\":20.0" +
                "}";

        Produit produit = gson.fromJson(json, Produit.class);

        assertNotNull("Le produit ne devrait pas être null", produit);
        assertEquals("ID", "456", produit.getId());
        assertEquals("Libellé", "Contrex 1L", produit.getLibelle());
        assertEquals("Description", "Eau riche en minéraux", produit.getDescription());
        assertEquals("Prix unitaire", 2.30, produit.getPrixUnitaire(), 0.01);
        assertEquals("Taux TVA", 20.0, produit.getTauxTva(), 0.01);
    }

    @Test
    public void deserialisation_SansTauxTva_UtiliseValeurParDefaut() {
        String json = "{" +
                "\"id\":\"1\"," +
                "\"libelle\":\"Test\"," +
                "\"description\":\"Desc\"," +
                "\"prixUnitaire\":10.0" +
                "}";

        Produit produit = gson.fromJson(json, Produit.class);

        assertEquals("TVA par défaut doit être 20%", 20.0, produit.getTauxTva(), 0.01);
    }

    // ==================== Tests cycle complet ====================

    @Test
    public void cycleComplet_SerialisationDeserialisation_PreserveLesDonnees() {
        String json = gson.toJson(produitTest);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertNotNull("Le produit reconstitué ne devrait pas être null", produitReconstitue);
        assertEquals("ID", produitTest.getId(), produitReconstitue.getId());
        assertEquals("Libellé", produitTest.getLibelle(), produitReconstitue.getLibelle());
        assertEquals("Description", produitTest.getDescription(), produitReconstitue.getDescription());
        assertEquals("Prix unitaire", produitTest.getPrixUnitaire(), produitReconstitue.getPrixUnitaire(), 0.01);
        assertEquals("Taux TVA", produitTest.getTauxTva(), produitReconstitue.getTauxTva(), 0.01);
    }

    @Test
    public void cycleComplet_AvecTvaDifferente_PreserveTva() {
        Produit produitTvaReduite = new Produit("1", "Livre", "Roman", 15.0, 5.5);

        String json = gson.toJson(produitTvaReduite);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("TVA réduite doit être préservée", 5.5, produitReconstitue.getTauxTva(), 0.01);
    }

    @Test
    public void cycleComplet_AvecPrixZero_PreservePrix() {
        Produit produitGratuit = new Produit("1", "Échantillon", "Gratuit", 0.0);

        String json = gson.toJson(produitGratuit);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Prix zéro doit être préservé", 0.0, produitReconstitue.getPrixUnitaire(), 0.01);
    }

    // ==================== Tests caractères spéciaux ====================

    @Test
    public void cycleComplet_AvecCaracteresSpeciaux_Reussit() {
        Produit produitSpecial = new Produit("1", "Café éthiopien", "Arôme corsé & intense", 12.50);

        String json = gson.toJson(produitSpecial);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Libellé avec accents", "Café éthiopien", produitReconstitue.getLibelle());
        assertEquals("Description avec caractères spéciaux", "Arôme corsé & intense", produitReconstitue.getDescription());
    }

    @Test
    public void cycleComplet_AvecDescriptionVide_Reussit() {
        Produit produitSansDesc = new Produit("1", "Produit", "", 10.0);

        String json = gson.toJson(produitSansDesc);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Description vide préservée", "", produitReconstitue.getDescription());
    }

    // ==================== Tests cas limites ====================

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

    @Test
    public void cycleComplet_AvecPrixTresGrand_Reussit() {
        Produit produitCher = new Produit("1", "Produit Luxe", "Très cher", 999999.99, 20.0);

        String json = gson.toJson(produitCher);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Prix très grand préservé", 999999.99, produitReconstitue.getPrixUnitaire(), 0.01);
    }

    @Test
    public void cycleComplet_AvecPrixDecimal_Reussit() {
        Produit produitDecimal = new Produit("1", "Produit", "Desc", 1.234567, 20.0);

        String json = gson.toJson(produitDecimal);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Prix décimal préservé", 1.234567, produitReconstitue.getPrixUnitaire(), 0.000001);
    }

    @Test
    public void cycleComplet_AvecDescriptionLongue_Reussit() {
        String longueDescription = "A".repeat(1000);
        Produit produitLongDesc = new Produit("1", "Produit", longueDescription, 10.0);

        String json = gson.toJson(produitLongDesc);
        Produit produitReconstitue = gson.fromJson(json, Produit.class);

        assertEquals("Description longue préservée", longueDescription, produitReconstitue.getDescription());
    }
}

