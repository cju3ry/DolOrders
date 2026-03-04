package com.example.dolorders.objet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Tests unitaires pour la classe LigneCommande.
 * Valide la création, les validations et le calcul du montant de ligne.
 */
public class LigneCommandeTest {

    private Produit produitValide;

    @Before
    public void setUp() {
        produitValide = new Produit("1", "Produit Test", "Description", 100.0, 20.0);
    }

    // ==================== Tests création ligne valide ====================

    @Test
    public void creation_AvecTousLesChamps_Reussit() {
        Date dateCreation = new Date();
        LigneCommande ligne = new LigneCommande(produitValide, 5, 10.0, true, dateCreation);

        assertNotNull(ligne);
        assertEquals(produitValide, ligne.getProduit());
        assertEquals(5, ligne.getQuantite());
        assertEquals(10.0, ligne.getRemise(), 0.01);
        assertTrue(ligne.isValidee());
        assertEquals(dateCreation, ligne.getDateCreation());
    }

    @Test
    public void creation_SansDateCreation_UtiliseDateActuelle() {
        LigneCommande ligne = new LigneCommande(produitValide, 2, 0.0, false);

        assertNotNull(ligne.getDateCreation());
    }

    @Test
    public void creation_AvecDateCreationNull_UtiliseDateActuelle() {
        LigneCommande ligne = new LigneCommande(produitValide, 2, 0.0, false, null);

        assertNotNull(ligne.getDateCreation());
    }

    @Test
    public void creation_LigneNonValidee_Reussit() {
        LigneCommande ligne = new LigneCommande(produitValide, 1, 0.0, false);

        assertFalse(ligne.isValidee());
    }

    @Test
    public void creation_LigneValidee_Reussit() {
        LigneCommande ligne = new LigneCommande(produitValide, 1, 0.0, true);

        assertTrue(ligne.isValidee());
    }

    // ==================== Tests validation produit ====================

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecProduitNull_LanceException() {
        new LigneCommande(null, 1, 0.0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecProduitNullEtDate_LanceException() {
        new LigneCommande(null, 1, 0.0, false, new Date());
    }

    // ==================== Tests validation quantité ====================

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecQuantiteZero_LanceException() {
        new LigneCommande(produitValide, 0, 0.0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecQuantiteNegative_LanceException() {
        new LigneCommande(produitValide, -1, 0.0, false);
    }

    @Test
    public void creation_AvecQuantiteUn_Reussit() {
        LigneCommande ligne = new LigneCommande(produitValide, 1, 0.0, false);

        assertEquals(1, ligne.getQuantite());
    }

    @Test
    public void creation_AvecGrandeQuantite_Reussit() {
        LigneCommande ligne = new LigneCommande(produitValide, 10000, 0.0, false);

        assertEquals(10000, ligne.getQuantite());
    }

    // ==================== Tests validation remise ====================

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecRemiseNegative_LanceException() {
        new LigneCommande(produitValide, 1, -5.0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creation_AvecRemiseSuperieure100_LanceException() {
        new LigneCommande(produitValide, 1, 101.0, false);
    }

    @Test
    public void creation_AvecRemiseZero_Reussit() {
        LigneCommande ligne = new LigneCommande(produitValide, 1, 0.0, false);

        assertEquals(0.0, ligne.getRemise(), 0.01);
    }

    @Test
    public void creation_AvecRemise100_Reussit() {
        LigneCommande ligne = new LigneCommande(produitValide, 1, 100.0, false);

        assertEquals(100.0, ligne.getRemise(), 0.01);
    }

    @Test
    public void creation_AvecRemise50_Reussit() {
        LigneCommande ligne = new LigneCommande(produitValide, 1, 50.0, false);

        assertEquals(50.0, ligne.getRemise(), 0.01);
    }

    // ==================== Tests calcul montant ligne ====================

    @Test
    public void getMontantLigne_SansRemise_CalculCorrect() {
        // Produit à 100€, quantité 3, remise 0%
        LigneCommande ligne = new LigneCommande(produitValide, 3, 0.0, false);

        // 100 x 3 = 300€
        assertEquals(300.0, ligne.getMontantLigne(), 0.01);
    }

    @Test
    public void getMontantLigne_AvecRemise10Pourcent_CalculCorrect() {
        // Produit à 100€, quantité 2, remise 10%
        LigneCommande ligne = new LigneCommande(produitValide, 2, 10.0, false);

        // 100 x 2 = 200€, remise 10% = 180€
        assertEquals(180.0, ligne.getMontantLigne(), 0.01);
    }

    @Test
    public void getMontantLigne_AvecRemise50Pourcent_CalculCorrect() {
        // Produit à 100€, quantité 1, remise 50%
        LigneCommande ligne = new LigneCommande(produitValide, 1, 50.0, false);

        // 100 x 1 = 100€, remise 50% = 50€
        assertEquals(50.0, ligne.getMontantLigne(), 0.01);
    }

    @Test
    public void getMontantLigne_AvecRemise100Pourcent_RetourneZero() {
        // Produit à 100€, quantité 5, remise 100%
        LigneCommande ligne = new LigneCommande(produitValide, 5, 100.0, false);

        // 100 x 5 = 500€, remise 100% = 0€
        assertEquals(0.0, ligne.getMontantLigne(), 0.01);
    }

    @Test
    public void getMontantLigne_AvecRemiseDecimale_CalculCorrect() {
        // Produit à 100€, quantité 1, remise 33.33%
        LigneCommande ligne = new LigneCommande(produitValide, 1, 33.33, false);

        // 100 x 1 = 100€, remise 33.33% = 66.67€
        assertEquals(66.67, ligne.getMontantLigne(), 0.01);
    }

    @Test
    public void getMontantLigne_AvecProduitPetitPrix_CalculCorrect() {
        // Produit à 0.99€, quantité 10, remise 0%
        Produit produitPasCher = new Produit("2", "Petit produit", "", 0.99);
        LigneCommande ligne = new LigneCommande(produitPasCher, 10, 0.0, false);

        // 0.99 x 10 = 9.90€
        assertEquals(9.90, ligne.getMontantLigne(), 0.01);
    }

    @Test
    public void getMontantLigne_AvecProduitGratuit_RetourneZero() {
        // Produit à 0€, quantité 100, remise 0%
        Produit produitGratuit = new Produit("3", "Gratuit", "", 0.0);
        LigneCommande ligne = new LigneCommande(produitGratuit, 100, 0.0, false);

        assertEquals(0.0, ligne.getMontantLigne(), 0.01);
    }

    @Test
    public void getMontantLigne_GrandeQuantiteEtRemise_CalculCorrect() {
        // Produit à 100€, quantité 1000, remise 25%
        LigneCommande ligne = new LigneCommande(produitValide, 1000, 25.0, false);

        // 100 x 1000 = 100000€, remise 25% = 75000€
        assertEquals(75000.0, ligne.getMontantLigne(), 0.01);
    }

    // ==================== Tests getters ====================

    @Test
    public void getProduit_RetourneProduitCorrect() {
        LigneCommande ligne = new LigneCommande(produitValide, 1, 0.0, false);

        assertEquals("Produit Test", ligne.getProduit().getLibelle());
        assertEquals(100.0, ligne.getProduit().getPrixUnitaire(), 0.01);
    }

    @Test
    public void getQuantite_RetourneQuantiteCorrecte() {
        LigneCommande ligne = new LigneCommande(produitValide, 7, 0.0, false);

        assertEquals(7, ligne.getQuantite());
    }

    @Test
    public void getRemise_RetourneRemiseCorrecte() {
        LigneCommande ligne = new LigneCommande(produitValide, 1, 15.5, false);

        assertEquals(15.5, ligne.getRemise(), 0.01);
    }
}

