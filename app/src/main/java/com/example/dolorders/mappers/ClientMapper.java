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
        // Utilisation de chaînes vides ou texte par défaut si null ou vide
        String nom = dto.nom != null && !dto.nom.trim().isEmpty() ? dto.nom : "Nom inconnu";
        String adresse = dto.adresse != null && !dto.adresse.trim().isEmpty() ? dto.adresse : "Adresse non renseignée";
        String cp = dto.codePostal != null && !dto.codePostal.trim().isEmpty() ? dto.codePostal : "00000";
        String ville = dto.ville != null && !dto.ville.trim().isEmpty() ? dto.ville : "Ville inconnue";
        String tel = dto.numTel != null && !dto.numTel.trim().isEmpty() ? dto.numTel : "0000000000";
        String mail = dto.mail != null && !dto.mail.trim().isEmpty() ? dto.mail : "inconnu@email.com";

        // Conversion du timestamp en secondes vers un objet Date
        // Le constructeur de Date attend des millisecondes, donc on multiplie par 1000.
        Date dateSaisie = new Date(dto.dateSaisie * 1000L);

        // TODO Corriger ces valeurs par défaut si besoin
        // Valeurs par défaut pour les champs non fournis par l'API Dolibarr
        String utilisateur = "Système";
        String utilisateurEnvoie = "Système";
        Date dateEnvoie = new Date();
        Date dateMiseAJour = dateSaisie;

        // On utilise le Builder pour garantir la validité de l'objet créé.
        return new Client.Builder()
                .setId(dto.id)
                .setNom(nom)
                .setAdresse(adresse)
                .setCodePostal(cp)
                .setVille(ville)
                .setAdresseMail(mail)
                .setTelephone(tel)
                .setUtilisateur(utilisateur)
                .setDateSaisie(dateSaisie)
                .setUtilisateurEnvoie(utilisateurEnvoie)
                .setDateEnvoie(dateEnvoie)
                .setDateMiseAJour(dateMiseAJour)
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
