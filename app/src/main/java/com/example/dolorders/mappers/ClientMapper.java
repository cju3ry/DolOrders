package com.example.dolorders.mappers;

import com.example.dolorders.Client;
import com.example.dolorders.data.dto.ClientApiReponseDto;
import com.example.dolorders.data.dto.ClientApiRequeteDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Mapper responsable de la conversion entre les DTOs (Data Transfer Objects)
 * et le modèle de domaine Client.
 */
public class ClientMapper {

    /**
     * Convertit un DTO de réponse API en un objet Client du domaine.
     * C'est ici qu'on transforme les données brutes en un objet métier valide.
     *
     * @param dto Le DTO reçu de l'API.
     * @return Un objet Client valide.
     */
    public Client getClient(ClientApiReponseDto dto) {
        if (dto == null) {
            return null;
        }

        // Conversion du timestamp en secondes vers un objet Date
        // Le constructeur de Date attend des millisecondes, donc on multiplie par 1000.
        Date dateSaisie = new Date(dto.dateSaisie * 1000L);

        // On utilise le Builder pour garantir la validité de l'objet créé.
        // Si le DTO contient des données invalides (ex: nom vide), le .build() lèvera une exception
        return new Client.Builder()
                .setId(dto.id)
                .setNom(dto.nom)
                .setAdresse(dto.adresse)
                .setCodePostal(dto.codePostal)
                .setVille(dto.ville)
                .setAdresseMail(dto.mail)
                .setTelephone(dto.numTel)
                .setDateSaisie(dateSaisie)
                .build();
    }

    /**
     * Convertit un objet Client du domaine en un DTO prêt à être envoyé à l'API.
     *
     * @param client L'objet Client de votre application.
     * @return Un DTO prêt à être sérialisé en JSON par Gson.
     */
    public ClientApiRequeteDto requeteDto(Client client) {
        if (client == null) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

        ClientApiRequeteDto dto = new ClientApiRequeteDto();
        dto.id = client.getId();
        dto.nom = client.getNom();
        dto.adresse = client.getAdresse();
        dto.codePostal = client.getCodePostal();
        dto.ville = client.getVille();
        dto.mail = client.getAdresseMail();
        dto.numTel = client.getTelephone();
        dto.dateSaisie = sdf.format(client.getDateSaisie());
        dto.utilisateur = client.getUtilisateur();
        dto.utilisateurEnvoie = client.getUtilisateurEnvoie();
        dto.dateEnvoie = sdf.format(client.getDateEnvoie());
        dto.dateMiseAJour = sdf.format(client.getDateMiseAJour());


        return dto;
    }
}
