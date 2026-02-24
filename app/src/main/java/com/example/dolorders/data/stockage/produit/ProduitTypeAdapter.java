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

    /** Sérialise un objet Produit en JSON.
     * @param out Le JsonWriter pour écrire le JSON
     * @param produit L'objet Produit à sérialiser
     * @throws IOException Si une erreur d'écriture se produit
     */
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
        out.name("tauxTva").value(produit.getTauxTva());
        out.endObject();
    }

    /** Désérialise un objet Produit à partir de JSON.
     * @param in Le JsonReader pour lire le JSON
     * @return L'objet Produit désérialisé
     * @throws IOException Si une erreur de lecture se produit
     */
    @Override
    public Produit read(JsonReader in) throws IOException {
        String id = "0";
        String libelle = "";
        String description = "";
        double prixUnitaire = 0.0;
        double tauxTva = 20.0; // Valeur par défaut

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
                case "tauxTva":
                    tauxTva = in.nextDouble();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return new Produit(id, libelle, description, prixUnitaire, tauxTva);
    }
}
