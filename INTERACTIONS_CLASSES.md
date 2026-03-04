# 🔗 Documentation des Interactions entre Classes - DolOrders

<div align="center">

**Application Android de gestion de commandes pour Dolibarr ERP**

![Version](https://img.shields.io/badge/Version-3.0-blue)
![Date](https://img.shields.io/badge/Mise%20à%20jour-02%2F03%2F2026-green)
![Classes](https://img.shields.io/badge/Classes-44-orange)

</div>

---

## 📋 Table des Matières

1. [Vue d'Ensemble](#1-vue-densemble)
2. [Modèles de Domaine (objet)](#2-modèles-de-domaine-objet)
3. [Couche de Données (data)](#3-couche-de-données-data)
4. [Mappers](#4-mappers)
5. [Repositories](#5-repositories)
6. [Services Métier](#6-services-métier)
7. [Interface Utilisateur (ui)](#7-interface-utilisateur-ui)
8. [Flux de Données](#8-flux-de-données)
9. [Annexes](#9-annexes)

---

## 1. Vue d'Ensemble

### 1.1 Architecture Générale

L'application DolOrders suit une architecture **MVVM** (Model-View-ViewModel) avec une séparation claire des responsabilités :

```
┌─────────────────────────────────────────────────────────────────┐
│                         📱 UI LAYER                              │
│         Activities • Fragments • Adapteurs • ViewModels         │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                      💼 BUSINESS LAYER                           │
│              Services • Repositories • Mappers                   │
└───────────────────────────────┬─────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                        💾 DATA LAYER                             │
│         Stockage JSON Local • API Dolibarr (Volley)             │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Statistiques du Projet

| Package | Nombre de Classes | Rôle |
|---------|:-----------------:|------|
| `objet` | 6 | Modèles de domaine (avec Builders) |
| `data.dto` | 2 | Data Transfer Objects |
| `data.stockage` | 6 | Persistance JSON locale |
| `mapper` | 2 | Conversion DTO ↔ Modèle |
| `repository` | 3 | Communication API Dolibarr |
| `service` | 4 | Logique métier |
| `ui.activity` | 3 | Activités Android |
| `ui.fragment` | 9 | Écrans de l'application |
| `ui.viewModel` | 3 | Gestion d'état MVVM |
| `ui.adapteur` | 4 | Adaptateurs RecyclerView |
| `ui.util` | 2 | Classes utilitaires |
| **TOTAL** | **44** | |

---

## 2. Modèles de Domaine (objet)

Le package `objet` contient les entités métier principales de l'application. Ces classes sont **immuables** et utilisent le pattern **Builder** pour leur construction.

### 2.1 Client

**Fichier :** `objet/Client.java`

**Rôle :** Représente un client de l'application (tiers dans Dolibarr).

#### Attributs

| Attribut | Type | Description | Validation |
|----------|------|-------------|------------|
| `id` | String | Identifiant unique | Auto-généré |
| `nom` | String | Nom du client | Obligatoire, non vide |
| `adresse` | String | Adresse postale | Obligatoire |
| `codePostal` | String | Code postal | 5 chiffres |
| `ville` | String | Ville | Obligatoire |
| `adresseMail` | String | Email | Format valide |
| `telephone` | String | Téléphone | 10 chiffres |
| `utilisateur` | String | Créateur | Obligatoire |
| `dateSaisie` | Date | Date de création | Obligatoire |
| `fromApi` | boolean | Provient de l'API | Défaut: false |

#### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `getId()` | String | Retourne l'identifiant |
| `getNom()` | String | Retourne le nom |
| `getAdresse()` | String | Retourne l'adresse |
| `getCodePostal()` | String | Retourne le code postal |
| `getVille()` | String | Retourne la ville |
| `getAdresseMail()` | String | Retourne l'email |
| `getTelephone()` | String | Retourne le téléphone |
| `getUtilisateur()` | String | Retourne l'utilisateur créateur |
| `getDateSaisie()` | Date | Retourne la date de saisie |
| `isFromApi()` | boolean | Indique si le client vient de l'API |

#### Interactions

```
Client ◄── Client.Builder         : construit par
Client ◄── GestionnaireStockageClient : persisté par
Client ◄── ClientApiRepository    : envoyé/récupéré via
Client ◄── ClientApiMapper        : créé depuis DTO par
Client ──► Commande               : référencé par
Client ◄── ClientAdapteur         : affiché par
Client ◄── ClientsAttenteAdapteur : affiché par
Client ◄── ClientsFragmentViewModel : géré par
```

---

### 2.2 Client.Builder

**Fichier :** `objet/Client.java` (classe interne)

**Rôle :** Pattern Builder pour construire des instances de Client de manière fluide.

#### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `id` | String | Identifiant à définir |
| `nom` | String | Nom à définir |
| `adresse` | String | Adresse à définir |
| `codePostal` | String | Code postal à définir |
| `ville` | String | Ville à définir |
| `adresseMail` | String | Email à définir |
| `telephone` | String | Téléphone à définir |
| `utilisateur` | String | Utilisateur à définir |
| `dateSaisie` | Date | Date à définir |
| `fromApi` | boolean | Origine API à définir |

#### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `setId(String)` | Builder | Définit l'ID |
| `setNom(String)` | Builder | Définit le nom |
| `setAdresse(String)` | Builder | Définit l'adresse |
| `setCodePostal(String)` | Builder | Définit le code postal |
| `setVille(String)` | Builder | Définit la ville |
| `setAdresseMail(String)` | Builder | Définit l'email |
| `setTelephone(String)` | Builder | Définit le téléphone |
| `setUtilisateur(String)` | Builder | Définit l'utilisateur |
| `setDateSaisie(Date)` | Builder | Définit la date |
| `setFromApi(boolean)` | Builder | Définit l'origine |
| `build()` | Client | **Construit avec validation stricte** |
| `buildFromApi()` | Client | **Construit avec valeurs par défaut** |

#### Différence build() vs buildFromApi()

| Aspect | `build()` | `buildFromApi()` |
|--------|-----------|------------------|
| Validation | Stricte (exception si invalide) | Souple (valeurs par défaut) |
| Usage | Clients créés localement | Clients de l'API Dolibarr |
| `fromApi` | Non défini | Automatiquement `true` |

---

### 2.3 Commande

**Fichier :** `objet/Commande.java`

**Rôle :** Représente une commande client avec ses lignes de produits.

#### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `id` | String | Identifiant unique |
| `client` | Client | Client associé (obligatoire) |
| `dateCommande` | Date | Date de la commande |
| `lignesCommande` | List\<LigneCommande\> | Lignes (min. 1) |
| `montantTotal` | double | Calculé automatiquement |
| `utilisateur` | String | Utilisateur créateur |

#### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `getId()` | String | Retourne l'identifiant |
| `getClient()` | Client | Retourne le client |
| `getDateCommande()` | Date | Retourne la date |
| `getLignesCommande()` | List\<LigneCommande\> | Retourne les lignes |
| `getMontantTotal()` | double | Retourne le total calculé |
| `getUtilisateur()` | String | Retourne l'utilisateur |
| `calculerMontantTotal()` | double | Calcule la somme des lignes |

#### Interactions

```
Commande ◄── Commande.Builder           : construit par
Commande ──► Client                      : contient (1)
Commande ──► LigneCommande               : contient (1..*)
Commande ◄── GestionnaireStockageCommande : persisté par
Commande ◄── CommandeApiRepository       : envoyé via
Commande ◄── CommandesAttenteAdapteur    : affiché par
```

---

### 2.4 Commande.Builder

**Fichier :** `objet/Commande.java` (classe interne)

**Rôle :** Pattern Builder pour construire des instances de Commande.

#### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `setId(String)` | Builder | Définit l'ID |
| `setClient(Client)` | Builder | Définit le client |
| `setDateCommande(Date)` | Builder | Définit la date |
| `setLignesCommande(List)` | Builder | Définit les lignes |
| `setUtilisateur(String)` | Builder | Définit l'utilisateur |
| `build()` | Commande | Construit avec validation |

#### Validations à la construction

- Client obligatoire (non null)
- Au moins une ligne de commande
- Utilisateur obligatoire

---

### 2.5 LigneCommande

**Fichier :** `objet/LigneCommande.java`

**Rôle :** Représente une ligne de commande (produit + quantité + remise).

#### Attributs

| Attribut | Type | Description | Contraintes |
|----------|------|-------------|-------------|
| `produit` | Produit | Produit commandé | Non null |
| `quantite` | int | Quantité | > 0 |
| `remise` | double | Remise en % | 0-100 |
| `validee` | boolean | Ligne verrouillée | - |
| `dateCreation` | Date | Date de création | Auto si null |

#### Constructeurs

| Constructeur | Description |
|--------------|-------------|
| `LigneCommande(Produit, int, double, boolean)` | Avec date auto |
| `LigneCommande(Produit, int, double, boolean, Date)` | Avec date spécifiée |

#### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `getProduit()` | Produit | Retourne le produit |
| `getQuantite()` | int | Retourne la quantité |
| `getRemise()` | double | Retourne la remise (%) |
| `isValidee()` | boolean | Indique si verrouillée |
| `getDateCreation()` | Date | Retourne la date de création |
| `getMontantLigne()` | double | **Calcule : prix × qté × (1 - remise/100)** |

#### Interactions

```
LigneCommande ──► Produit  : référence (1)
LigneCommande ◄── Commande : appartient à
LigneCommande ◄── AdapteurStockageCommande : sérialisé par
```

---

### 2.6 Produit

**Fichier :** `objet/Produit.java`

**Rôle :** Représente un produit du catalogue Dolibarr.

#### Attributs

| Attribut | Type | Description | Contraintes |
|----------|------|-------------|-------------|
| `id` | String | Identifiant unique | - |
| `libelle` | String | Nom du produit | Non vide |
| `description` | String | Description | Peut être vide |
| `prixUnitaire` | double | Prix HT | >= 0 |
| `tauxTva` | double | Taux de TVA | >= 0 (défaut: 20) |

#### Constructeurs

| Constructeur | Description |
|--------------|-------------|
| `Produit(String, String, String, double, double)` | Complet avec TVA |
| `Produit(String, String, String, double)` | TVA = 20% par défaut |

#### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `getId()` | String | Retourne l'identifiant |
| `getLibelle()` | String | Retourne le nom |
| `getDescription()` | String | Retourne la description |
| `getPrixUnitaire()` | double | Retourne le prix HT |
| `getTauxTva()` | double | Retourne le taux de TVA |
| `equals(Object)` | boolean | Comparaison par ID |
| `hashCode()` | int | Hash basé sur ID |
| `toString()` | String | Retourne le libellé |

#### Interactions

```
Produit ◄── LigneCommande       : référencé par
Produit ◄── ProduitStorageManager : persisté par
Produit ◄── ProduitRepository    : récupéré via
Produit ◄── ProduitMapper        : créé depuis DTO par
Produit ◄── ProduitAdapter       : affiché par
```

---

## 3. Couche de Données (data)

### 3.1 DTOs (Data Transfer Objects)

Les DTOs représentent la structure exacte des réponses JSON de l'API Dolibarr.

#### 3.1.1 ClientApiReponseDto

**Fichier :** `data/dto/ClientApiReponseDto.java`

**Rôle :** DTO pour la réponse GET `/thirdparties`

| Champ JSON | Attribut Java | Type |
|------------|---------------|------|
| `id` | id | String |
| `name` | name | String |
| `phone` | phone | String |
| `email` | email | String |
| `address` | address | String |
| `zip` | zip | String |
| `town` | town | String |

**Méthodes :** Getters et Setters pour chaque attribut + constructeur par défaut.

---

#### 3.1.2 ProduitApiReponseDto

**Fichier :** `data/dto/ProduitApiReponseDto.java`

**Rôle :** DTO pour la réponse GET `/products`

| Champ JSON | Attribut Java | Type |
|------------|---------------|------|
| `id` | id | String |
| `label` | label | String |
| `description` | description | String |
| `price` | price | String |
| `tva_tx` | tvaTx | String |
| `ref` | ref | String |
| `status` | status | String |

**Méthodes :** Getters et Setters pour chaque attribut + constructeur par défaut.

---

### 3.2 Stockage Local

Le stockage local utilise des fichiers JSON dans le répertoire interne de l'application.

#### 3.2.1 GestionnaireStockageClient

**Fichier :** `data/stockage/client/GestionnaireStockageClient.java`

**Rôle :** Gestionnaire unifié de persistance des clients en JSON.

##### Constantes

| Constante | Valeur | Description |
|-----------|--------|-------------|
| `DEFAULT_FILE_NAME` | "clients_data.json" | Clients créés localement |
| `API_CLIENTS_FILE` | "clients_api_data.json" | Clients synchronisés depuis l'API |

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `context` | Context | Contexte Android |
| `gson` | Gson | Instance configurée avec AdaptateurStockageClient |
| `fileName` | String | Nom du fichier utilisé |

##### Constructeurs

| Constructeur | Description |
|--------------|-------------|
| `GestionnaireStockageClient(Context)` | Utilise DEFAULT_FILE_NAME |
| `GestionnaireStockageClient(Context, String)` | Fichier personnalisé |

##### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `saveClients(List<Client>)` | boolean | Sauvegarde complète (écrase) |
| `loadClients()` | List\<Client\> | Charge tous les clients |
| `addClient(Client)` | boolean | Ajoute un client |
| `deleteClient(Client)` | boolean | Supprime un client |
| `modifierClient(Client)` | boolean | Modifie un client existant |
| `clearClients()` | boolean | Supprime tous les clients |
| `hasStoredClients()` | boolean | Vérifie si fichier existe |
| `getClientCount()` | int | Compte les clients |
| `getFileName()` | String | Retourne le nom du fichier |

##### Interactions

```
GestionnaireStockageClient ──► Client : gère
GestionnaireStockageClient ──► AdaptateurStockageClient : utilise
GestionnaireStockageClient ◄── ServiceClient : utilisé par
GestionnaireStockageClient ◄── ClientsFragmentViewModel : utilisé par
GestionnaireStockageClient ◄── TableauClientsFragment : utilisé par
GestionnaireStockageClient ◄── ListeAttenteFragment : utilisé par
GestionnaireStockageClient ◄── CommandesFragment : utilisé par
GestionnaireStockageClient ◄── HomeFragment : utilisé par
```

---

#### 3.2.2 AdaptateurStockageClient

**Fichier :** `data/stockage/client/AdaptateurStockageClient.java`

**Rôle :** TypeAdapter Gson pour sérialiser/désérialiser les objets Client.

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `write(JsonWriter, Client)` | Sérialise un Client en JSON |
| `read(JsonReader)` | Désérialise un JSON en Client |

---

#### 3.2.3 GestionnaireStockageCommande

**Fichier :** `data/stockage/commande/GestionnaireStockageCommande.java`

**Rôle :** Gestionnaire de persistance des commandes en JSON.

##### Constantes

| Constante | Valeur |
|-----------|--------|
| `FILE_NAME` | "commandes_data.json" |

##### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `saveCommandes(List<Commande>)` | boolean | Sauvegarde complète |
| `loadCommandes()` | List\<Commande\> | Charge toutes les commandes |
| `addCommande(Commande)` | boolean | Ajoute une commande |
| `deleteCommande(Commande)` | boolean | Supprime une commande |
| `modifierCommande(Commande)` | boolean | Modifie une commande |
| `deleteCommandesByClient(String)` | boolean | **Supprime toutes les commandes d'un client** |
| `clearCommandes()` | boolean | Supprime toutes les commandes |
| `hasStoredCommandes()` | boolean | Vérifie si fichier existe |
| `getFileName()` | String | Retourne le nom du fichier |

##### Interactions

```
GestionnaireStockageCommande ──► Commande : gère
GestionnaireStockageCommande ──► AdapteurStockageCommande : utilise
GestionnaireStockageCommande ◄── ServiceClient : utilisé par (cascade)
GestionnaireStockageCommande ◄── CommandesFragment : utilisé par
GestionnaireStockageCommande ◄── TableauCommandesFragment : utilisé par
GestionnaireStockageCommande ◄── ListeAttenteFragment : utilisé par
GestionnaireStockageCommande ◄── HomeFragment : utilisé par
```

---

#### 3.2.4 AdapteurStockageCommande

**Fichier :** `data/stockage/commande/AdapteurStockageCommande.java`

**Rôle :** TypeAdapter Gson pour sérialiser/désérialiser les objets Commande.

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `write(JsonWriter, Commande)` | Sérialise une Commande en JSON |
| `read(JsonReader)` | Désérialise un JSON en Commande |

---

#### 3.2.5 ProduitStorageManager

**Fichier :** `data/stockage/produit/ProduitStorageManager.java`

**Rôle :** Cache local des produits synchronisés depuis Dolibarr.

##### Constantes

| Constante | Valeur |
|-----------|--------|
| `FILE_NAME` | "produits_data.json" |

##### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `saveProduits(List<Produit>)` | boolean | Sauvegarde le cache |
| `loadProduits()` | List\<Produit\> | Charge le cache |
| `getProduitById(String)` | Produit | Recherche par ID |
| `clearProduits()` | boolean | Vide le cache |
| `hasStoredProduits()` | boolean | Vérifie si cache existe |

##### Interactions

```
ProduitStorageManager ──► Produit : gère
ProduitStorageManager ──► ProduitTypeAdapter : utilise
ProduitStorageManager ◄── CommandesFragmentViewModel : utilisé par
ProduitStorageManager ◄── TableauCommandesFragment : utilisé par
```

---

#### 3.2.6 ProduitTypeAdapter

**Fichier :** `data/stockage/produit/ProduitTypeAdapter.java`

**Rôle :** TypeAdapter Gson pour sérialiser/désérialiser les objets Produit.

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `write(JsonWriter, Produit)` | Sérialise un Produit en JSON |
| `read(JsonReader)` | Désérialise un JSON en Produit |

---

## 4. Mappers

Les mappers convertissent les DTOs en objets métier.

### 4.1 ClientApiMapper

**Fichier :** `mapper/ClientApiMapper.java`

**Rôle :** Convertit `ClientApiReponseDto` → `Client`

##### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `fromDto(ClientApiReponseDto)` | Client | Conversion principale |
| `formatPhoneNumber(String)` | String | Formate le numéro (retire +33, espaces) |

##### Particularités

- Utilise `buildFromApi()` pour gérer les champs null
- Force `fromApi = true`
- Force `utilisateur = "API_DOLIBARR"`
- Formate le téléphone pour la validation

##### Interactions

```
ClientApiMapper ──► ClientApiReponseDto : convertit
ClientApiMapper ──► Client : produit
ClientApiMapper ◄── ClientApiRepository : utilisé par
```

---

### 4.2 ProduitMapper

**Fichier :** `mapper/ProduitMapper.java`

**Rôle :** Convertit `ProduitApiReponseDto` → `Produit`

##### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `fromDto(ProduitApiReponseDto)` | Produit | Conversion principale |

##### Particularités

- Gère la conversion prix String → double
- Utilise `label` ou `ref` comme libellé si l'un est vide
- TVA par défaut à 20% si invalide

##### Interactions

```
ProduitMapper ──► ProduitApiReponseDto : convertit
ProduitMapper ──► Produit : produit
ProduitMapper ◄── ProduitRepository : utilisé par
```

---

## 5. Repositories

Les repositories gèrent la communication avec l'API Dolibarr via **Volley**.

### 5.1 ClientApiRepository

**Fichier :** `repository/ClientApiRepository.java`

**Rôle :** Communication avec l'API Dolibarr pour les clients.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `context` | Context | Contexte Android |
| `requestQueue` | RequestQueue | File Volley |
| `gson` | Gson | Instance pour parsing |

##### Endpoints Utilisés

| Méthode HTTP | Endpoint | Description |
|--------------|----------|-------------|
| GET | `/thirdparties` | Récupération des clients |
| POST | `/thirdparties` | Création d'un client |
| POST | `/dolcustomersapi/clients` | Historique client |
| GET | `/users/login/{username}` | Récupération ID utilisateur |

##### Méthodes Publiques

| Méthode | Description |
|---------|-------------|
| `synchroniserDepuisApi(ClientCallback)` | Récupère tous les clients |
| `envoyerClient(Client, ClientEnvoiCallback)` | Envoie un client vers Dolibarr |
| `envoyerClientVersHistorique(Client, String, ClientHistoriqueCallback)` | Envoie vers module historique |
| `recupererIdUtilisateur(UserIdCallback)` | Récupère l'ID de l'utilisateur connecté |

##### Méthodes Privées

| Méthode | Description |
|---------|-------------|
| `envoyerClientAvecUserId(...)` | Envoi avec ID utilisateur |
| `envoyerVersHistorique(...)` | Envoi vers historique |
| `parseJsonResponse(JSONArray)` | Parse la réponse JSON |
| `creerJsonClient(Client, String)` | Crée le JSON pour POST |
| `getBaseUrl()` | Récupère l'URL depuis SharedPreferences cryptées |
| `getApiKey()` | Récupère la clé API cryptée |
| `getUsername()` | Récupère le nom d'utilisateur |

##### Interfaces de Callback

| Interface | Méthodes | Description |
|-----------|----------|-------------|
| `ClientCallback` | `onSuccess(List<Client>)`, `onError(String)` | Récupération |
| `ClientEnvoiCallback` | `onSuccess(String dolibarrId)`, `onError(String)` | Envoi natif |
| `ClientHistoriqueCallback` | `onSuccess(String)`, `onError(String)` | Envoi historique |
| `UserIdCallback` | `onSuccess(String userId)`, `onError(String)` | ID utilisateur |

##### Interactions

```
ClientApiRepository ──► Client : envoie/récupère
ClientApiRepository ──► ClientApiMapper : utilise
ClientApiRepository ──► ClientApiReponseDto : reçoit
ClientApiRepository ◄── ClientsFragmentViewModel : appelé par
ClientApiRepository ◄── ListeAttenteFragment : appelé par
```

---

### 5.2 CommandeApiRepository

**Fichier :** `repository/CommandeApiRepository.java`

**Rôle :** Communication avec l'API Dolibarr pour les commandes.

##### Endpoints Utilisés

| Méthode HTTP | Endpoint | Description |
|--------------|----------|-------------|
| POST | `/orders` | Création commande native |
| POST | `/dolordersapi/fournisseurss` | Historique (ligne par ligne) |

##### Méthodes Publiques

| Méthode | Description |
|---------|-------------|
| `envoyerCommandeVersModuleNatif(Commande, CommandeNativeEnvoiCallback)` | Envoie vers module natif Dolibarr |
| `envoyerCommandeVersHistoriqueAvecId(Commande, String, CommandeEnvoiCallback)` | Envoie vers historique avec ID |
| `envoyerCommandeVersHistoriqueSansId(Commande, CommandeEnvoiCallback)` | Envoie vers historique sans ID (échec natif) |

##### Méthodes Privées

| Méthode | Description |
|---------|-------------|
| `creerJsonCommandeNative(Commande)` | Crée le JSON pour /orders |
| `creerJsonLigneHistorique(...)` | Crée le JSON pour une ligne historique |
| `getBaseUrl()`, `getApiKey()`, `getUsername()` | Accès aux credentials |

##### Interfaces de Callback

| Interface | Méthodes | Description |
|-----------|----------|-------------|
| `CommandeNativeEnvoiCallback` | `onSuccess(String dolibarrId)`, `onError(String)` | Module natif |
| `CommandeEnvoiCallback` | `onSuccess(String)`, `onError(String)` | Historique |

##### Interactions

```
CommandeApiRepository ──► Commande : envoie
CommandeApiRepository ◄── ListeAttenteFragment : appelé par
```

---

### 5.3 ProduitRepository

**Fichier :** `repository/ProduitRepository.java`

**Rôle :** Récupération des produits depuis l'API Dolibarr.

##### Endpoints Utilisés

| Méthode HTTP | Endpoint | Description |
|--------------|----------|-------------|
| GET | `/products` | Récupération du catalogue |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `synchroniserDepuisApi(ProduitCallback)` | Récupère tous les produits |
| `parseJsonResponse(JSONArray)` | Parse la réponse JSON |
| `getBaseUrl()`, `getApiKey()` | Accès aux credentials |

##### Interface de Callback

| Interface | Méthodes | Description |
|-----------|----------|-------------|
| `ProduitCallback` | `onSuccess(List<Produit>)`, `onError(String)` | Récupération |

##### Interactions

```
ProduitRepository ──► Produit : récupère
ProduitRepository ──► ProduitMapper : utilise
ProduitRepository ──► ProduitApiReponseDto : reçoit
ProduitRepository ◄── CommandesFragmentViewModel : appelé par
```

---

## 6. Services Métier

### 6.1 ServiceClient

**Fichier :** `service/ServiceClient.java`

**Rôle :** Couche métier pour les opérations sur les clients.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `storageManager` | GestionnaireStockageClient | Stockage clients |
| `commandeStorageManager` | GestionnaireStockageCommande | Stockage commandes |

##### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `filter(String, String, String, String, String)` | List\<Client\> | Filtrage multicritères (nom, adresse, CP, ville, tél) |
| `deleteClient(Client)` | boolean | **Suppression en cascade (client + commandes associées)** |

##### Interactions

```
ServiceClient ──► GestionnaireStockageClient : utilise
ServiceClient ──► GestionnaireStockageCommande : utilise
ServiceClient ──► Client : manipule
ServiceClient ◄── TableauClientsFragment : utilisé par
ServiceClient ◄── ListeAttenteFragment : utilisé par
```

---

### 6.2 ServiceGestionSession

**Fichier :** `service/ServiceGestionSession.java`

**Rôle :** Gestion de l'authentification Dolibarr.

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `login(String url, String user, String password, ApiCallback)` | Authentification via API |
| `logout(AppCompatActivity)` | **Statique** - Déconnexion complète |
| `parseError(VolleyError)` | Convertit erreur Volley en message lisible |

##### Interface de Callback

| Interface | Méthodes |
|-----------|----------|
| `ApiCallback` | `onSuccess(JSONObject)`, `onError(String)` |

##### Particularités de `logout()`

1. Sauvegarde l'URL avant effacement
2. Efface les SharedPreferences cryptées
3. Restaure l'URL dans les préférences normales
4. Redirige vers LoginActivity
5. Affiche un Toast de confirmation

##### Interactions

```
ServiceGestionSession ◄── LoginActivity : utilisé par
ServiceGestionSession ◄── MainActivity : utilisé par (logout)
```

---

### 6.3 ServiceUrl

**Fichier :** `service/ServiceUrl.java`

**Rôle :** Gestion de l'historique des URLs Dolibarr utilisées.

##### Constantes

| Constante | Valeur |
|-----------|--------|
| `FILENAME` | "dolibarr_urls.json" |

##### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `addUrl(String)` | boolean | Ajoute une URL (en première position, max 10) |
| `getAllUrls()` | List\<String\> | Retourne toutes les URLs |
| `getMostRecentUrl()` | String | Retourne la dernière URL utilisée |
| `removeUrl(String)` | boolean | Supprime une URL |
| `clearAllUrls()` | boolean | Supprime toutes les URLs |
| `loadUrls()` | JSONArray | Charge depuis le fichier |
| `saveUrls(JSONArray)` | boolean | Sauvegarde dans le fichier |

##### Interactions

```
ServiceUrl ◄── LoginActivity : utilisé par (auto-complétion)
ServiceUrl ◄── MainActivity : utilisé par (sauvegarde)
```

---

### 6.4 ServiceConnexionInternet

**Fichier :** `service/ServiceConnexionInternet.java`

**Rôle :** Surveillance de la connexion Internet en temps réel.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `connectivityManager` | ConnectivityManager | Gestionnaire système |
| `networkCallback` | NetworkCallback | Callback de changement |
| `listener` | ConnectionStatusListener | Listener externe |
| `isConnected` | boolean | État actuel |

##### Méthodes

| Méthode | Retour | Description |
|---------|--------|-------------|
| `startMonitoring(ConnectionStatusListener)` | void | Démarre la surveillance |
| `stopMonitoring()` | void | Arrête la surveillance |
| `isInternetAvailable()` | boolean | Vérification ponctuelle |
| `isConnected()` | boolean | Retourne l'état actuel |

##### Interface

| Interface | Méthodes |
|-----------|----------|
| `ConnectionStatusListener` | `onConnectionStatusChanged(boolean)` |

##### Interactions

```
ServiceConnexionInternet ◄── MainActivity : utilisé par (indicateur visuel)
```

---

## 7. Interface Utilisateur (ui)

### 7.1 Activities

#### 7.1.1 LoginActivity

**Fichier :** `ui/activity/LoginActivity.java`

**Rôle :** Écran de connexion à Dolibarr.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `etUsername` | TextInputEditText | Champ utilisateur |
| `etPassword` | TextInputEditText | Champ mot de passe |
| `etUrl` | AutoCompleteTextView | Champ URL avec auto-complétion |
| `btnLogin` | Button | Bouton de connexion |
| `requestQueue` | RequestQueue | File Volley |
| `securePrefs` | SharedPreferences | Préférences cryptées |
| `serviceUrl` | ServiceUrl | Service pour URLs |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreate(Bundle)` | Initialisation de l'activité |
| `getUsername(Context)` | **Statique** - Récupère le username stocké |
| `setupUrlAutoComplete()` | Configure l'auto-complétion |
| `getEncryptedSharedPreferences()` | Initialise le stockage sécurisé |
| `validateInputs(...)` | Valide les champs |
| `login(...)` | Effectue la connexion |
| `validerSessionAvantReconnexion()` | Vérifie si la clé API est encore valide |
| `getLastUsedUrl()` | Récupère la dernière URL |

##### Interactions

```
LoginActivity ──► ServiceGestionSession : utilise
LoginActivity ──► ServiceUrl : utilise
```

---

#### 7.1.2 MainActivity

**Fichier :** `ui/activity/MainActivity.java`

**Rôle :** Activité principale avec navigation par onglets.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `serviceUrl` | ServiceUrl | Service pour URLs |
| `serviceConnexion` | ServiceConnexionInternet | Service de connectivité |
| `connectionIndicator` | View | Indicateur visuel (rond coloré) |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreate(Bundle)` | Initialisation avec BottomNavigationView |
| `onCreateOptionsMenu(Menu)` | Création du menu d'options |
| `onOptionsItemSelected(MenuItem)` | Gestion des clics menu |
| `setupConnectionMonitoring()` | Configure l'indicateur de connexion |
| `recupererUrl()` | Sauvegarde l'URL utilisée |
| `recupererUtilisateur()` | Affiche "Bienvenue {user}" |
| `getBaseUrl()` | Récupère l'URL depuis les prefs cryptées |

##### Interactions

```
MainActivity ──► ServiceGestionSession : utilise (logout)
MainActivity ──► ServiceUrl : utilise
MainActivity ──► ServiceConnexionInternet : utilise
MainActivity ──► HomeFragment, ClientsFragment, CommandesFragment, ListeAttenteFragment : héberge
```

---

#### 7.1.3 AboutActivity

**Fichier :** `ui/activity/AboutActivity.java`

**Rôle :** Affichage de la notice utilisateur en PDF.

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreate(Bundle)` | Initialisation |
| `afficherPdf()` | Charge et affiche le PDF |

---

### 7.2 Fragments

#### 7.2.1 HomeFragment

**Fichier :** `ui/fragment/HomeFragment.java`

**Rôle :** Page d'accueil avec statistiques et actions rapides.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `textClients` | TextView | Nombre de clients en attente |
| `textCommandes` | TextView | Nombre de commandes en attente |
| `textTotal` | TextView | Montant total en attente |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreateView(...)` | Création de la vue |
| `updateStats(int, int)` | Met à jour les statistiques |
| `convertirErreurEnMessageConvivial(String)` | Convertit erreur technique en message lisible |

##### Boutons d'action

| Bouton | Action |
|--------|--------|
| Nouveau client | Navigation vers ClientsAjoutFragment |
| Nouvelle commande | Navigation vers CommandesFragment |
| Synchroniser clients | Appel ClientsFragmentViewModel.synchroniserClientsDepuisApi() |
| Synchroniser produits | Appel CommandesFragmentViewModel.synchroniserProduitsDepuisApi() |
| Données en attente | Navigation vers ListeAttenteFragment |

##### Interactions

```
HomeFragment ──► ClientsFragmentViewModel : observe
HomeFragment ──► CommandesFragmentViewModel : observe
HomeFragment ──► GestionnaireStockageClient : utilise
HomeFragment ──► GestionnaireStockageCommande : utilise
HomeFragment ──► NavigationUtils : utilise
```

---

#### 7.2.2 ClientsFragment

**Fichier :** `ui/fragment/ClientsFragment.java`

**Rôle :** Liste et consultation des clients (locaux + API).

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `viewModel` | ClientsFragmentViewModel | ViewModel |
| `adapter` | ClientAdapteur | Adaptateur RecyclerView |
| `listeClientsOriginale` | List\<Client\> | Liste complète |
| `filtreNom`, `filtreAdresse`, `filtreCP`, `filtreVille`, `filtreTel` | String | Filtres actifs |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreateView(...)`, `onViewCreated(...)` | Lifecycle |
| `chargerClients()` | Charge tous les clients |
| `appliquerFiltres()` | Applique les filtres multicritères |
| `showFilterDialog()` | Affiche le dialogue de filtrage |

##### Interactions

```
ClientsFragment ──► ClientsFragmentViewModel : observe
ClientsFragment ──► ClientAdapteur : utilise
```

---

#### 7.2.3 ClientsAjoutFragment

**Fichier :** `ui/fragment/ClientsAjoutFragment.java`

**Rôle :** Formulaire de création d'un nouveau client.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `viewModel` | ClientsAjoutFragmentViewModel | ViewModel |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreateView(...)` | Création de la vue |
| `validerFormulaire()` | Validation des champs |
| `creerClient()` | Création et enregistrement du client |

##### Interactions

```
ClientsAjoutFragment ──► ClientsAjoutFragmentViewModel : observe
ClientsAjoutFragment ──► GestionnaireStockageClient : utilise
```

---

#### 7.2.4 ClientFormulaireFragment (DialogFragment)

**Fichier :** `ui/fragment/ClientFormulaireFragment.java`

**Rôle :** Formulaire de détails/modification d'un client (popup).

##### Constantes

| Constante | Valeur | Description |
|-----------|--------|-------------|
| `MODE_DETAILS` | 0 | Affichage lecture seule |
| `MODE_EDIT` | 1 | Modification |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `newInstance(int, Client)` | **Statique** - Crée une instance |
| `setOnClientEditedListener(...)` | Définit le callback de modification |
| `onCreateDialog(Bundle)` | Création du dialogue |
| `validerChamps()` | Validation des champs |

##### Interface

| Interface | Méthodes |
|-----------|----------|
| `OnClientEditedListener` | `onClientEdited(String nom, String adresse, ...)` |

---

#### 7.2.5 TableauClientsFragment

**Fichier :** `ui/fragment/TableauClientsFragment.java`

**Rôle :** Liste des clients en attente d'envoi (onglet dans ListeAttenteFragment).

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `adapter` | ClientsAttenteAdapteur | Adaptateur |
| `listeClients` | List\<Client\> | Clients locaux |
| `serviceClient` | ServiceClient | Service métier |
| `gestionnaireStockageClient` | GestionnaireStockageClient | Stockage |
| `dialog` | ClientFormulaireFragment | Popup de modification |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreateView(...)`, `onViewCreated(...)` | Lifecycle |
| `chargerClients()` | Charge les clients locaux |
| `supprimerClient(Client)` | Supprime avec confirmation |
| `modifierClient(Client)` | Ouvre le formulaire de modification |

##### Interactions

```
TableauClientsFragment ──► ClientsAttenteAdapteur : utilise
TableauClientsFragment ──► ServiceClient : utilise
TableauClientsFragment ──► GestionnaireStockageClient : utilise
TableauClientsFragment ──► ClientFormulaireFragment : affiche
```

---

#### 7.2.6 CommandesFragment

**Fichier :** `ui/fragment/CommandesFragment.java`

**Rôle :** Formulaire de création d'une nouvelle commande.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `viewModel` | CommandesFragmentViewModel | ViewModel |
| `clientStorageManager` | GestionnaireStockageClient | Clients locaux |
| `clientApiStorageManager` | GestionnaireStockageClient | Clients API |
| `commandeStorageManager` | GestionnaireStockageCommande | Commandes |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreateView(...)`, `onViewCreated(...)` | Lifecycle |
| `setupClientSpinner()` | Configure le sélecteur de client |
| `ajouterLigneVue(LigneCommande)` | Ajoute une ligne visuellement |
| `validerCommande()` | Valide et enregistre la commande |

##### Particularités

- Le client est **verrouillé** après sélection
- Chaque ligne doit être **validée** individuellement
- Le bouton affiche le nombre de lignes à valider

##### Interactions

```
CommandesFragment ──► CommandesFragmentViewModel : observe
CommandesFragment ──► GestionnaireStockageClient : utilise (x2)
CommandesFragment ──► GestionnaireStockageCommande : utilise
CommandesFragment ──► ProduitAdapter : utilise
```

---

#### 7.2.7 CommandeFormDialogFragment (DialogFragment)

**Fichier :** `ui/fragment/CommandeFormDialogFragment.java`

**Rôle :** Formulaire de modification d'une commande existante (popup).

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `commandeInitiale` | Commande | Commande à modifier |
| `lignesEditees` | List\<LigneCommande\> | Lignes modifiées |
| `tousLesProduits` | List\<Produit\> | Catalogue |
| `dateModifiee` | Date | Nouvelle date |
| `listener` | OnCommandeEditedListener | Callback |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `newInstance()` | **Statique** - Crée une instance |
| `setOnCommandeEditedListener(...)` | Définit le callback |
| `setCommandeInitiale(Commande)` | Définit la commande à modifier |
| `setListeProduits(List<Produit>)` | Définit le catalogue |
| `onCreateDialog(Bundle)` | Création du dialogue |
| `ajouterLigneVue(...)` | Ajoute une ligne visuellement |
| `updateTotaux()` | Met à jour les totaux |

##### Interface

| Interface | Méthodes |
|-----------|----------|
| `OnCommandeEditedListener` | `onCommandeEdited(Date, List<LigneCommande>)` |

##### Interactions

```
CommandeFormDialogFragment ──► CommandesFragmentViewModel : observe
CommandeFormDialogFragment ──► GestionnaireStockageCommande : utilise
CommandeFormDialogFragment ──► ProduitAdapter : utilise
```

---

#### 7.2.8 TableauCommandesFragment

**Fichier :** `ui/fragment/TableauCommandesFragment.java`

**Rôle :** Liste des commandes en attente d'envoi.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `adapter` | CommandesAttenteAdapteur | Adaptateur |
| `listeCommandes` | List\<Commande\> | Commandes |
| `gestionnaireStockageCommande` | GestionnaireStockageCommande | Stockage |
| `produitStorageManager` | ProduitStorageManager | Cache produits |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreateView(...)`, `onViewCreated(...)` | Lifecycle |
| `chargerCommandes()` | Charge les commandes |
| `modifierCommande(Commande)` | Ouvre le formulaire |
| `supprimerCommande(Commande)` | Supprime avec confirmation |

##### Interactions

```
TableauCommandesFragment ──► CommandesAttenteAdapteur : utilise
TableauCommandesFragment ──► GestionnaireStockageCommande : utilise
TableauCommandesFragment ──► ProduitStorageManager : utilise
TableauCommandesFragment ──► CommandeFormDialogFragment : affiche
```

---

#### 7.2.9 ListeAttenteFragment

**Fichier :** `ui/fragment/ListeAttenteFragment.java`

**Rôle :** Écran d'envoi des données vers Dolibarr avec onglets.

##### Sous-fragments (ViewPager2)

| Onglet | Fragment |
|--------|----------|
| CLIENTS | TableauClientsFragment |
| COMMANDES | TableauCommandesFragment |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreateView(...)`, `onViewCreated(...)` | Lifecycle |
| `envoyerToutVersDolibarr()` | Orchestration de l'envoi complet |
| `envoyerClientEtCommandes(...)` | Envoie un client puis ses commandes |
| `envoyerCommandesClient(...)` | Envoie les commandes d'un client |
| `verifierConnexion()` | Vérifie la connectivité |

##### Flux d'envoi

1. Vérification de la connexion Internet
2. Pour chaque client local :
   - Envoi vers module natif `/thirdparties`
   - Récupération de l'ID Dolibarr
   - Envoi vers module historique `/dolcustomersapi/clients`
3. Pour chaque commande du client :
   - Envoi vers module natif `/orders`
   - Récupération de l'ID commande
   - Envoi vers module historique `/dolordersapi/fournisseurss`
4. Si historique OK → suppression locale
5. Génération du rapport

##### Interactions

```
ListeAttenteFragment ──► ClientApiRepository : utilise
ListeAttenteFragment ──► CommandeApiRepository : utilise
ListeAttenteFragment ──► GestionnaireStockageClient : utilise
ListeAttenteFragment ──► GestionnaireStockageCommande : utilise
ListeAttenteFragment ──► ServiceClient : utilise
ListeAttenteFragment ──► RapportSynchronisation : génère
```

---

### 7.3 ViewModels

#### 7.3.1 ClientsFragmentViewModel

**Fichier :** `ui/viewModel/ClientsFragmentViewModel.java`

**Rôle :** Gestion de l'état des clients et de la synchronisation.

##### Attributs (LiveData)

| Attribut | Type | Description |
|----------|------|-------------|
| `clientCree` | MutableLiveData\<Client\> | Dernier client créé |
| `listeClients` | MutableLiveData\<List\<Client\>\> | Liste fusionnée |
| `erreurSynchronisation` | MutableLiveData\<String\> | Message d'erreur |
| `synchronisationReussie` | MutableLiveData\<Boolean\> | Succès sync |
| `nombreClientsSynchronises` | MutableLiveData\<Integer\> | Compteur |

##### Attributs (Repositories/Storage)

| Attribut | Type | Description |
|----------|------|-------------|
| `clientApiRepository` | ClientApiRepository | Repository API |
| `clientApiStorageManager` | GestionnaireStockageClient | Clients API |
| `clientStorageManager` | GestionnaireStockageClient | Clients locaux |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `getClientCree()` | Retourne LiveData du client créé |
| `publierClientCree(Client)` | Publie un nouveau client |
| `consommerClientCree()` | Consomme la notification |
| `getListeClients()` | Retourne LiveData de la liste |
| `getErreurSynchronisation()` | Retourne LiveData des erreurs |
| `getSynchronisationReussie()` | Retourne LiveData du succès |
| `getNombreClientsSynchronises()` | Retourne LiveData du compteur |
| `consommerErreur()` | Consomme l'erreur |
| `consommerSucces()` | Consomme le succès |
| `chargerTousLesClients(Context)` | **Charge locaux + API** |
| `synchroniserClientsDepuisApi(Context)` | **Appelle l'API et sauvegarde** |

##### Interactions

```
ClientsFragmentViewModel ──► ClientApiRepository : appelle
ClientsFragmentViewModel ──► GestionnaireStockageClient : utilise
ClientsFragmentViewModel ◄── HomeFragment : observé par
ClientsFragmentViewModel ◄── ClientsFragment : observé par
```

---

#### 7.3.2 CommandesFragmentViewModel

**Fichier :** `ui/viewModel/CommandesFragmentViewModel.java`

**Rôle :** Gestion de l'état du formulaire de commande et des produits.

##### Attributs (LiveData)

| Attribut | Type | Description |
|----------|------|-------------|
| `lignesCommande` | MutableLiveData\<List\<LigneCommande\>\> | Lignes en cours |
| `clientSelectionne` | MutableLiveData\<Client\> | Client choisi |
| `date` | MutableLiveData\<String\> | Date de commande |
| `listeClients` | MutableLiveData\<List\<Client\>\> | Liste clients |
| `listeProduits` | MutableLiveData\<List\<Produit\>\> | Catalogue |
| `fromAccueil` | MutableLiveData\<Boolean\> | Navigation depuis accueil |
| `fromListeClients` | MutableLiveData\<Boolean\> | Navigation depuis clients |
| `erreurSynchronisation` | MutableLiveData\<String\> | Erreur sync |
| `synchronisationReussie` | MutableLiveData\<Boolean\> | Succès sync |
| `nombreProduitsSynchronises` | MutableLiveData\<Integer\> | Compteur |
| `fragmentOrigine` | MutableLiveData\<String\> | Fragment d'origine |

##### Attributs (Repositories/Storage)

| Attribut | Type | Description |
|----------|------|-------------|
| `produitRepository` | ProduitRepository | Repository API |
| `produitStorageManager` | ProduitStorageManager | Cache produits |
| `clientStorageManager` | GestionnaireStockageClient | Clients locaux |
| `clientApiStorageManager` | GestionnaireStockageClient | Clients API |

##### Méthodes principales

| Méthode | Description |
|---------|-------------|
| `getLignesCommande()` | Retourne LiveData des lignes |
| `getClientSelectionne()` | Retourne LiveData du client |
| `getListeProduits()` | Retourne LiveData des produits |
| `setClientSelectionne(Client)` | Définit le client |
| `setDate(String)` | Définit la date |
| `addLigne(LigneCommande)` | Ajoute ou remplace une ligne |
| `removeLigne(LigneCommande)` | Supprime une ligne |
| `clear()` | Réinitialise le formulaire |
| `chargerProduits(Context)` | Charge depuis le cache |
| `synchroniserProduitsDepuisApi(Context)` | **Appelle l'API et sauvegarde** |
| `chargerTousLesClients(Context)` | Charge locaux + API |

##### Interactions

```
CommandesFragmentViewModel ──► ProduitRepository : appelle
CommandesFragmentViewModel ──► ProduitStorageManager : utilise
CommandesFragmentViewModel ──► GestionnaireStockageClient : utilise
CommandesFragmentViewModel ◄── HomeFragment : observé par
CommandesFragmentViewModel ◄── CommandesFragment : observé par
CommandesFragmentViewModel ◄── CommandeFormDialogFragment : observé par
```

---

#### 7.3.3 ClientsAjoutFragmentViewModel

**Fichier :** `ui/viewModel/ClientsAjoutFragmentViewModel.java`

**Rôle :** Gestion de l'état du formulaire d'ajout de client.

##### Attributs (LiveData)

| Attribut | Type |
|----------|------|
| `nom` | MutableLiveData\<String\> |
| `adresse` | MutableLiveData\<String\> |
| `codePostal` | MutableLiveData\<String\> |
| `ville` | MutableLiveData\<String\> |
| `email` | MutableLiveData\<String\> |
| `telephone` | MutableLiveData\<String\> |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `getNom()`, `getAdresse()`, `getCodePostal()`, `getVille()`, `getEmail()`, `getTelephone()` | Getters LiveData |
| `setNom(String)`, `setAdresse(String)`, `setCodePostal(String)`, `setVille(String)`, `setEmail(String)`, `setTelephone(String)` | Setters |
| `clear()` | **Réinitialise tous les champs à ""** |

##### Interactions

```
ClientsAjoutFragmentViewModel ◄── ClientsAjoutFragment : observé par
ClientsAjoutFragmentViewModel ◄── NavigationUtils : utilisé par (clear)
```

---

### 7.4 Adapteurs

#### 7.4.1 ClientAdapteur

**Fichier :** `ui/adapteur/ClientAdapteur.java`

**Rôle :** Adaptateur RecyclerView pour afficher les clients avec menu contextuel.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `clients` | List\<Client\> | Liste des clients |
| `listener` | OnClientActionListener | Callback d'actions |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `onCreateViewHolder(...)` | Crée le ViewHolder |
| `onBindViewHolder(...)` | Lie les données à la vue |
| `getItemCount()` | Retourne le nombre d'items |
| `showActionsMenu(View, Client)` | Affiche le menu popup |
| `handleMenuClick(MenuItem, Client)` | Gère le clic sur une action |

##### Interface

| Interface | Méthodes |
|-----------|----------|
| `OnClientActionListener` | `onDetails(Client)`, `onModifier(Client)`, `onNouvelleCommande(Client)` |

##### Particularités

- Masque "Modifier" pour les clients API (`isFromApi() == true`)

---

#### 7.4.2 ClientsAttenteAdapteur

**Fichier :** `ui/adapteur/ClientsAttenteAdapteur.java`

**Rôle :** Adaptateur pour les clients en attente d'envoi.

##### Interface

| Interface | Méthodes |
|-----------|----------|
| `OnClientActionListener` | `onEdit(Client)`, `onDelete(Client)` |

---

#### 7.4.3 CommandesAttenteAdapteur

**Fichier :** `ui/adapteur/CommandesAttenteAdapteur.java`

**Rôle :** Adaptateur pour les commandes en attente d'envoi.

##### Interface

| Interface | Méthodes |
|-----------|----------|
| `OnCommandeActionListener` | `onEdit(Commande)`, `onDelete(Commande)` |

---

#### 7.4.4 ProduitAdapter

**Fichier :** `ui/adapteur/ProduitAdapter.java`

**Rôle :** Adaptateur ArrayAdapter pour AutoCompleteTextView avec filtrage.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `produitsComplet` | List\<Produit\> | Liste originale |
| `produitsFiltres` | List\<Produit\> | Liste filtrée |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `getCount()` | Nombre d'items filtrés |
| `getItem(int)` | Retourne le produit à la position |
| `getView(...)` | Affiche nom + prix + description |
| `getFilter()` | Retourne le filtre personnalisé |
| `resetFilter()` | Réinitialise le filtre |

##### Particularités

- Affiche : `{libellé} - {prix}€` + description en dessous
- Filtre sur libellé ET description

---

### 7.5 Utilitaires

#### 7.5.1 RapportSynchronisation

**Fichier :** `ui/util/RapportSynchronisation.java`

**Rôle :** Génération du rapport d'envoi vers Dolibarr.

##### Attributs

| Attribut | Type | Description |
|----------|------|-------------|
| `clientsReussis` | List\<String\> | Noms des clients envoyés |
| `clientsEchoues` | List\<String\> | Détails des échecs clients |
| `commandesReussies` | List\<String\> | IDs des commandes envoyées |
| `commandesEchouees` | List\<String\> | Détails des échecs commandes |

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `ajouterClientReussi(String)` | Ajoute un succès client |
| `ajouterClientEchoue(String, String)` | Ajoute un échec client avec raison |
| `ajouterCommandeReussie(String)` | Ajoute un succès commande |
| `ajouterCommandeEchouee(String, String)` | Ajoute un échec commande avec raison |
| `genererRapportDetaille()` | Génère le rapport complet formaté |
| `genererRapportSimple()` | Génère un résumé court |
| `hasErrors()` | Indique s'il y a des erreurs |
| `getNbClientsReussis()` | Compteur succès clients |
| `getNbClientsEchoues()` | Compteur échecs clients |
| `getNbCommandesReussies()` | Compteur succès commandes |
| `getNbCommandesEchouees()` | Compteur échecs commandes |

---

#### 7.5.2 NavigationUtils

**Fichier :** `ui/util/NavigationUtils.java`

**Rôle :** Utilitaire de navigation avec nettoyage du ViewModel.

##### Méthodes

| Méthode | Description |
|---------|-------------|
| `navigateToClientAjout(Fragment)` | **Statique** - Navigue vers ClientsAjoutFragment en vidant le ViewModel |

##### Interactions

```
NavigationUtils ──► ClientsAjoutFragmentViewModel : utilise (clear)
NavigationUtils ──► ClientsAjoutFragment : navigue vers
NavigationUtils ◄── HomeFragment : utilisé par
```

---

## 8. Flux de Données

### 8.1 Synchronisation des Clients depuis Dolibarr

```
┌──────────────┐     ┌───────────────────────┐     ┌───────────────────┐
│ HomeFragment │────►│ ClientsFragmentViewModel │────►│ ClientApiRepository │
│ (clic sync)  │     │ synchroniserClients()   │     │ synchroniserDepuisApi() │
└──────────────┘     └───────────────────────┘     └─────────┬─────────┘
                                                              │
                     ┌────────────────────────────────────────┘
                     │
                     ▼
              ┌─────────────┐     ┌─────────────────┐     ┌─────────────┐
              │ GET /api/   │────►│ ClientApiReponse │────►│ ClientApi   │
              │ thirdparties │     │ Dto (JSON)       │     │ Mapper      │
              └─────────────┘     └─────────────────┘     └──────┬──────┘
                                                                  │
                     ┌────────────────────────────────────────────┘
                     │
                     ▼
              ┌─────────────────────────┐     ┌──────────────────────┐
              │ List<Client>            │────►│ GestionnaireStockage │
              │ (fromApi = true)        │     │ Client.saveClients() │
              └─────────────────────────┘     └──────────┬───────────┘
                                                          │
                     ┌────────────────────────────────────┘
                     │
                     ▼
              ┌─────────────────────────┐     ┌──────────────────────┐
              │ clients_api_data.json   │     │ LiveData notification │
              │ (fichier local)         │     │ → Toast succès        │
              └─────────────────────────┘     └──────────────────────┘
```

### 8.2 Envoi d'un Client vers Dolibarr

```
┌────────────────────┐     ┌─────────────────────┐
│ ListeAttenteFragment │────►│ GestionnaireStockage │
│ (clic Envoyer)      │     │ Client.loadClients() │
└────────────────────┘     └──────────┬──────────┘
                                       │
          ┌────────────────────────────┘
          │
          ▼
┌─────────────────────────┐
│ Pour chaque Client local │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐     ┌─────────────────────┐
│ ClientApiRepository     │────►│ POST /api/thirdparties │
│ envoyerClient()         │     │ → ID Dolibarr          │
└─────────────────────────┘     └───────────┬───────────┘
                                            │
            ┌───────────────────────────────┘
            │
            ▼
┌─────────────────────────┐     ┌──────────────────────────┐
│ POST /dolcustomersapi/  │     │ Si historique OK :       │
│ clients (historique)    │────►│ Suppression client local │
└─────────────────────────┘     └──────────────────────────┘
```

### 8.3 Création d'une Commande

```
┌──────────────────┐     ┌───────────────────────┐
│ CommandesFragment │────►│ Sélection Client      │
│                   │     │ (verrouillage spinner) │
└──────────────────┘     └───────────────────────┘
          │
          ▼
┌──────────────────────────────┐
│ Ajout lignes via AutoComplete │
│ + Validation individuelle (✓) │
└──────────────────────────────┘
          │
          ▼
┌──────────────────────────────┐
│ Clic "Enregistrer"           │
│ (si toutes lignes validées)  │
└──────────────────────────────┘
          │
          ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│ Commande.Builder.build() │────►│ GestionnaireStockage    │
│                          │     │ Commande.addCommande()  │
└─────────────────────────┘     └─────────────────────────┘
          │
          ▼
┌─────────────────────────┐
│ commandes_data.json     │
│ Navigation → Home       │
└─────────────────────────┘
```

---

## 9. Annexes

### 9.1 Fichiers de Stockage

| Fichier | Gestionnaire | Contenu |
|---------|--------------|---------|
| `clients_data.json` | GestionnaireStockageClient | Clients créés localement |
| `clients_api_data.json` | GestionnaireStockageClient | Clients synchronisés depuis API |
| `commandes_data.json` | GestionnaireStockageCommande | Commandes en attente |
| `produits_data.json` | ProduitStorageManager | Cache des produits |
| `dolibarr_urls.json` | ServiceUrl | Historique des URLs |
| `secure_prefs_crypto` | EncryptedSharedPreferences | Credentials cryptés |

### 9.2 Endpoints API Dolibarr

| Méthode | Endpoint | Repository | Description |
|---------|----------|------------|-------------|
| GET | `/thirdparties` | ClientApiRepository | Récupération clients |
| POST | `/thirdparties` | ClientApiRepository | Création client |
| GET | `/users/login/{user}` | ClientApiRepository | ID utilisateur |
| POST | `/dolcustomersapi/clients` | ClientApiRepository | Historique client |
| GET | `/products` | ProduitRepository | Récupération produits |
| POST | `/orders` | CommandeApiRepository | Création commande |
| POST | `/dolordersapi/fournisseurss` | CommandeApiRepository | Historique commande |

### 9.3 Technologies Utilisées

| Technologie | Usage |
|-------------|-------|
| **Volley** | Requêtes HTTP vers Dolibarr |
| **Gson** | Sérialisation/Désérialisation JSON |
| **EncryptedSharedPreferences** | Stockage sécurisé des credentials |
| **LiveData + ViewModel** | Architecture MVVM |
| **RecyclerView** | Listes performantes |
| **ViewPager2 + TabLayout** | Onglets clients/commandes |
| **Material Design 3** | Interface utilisateur |

---

<div align="center">

**DolOrders v3.0** - Application Android pour Dolibarr ERP

</div>

