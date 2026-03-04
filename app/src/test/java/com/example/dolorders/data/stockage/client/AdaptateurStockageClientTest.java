package com.example.dolorders.data.stockage.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.dolorders.objet.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Tests unitaires pour ClientTypeAdapter.
 * Vérifie que la sérialisation/désérialisation JSON fonctionne correctement.
 */
public class AdaptateurStockageClientTest {

    private Gson gson;
    private Client clientTest;
    private Date dateTest;

    @Before
    public void setUp() {
        // Configuration de Gson avec l'adaptateur personnalisé
        gson = new GsonBuilder()
                .registerTypeAdapter(Client.class, new AdaptateurStockageClient())
                .create();

        // Création d'un client de test avec une date fixe
        dateTest = new Date(1000000000000L); // Date fixe pour faciliter les tests

        clientTest = new Client.Builder()
                .setId("42")
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("dupont@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(dateTest)
                .build();
    }

    /**
     * Test : Sérialisation d'un client en JSON
     */
    @Test
    public void serialisation_ClientEnJson_Reussit() {
        String json = gson.toJson(clientTest);

        assertNotNull("Le JSON ne devrait pas être null", json);
        assertFalse("Le JSON ne devrait pas être vide", json.isEmpty());

        // Vérification que les champs principaux sont présents dans le JSON
        assertTrue("Le JSON devrait contenir le nom", json.contains("\"nom\":\"Dupont\""));
        assertTrue("Le JSON devrait contenir l'adresse", json.contains("\"adresse\":\"10 rue de la Paix\""));
        assertTrue("Le JSON devrait contenir le code postal", json.contains("\"codePostal\":\"75002\""));
        assertTrue("Le JSON devrait contenir la ville", json.contains("\"ville\":\"Paris\""));
        assertTrue("Le JSON devrait contenir l'email", json.contains("\"adresseMail\":\"dupont@example.com\""));
        assertTrue("Le JSON devrait contenir le téléphone", json.contains("\"telephone\":\"0123456789\""));
    }

    /**
     * Test : Désérialisation d'un JSON en Client
     */
    @Test
    public void deserialisation_JsonEnClient_Reussit() {
        // Création d'un JSON représentant un client
        String json = "{" +
                "\"id\":\"123\"," +
                "\"nom\":\"Martin\"," +
                "\"adresse\":\"25 avenue des Champs\"," +
                "\"codePostal\":\"69001\"," +
                "\"ville\":\"Lyon\"," +
                "\"adresseMail\":\"martin@example.com\"," +
                "\"telephone\":\"0987654321\"," +
                "\"utilisateur\":\"userTest\"," +
                "\"dateSaisie\":1000000000000" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertNotNull("Le client ne devrait pas être null", client);
        assertEquals("ID", "123", client.getId());
        assertEquals("Nom", "Martin", client.getNom());
        assertEquals("Adresse", "25 avenue des Champs", client.getAdresse());
        assertEquals("Code postal", "69001", client.getCodePostal());
        assertEquals("Ville", "Lyon", client.getVille());
        assertEquals("Email", "martin@example.com", client.getAdresseMail());
        assertEquals("Téléphone", "0987654321", client.getTelephone());
        assertEquals("Utilisateur", "userTest", client.getUtilisateur());
        assertNotNull("La date de saisie ne devrait pas être null", client.getDateSaisie());
    }

    /**
     * Test : Cycle complet sérialisation/désérialisation
     */
    @Test
    public void cycleComplet_SerialisationDeserialisation_PreserveLesDonnees() {
        // Sérialisation
        String json = gson.toJson(clientTest);

        // Désérialisation
        Client clientReconstitue = gson.fromJson(json, Client.class);

        // Vérifications
        assertNotNull("Le client reconstitué ne devrait pas être null", clientReconstitue);
        assertEquals("Nom", clientTest.getNom(), clientReconstitue.getNom());
        assertEquals("Adresse", clientTest.getAdresse(), clientReconstitue.getAdresse());
        assertEquals("Code postal", clientTest.getCodePostal(), clientReconstitue.getCodePostal());
        assertEquals("Ville", clientTest.getVille(), clientReconstitue.getVille());
        assertEquals("Email", clientTest.getAdresseMail(), clientReconstitue.getAdresseMail());
        assertEquals("Téléphone", clientTest.getTelephone(), clientReconstitue.getTelephone());
        assertEquals("Utilisateur", clientTest.getUtilisateur(), clientReconstitue.getUtilisateur());
        assertEquals("Date de saisie",
                clientTest.getDateSaisie().getTime(),
                clientReconstitue.getDateSaisie().getTime());
    }

    /**
     * Test : Sérialisation d'un client null
     */
    @Test
    public void serialisation_ClientNull_RetourneNull() {
        String json = gson.toJson(null, Client.class);

        assertEquals("Le JSON devrait être 'null'", "null", json);
    }

    /**
     * Test : Désérialisation avec des champs manquants
     */
    @Test
    public void deserialisation_AvecChampManquant_UtiliseValeursParDefaut() {
        // JSON minimal (certains champs manquent)
        String json = "{" +
                "\"nom\":\"TestNom\"," +
                "\"adresse\":\"TestAdresse\"," +
                "\"codePostal\":\"12345\"," +
                "\"ville\":\"TestVille\"," +
                "\"adresseMail\":\"test@test.com\"," +
                "\"telephone\":\"0123456789\"," +
                "\"utilisateur\":\"testUser\"," +
                "\"dateSaisie\":1000000000000" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertNotNull("Le client ne devrait pas être null", client);
        assertEquals("Nom", "TestNom", client.getNom());
    }

    /**
     * Test : Sérialisation avec toutes les dates
     */
    @Test
    public void serialisation_AvecToutesLesDates_Reussit() {
        Date dateSaisie = new Date(1000000000000L);

        Client clientAvecDates = new Client.Builder()
                .setNom("TestDates")
                .setAdresse("Adresse")
                .setCodePostal("12345")
                .setVille("Ville")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(dateSaisie)
                .build();

        String json = gson.toJson(clientAvecDates);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertEquals("Date saisie",
                dateSaisie.getTime(),
                clientReconstitue.getDateSaisie().getTime());
    }

    /**
     * Test : Désérialisation avec des champs inconnus (ne doit pas échouer)
     */
    @Test
    public void deserialisation_AvecChampsInconnus_IgnoreLesChamps() {
        String json = "{" +
                "\"nom\":\"Test\"," +
                "\"adresse\":\"Adresse\"," +
                "\"codePostal\":\"12345\"," +
                "\"ville\":\"Ville\"," +
                "\"adresseMail\":\"test@test.com\"," +
                "\"telephone\":\"0123456789\"," +
                "\"utilisateur\":\"user\"," +
                "\"dateSaisie\":1000000000000," +
                "\"champInconnu\":\"valeur\"," +
                "\"autreChampInconnu\":123" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertNotNull("Le client devrait être créé malgré les champs inconnus", client);
        assertEquals("Nom", "Test", client.getNom());
        assertEquals("Ville", "Ville", client.getVille());
    }

    /**
     * Test : Vérifie que l'ID est correctement sérialisé et désérialisé
     */
    @Test
    public void cycleComplet_AvecId_PreserveId() {
        Client clientAvecId = new Client.Builder()
                .setId("ID-12345")
                .setNom("TestID")
                .setAdresse("Adresse")
                .setCodePostal("12345")
                .setVille("Ville")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(new Date())
                .build();

        String json = gson.toJson(clientAvecId);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertEquals("L'ID devrait être préservé", "ID-12345", clientReconstitue.getId());
    }

    /**
     * Test : Vérifie la sérialisation de caractères spéciaux
     */
    @Test
    public void serialisation_AvecCaracteresSpeciaux_Reussit() {
        Client clientSpecial = new Client.Builder()
                .setNom("Dupönt-André")
                .setAdresse("10 rue de l'Église")
                .setCodePostal("75002")
                .setVille("Aix-en-Provence")
                .setAdresseMail("dupont.andre@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(new Date())
                .build();

        String json = gson.toJson(clientSpecial);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertEquals("Nom avec caractères spéciaux", "Dupönt-André", clientReconstitue.getNom());
        assertEquals("Ville avec tirets", "Aix-en-Provence", clientReconstitue.getVille());
        assertEquals("Adresse avec apostrophe", "10 rue de l'Église", clientReconstitue.getAdresse());
    }

    // ==================== Tests fromApi flag ====================

    /**
     * Test : Client local (fromApi = false) sérialisé et désérialisé correctement
     */
    @Test
    public void cycleComplet_ClientLocal_PreserveFlagFromApi() {
        Client clientLocal = new Client.Builder()
                .setNom("Client Local")
                .setAdresse("Adresse")
                .setCodePostal("12345")
                .setVille("Ville")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(new Date())
                .setFromApi(false)
                .build();

        String json = gson.toJson(clientLocal);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertFalse("Le client local doit garder fromApi=false", clientReconstitue.isFromApi());
    }

    /**
     * Test : Client API (fromApi = true) sérialisé et désérialisé correctement
     */
    @Test
    public void cycleComplet_ClientApi_PreserveFlagFromApi() {
        Client clientApi = new Client.Builder()
                .setNom("Client API")
                .setAdresse("Adresse")
                .setCodePostal("12345")
                .setVille("Ville")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("API_DOLIBARR")
                .setDateSaisie(new Date())
                .setFromApi(true)
                .build();

        String json = gson.toJson(clientApi);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertTrue("Le client API doit garder fromApi=true", clientReconstitue.isFromApi());
    }

    /**
     * Test : Désérialisation client API avec données partielles utilise buildFromApi()
     */
    @Test
    public void deserialisation_ClientApiAvecDonneesPartielles_UtiliseValeursParDefaut() {
        // JSON d'un client API avec des champs manquants
        String json = "{" +
                "\"id\":\"1\"," +
                "\"nom\":\"Client Test\"," +
                "\"fromApi\":true," +
                "\"dateSaisie\":1000000000000" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertNotNull("Le client ne devrait pas être null", client);
        assertTrue("fromApi doit être true", client.isFromApi());
        assertEquals("Nom doit être préservé", "Client Test", client.getNom());
        // buildFromApi() doit avoir mis des valeurs par défaut
        assertNotNull("L'adresse doit avoir une valeur par défaut", client.getAdresse());
    }

    /**
     * Test : Sérialisation inclut le flag fromApi
     */
    @Test
    public void serialisation_InclutFlagFromApi() {
        Client clientApi = new Client.Builder()
                .setNom("Test")
                .setAdresse("Adresse")
                .setCodePostal("12345")
                .setVille("Ville")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(new Date())
                .setFromApi(true)
                .build();

        String json = gson.toJson(clientApi);

        assertTrue("Le JSON doit contenir fromApi:true", json.contains("\"fromApi\":true"));
    }

    /**
     * Test : Désérialisation avec fromApi absent traite comme client local
     */
    @Test
    public void deserialisation_SansFlagFromApi_EstClientLocal() {
        String json = "{" +
                "\"nom\":\"Test\"," +
                "\"adresse\":\"Adresse\"," +
                "\"codePostal\":\"12345\"," +
                "\"ville\":\"Ville\"," +
                "\"adresseMail\":\"test@test.com\"," +
                "\"telephone\":\"0123456789\"," +
                "\"utilisateur\":\"user\"," +
                "\"dateSaisie\":1000000000000" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertFalse("Sans flag fromApi, le client doit être traité comme local", client.isFromApi());
    }

    // ==================== Tests cas limites ====================

    /**
     * Test : Sérialisation avec valeurs très longues
     */
    @Test
    public void serialisation_AvecValeursLongues_Reussit() {
        String nomLong = "A".repeat(200);
        String adresseLongue = "B".repeat(500);

        Client clientLong = new Client.Builder()
                .setNom(nomLong)
                .setAdresse(adresseLongue)
                .setCodePostal("12345")
                .setVille("Ville")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(new Date())
                .build();

        String json = gson.toJson(clientLong);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertEquals("Nom long préservé", nomLong, clientReconstitue.getNom());
        assertEquals("Adresse longue préservée", adresseLongue, clientReconstitue.getAdresse());
    }

    /**
     * Test : Désérialisation avec date à 0
     */
    @Test
    public void deserialisation_AvecDateZero_Reussit() {
        String json = "{" +
                "\"nom\":\"Test\"," +
                "\"adresse\":\"Adresse\"," +
                "\"codePostal\":\"12345\"," +
                "\"ville\":\"Ville\"," +
                "\"adresseMail\":\"test@test.com\"," +
                "\"telephone\":\"0123456789\"," +
                "\"utilisateur\":\"user\"," +
                "\"dateSaisie\":0" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertNotNull("Le client ne devrait pas être null", client);
        assertEquals("La date devrait être 0", 0, client.getDateSaisie().getTime());
    }
}

