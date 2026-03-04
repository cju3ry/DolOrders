package com.example.dolorders.objet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests unitaires pour la classe Produit.
 * Valide la création, les validations et les méthodes equals/hashCode.
 */
public class ProduitTest {

    // ==================== Tests création produit valide ====================

    @Test
    public void creation_AvecTousLesChamps_Reussit() {
        Produit produit = new Produit("1", "Produit Test", "Description", 10.50, 20.0);

        assertNotNull(produit);
        assertEquals("1", produit.getId());
        assertEquals("Produit Test", produit.getLibelle());
        assertEquals("Description", produit.getDescription());
        assertEquals(10.50, produit.getPrixUnitaire(), 0.01);
        assertEquals(20.0, produit.getTauxTva(), 0.01);
    }

    @Test
    public void creation_SansTva_UtiliseTvaParDefaut() {
        Produit produit = new Produit("1", "Produit Test", "Description", 10.50);

        assertEquals(20.0, produit.getTauxTva(), 0.01);
    }

    @Test
    public void creation_AvecDescriptionNull_UtiliseChaineVide() {
        Produit produit = new Produit("1", "Produit Test", null, 10.50);

        assertEquals("", produit.getDescription());
    }

    @Test
    public void creation_AvecPrixZero_Reussit() {
        Produit produit = new Produit("1", "Produit Gratuit", "Description", 0.0);

        assertEquals(0.0, produit.getPrixUnitaire(), 0.01);
    }

    @Test
    public void creation_AvecTvaZero_Reussit() {
        Produit produit = new Produit("1", "Produit", "Desc", 10.0, 0.0);

        assertEquals(0.0, produit.getTauxTva(), 0.01);
    }

    @Test
    public void creation_AvecTvaElevee_Reussit() {
        Produit produit = new Produit("1", "Produit", "Desc", 10.0, 33.0);

        assertEquals(33.0, produit.getTauxTva(), 0.01);
    }

    // ==================== Tests validation nom ====================

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecNomNull_LanceException() {
        new Produit("1", null, "Description", 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecNomVide_LanceException() {
        new Produit("1", "", "Description", 10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecNomEspaces_LanceException() {
        new Produit("1", "   ", "Description", 10.0);
    }

    // ==================== Tests validation prix ====================

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecPrixNegatif_LanceException() {
        new Produit("1", "Produit", "Description", -5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecPrixNegatifEtTva_LanceException() {
        new Produit("1", "Produit", "Description", -5.0, 20.0);
    }

    // ==================== Tests validation TVA ====================

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecTvaNegatif_LanceException() {
        new Produit("1", "Produit", "Description", 10.0, -5.0);
    }

    // ==================== Tests equals et hashCode ====================

    @Test
    public void equals_MemeId_SontEgaux() {
        Produit produit1 = new Produit("123", "Produit A", "DescA", 10.0);
        Produit produit2 = new Produit("123", "Produit B", "DescB", 20.0);

        assertEquals(produit1, produit2);
    }

    @Test
    public void equals_IdDifferents_SontDifferents() {
        Produit produit1 = new Produit("1", "Produit", "Desc", 10.0);
        Produit produit2 = new Produit("2", "Produit", "Desc", 10.0);

        assertNotEquals(produit1, produit2);
    }

    @Test
    public void equals_AvecNull_RetourneFalse() {
        Produit produit = new Produit("1", "Produit", "Desc", 10.0);

        assertNotEquals(null, produit);
    }

    @Test
    public void equals_AvecAutreType_RetourneFalse() {
        Produit produit = new Produit("1", "Produit", "Desc", 10.0);

        assertNotEquals("String", produit);
    }

    @Test
    public void hashCode_MemeId_SontEgaux() {
        Produit produit1 = new Produit("123", "Produit A", "DescA", 10.0);
        Produit produit2 = new Produit("123", "Produit B", "DescB", 20.0);

        assertEquals(produit1.hashCode(), produit2.hashCode());
    }

    @Test
    public void hashCode_IdDifferents_SontDifferents() {
        Produit produit1 = new Produit("1", "Produit", "Desc", 10.0);
        Produit produit2 = new Produit("2", "Produit", "Desc", 10.0);

        assertNotEquals(produit1.hashCode(), produit2.hashCode());
    }

    // ==================== Tests toString ====================

    @Test
    public void toString_RetourneLibelle() {
        Produit produit = new Produit("1", "Mon Produit", "Description", 10.0);

        assertEquals("Mon Produit", produit.toString());
    }

    // ==================== Tests cas limites ====================

    @Test
    public void creation_AvecIdNull_Reussit() {
        Produit produit = new Produit(null, "Produit", "Description", 10.0);

        assertNull("L'ID peut être null", produit.getId());
    }

    @Test
    public void creation_AvecPrixTresGrand_Reussit() {
        Produit produit = new Produit("1", "Produit Cher", "Desc", 999999.99);

        assertEquals(999999.99, produit.getPrixUnitaire(), 0.01);
    }

    @Test
    public void creation_AvecDescriptionLongue_Reussit() {
        String longueDescription = "A".repeat(1000);
        Produit produit = new Produit("1", "Produit", longueDescription, 10.0);

        assertEquals(longueDescription, produit.getDescription());
    }

    @Test
    public void creation_AvecCaracteresSpeciaux_Reussit() {
        Produit produit = new Produit("1", "Produit éàù@#!", "Desc avec émojis 🎉", 10.0);

        assertEquals("Produit éàù@#!", produit.getLibelle());
        assertEquals("Desc avec émojis 🎉", produit.getDescription());
    }
}

