package com.example.dolorders.objet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Tests unitaires pour la classe Client et son Builder.
 * Ces tests valident la logique métier de création et validation des clients.
 */
public class ClientTest {

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

    // ==================== Tests création client valide ====================

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
        assertFalse("Par défaut fromApi doit être false", client.isFromApi());
    }

    @Test
    public void creationClient_AvecId_Reussit() {
        Client client = builder.setId("123").build();

        assertEquals("123", client.getId());
    }

    @Test
    public void creationClient_AvecFromApiTrue_Reussit() {
        Client client = builder.setFromApi(true).build();

        assertTrue("fromApi doit être true", client.isFromApi());
    }

    @Test
    public void creationClient_EmailAvecSousDomaine_Reussit() {
        Client client = builder.setAdresseMail("test@sub.example.com").build();

        assertEquals("test@sub.example.com", client.getAdresseMail());
    }

    @Test
    public void creationClient_EmailAvecChiffres_Reussit() {
        Client client = builder.setAdresseMail("test123@example456.com").build();

        assertEquals("test123@example456.com", client.getAdresseMail());
    }

    // ==================== Tests validation nom ====================

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecNomNull_LanceException() {
        builder.setNom(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecNomVide_LanceException() {
        builder.setNom("").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecNomEspaces_LanceException() {
        builder.setNom("   ").build();
    }

    // ==================== Tests validation adresse ====================

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecAdresseNull_LanceException() {
        builder.setAdresse(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecAdresseVide_LanceException() {
        builder.setAdresse("").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecAdresseEspaces_LanceException() {
        builder.setAdresse("   ").build();
    }

    // ==================== Tests validation code postal ====================

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecCodePostalNull_LanceException() {
        builder.setCodePostal(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecCodePostalTropCourt_LanceException() {
        builder.setCodePostal("7500").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecCodePostalTropLong_LanceException() {
        builder.setCodePostal("750022").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecCodePostalNonNumerique_LanceException() {
        builder.setCodePostal("abcde").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecCodePostalMixte_LanceException() {
        builder.setCodePostal("75A02").build();
    }

    // ==================== Tests validation ville ====================

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecVilleNull_LanceException() {
        builder.setVille(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecVilleVide_LanceException() {
        builder.setVille("").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecVilleEspaces_LanceException() {
        builder.setVille("   ").build();
    }

    // ==================== Tests validation email ====================

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecEmailNull_LanceException() {
        builder.setAdresseMail(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecEmailVide_LanceException() {
        builder.setAdresseMail("").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecEmailSansArobase_LanceException() {
        builder.setAdresseMail("testexample.com").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecEmailSansDomaine_LanceException() {
        builder.setAdresseMail("test@").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecEmailSansExtension_LanceException() {
        builder.setAdresseMail("test@example").build();
    }

    // ==================== Tests validation téléphone ====================

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecTelephoneNull_LanceException() {
        builder.setTelephone(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecTelephoneTropCourt_LanceException() {
        builder.setTelephone("012345678").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecTelephoneTropLong_LanceException() {
        builder.setTelephone("01234567890").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecTelephoneNonNumerique_LanceException() {
        builder.setTelephone("abcdefghij").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecTelephoneAvecEspaces_LanceException() {
        builder.setTelephone("01 23 45 67 89").build();
    }

    // ==================== Tests validation utilisateur ====================

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecUtilisateurNull_LanceException() {
        builder.setUtilisateur(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecUtilisateurVide_LanceException() {
        builder.setUtilisateur("").build();
    }

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecUtilisateurEspaces_LanceException() {
        builder.setUtilisateur("   ").build();
    }

    // ==================== Tests validation date saisie ====================

    @Test(expected = IllegalStateException.class)
    public void creationClient_AvecDateSaisieNull_LanceException() {
        builder.setDateSaisie(null).build();
    }

    // ==================== Tests buildFromApi ====================

    @Test
    public void buildFromApi_AvecChampsComplets_Reussit() {
        Client client = builder.buildFromApi();

        assertNotNull(client);
        assertTrue("buildFromApi doit forcer fromApi à true", client.isFromApi());
    }

    @Test
    public void buildFromApi_AvecNomNull_UtiliseValeurParDefaut() {
        Client client = new Client.Builder()
                .setNom(null)
                .setAdresse("Adresse")
                .setCodePostal("75000")
                .setVille("Paris")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(maintenant)
                .buildFromApi();

        assertEquals("Client inconnu", client.getNom());
    }

    @Test
    public void buildFromApi_AvecAdresseVide_UtiliseValeurParDefaut() {
        Client client = new Client.Builder()
                .setNom("Test")
                .setAdresse("")
                .setCodePostal("75000")
                .setVille("Paris")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(maintenant)
                .buildFromApi();

        assertEquals("Adresse non renseignée", client.getAdresse());
    }

    @Test
    public void buildFromApi_AvecCodePostalInvalide_UtiliseValeurParDefaut() {
        Client client = new Client.Builder()
                .setNom("Test")
                .setAdresse("Adresse")
                .setCodePostal("123")
                .setVille("Paris")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(maintenant)
                .buildFromApi();

        assertEquals("00000", client.getCodePostal());
    }

    @Test
    public void buildFromApi_AvecVilleNull_UtiliseValeurParDefaut() {
        Client client = new Client.Builder()
                .setNom("Test")
                .setAdresse("Adresse")
                .setCodePostal("75000")
                .setVille(null)
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(maintenant)
                .buildFromApi();

        assertEquals("Ville non renseignée", client.getVille());
    }

    @Test
    public void buildFromApi_AvecEmailInvalide_UtiliseValeurParDefaut() {
        Client client = new Client.Builder()
                .setNom("Test")
                .setAdresse("Adresse")
                .setCodePostal("75000")
                .setVille("Paris")
                .setAdresseMail("email_invalide")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(maintenant)
                .buildFromApi();

        assertEquals("noemail@inconnu.com", client.getAdresseMail());
    }

    @Test
    public void buildFromApi_AvecTelephoneInvalide_UtiliseValeurParDefaut() {
        Client client = new Client.Builder()
                .setNom("Test")
                .setAdresse("Adresse")
                .setCodePostal("75000")
                .setVille("Paris")
                .setAdresseMail("test@test.com")
                .setTelephone("123")
                .setUtilisateur("user")
                .setDateSaisie(maintenant)
                .buildFromApi();

        assertEquals("0000000000", client.getTelephone());
    }

    @Test
    public void buildFromApi_AvecUtilisateurVide_UtiliseValeurParDefaut() {
        Client client = new Client.Builder()
                .setNom("Test")
                .setAdresse("Adresse")
                .setCodePostal("75000")
                .setVille("Paris")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("")
                .setDateSaisie(maintenant)
                .buildFromApi();

        assertEquals("API_DOLIBARR", client.getUtilisateur());
    }

    @Test
    public void buildFromApi_AvecDateNull_UtiliseDateActuelle() {
        Client client = new Client.Builder()
                .setNom("Test")
                .setAdresse("Adresse")
                .setCodePostal("75000")
                .setVille("Paris")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(null)
                .buildFromApi();

        assertNotNull("La date ne doit pas être null", client.getDateSaisie());
    }

    @Test
    public void buildFromApi_AvecToutNull_UtiliseValeursParDefaut() {
        Client client = new Client.Builder().buildFromApi();

        assertEquals("Client inconnu", client.getNom());
        assertEquals("Adresse non renseignée", client.getAdresse());
        assertEquals("00000", client.getCodePostal());
        assertEquals("Ville non renseignée", client.getVille());
        assertEquals("noemail@inconnu.com", client.getAdresseMail());
        assertEquals("0000000000", client.getTelephone());
        assertEquals("API_DOLIBARR", client.getUtilisateur());
        assertNotNull(client.getDateSaisie());
        assertTrue(client.isFromApi());
    }

    // ==================== Tests toString ====================

    @Test
    public void toString_RetourneNom() {
        Client client = builder.build();

        assertEquals("Dupont", client.toString());
    }
}
