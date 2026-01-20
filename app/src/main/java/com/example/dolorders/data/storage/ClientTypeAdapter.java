package com.example.dolorders.data.storage;

import com.example.dolorders.Client;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;

/**
 * Adaptateur Gson personnalisé pour la sérialisation/désérialisation de la classe Client.
 * Nécessaire car Client utilise un pattern Builder et n'a pas de constructeur public sans paramètres.
 */
public class ClientTypeAdapter extends TypeAdapter<Client> {

    @Override
    public void write(JsonWriter out, Client client) throws IOException {
        if (client == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        out.name("id").value(client.getId());
        out.name("nom").value(client.getNom());
        out.name("adresse").value(client.getAdresse());
        out.name("codePostal").value(client.getCodePostal());
        out.name("ville").value(client.getVille());
        out.name("adresseMail").value(client.getAdresseMail());
        out.name("telephone").value(client.getTelephone());
        out.name("utilisateur").value(client.getUtilisateur());
        out.name("dateSaisie").value(client.getDateSaisie() != null ? client.getDateSaisie().getTime() : 0);
        out.name("utilisateurEnvoie").value(client.getUtilisateurEnvoie());
        out.name("dateEnvoie").value(client.getDateEnvoie() != null ? client.getDateEnvoie().getTime() : 0);
        out.name("dateMiseAJour").value(client.getDateMiseAJour() != null ? client.getDateMiseAJour().getTime() : 0);
        out.endObject();
    }

    @Override
    public Client read(JsonReader in) throws IOException {
        if (in == null) {
            return null;
        }

        Client.Builder builder = new Client.Builder();

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();

            switch (name) {
                case "id":
                    builder.setId(in.nextString());
                    break;
                case "nom":
                    builder.setNom(in.nextString());
                    break;
                case "adresse":
                    builder.setAdresse(in.nextString());
                    break;
                case "codePostal":
                    builder.setCodePostal(in.nextString());
                    break;
                case "ville":
                    builder.setVille(in.nextString());
                    break;
                case "adresseMail":
                    builder.setAdresseMail(in.nextString());
                    break;
                case "telephone":
                    builder.setTelephone(in.nextString());
                    break;
                case "utilisateur":
                    builder.setUtilisateur(in.nextString());
                    break;
                case "dateSaisie":
                    long dateSaisieMs = in.nextLong();
                    builder.setDateSaisie(new Date(dateSaisieMs));
                    break;
                case "utilisateurEnvoie":
                    builder.setUtilisateurEnvoie(in.nextString());
                    break;
                case "dateEnvoie":
                    long dateEnvoieMs = in.nextLong();
                    builder.setDateEnvoie(new Date(dateEnvoieMs));
                    break;
                case "dateMiseAJour":
                    long dateMajMs = in.nextLong();
                    builder.setDateMiseAJour(new Date(dateMajMs));
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return builder.build();
    }
}

