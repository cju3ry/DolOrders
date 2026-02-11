package com.example.dolorders.ui.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe pour gérer le rapport de synchronisation des clients et commandes vers Dolibarr.
 * Collecte les résultats d'envoi (succès et échecs) et génère un rapport détaillé.
 */
public class RapportSynchronisation {

    private final List<String> clientsReussis = new ArrayList<>();
    private final List<String> clientsEchoues = new ArrayList<>();
    private final List<String> commandesReussies = new ArrayList<>();
    private final List<String> commandesEchouees = new ArrayList<>();

    /**
     * Ajoute un client envoyé avec succès au rapport.
     *
     * @param nomClient Nom du client envoyé
     */
    public void ajouterClientReussi(String nomClient) {
        clientsReussis.add(nomClient);
    }

    /**
     * Ajoute un client qui a échoué au rapport.
     *
     * @param nomClient Nom du client
     * @param raison    Raison de l'échec
     */
    public void ajouterClientEchoue(String nomClient, String raison) {
        clientsEchoues.add(nomClient + " : " + raison);
    }

    /**
     * Ajoute une commande envoyée avec succès au rapport.
     *
     * @param idCommande ID de la commande envoyée
     */
    public void ajouterCommandeReussie(String idCommande) {
        commandesReussies.add(idCommande);
    }

    /**
     * Ajoute une commande qui a échoué au rapport.
     *
     * @param idCommande ID de la commande
     * @param raison     Raison de l'échec
     */
    public void ajouterCommandeEchouee(String idCommande, String raison) {
        commandesEchouees.add(idCommande + " : " + raison);
    }

    /**
     * Génère un rapport détaillé de la synchronisation avec des sections.
     *
     * @return Rapport formaté sous forme de String
     */
    public String genererRapportDetaille() {
        StringBuilder rapport = new StringBuilder();

        // Résumé global
        rapport.append("📊 RÉSUMÉ DE LA SYNCHRONISATION\n");
        rapport.append("═══════════════════════════════\n\n");

        // Clients
        rapport.append("👥 CLIENTS :\n");
        rapport.append("✅ Envoyés avec succès : ").append(clientsReussis.size()).append("\n");
        rapport.append("❌ Échecs : ").append(clientsEchoues.size()).append("\n\n");

        // Commandes
        rapport.append("📦 COMMANDES :\n");
        rapport.append("✅ Envoyées avec succès : ").append(commandesReussies.size()).append("\n");
        rapport.append("❌ Échecs : ").append(commandesEchouees.size()).append("\n\n");

        // Détails des échecs
        if (!clientsEchoues.isEmpty() || !commandesEchouees.isEmpty()) {
            rapport.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            rapport.append("📋 DÉTAILS DES ÉCHECS :\n\n");

            if (!clientsEchoues.isEmpty()) {
                rapport.append("❌ Clients non envoyés :\n");
                for (String erreur : clientsEchoues) {
                    rapport.append("  • ").append(erreur).append("\n");
                }
                rapport.append("\n");
            }

            if (!commandesEchouees.isEmpty()) {
                rapport.append("❌ Commandes non envoyées :\n");
                for (String erreur : commandesEchouees) {
                    rapport.append("  • ").append(erreur).append("\n");
                }
            }
        }

        return rapport.toString();
    }

    /**
     * Vérifie si toutes les opérations ont réussi (aucun échec).
     *
     * @return true si aucun échec, false sinon
     */
    public boolean aToutReussi() {
        return clientsEchoues.isEmpty() && commandesEchouees.isEmpty();
    }

    /**
     * Vérifie si des erreurs ont été enregistrées.
     *
     * @return true si au moins un échec existe, false sinon
     */
    public boolean aDesErreurs() {
        return !clientsEchoues.isEmpty() || !commandesEchouees.isEmpty();
    }

    /**
     * Retourne le nombre de clients envoyés avec succès.
     *
     * @return Nombre de clients réussis
     */
    public int getNombreClientsReussis() {
        return clientsReussis.size();
    }

    /**
     * Retourne le nombre de clients en échec.
     *
     * @return Nombre de clients échoués
     */
    public int getNombreClientsEchoues() {
        return clientsEchoues.size();
    }

    /**
     * Retourne le nombre de commandes envoyées avec succès.
     *
     * @return Nombre de commandes réussies
     */
    public int getNombreCommandesReussies() {
        return commandesReussies.size();
    }

    /**
     * Retourne le nombre de commandes en échec.
     *
     * @return Nombre de commandes échouées
     */
    public int getNombreCommandesEchouees() {
        return commandesEchouees.size();
    }
}

