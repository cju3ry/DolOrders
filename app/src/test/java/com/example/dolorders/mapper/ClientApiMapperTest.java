package com.example.dolorders.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.dolorders.data.dto.ClientApiReponseDto;
import com.example.dolorders.objet.Client;

import org.junit.Test;

/**
 * Tests unitaires pour le mapper ClientApiMapper.
 * Teste la conversion DTO -> Client sans dépendance API.
 */
public class ClientApiMapperTest {

    // ==================== Tests fromDto valides ====================

    @Test
    public void fromDto_AvecTousLesChamps_Reussit() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("123");
        dto.setName("Client Test");
        dto.setAddress("10 rue de Paris");
        dto.setZip("75001");
        dto.setTown("Paris");
        dto.setEmail("test@example.com");
        dto.setPhone("0123456789");

        Client client = ClientApiMapper.fromDto(dto);

        assertNotNull(client);
        assertEquals("123", client.getId());
        assertEquals("Client Test", client.getNom());
        assertEquals("10 rue de Paris", client.getAdresse());
        assertEquals("75001", client.getCodePostal());
        assertEquals("Paris", client.getVille());
        assertEquals("test@example.com", client.getAdresseMail());
        assertEquals("0123456789", client.getTelephone());
        assertTrue(client.isFromApi());
        assertEquals("API_DOLIBARR", client.getUtilisateur());
    }

    @Test
    public void fromDto_AvecDtoNull_RetourneNull() {
        Client client = ClientApiMapper.fromDto(null);

        assertNull(client);
    }

    // ==================== Tests valeurs par défaut ====================

    @Test
    public void fromDto_AvecNomNull_UtiliseValeurParDefaut() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName(null);

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("Client inconnu", client.getNom());
    }

    @Test
    public void fromDto_AvecAdresseVide_UtiliseValeurParDefaut() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setAddress("");

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("Adresse non renseignée", client.getAdresse());
    }

    @Test
    public void fromDto_AvecCodePostalNull_UtiliseValeurParDefaut() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setZip(null);

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("00000", client.getCodePostal());
    }

    @Test
    public void fromDto_AvecVilleNull_UtiliseValeurParDefaut() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setTown(null);

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("Ville non renseignée", client.getVille());
    }

    @Test
    public void fromDto_AvecEmailInvalide_UtiliseValeurParDefaut() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setEmail("email_invalide");

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("noemail@inconnu.com", client.getAdresseMail());
    }

    @Test
    public void fromDto_AvecTelephoneNull_UtiliseValeurParDefaut() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setPhone(null);

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("0000000000", client.getTelephone());
    }

    // ==================== Tests formatage téléphone ====================

    @Test
    public void fromDto_AvecTelephoneAvecEspaces_FormateCorrectement() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setPhone("01 23 45 67 89");

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("0123456789", client.getTelephone());
    }

    @Test
    public void fromDto_AvecTelephoneAvecTirets_FormateCorrectement() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setPhone("01-23-45-67-89");

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("0123456789", client.getTelephone());
    }

    @Test
    public void fromDto_AvecTelephonePrefixe33_FormateCorrectement() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setPhone("+33123456789");

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("0123456789", client.getTelephone());
    }

    @Test
    public void fromDto_AvecTelephoneTropLong_TronqueA10Chiffres() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setPhone("01234567890123");

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("0123456789", client.getTelephone());
    }

    @Test
    public void fromDto_AvecTelephoneTropCourt_UtiliseValeurParDefaut() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");
        dto.setPhone("0123");

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("0000000000", client.getTelephone());
    }

    // ==================== Tests champs complets vides ====================

    @Test
    public void fromDto_AvecDtoVide_UtiliseToutesValeursParDefaut() {
        ClientApiReponseDto dto = new ClientApiReponseDto();

        Client client = ClientApiMapper.fromDto(dto);

        assertNotNull(client);
        assertEquals("Client inconnu", client.getNom());
        assertEquals("Adresse non renseignée", client.getAdresse());
        assertEquals("00000", client.getCodePostal());
        assertEquals("Ville non renseignée", client.getVille());
        assertEquals("noemail@inconnu.com", client.getAdresseMail());
        assertEquals("0000000000", client.getTelephone());
        assertTrue(client.isFromApi());
    }

    // ==================== Tests flag fromApi ====================

    @Test
    public void fromDto_TousLesClients_SontMarquesFromApi() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");

        Client client = ClientApiMapper.fromDto(dto);

        assertTrue("Tous les clients du mapper doivent être marqués fromApi=true",
                client.isFromApi());
    }

    @Test
    public void fromDto_TousLesClients_OntUtilisateurApiDolibarr() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");

        Client client = ClientApiMapper.fromDto(dto);

        assertEquals("API_DOLIBARR", client.getUtilisateur());
    }

    // ==================== Tests date saisie ====================

    @Test
    public void fromDto_DateSaisie_EstRenseignee() {
        ClientApiReponseDto dto = new ClientApiReponseDto();
        dto.setId("1");
        dto.setName("Test");

        Client client = ClientApiMapper.fromDto(dto);

        assertNotNull("La date de saisie doit être renseignée", client.getDateSaisie());
    }
}

