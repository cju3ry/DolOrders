package com.example.dolorders.objet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tests unitaires pour la classe Commande et son Builder.
 * Ces tests valident la logique métier du modèle de commande.
 */
public class CommandeTest {

    private Client clientValide;
    private List<LigneCommande> lignesValides;
    private Date dateValide;
    private String utilisateurValide;
    private Produit produitA;
    private Produit produitB;

    @Before
    public void setUp() {
        // Initialisation des données valides pour les tests
        clientValide = new Client.Builder()
                .setId("1")
                .setNom("Client Test")
                .setAdresse("1 rue du Test")
                .setCodePostal("00000")
                .setVille("Testville")
                .setAdresseMail("test@test.com")
                .setTelephone("0102030405")
                .setUtilisateur("testeur")
                .setDateSaisie(new Date())
                .build();

        // Création des produits
        produitA = new Produit("1", "Produit A", "Description Produit A", 10.0);
        produitB = new Produit("2", "Produit B", "Description Produit B", 5.0);

        // Initialisation de la liste des lignes de commande
        lignesValides = new ArrayList<>();
        lignesValides.add(new LigneCommande(produitA, 2, 0.0, true)); // 10€ x 2 = 20€
        lignesValides.add(new LigneCommande(produitB, 1, 0.0, true)); // 5€ x 1 = 5€

        dateValide = new Date();
        utilisateurValide = "Admin";
    }

    // ==================== Tests création commande valide ====================

    @Test
    public void build_AvecTousLesChampsValides_Reussit() {
        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();

        assertNotNull(commande);
        assertEquals(clientValide, commande.getClient());
        assertEquals(dateValide, commande.getDateCommande());
        assertEquals(utilisateurValide, commande.getUtilisateur());
        assertEquals(2, commande.getLignesCommande().size());
    }

    @Test
    public void build_AvecId_Reussit() {
        Commande commande = new Commande.Builder()
                .setId("CMD001")
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();

        assertEquals("CMD001", commande.getId());
    }

    @Test
    public void build_SansDate_UtiliseDateActuelle() {
        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setUtilisateur(utilisateurValide)
                .build();

        assertNotNull(commande.getDateCommande());
    }

    // ==================== Tests calcul montant total ====================

    @Test
    public void build_CalculeMontantTotalCorrectement() {
        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();

        // 20€ + 5€ = 25€
        assertEquals(25.0, commande.getMontantTotal(), 0.01);
    }

    @Test
    public void build_CalculeMontantAvecRemiseParLigne() {
        List<LigneCommande> lignesAvecRemise = new ArrayList<>();
        // Produit à 100€, qté 1, remise 50% -> 50€
        Produit produitCher = new Produit("3", "Produit Cher", "Desc", 100.0);
        lignesAvecRemise.add(new LigneCommande(produitCher, 1, 50.0, true));

        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesAvecRemise)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();

        assertEquals(50.0, commande.getMontantTotal(), 0.01);
    }

    @Test
    public void build_CalculeMontantAvecPlusieursLignesEtRemises() {
        List<LigneCommande> lignes = new ArrayList<>();
        // Ligne 1: 100€ x 2 = 200€, remise 10% -> 180€
        lignes.add(new LigneCommande(new Produit("1", "P1", "", 100.0), 2, 10.0, true));
        // Ligne 2: 50€ x 3 = 150€, remise 20% -> 120€
        lignes.add(new LigneCommande(new Produit("2", "P2", "", 50.0), 3, 20.0, true));

        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignes)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();

        // Total: 180€ + 120€ = 300€
        assertEquals(300.0, commande.getMontantTotal(), 0.01);
    }

    @Test
    public void build_CalculeMontantAvecUneLigneUnique() {
        List<LigneCommande> uneLigne = new ArrayList<>();
        uneLigne.add(new LigneCommande(produitA, 5, 0.0, true)); // 10€ x 5 = 50€

        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(uneLigne)
                .setUtilisateur(utilisateurValide)
                .build();

        assertEquals(50.0, commande.getMontantTotal(), 0.01);
    }

    @Test
    public void build_CalculeMontantAvecRemise100Pourcent() {
        List<LigneCommande> ligneGratuite = new ArrayList<>();
        ligneGratuite.add(new LigneCommande(produitA, 10, 100.0, true)); // Remise 100%

        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(ligneGratuite)
                .setUtilisateur(utilisateurValide)
                .build();

        assertEquals(0.0, commande.getMontantTotal(), 0.01);
    }

    // ==================== Tests validation client ====================

    @Test(expected = IllegalStateException.class)
    public void build_SansClient_LanceException() {
        new Commande.Builder()
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_AvecClientNull_LanceException() {
        new Commande.Builder()
                .setClient(null)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    // ==================== Tests validation lignes ====================

    @Test(expected = IllegalStateException.class)
    public void build_SansLignes_LanceException() {
        new Commande.Builder()
                .setClient(clientValide)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_AvecLignesNull_LanceException() {
        new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(null)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_AvecLignesVides_LanceException() {
        new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(new ArrayList<>())
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    // ==================== Tests validation utilisateur ====================

    @Test(expected = IllegalStateException.class)
    public void build_SansUtilisateur_LanceException() {
        new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_AvecUtilisateurNull_LanceException() {
        new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur(null)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_AvecUtilisateurVide_LanceException() {
        new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur("")
                .build();
    }

    // ==================== Tests getters ====================

    @Test
    public void getClient_RetourneClientCorrect() {
        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setUtilisateur(utilisateurValide)
                .build();

        assertEquals("Client Test", commande.getClient().getNom());
    }

    @Test
    public void getLignesCommande_RetourneListeCorrecte() {
        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setUtilisateur(utilisateurValide)
                .build();

        assertEquals(2, commande.getLignesCommande().size());
        assertEquals(produitA, commande.getLignesCommande().get(0).getProduit());
        assertEquals(produitB, commande.getLignesCommande().get(1).getProduit());
    }
}

