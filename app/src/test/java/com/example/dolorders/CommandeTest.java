package com.example.dolorders;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests unitaires pour la classe Commande et son Builder.
 * Ces tests s'exécutent sur la JVM et valident la logique métier du modèle.
 */
public class CommandeTest {

    private Client clientValide;
    private Map<Produit, Integer> produitsValides;
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

        produitsValides = new HashMap<>();
        produitsValides.put(new Produit(1, "Produit A", 10.0, "Cat A"), 2);
        produitsValides.put(new Produit(2, "Produit B", 5.0, "Cat B"), 1);

        dateValide = new Date();
        utilisateurValide = "Admin";
    }

    @Test
    public void build_reussitAvecTousLesChampsValides() {
        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setProduitsEtQuantites(produitsValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();

        assertNotNull(commande);
        assertEquals(clientValide, commande.getClient());
        assertEquals(25.0, commande.getMontantTotal(), 0.01);
    }

    @Test
    public void build_reussitAvecRemiseCalculeCorrectementLeTotal() {
        Commande commande = new Commande.Builder()
                .setClient(clientValide)
                .setProduitsEtQuantites(produitsValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .setRemise(10.0)
                .build();

        assertNotNull(commande);
        assertEquals(10.0, commande.getRemise(), 0.01);
        assertEquals(22.5, commande.getMontantTotal(), 0.01);
    }

    @Test(expected = IllegalStateException.class)
    public void build_echoueSansClient() {
        new Commande.Builder()
                .setProduitsEtQuantites(produitsValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_echoueSansProduits() {
        new Commande.Builder()
                .setClient(clientValide)
                .setProduitsEtQuantites(new HashMap<>()) // Panier vide
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_echoueSansUtilisateur() {
        new Commande.Builder()
                .setClient(clientValide)
                .setProduitsEtQuantites(produitsValides)
                .setDateCommande(dateValide)
                .build(); // Pas de setUtilisateur
    }

    @Test(expected = IllegalStateException.class)
    public void build_echoueAvecRemiseNegative() {
        new Commande.Builder()
                .setClient(clientValide)
                .setProduitsEtQuantites(produitsValides)
                .setDateCommande(dateValide)
                .setUtilisateur(utilisateurValide)
                .setRemise(-5.0) // Remise négative invalide
                .build();
    }
}
