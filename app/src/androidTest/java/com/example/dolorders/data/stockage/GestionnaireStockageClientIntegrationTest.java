package com.example.dolorders.data.stockage;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.dolorders.data.stockage.client.GestionnaireStockageClient;
import com.example.dolorders.objet.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tests instrumentés pour ClientStorageManager.
 * Ces tests s'exécutent sur un appareil Android ou un émulateur et testent
 * l'enregistrement des clients dans le système de fichiers.
 * Les tests utilisent un fichier séparé (test_clients_data.json) pour ne pas
 * interférer avec les données existantes.
 */
@RunWith(AndroidJUnit4.class)
public class GestionnaireStockageClientIntegrationTest {

    private TestGestionnaireStockageClient storageManager;
    private Context context;
    private Client clientTest1;
    private Client clientTest2;
    private Client clientTest3;

    @Before
    public void setUp() {
        context = getApplicationContext();
        // Utilisation du TestClientStorageManager qui écrit dans un fichier de test
        storageManager = new TestGestionnaireStockageClient(context);

        // Nettoyage avant chaque test pour garantir un état propre
        storageManager.clearClients();

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

        clientTest3 = new Client.Builder()
                .setNom("Bernard")
                .setAdresse("5 place du Marché")
                .setCodePostal("31000")
                .setVille("Toulouse")
                .setAdresseMail("bernard@example.com")
                .setTelephone("0555555555")
                .setUtilisateur("userTest")
                .setDateSaisie(maintenant)
                .build();
    }

    @After
    public void tearDown() {
        // Nettoyage après chaque test
        if (storageManager != null) {
            storageManager.clearClients();
        }
    }

    // ==================== TESTS DE SAUVEGARDE ====================

    /**
     * Test : Sauvegarder une liste vide dans un fichier réel doit réussir
     */
    @Test
    public void saveClients_AvecListeVide_CreeUnFichierVide() {
        List<Client> clients = new ArrayList<>();
        boolean result = storageManager.saveClients(clients);

        assertTrue("La sauvegarde d'une liste vide devrait réussir", result);
        assertTrue("Le fichier devrait exister", storageManager.hasStoredClients());
    }

    /**
     * Test : Sauvegarder une liste null ne crée pas de fichier
     */
    @Test
    public void saveClients_AvecListeNull_NeCreeRien() {
        boolean result = storageManager.saveClients(null);

        assertFalse("La sauvegarde d'une liste null devrait échouer", result);
    }

    /**
     * Test : Sauvegarder un seul client crée un fichier avec ce client
     */
    @Test
    public void saveClients_AvecUnClient_CreeUnFichierAvecUnClient() {
        List<Client> clients = new ArrayList<>();
        clients.add(clientTest1);

        boolean saved = storageManager.saveClients(clients);

        assertTrue("La sauvegarde devrait réussir", saved);
        assertTrue("Le fichier devrait exister", storageManager.hasStoredClients());
        assertEquals("Le nombre de clients devrait être 1", 1, storageManager.getClientCount());
    }

    /**
     * Test : Sauvegarder plusieurs clients dans un fichier
     */
    @Test
    public void saveClients_AvecPlusieursClients_CreeUnFichierAvecTousLesClients() {
        List<Client> clients = new ArrayList<>();
        clients.add(clientTest1);
        clients.add(clientTest2);
        clients.add(clientTest3);

        boolean saved = storageManager.saveClients(clients);

        assertTrue("La sauvegarde devrait réussir", saved);
        assertEquals("Le nombre de clients devrait être 3", 3, storageManager.getClientCount());
    }

    // ==================== TESTS DE CHARGEMENT ====================

    /**
     * Test : Charge depuis un fichier inexistant retourne une liste vide
     */
    @Test
    public void loadClients_SansFichier_RetourneListeVide() {
        List<Client> clients = storageManager.loadClients();

        assertNotNull("La liste ne devrait pas être null", clients);
        assertTrue("La liste devrait être vide", clients.isEmpty());
        assertEquals("La taille devrait être 0", 0, clients.size());
    }

    /**
     * Test : Sauvegarde puis charger un client depuis le fichier réel
     */
    @Test
    public void saveEtLoad_AvecUnClient_RecupereLeClientDepuisFichier() {
        // Sauvegarde
        List<Client> clientsToSave = new ArrayList<>();
        clientsToSave.add(clientTest1);
        storageManager.saveClients(clientsToSave);

        // Chargement depuis le fichier
        List<Client> clientsLoaded = storageManager.loadClients();

        assertNotNull("La liste chargée ne devrait pas être null", clientsLoaded);
        assertEquals("Le nombre de clients devrait être 1", 1, clientsLoaded.size());

        Client clientCharge = clientsLoaded.get(0);
        assertEquals("Le nom devrait correspondre", "Dupont", clientCharge.getNom());
        assertEquals("L'adresse devrait correspondre", "10 rue de la Paix", clientCharge.getAdresse());
        assertEquals("Le code postal devrait correspondre", "75002", clientCharge.getCodePostal());
        assertEquals("La ville devrait correspondre", "Paris", clientCharge.getVille());
        assertEquals("L'email devrait correspondre", "dupont@example.com", clientCharge.getAdresseMail());
        assertEquals("Le téléphone devrait correspondre", "0123456789", clientCharge.getTelephone());
    }

    /**
     * Test : Sauvegarde puis charger plusieurs clients depuis le fichier réel
     */
    @Test
    public void saveEtLoad_AvecPlusieursClients_RecupereTousLesClients() {
        // Sauvegarde
        List<Client> clientsToSave = new ArrayList<>();
        clientsToSave.add(clientTest1);
        clientsToSave.add(clientTest2);
        clientsToSave.add(clientTest3);
        storageManager.saveClients(clientsToSave);

        // Chargement depuis le fichier
        List<Client> clientsLoaded = storageManager.loadClients();

        assertEquals("Le nombre de clients devrait être 3", 3, clientsLoaded.size());
        assertEquals("Dupont", clientsLoaded.get(0).getNom());
        assertEquals("Martin", clientsLoaded.get(1).getNom());
        assertEquals("Bernard", clientsLoaded.get(2).getNom());
    }

    // ==================== TESTS DE VÉRIFICATION ====================

    /**
     * Test : hasStoredClients retourne false quand aucun fichier n'existe
     */
    @Test
    public void hasStoredClients_SansFichier_RetourneFalse() {
        boolean result = storageManager.hasStoredClients();

        assertFalse("Devrait retourner false quand aucun fichier n'existe", result);
    }

    /**
     * Test : hasStoredClients retourne true après avoir créé un fichier
     */
    @Test
    public void hasStoredClients_ApresSauvegardeEnFichier_RetourneTrue() {
        List<Client> clients = new ArrayList<>();
        clients.add(clientTest1);
        storageManager.saveClients(clients);

        boolean result = storageManager.hasStoredClients();

        assertTrue("Devrait retourner true après une sauvegarde", result);
    }

    // ==================== TESTS DE SUPPRESSION ====================

    /**
     * Test : clearClients supprime le fichier du système
     */
    @Test
    public void clearClients_SupprimePhysiquementLeFichier() {
        // Création d'un fichier
        List<Client> clients = new ArrayList<>();
        clients.add(clientTest1);
        storageManager.saveClients(clients);
        assertTrue("Le fichier devrait exister", storageManager.hasStoredClients());

        // Suppression
        boolean result = storageManager.clearClients();

        assertTrue("La suppression devrait réussir", result);
        assertFalse("Le fichier ne devrait plus exister physiquement", storageManager.hasStoredClients());
    }

    /**
     * Test : clearClients sur un fichier inexistant retourne true
     */
    @Test
    public void clearClients_SansFichier_RetourneTrue() {
        boolean result = storageManager.clearClients();

        assertTrue("Devrait retourner true même sans fichier", result);
    }

    // ==================== TESTS D'AJOUT ====================

    /**
     * Test : addClient crée un fichier et ajoute le client
     */
    @Test
    public void addClient_SurFichierInexistant_CreeFichierAvecClient() {
        boolean result = storageManager.addClient(clientTest1);

        assertTrue("L'ajout devrait réussir", result);
        assertEquals("Il devrait y avoir 1 client dans le fichier", 1, storageManager.getClientCount());

        List<Client> clients = storageManager.loadClients();
        assertEquals("Dupont", clients.get(0).getNom());
    }

    /**
     * Test : addClient avec null ne modifie pas le fichier
     */
    @Test
    public void addClient_AvecNull_NeCreeRien() {
        boolean result = storageManager.addClient(null);

        assertFalse("L'ajout d'un client null devrait échouer", result);
        assertEquals("Il ne devrait y avoir aucun client", 0, storageManager.getClientCount());
        assertFalse("Aucun fichier ne devrait être créé", storageManager.hasStoredClients());
    }

    /**
     * Test : addClient ajoute à un fichier existant
     */
    @Test
    public void addClient_SurFichierExistant_AjouteAuFichier() {
        // Ajout du premier client
        storageManager.addClient(clientTest1);
        assertEquals("Il devrait y avoir 1 client", 1, storageManager.getClientCount());

        // Ajout du deuxième client
        boolean result = storageManager.addClient(clientTest2);

        assertTrue("L'ajout devrait réussir", result);
        assertEquals("Il devrait y avoir 2 clients dans le fichier", 2, storageManager.getClientCount());

        List<Client> clients = storageManager.loadClients();
        assertEquals("Dupont", clients.get(0).getNom());
        assertEquals("Martin", clients.get(1).getNom());
    }

    // ==================== TESTS DE COMPTAGE ====================

    /**
     * Test : getClientCount retourne 0 sans fichier
     */
    @Test
    public void getClientCount_SansFichier_Retourne0() {
        int count = storageManager.getClientCount();

        assertEquals("Le nombre devrait être 0", 0, count);
    }

    /**
     * Test : getClientCount lit le fichier et retourne le bon nombre
     */
    @Test
    public void getClientCount_AvecFichier_RetourneBonNombre() {
        storageManager.addClient(clientTest1);
        storageManager.addClient(clientTest2);
        storageManager.addClient(clientTest3);

        int count = storageManager.getClientCount();

        assertEquals("Le nombre devrait être 3", 3, count);
    }

    // ==================== TESTS D'ÉCRASEMENT ====================

    /**
     * Test : Une deuxième sauvegarde écrase le fichier existant
     */
    @Test
    public void saveClients_Ecrase_FichierExistant() {
        // Première sauvegarde
        List<Client> premiersClients = new ArrayList<>();
        premiersClients.add(clientTest1);
        premiersClients.add(clientTest2);
        storageManager.saveClients(premiersClients);
        assertEquals("Il devrait y avoir 2 clients", 2, storageManager.getClientCount());

        // Deuxième sauvegarde (écrasement du fichier)
        List<Client> nouveauxClients = new ArrayList<>();
        nouveauxClients.add(clientTest3);
        storageManager.saveClients(nouveauxClients);

        // Vérification
        List<Client> clients = storageManager.loadClients();
        assertEquals("Il ne devrait y avoir qu'1 client dans le fichier", 1, clients.size());
        assertEquals("Le client devrait être Bernard", "Bernard", clients.get(0).getNom());
    }

    // ==================== TESTS DE PERSISTANCE DES DONNÉES ====================

    /**
     * Test : Les dates sont préservées dans le fichier
     */
    @Test
    public void saveEtLoad_AvecDates_PreserveDatesEnFichier() {
        Date dateSaisie = new Date(1000000000000L);

        Client clientAvecDates = new Client.Builder()
                .setNom("ClientTest")
                .setAdresse("Test Address")
                .setCodePostal("12345")
                .setVille("TestVille")
                .setAdresseMail("test@test.com")
                .setTelephone("0123456789")
                .setUtilisateur("testUser")
                .setDateSaisie(dateSaisie)
                .build();

        // Sauvegarde dans le fichier
        storageManager.addClient(clientAvecDates);

        // Chargement depuis le fichier
        List<Client> clientsLoaded = storageManager.loadClients();

        assertNotNull("La date de saisie ne devrait pas être null", clientsLoaded.get(0).getDateSaisie());
        assertEquals("Les dates devraient être identiques",
                dateSaisie.getTime(),
                clientsLoaded.get(0).getDateSaisie().getTime());
    }

    /**
     * Test : Tous les champs d'un client sont préservés dans le fichier
     */
    @Test
    public void saveEtLoad_TousLesChamps_SontPreservesEnFichier() {
        Date dateSaisie = new Date(1000000000000L);

        Client clientComplet = new Client.Builder()
                .setId("123")
                .setNom("NomComplet")
                .setAdresse("Adresse Complète")
                .setCodePostal("99999")
                .setVille("VilleTest")
                .setAdresseMail("complet@test.com")
                .setTelephone("9876543210")
                .setUtilisateur("utilisateurTest")
                .setDateSaisie(dateSaisie)
                .build();

        // Sauvegarde dans le fichier
        storageManager.addClient(clientComplet);

        // Chargement depuis le fichier
        List<Client> clients = storageManager.loadClients();
        Client clientCharge = clients.get(0);

        // Vérifications complètes
        assertEquals("ID", "123", clientCharge.getId());
        assertEquals("Nom", "NomComplet", clientCharge.getNom());
        assertEquals("Adresse", "Adresse Complète", clientCharge.getAdresse());
        assertEquals("Code Postal", "99999", clientCharge.getCodePostal());
        assertEquals("Ville", "VilleTest", clientCharge.getVille());
        assertEquals("Email", "complet@test.com", clientCharge.getAdresseMail());
        assertEquals("Téléphone", "9876543210", clientCharge.getTelephone());
        assertEquals("Utilisateur", "utilisateurTest", clientCharge.getUtilisateur());
        assertEquals("Date saisie", dateSaisie.getTime(), clientCharge.getDateSaisie().getTime());
    }

    /**
     * Test : Persistance entre deux instances différentes
     */
    @Test
    public void persistance_EntreDeuxInstances_LeFichierEstPartage() {
        // Première instance : sauvegarde dans le fichier de test
        TestGestionnaireStockageClient manager1 = new TestGestionnaireStockageClient(context);
        manager1.addClient(clientTest1);

        // Deuxième instance : chargement depuis le même fichier de test
        TestGestionnaireStockageClient manager2 = new TestGestionnaireStockageClient(context);
        List<Client> clients = manager2.loadClients();

        assertEquals("Le client devrait être persistant dans le fichier", 1, clients.size());
        assertEquals("Le nom devrait correspondre", "Dupont", clients.get(0).getNom());

        // Nettoyage du fichier créé par ce test
        manager2.clearClients();
    }

    /**
     * Test : Sauvegarde de caractères spéciaux dans le fichier
     */
    @Test
    public void saveEtLoad_AvecCaracteresSpeciaux_PreserveCaracteresEnFichier() {
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

        // Sauvegarde dans le fichier
        storageManager.addClient(clientSpecial);

        // Chargement depuis le fichier
        List<Client> clients = storageManager.loadClients();
        Client clientCharge = clients.get(0);

        assertEquals("Nom avec caractères spéciaux", "Dupönt-André", clientCharge.getNom());
        assertEquals("Ville avec tirets", "Aix-en-Provence", clientCharge.getVille());
        assertEquals("Adresse avec apostrophe", "10 rue de l'Église", clientCharge.getAdresse());
    }

    /**
     * Test : Sauvegarde de nombreux clients (test de performance)
     */
    @Test
    public void saveEtLoad_Avec50Clients_GereLeFichierCorrectement() {
        List<Client> clients = new ArrayList<>();

        // Création de 50 clients
        for (int i = 1; i <= 50; i++) {
            Client client = new Client.Builder()
                    .setNom("Client" + i)
                    .setAdresse("Adresse " + i)
                    .setCodePostal(String.format("%05d", i))
                    .setVille("Ville" + i)
                    .setAdresseMail("client" + i + "@test.com")
                    .setTelephone(String.format("06%08d", i))
                    .setUtilisateur("user")
                    .setDateSaisie(new Date())
                    .build();
            clients.add(client);
        }

        // Sauvegarde dans le fichier
        boolean saved = storageManager.saveClients(clients);
        assertTrue("La sauvegarde de 50 clients devrait réussir", saved);

        // Chargement depuis le fichier
        List<Client> clientsLoaded = storageManager.loadClients();

        assertEquals("Le nombre de clients devrait être 50", 50, clientsLoaded.size());
        assertEquals("Client1", clientsLoaded.get(0).getNom());
        assertEquals("Client50", clientsLoaded.get(49).getNom());
    }

    /**
     * Test : Ajouts multiples successifs modifient le fichier à chaque fois
     */
    @Test
    public void addClient_MultiplesFois_ModifieFichierAChaqueFois() {
        // Vérification qu'aucun fichier n'existe
        assertFalse("Pas de fichier au départ", storageManager.hasStoredClients());

        // Premier ajout
        assertTrue(storageManager.addClient(clientTest1));
        assertEquals(1, storageManager.getClientCount());
        assertTrue("Le fichier doit exister", storageManager.hasStoredClients());

        // Deuxième ajout
        assertTrue(storageManager.addClient(clientTest2));
        assertEquals(2, storageManager.getClientCount());

        // Troisième ajout
        assertTrue(storageManager.addClient(clientTest3));
        assertEquals(3, storageManager.getClientCount());

        // Vérification de l'ordre dans le fichier
        List<Client> clients = storageManager.loadClients();
        assertEquals("Dupont", clients.get(0).getNom());
        assertEquals("Martin", clients.get(1).getNom());
        assertEquals("Bernard", clients.get(2).getNom());
    }

    /**
     * Test : Suppression puis ajout recrée un nouveau fichier
     */
    @Test
    public void clearPuisAdd_RecreeUnNouveauFichier() {
        // Ajout initial
        storageManager.addClient(clientTest1);
        assertEquals(1, storageManager.getClientCount());

        // Suppression du fichier
        storageManager.clearClients();
        assertEquals(0, storageManager.getClientCount());
        assertFalse("Le fichier ne devrait plus exister", storageManager.hasStoredClients());

        // Nouvel ajout (crée un nouveau fichier)
        storageManager.addClient(clientTest2);
        assertEquals(1, storageManager.getClientCount());
        assertTrue("Un nouveau fichier doit être créé", storageManager.hasStoredClients());

        // Vérification du contenu
        List<Client> clients = storageManager.loadClients();
        assertEquals("Martin", clients.get(0).getNom());
    }

    /**
     * Test : Vérifie que le fichier de test et le fichier de production sont séparés
     */
    @Test
    public void fichierTest_EstSepareDeFichierProduction() {
        // Ajout d'un client dans le fichier de TEST
        TestGestionnaireStockageClient testManager = new TestGestionnaireStockageClient(context);
        testManager.clearClients();
        testManager.addClient(clientTest1);
        assertEquals("Le fichier de test devrait contenir 1 client", 1, testManager.getClientCount());

        // Vérification que le fichier de PRODUCTION est vide
        GestionnaireStockageClient prodManager = new GestionnaireStockageClient(context);
        assertEquals("Le fichier de production devrait être vide", 0, prodManager.getClientCount());
        assertFalse("Le fichier de production ne devrait pas exister", prodManager.hasStoredClients());

        // Ajout d'un client dans le fichier de PRODUCTION
        prodManager.addClient(clientTest2);
        assertEquals("Le fichier de production devrait contenir 1 client", 1, prodManager.getClientCount());

        // Vérification que le fichier de TEST contient toujours 1 seul client
        assertEquals("Le fichier de test devrait toujours contenir 1 client", 1, testManager.getClientCount());

        // Nettoyage
        testManager.clearClients();
        prodManager.clearClients();
    }
}

