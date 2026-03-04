package com.example.dolorders.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.dolorders.data.dto.ProduitApiReponseDto;
import com.example.dolorders.objet.Produit;

import org.junit.Test;

/**
 * Tests unitaires pour le mapper ProduitMapper.
 * Teste la conversion DTO -> Produit sans dépendance API.
 */
public class ProduitMapperTest {

    // ==================== Tests fromDto valides ====================

    @Test
    public void fromDto_AvecTousLesChamps_Reussit() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("123");
        dto.setLabel("Produit Test");
        dto.setDescription("Description du produit");
        dto.setPrice("99.99");
        dto.setTvaTx("20.0000");

        Produit produit = ProduitMapper.fromDto(dto);

        assertNotNull(produit);
        assertEquals("123", produit.getId());
        assertEquals("Produit Test", produit.getLibelle());
        assertEquals("Description du produit", produit.getDescription());
        assertEquals(99.99, produit.getPrixUnitaire(), 0.01);
        assertEquals(20.0, produit.getTauxTva(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromDto_AvecDtoNull_LanceException() {
        ProduitMapper.fromDto(null);
    }

    // ==================== Tests valeurs par défaut ID ====================

    @Test
    public void fromDto_AvecIdNull_UtiliseIdParDefaut() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId(null);
        dto.setLabel("Test");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("0", produit.getId());
    }

    @Test
    public void fromDto_AvecIdVide_UtiliseIdParDefaut() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("");
        dto.setLabel("Test");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("0", produit.getId());
    }

    // ==================== Tests valeurs par défaut libellé ====================

    @Test
    public void fromDto_AvecLabelNull_UtiliseRef() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel(null);
        dto.setRef("REF123");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("REF123", produit.getLibelle());
    }

    @Test
    public void fromDto_AvecLabelVide_UtiliseRef() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("   ");
        dto.setRef("REF456");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("REF456", produit.getLibelle());
    }

    @Test
    public void fromDto_AvecLabelEtRefNull_UtiliseValeurParDefaut() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel(null);
        dto.setRef(null);

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("Produit sans nom", produit.getLibelle());
    }

    @Test
    public void fromDto_AvecLabelEtRefVides_UtiliseValeurParDefaut() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("");
        dto.setRef("  ");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("Produit sans nom", produit.getLibelle());
    }

    // ==================== Tests description ====================

    @Test
    public void fromDto_AvecDescriptionNull_UtiliseChaineVide() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setDescription(null);

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("", produit.getDescription());
    }

    @Test
    public void fromDto_AvecDescriptionAvecEspaces_Trimme() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setDescription("  Description avec espaces  ");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("Description avec espaces", produit.getDescription());
    }

    // ==================== Tests prix ====================

    @Test
    public void fromDto_AvecPrixNull_UtilisePrixZero() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setPrice(null);

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(0.0, produit.getPrixUnitaire(), 0.01);
    }

    @Test
    public void fromDto_AvecPrixVide_UtilisePrixZero() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setPrice("");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(0.0, produit.getPrixUnitaire(), 0.01);
    }

    @Test
    public void fromDto_AvecPrixInvalide_UtilisePrixZero() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setPrice("prix_invalide");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(0.0, produit.getPrixUnitaire(), 0.01);
    }

    @Test
    public void fromDto_AvecPrixNegatif_UtilisePrixZero() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setPrice("-50.00");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(0.0, produit.getPrixUnitaire(), 0.01);
    }

    @Test
    public void fromDto_AvecPrixDecimal_ParseCorrectement() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setPrice("1.69000000");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(1.69, produit.getPrixUnitaire(), 0.01);
    }

    @Test
    public void fromDto_AvecPrixEntier_ParseCorrectement() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setPrice("100");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(100.0, produit.getPrixUnitaire(), 0.01);
    }

    // ==================== Tests TVA ====================

    @Test
    public void fromDto_AvecTvaNull_UtiliseTvaParDefaut() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setTvaTx(null);

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(20.0, produit.getTauxTva(), 0.01);
    }

    @Test
    public void fromDto_AvecTvaVide_UtiliseTvaParDefaut() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setTvaTx("");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(20.0, produit.getTauxTva(), 0.01);
    }

    @Test
    public void fromDto_AvecTvaInvalide_UtiliseTvaParDefaut() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setTvaTx("tva_invalide");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(20.0, produit.getTauxTva(), 0.01);
    }

    @Test
    public void fromDto_AvecTvaNegatif_UtiliseTvaParDefaut() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setTvaTx("-5.0");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(20.0, produit.getTauxTva(), 0.01);
    }

    @Test
    public void fromDto_AvecTva5_5_ParseCorrectement() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setTvaTx("5.5000");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(5.5, produit.getTauxTva(), 0.01);
    }

    @Test
    public void fromDto_AvecTvaZero_ParseCorrectement() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("Test");
        dto.setTvaTx("0.0000");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals(0.0, produit.getTauxTva(), 0.01);
    }

    // ==================== Tests cas réels API Dolibarr ====================

    @Test
    public void fromDto_CasReelDolibarr_ParseCorrectement() {
        // Simulation d'une réponse réelle de l'API Dolibarr
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("9324");
        dto.setLabel("Contrex - 1.5L");
        dto.setDescription("Contrex - Origine: France - Format: 1.5L");
        dto.setRef("PROD5914");
        dto.setPrice("1.69000000");
        dto.setTvaTx("20.0000");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("9324", produit.getId());
        assertEquals("Contrex - 1.5L", produit.getLibelle());
        assertEquals("Contrex - Origine: France - Format: 1.5L", produit.getDescription());
        assertEquals(1.69, produit.getPrixUnitaire(), 0.01);
        assertEquals(20.0, produit.getTauxTva(), 0.01);
    }

    // ==================== Tests trimming ====================

    @Test
    public void fromDto_AvecLabelAvecEspaces_Trimme() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel("  Produit avec espaces  ");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("Produit avec espaces", produit.getLibelle());
    }

    @Test
    public void fromDto_AvecRefAvecEspaces_Trimme() {
        ProduitApiReponseDto dto = new ProduitApiReponseDto();
        dto.setId("1");
        dto.setLabel(null);
        dto.setRef("  REF123  ");

        Produit produit = ProduitMapper.fromDto(dto);

        assertEquals("REF123", produit.getLibelle());
    }
}

