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
 * Ces tests s'exécutent sur la JVM et valident la logique métier du modèle.
 */
public class CommandeTest {

    private Client clientValide;
    private List<LigneCommande> lignesValides;
    private Date dateValide;
    private String utilisateurValide;

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

        // Initialisation de la liste des lignes de commande
        lignesValides = new ArrayList<>();

        // Ajout d'une ligne : Produit A (10.0€) x 2 sans remise = 20.0€
        Produit produitA = new Produit("1", "Produit A","Description Produit A", 10.0);
        lignesValides.add(new LigneCommande(produitA, 2, 0.0,true));

        // Ajout d'une ligne : Produit B (5.0€) x 1 sans remise = 5.0€
        Produit produitB = new Produit("2", "Produit B", "Description Produit B",5.0);
        lignesValides.add(new LigneCommande(produitB, 1, 0.0,true));

        dateValide = new Date();
        utilisateurValide = "Admin";
    }

    @Test
    public void build_reussitAvecTousLesChampsValides() {
        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();

        assertNotNull(commande);
        assertEquals(clientValide, commande.getClient());
        // Vérification du total : 20.0 + 5.0 = 25.0
        assertEquals(25.0, commande.getMontantTotal(), 0.01);
    }

    @Test
    public void build_calculeCorrectementAvecRemiseParLigne() {
        // Création d'un cas de test spécifique pour vérifier le calcul de la remise par ligne
        List<LigneCommande> lignesAvecRemise = new ArrayList<>();

        // Produit à 100€, qté 1, remise 50% -> Doit faire 50€
        Produit produitCher = new Produit("3", "Produit Cher", "Desc Produit Cher", 100.0);
        lignesAvecRemise.add(new LigneCommande(produitCher, 1, 50.0,true));

        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesAvecRemise)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();

        assertEquals(50.0, commande.getMontantTotal(), 0.01);
    }


    @Test(expected = IllegalStateException.class)
    public void build_echoueSansClient() {
        new Commande.Builder()
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_echoueSansProduits() {
        new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(new ArrayList<>()) // Liste vide interdite
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_echoueSansUtilisateur() {
        new Commande.Builder()
                .setClient(clientValide)
                .setLignesCommande(lignesValides)
                .setDateCommande(dateValide)
                .build(); // Champ utilisateur manquant
    }
}