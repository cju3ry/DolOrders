package com.example.dolorders.data.stockage.commande;

import com.example.dolorders.objet.Client;
import com.example.dolorders.objet.Commande;
import com.example.dolorders.objet.LigneCommande;
import com.example.dolorders.objet.Produit;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adaptateur Gson personnalisé pour la sérialisation/désérialisation de la classe Commande.
 * Nécessaire car Commande utilise un pattern Builder et contient des objets complexes
 * (Client, List<LigneCommande>).
 */
public class AdapteurStockageCommande extends TypeAdapter<Commande> {

    private static final String CLE_UTILISATEUR = "utilisateur";

    @Override
    public void write(JsonWriter out, Commande commande) throws IOException {
        if (commande == null) {
            out.nullValue();
            return;
        }

        out.beginObject();

        // Écriture des champs simples
        out.name("id").value(commande.getId());
        out.name("dateCommande").value(commande.getDateCommande() != null ? commande.getDateCommande().getTime() : 0);
        out.name("montantTotal").value(commande.getMontantTotal());
        out.name(CLE_UTILISATEUR).value(commande.getUtilisateur());

        // Écriture du client
        if (commande.getClient() != null) {
            out.name("client");
            writeClient(out, commande.getClient());
        }

        // Écriture des lignes de commande
        if (commande.getLignesCommande() != null && !commande.getLignesCommande().isEmpty()) {
            out.name("lignesCommande");
            out.beginArray();
            for (LigneCommande ligne : commande.getLignesCommande()) {
                writeLigneCommande(out, ligne);
            }
            out.endArray();
        }

        out.endObject();
    }

    private void writeClient(JsonWriter out, Client client) throws IOException {
        out.beginObject();
        out.name("id").value(client.getId());
        out.name("nom").value(client.getNom());
        out.name("adresse").value(client.getAdresse());
        out.name("codePostal").value(client.getCodePostal());
        out.name("ville").value(client.getVille());
        out.name("adresseMail").value(client.getAdresseMail());
        out.name("telephone").value(client.getTelephone());
        out.name(CLE_UTILISATEUR).value(client.getUtilisateur());
        out.name("dateSaisie").value(client.getDateSaisie() != null ? client.getDateSaisie().getTime() : 0);
        out.endObject();
    }

    private void writeLigneCommande(JsonWriter out, LigneCommande ligne) throws IOException {
        out.beginObject();

        // Écriture du produit
        if (ligne.getProduit() != null) {
            out.name("produit");
            writeProduit(out, ligne.getProduit());
        }

        out.name("quantite").value(ligne.getQuantite());
        out.name("remise").value(ligne.getRemise());
        out.name("montantLigne").value(ligne.getMontantLigne());

        out.endObject();
    }

    private void writeProduit(JsonWriter out, Produit produit) throws IOException {
        out.beginObject();
        out.name("id").value(produit.getId());
        out.name("libelle").value(produit.getLibelle());
        out.name("prixUnitaire").value(produit.getPrixUnitaire());
        out.endObject();
    }

    @Override
    public Commande read(JsonReader in) throws IOException {
        if (in == null) {
            return null;
        }

        Commande.Builder builder = new Commande.Builder();

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();

            switch (name) {
                case "id":
                    builder.setId(in.nextString());
                    break;
                case "dateCommande":
                    long dateMs = in.nextLong();
                    builder.setDateCommande(new Date(dateMs));
                    break;
                case "montantTotal":
                    // Le montant total est recalculé automatiquement
                    in.nextDouble();
                    break;
                case CLE_UTILISATEUR:
                    builder.setUtilisateur(in.nextString());
                    break;
                case "client":
                    Client client = readClient(in);
                    builder.setClient(client);
                    break;
                case "lignesCommande":
                    List<LigneCommande> lignes = readLignesCommande(in);
                    builder.setLignesCommande(lignes);
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return builder.build();
    }

    private Client readClient(JsonReader in) throws IOException {
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
                case CLE_UTILISATEUR:
                    builder.setUtilisateur(in.nextString());
                    break;
                case "dateSaisie":
                    long dateSaisieMs = in.nextLong();
                    builder.setDateSaisie(new Date(dateSaisieMs));
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return builder.build();
    }

    private List<LigneCommande> readLignesCommande(JsonReader in) throws IOException {
        List<LigneCommande> lignes = new ArrayList<>();

        in.beginArray();
        while (in.hasNext()) {
            lignes.add(readLigneCommande(in));
        }
        in.endArray();

        return lignes;
    }

    private LigneCommande readLigneCommande(JsonReader in) throws IOException {
        Produit produit = null;
        int quantite = 0;
        double remise = 0.0;

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();

            switch (name) {
                case "produit":
                    produit = readProduit(in);
                    break;
                case "quantite":
                    quantite = in.nextInt();
                    break;
                case "remise":
                    remise = in.nextDouble();
                    break;
                case "montantLigne":
                    // Le montant est recalculé automatiquement
                    in.nextDouble();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return new LigneCommande(produit, quantite, remise);
    }

    private Produit readProduit(JsonReader in) throws IOException {
        int id = 0;
        String libelle = "";
        double prixUnitaire = 0.0;

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();

            switch (name) {
                case "id":
                    id = in.nextInt();
                    break;
                case "libelle":
                    libelle = in.nextString();
                    break;
                case "prixUnitaire":
                    prixUnitaire = in.nextDouble();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return new Produit(id, libelle, prixUnitaire);
    }
}

