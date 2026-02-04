package com.example.dolorders.data.stockage.client;

import com.example.dolorders.objet.Client;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;

/**
 * Adaptateur Gson unifié pour la sérialisation/désérialisation de la classe Client.
 * Gère intelligemment les clients locaux et les clients provenant de l'API.
 * Nécessaire car Client utilise un pattern Builder et n'a pas de constructeur public sans paramètres.
 */
public class AdaptateurStockageClient extends TypeAdapter<Client> {

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
        out.name("fromApi").value(client.isFromApi());
        out.endObject();
    }

    @Override
    public Client read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        Client.Builder builder = new Client.Builder();
        boolean fromApi = false; // Par défaut, considéré comme client local

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();

            // Gérer les valeurs null
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                continue;
            }

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
                case "fromApi":
                    fromApi = in.nextBoolean();
                    builder.setFromApi(fromApi);
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        // Utiliser buildFromApi() pour les clients API (validation souple avec valeurs par défaut)
        // Utiliser build() pour les clients locaux (validation stricte)
        return fromApi ? builder.buildFromApi() : builder.build();
    }
}

