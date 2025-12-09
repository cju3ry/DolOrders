package com.example.dolorders.mappers;

import com.example.dolorders.Client;
import com.example.dolorders.Commande;
import com.example.dolorders.Produit;
import com.example.dolorders.data.dto.CommandeApiRequeteDto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Classe responsable de la conversion de l'objet Commande du domaine
 * vers une liste de DTOs prêts à être envoyés à l'API.
 */
public class CommandeMapper {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

    /**
     * Convertit UN objet Commande de l'application en une LISTE de DTOs,
     * où chaque DTO représente une ligne de commande pour l'API.
     *
     * @param commande L'objet Commande complet provenant de votre application.
     * @param nomEnvoi Le nom de l'utilisateur qui effectue l'envoi.
     * @param dateEnvoi La date de l'envoi.
     * @param dateMiseAJour La date de la mise à jour.
     * @return Une liste de DTOs, prête à être convertie en JSON par Gson.
     */
    public List<CommandeApiRequeteDto> requestDtoListe(Commande commande, String nomEnvoi, Date dateEnvoi, Date dateMiseAJour) {
        if (commande == null || commande.getProduitsEtQuantites() == null || commande.getProduitsEtQuantites().isEmpty()) {
            return new ArrayList<>();
        }

        List<CommandeApiRequeteDto> dtoList = new ArrayList<>();
        Client client = commande.getClient();

        // Pour chaque produit dans la commande de l'application, nous créons un DTO séparé.
        for (Map.Entry<Produit, Integer> entry : commande.getProduitsEtQuantites().entrySet()) {
            Produit produit = entry.getKey();
            Integer quantite = entry.getValue();

            CommandeApiRequeteDto dto = new CommandeApiRequeteDto();

            // Remplissage du DTO
            dto.idCommande = commande.getId();
            dto.idClient = client.getId();
            dto.nomClient = client.getNom();
            dto.dateCommande = sdf.format(commande.getDateCommande());

            dto.codeArticle = produit.getId();
            dto.labelProduit = produit.getNom();
            dto.quantite = quantite;
            dto.prixUnitaire = produit.getPrixUnitaire();

            dto.remise = commande.getRemise();
            dto.nomCreateur = commande.getUtilisateur();
            dto.dateCreation = sdf.format(commande.getDateCreation());

            dto.nomEnvoi = nomEnvoi;
            dto.dateEnvoi = sdf.format(dateEnvoi);
            dto.dateMiseAJour = sdf.format(dateMiseAJour);

            dtoList.add(dto);
        }
        return dtoList;
    }
}
