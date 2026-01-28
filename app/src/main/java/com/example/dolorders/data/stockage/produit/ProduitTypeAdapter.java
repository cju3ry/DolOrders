package com.example.dolorders.data.stockage.produit;

import com.example.dolorders.objet.Produit;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * TypeAdapter Gson personnalisé pour la sérialisation/désérialisation de Produit.
 * Permet un contrôle précis de la façon dont les produits sont sauvegardés en JSON.
 */
public class ProduitTypeAdapter extends TypeAdapter<Produit> {

    @Override
    public void write(JsonWriter out, Produit produit) throws IOException {
        if (produit == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        out.name("id").value(produit.getId());
        out.name("libelle").value(produit.getLibelle());
        out.name("description").value(produit.getDescription());
        out.name("prixUnitaire").value(produit.getPrixUnitaire());
        out.endObject();
    }

    @Override
    public Produit read(JsonReader in) throws IOException {
        String id = "0";
        String libelle = "";
        String description = "";
        double prixUnitaire = 0.0;

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();

            switch (name) {
                case "id":
                    id = in.nextString();
                    break;
                case "libelle":
                    libelle = in.nextString();
                    break;
                case "description":
                    description = in.nextString();
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

        return new Produit(id, libelle, description, prixUnitaire);
    }
}

