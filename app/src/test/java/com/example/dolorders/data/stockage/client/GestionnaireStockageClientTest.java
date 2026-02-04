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
 * Tests unitaires simplifiés pour les composants de stockage.
 * Ces tests se concentrent sur la logique métier sans dépendances Android.
 * Note: Les tests de ClientStorageManager nécessitent un contexte Android et
 * doivent être effectués dans androidTest.
 */
public class GestionnaireStockageClientTest {

    private Gson gson;
    private Client clientTest1;
    private Client clientTest2;

    @Before
    public void setUp() {
        // Configuration de Gson avec l'adaptateur personnalisé
        gson = new GsonBuilder()
                .registerTypeAdapter(Client.class, new AdaptateurStockageClient())
                .create();

        // Création de clients de test
        Date maintenant = new Date();

        clientTest1 = new Client.Builder()
                .setNom("Dupont")
                .setAdresse("10 rue de la Paix")
                .setCodePostal("75002")
                .setVille("Paris")
                .setAdresseMail("dupont@example.com")
                .setTelephone("0123456789")
                .setUtilisateur("userTest")
                .setDateSaisie(maintenant)
                .build();

        clientTest2 = new Client.Builder()
                .setNom("Martin")
                .setAdresse("25 avenue des Champs")
                .setCodePostal("69001")
                .setVille("Lyon")
                .setAdresseMail("martin@example.com")
                .setTelephone("0987654321")
                .setUtilisateur("userTest")
                .setDateSaisie(maintenant)
                .build();
    }

    /**
     * Test : Vérifie que le client est bien créé avec toutes ses données
     */
    @Test
    public void creationClient_AvecToutesLesDonnees_Reussit() {
        assertNotNull("Le client ne devrait pas être null", clientTest1);
        assertEquals("Dupont", clientTest1.getNom());
        assertEquals("10 rue de la Paix", clientTest1.getAdresse());
        assertEquals("75002", clientTest1.getCodePostal());
        assertEquals("Paris", clientTest1.getVille());
        assertEquals("dupont@example.com", clientTest1.getAdresseMail());
        assertEquals("0123456789", clientTest1.getTelephone());
    }

    /**
     * Test : Sérialisation d'un client en JSON
     */
    @Test
    public void serialisation_ClientEnJson_Contient_ChampsPrincipaux() {
        String json = gson.toJson(clientTest1);

        assertNotNull("Le JSON ne devrait pas être null", json);
        assertFalse("Le JSON ne devrait pas être vide", json.isEmpty());
        assertTrue("Le JSON devrait contenir le nom", json.contains("Dupont"));
        assertTrue("Le JSON devrait contenir la ville", json.contains("Paris"));
    }

    /**
     * Test : Désérialisation d'un JSON simple en Client
     */
    @Test
    public void deserialisation_JsonSimpleEnClient_Reussit() {
        String json = "{" +
                "\"id\":\"123\"," +
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
        assertEquals("ID", "123", client.getId());
        assertEquals("Nom", "TestNom", client.getNom());
        assertEquals("Ville", "TestVille", client.getVille());
    }

    /**
     * Test : Cycle complet sérialisation/désérialisation
     */
    @Test
    public void cycleComplet_SerialisationDeserialisation_PreserveLesDonnees() {
        // Sérialisation
        String json = gson.toJson(clientTest1);

        // Désérialisation
        Client clientReconstitue = gson.fromJson(json, Client.class);

        // Vérifications
        assertNotNull("Le client reconstitué ne devrait pas être null", clientReconstitue);
        assertEquals("Nom", clientTest1.getNom(), clientReconstitue.getNom());
        assertEquals("Adresse", clientTest1.getAdresse(), clientReconstitue.getAdresse());
        assertEquals("Code postal", clientTest1.getCodePostal(), clientReconstitue.getCodePostal());
        assertEquals("Ville", clientTest1.getVille(), clientReconstitue.getVille());
        assertEquals("Email", clientTest1.getAdresseMail(), clientReconstitue.getAdresseMail());
        assertEquals("Téléphone", clientTest1.getTelephone(), clientReconstitue.getTelephone());
    }

    /**
     * Test : Sérialisation de plusieurs clients
     */
    @Test
    public void serialisation_PlusieursClients_Reussit() {
        String json1 = gson.toJson(clientTest1);
        String json2 = gson.toJson(clientTest2);

        assertNotNull("Le JSON du premier client ne devrait pas être null", json1);
        assertNotNull("Le JSON du deuxième client ne devrait pas être null", json2);
        assertTrue("Le premier JSON devrait contenir Dupont", json1.contains("Dupont"));
        assertTrue("Le deuxième JSON devrait contenir Martin", json2.contains("Martin"));
    }

    /**
     * Test : Désérialisation avec des champs manquants utilise valeurs par défaut
     */
    @Test
    public void deserialisation_AvecChampsManquants_UtiliseValeursParDefaut() {
        String json = "{" +
                "\"nom\":\"TestMinimal\"," +
                "\"adresse\":\"Adresse\"," +
                "\"codePostal\":\"12345\"," +
                "\"ville\":\"Ville\"," +
                "\"adresseMail\":\"test@test.com\"," +
                "\"telephone\":\"0123456789\"," +
                "\"utilisateur\":\"user\"," +
                "\"dateSaisie\":1000000000000" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertNotNull("Le client devrait être créé", client);
        assertEquals("TestMinimal", client.getNom());
    }

    /**
     * Test : Sérialisation avec null retourne "null"
     */
    @Test
    public void serialisation_ClientNull_RetourneNull() {
        String json = gson.toJson(null, Client.class);
        assertEquals("null", json);
    }

    /**
     * Test : Vérifie que les dates sont correctement sérialisées
     */
    @Test
    public void serialisation_AvecDates_ContientTimestamp() {
        Date dateSaisie = new Date(1000000000000L);

        Client clientAvecDate = new Client.Builder()
                .setNom("Test")
                .setAdresse("Adresse")
                .setCodePostal("12345")
                .setVille("Ville")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(dateSaisie)
                .build();

        String json = gson.toJson(clientAvecDate);

        assertTrue("Le JSON devrait contenir le timestamp", json.contains("1000000000000"));
    }

    /**
     * Test : Cycle complet avec toutes les dates
     */
    @Test
    public void cycleComplet_AvecToutesLesDates_PreserveLesTimestamps() {
        Date dateSaisie = new Date(1000000000000L);

        Client clientComplet = new Client.Builder()
                .setId("999")
                .setNom("ClientComplet")
                .setAdresse("Adresse")
                .setCodePostal("12345")
                .setVille("Ville")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("user")
                .setDateSaisie(dateSaisie)
                .build();

        String json = gson.toJson(clientComplet);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertEquals("Date saisie", dateSaisie.getTime(), clientReconstitue.getDateSaisie().getTime());
    }

    /**
     * Test : Désérialisation avec caractères spéciaux
     */
    @Test
    public void deserialisation_AvecCaracteresSpeciaux_Reussit() {
        String json = "{" +
                "\"nom\":\"Dupönt-André\"," +
                "\"adresse\":\"10 rue de l'Église\"," +
                "\"codePostal\":\"75002\"," +
                "\"ville\":\"Aix-en-Provence\"," +
                "\"adresseMail\":\"test@test.com\"," +
                "\"telephone\":\"0123456789\"," +
                "\"utilisateur\":\"user\"," +
                "\"dateSaisie\":1000000000000" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertEquals("Dupönt-André", client.getNom());
        assertEquals("10 rue de l'Église", client.getAdresse());
        assertEquals("Aix-en-Provence", client.getVille());
    }

    /**
     * Test : Vérifie que l'ID est préservé dans la sérialisation
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
     * Test : Vérifie que le champ fromApi est correctement sérialisé et désérialisé
     */
    @Test
    public void cycleComplet_AvecFromApi_PreserveLeFlag() {
        Client clientApi = new Client.Builder()
                .setId("API-123")
                .setNom("ClientAPI")
                .setAdresse("Adresse API")
                .setCodePostal("12345")
                .setVille("Ville API")
                .setAdresseMail("api@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("API_DOLIBARR")
                .setDateSaisie(new Date())
                .setFromApi(true)
                .buildFromApi();

        String json = gson.toJson(clientApi);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertTrue("Le flag fromApi devrait être true", clientReconstitue.isFromApi());
        assertEquals("ClientAPI", clientReconstitue.getNom());
    }

    /**
     * Test : Vérifie que les clients locaux ont fromApi à false par défaut
     */
    @Test
    public void cycleComplet_ClientLocal_FromApiFalse() {
        Client clientLocal = new Client.Builder()
                .setId("LOCAL-123")
                .setNom("ClientLocal")
                .setAdresse("Adresse locale")
                .setCodePostal("12345")
                .setVille("Ville locale")
                .setAdresseMail("local@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("userLocal")
                .setDateSaisie(new Date())
                .build();

        String json = gson.toJson(clientLocal);
        Client clientReconstitue = gson.fromJson(json, Client.class);

        assertFalse("Le flag fromApi devrait être false pour un client local", clientReconstitue.isFromApi());
        assertEquals("ClientLocal", clientReconstitue.getNom());
    }

    /**
     * Test : Vérifie que buildFromApi() utilise des valeurs par défaut pour les champs manquants
     */
    @Test
    public void deserialisation_ClientApiAvecChampsManquants_UtiliseValeursParDefaut() {
        String json = "{" +
                "\"id\":\"API-999\"," +
                "\"nom\":\"\"," +  // Nom vide
                "\"adresse\":null," +  // Adresse null
                "\"codePostal\":\"123\"," +  // Code postal invalide
                "\"ville\":null," +
                "\"adresseMail\":null," +
                "\"telephone\":\"123\"," +  // Téléphone invalide
                "\"utilisateur\":null," +
                "\"dateSaisie\":null," +
                "\"fromApi\":true" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertNotNull("Le client API devrait être créé malgré les champs manquants", client);
        assertTrue("Le flag fromApi devrait être true", client.isFromApi());
        assertEquals("Client inconnu", client.getNom());
        assertEquals("Adresse non renseignée", client.getAdresse());
        assertEquals("00000", client.getCodePostal());
        assertEquals("Ville non renseignée", client.getVille());
        assertEquals("noemail@inconnu.com", client.getAdresseMail());
        assertEquals("0000000000", client.getTelephone());
        assertEquals("API_DOLIBARR", client.getUtilisateur());
        assertNotNull("Date saisie devrait avoir une valeur par défaut", client.getDateSaisie());
    }

    /**
     * Test : Vérifie que les clients sans le champ fromApi sont considérés comme locaux
     */
    @Test
    public void deserialisation_SansChampFromApi_ConsidereCommeLocal() {
        String json = "{" +
                "\"id\":\"OLD-123\"," +
                "\"nom\":\"AncienClient\"," +
                "\"adresse\":\"Adresse\"," +
                "\"codePostal\":\"12345\"," +
                "\"ville\":\"Ville\"," +
                "\"adresseMail\":\"old@test.com\"," +
                "\"telephone\":\"0123456789\"," +
                "\"utilisateur\":\"oldUser\"," +
                "\"dateSaisie\":1000000000000" +
                "}";

        Client client = gson.fromJson(json, Client.class);

        assertNotNull("Le client devrait être créé", client);
        assertFalse("Le flag fromApi devrait être false par défaut", client.isFromApi());
        assertEquals("AncienClient", client.getNom());
    }
}
