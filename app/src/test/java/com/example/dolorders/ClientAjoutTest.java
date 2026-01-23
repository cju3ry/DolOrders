package com.example.dolorders;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class ClientAjoutTest {

    private Client.Builder builder;
    private Date maintenant;

    @Before
    public void initialisation() {
        maintenant = new Date();
        builder = new Client.Builder()
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("test@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(maintenant);
    }

    // Teste la création réussie d'un client avec des données valides.
    @Test
    public void creationClient_AvecDonneesValides_Reussit() {
        Client client = builder.build();

        assertNotNull("Le client ne devrait pas être null", client);
        assertEquals("Dupont", client.getNom());
        assertEquals("10 rue de la Paix", client.getAdresse());
        assertEquals("75002", client.getCodePostal());
        assertEquals("Paris", client.getVille());
        assertEquals("test@example.com", client.getAdresseMail());
        assertEquals("0123456789", client.getTelephone());
        assertEquals("userTest", client.getUtilisateur());
        assertEquals(maintenant, client.getDateSaisie());
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecNomNull() {
        builder.setNom(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecNomVide() {
        builder.setNom("   ").build(); // Nom avec seulement des espaces
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecCodePostalInvalide() {
        builder.setCodePostal("7502").build(); // 4 chiffres au lieu de 5
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecCodePostalNonNumerique() {
        builder.setCodePostal("abcde").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecEmailInvalide() {
        builder.setAdresseMail("test@example").build(); // Domaine de premier niveau manquant
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecEmailVide() {
        builder.setAdresseMail("").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecTelephoneInvalide() {
        builder.setTelephone("012345").build(); // Moins de 10 chiffres
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecTelephoneNonNumerique() {
        builder.setTelephone("abcdefghij").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecDateSaisieNulle() {
        builder.setDateSaisie(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecUtilisateurVide() {
        builder.setUtilisateur("").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecAdresseNulle() {
        builder.setAdresse(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecAdresseVide() {
        // Vérifie que la validation avec .trim() fonctionne
        builder.setAdresse("   ").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecVilleNulle() {
        builder.setVille(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecVilleVide() {
        builder.setVille(" ").build();
    }
}
