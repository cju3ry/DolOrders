# Guide des Actions de l'Application DolOrders

> **Date de création** : 05/02/2026  
> **Version** : 1.0  
> **Description** : Récapitulatif complet de toutes les actions utilisateur avec les méthodes et classes associées

---

## 📋 Table des matières

1. [Connexion & Authentification](#1-connexion--authentification)
2. [Page d'Accueil (Home)](#2-page-daccueil-home)
3. [Gestion des Clients](#3-gestion-des-clients)
4. [Gestion des Commandes](#4-gestion-des-commandes)
5. [Liste d'Attente & Synchronisation](#5-liste-dattente--synchronisation)
6. [Déconnexion](#6-déconnexion)

---

## 1. Connexion & Authentification

### 🔐 Action : Se connecter à l'application

**Interface** : `LoginActivity`

**Étapes** :
1. L'utilisateur saisit l'URL du serveur Dolibarr
2. L'utilisateur saisit son nom d'utilisateur
3. L'utilisateur saisit sa clé API
4. L'utilisateur clique sur "Se connecter"

**Méthodes appelées** :
```java
// LoginActivity.java
private void handleLogin() {
    String url = edtUrl.getText().toString().trim();
    String username = edtUsername.getText().toString().trim();
    String apiKey = edtPassword.getText().toString().trim();
    
    // Sauvegarde des credentials
    saveCredentials(url, username, apiKey);
    
    // Navigation vers MainActivity
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
}

private void saveCredentials(String url, String username, String apiKey) {
    // Sauvegarde sécurisée dans EncryptedSharedPreferences
    // Clés : "base_url", "username", "api_key"
}

private void loadLastUrl() {
    // Charge la dernière URL utilisée pour pré-remplir le champ
}
```

**Classes impliquées** :
- `LoginActivity`
- `EncryptedSharedPreferences` (Android)
- `ServiceGestionSession`

**Stockage** :
- ✅ URL du serveur → `EncryptedSharedPreferences` (clé: `base_url`)
- ✅ Nom d'utilisateur → `EncryptedSharedPreferences` (clé: `username`)
- ✅ Clé API → `EncryptedSharedPreferences` (clé: `api_key`)

---

## 2. Page d'Accueil (Home)

### 📊 Action : Visualiser les statistiques

**Interface** : `HomeFragment`

**Données affichées** :
- Nombre de clients en attente (locaux uniquement)
- Nombre de commandes en attente (locales uniquement)
- Total = Clients + Commandes

**Méthodes appelées** :
```java
// HomeFragment.java - onCreateView()
GestionnaireStockageClient gestionnaireClientLocal = new GestionnaireStockageClient(requireContext());
int nbClientsEnAttente = gestionnaireClientLocal.loadClients().size();

GestionnaireStockageCommande gestionnaireCommande = new GestionnaireStockageCommande(requireContext());
int nbCommandesEnAttente = gestionnaireCommande.loadCommandes().size();

updateStats(nbClientsEnAttente, nbCommandesEnAttente);
```

**Classes impliquées** :
- `HomeFragment`
- `GestionnaireStockageClient`
- `GestionnaireStockageCommande`

**Fichiers JSON lus** :
- `clients.json` (clients locaux uniquement)
- `commandes.json` (commandes locales uniquement)

---

### 🔄 Action : Synchroniser les clients depuis Dolibarr

**Interface** : `HomeFragment` (Bouton "Synchroniser les clients")

**Méthodes appelées** :
```java
// HomeFragment.java
btnSyncClients.setOnClickListener(v -> {
    // 1. Désactive le bouton
    btnSyncClients.setEnabled(false);
    
    // 2. Appel API via le ViewModel
    clientsViewModel.synchroniserClientsDepuisApi(requireContext());
    
    // 3. Observer les erreurs
    clientsViewModel.getErreurSynchronisation().observe(...);
    
    // 4. Observer le succès
    clientsViewModel.getSynchronisationReussie().observe(...);
});
```

**Flux d'exécution** :
```
HomeFragment
    ↓
ClientsFragmentViewModel.synchroniserClientsDepuisApi()
    ↓
ClientApiRepository.synchroniserDepuisApi()
    ↓
Volley: GET /api/index.php/thirdparties?sortfield=t.rowid&sortorder=ASC&limit=100
    ↓
ClientApiMapper.toClient(ClientApiReponseDto) (pour chaque client)
    ↓
GestionnaireStockageClient.saveClients(List<Client>)
    ↓
Sauvegarde dans clients_api.json
    ↓
ClientsFragmentViewModel.synchronisationReussie.postValue(true)
    ↓
HomeFragment: Affiche Toast "✅ X clients synchronisés avec succès !"
```

**Classes impliquées** :
- `HomeFragment`
- `ClientsFragmentViewModel`
- `ClientApiRepository`
- `ClientApiMapper`
- `ClientApiReponseDto`
- `GestionnaireStockageClient`
- `AdaptateurStockageClient`

**Appel API** :
- **Méthode** : GET
- **Endpoint** : `/api/index.php/thirdparties`
- **Paramètres** : `sortfield=t.rowid&sortorder=ASC&limit=100&properties=id,name,phone,email,address,zip,town`
- **Header** : `DOLAPIKEY: {clé_api}`

**Gestion d'erreurs** :
```java
private String convertirErreurEnMessageConvivial(String errorMessage) {
    // Détecte : UnknownHostException, Timeout, No connection, etc.
    // Retourne un message convivial avec conseils d'action
}
```

**Fichiers JSON modifiés** :
- ✅ `clients_api.json` (écrasé avec les nouveaux clients)

---

### 📦 Action : Synchroniser les produits depuis Dolibarr

**Interface** : `HomeFragment` (Bouton "Synchroniser les produits")

**Méthodes appelées** :
```java
// HomeFragment.java
btnSyncProduits.setOnClickListener(v -> {
    // 1. Désactive le bouton
    btnSyncProduits.setEnabled(false);
    
    // 2. Appel API via le ViewModel
    commandesViewModel.chargerProduits(requireContext());
    
    // 3. Observer les erreurs
    commandesViewModel.getErreurSynchronisation().observe(...);
    
    // 4. Observer le succès
    commandesViewModel.getSynchronisationReussie().observe(...);
});
```

**Flux d'exécution** :
```
HomeFragment
    ↓
CommandesFragmentViewModel.chargerProduits()
    ↓
ProduitRepository.synchroniserDepuisApi()
    ↓
Volley: GET /api/index.php/products?sortfield=t.ref&sortorder=ASC&limit=99999
    ↓
ProduitMapper.toProduit(ProduitApiReponseDto) (pour chaque produit)
    ↓
ProduitStorageManager.saveProduits(List<Produit>)
    ↓
Sauvegarde dans produits.json
    ↓
CommandesFragmentViewModel.synchronisationReussie.postValue(true)
    ↓
HomeFragment: Affiche Toast "✅ X produits synchronisés avec succès !"
```

**Classes impliquées** :
- `HomeFragment`
- `CommandesFragmentViewModel`
- `ProduitRepository`
- `ProduitMapper`
- `ProduitApiReponseDto`
- `ProduitStorageManager`
- `ProduitTypeAdapter`

**Appel API** :
- **Méthode** : GET
- **Endpoint** : `/api/index.php/products`
- **Paramètres** : `sortfield=t.ref&sortorder=ASC&limit=99999`
- **Header** : `DOLAPIKEY: {clé_api}`

**Fichiers JSON modifiés** :
- ✅ `produits.json` (écrasé avec les nouveaux produits)

---

### ➕ Action : Créer un nouveau client

**Interface** : `HomeFragment` (Bouton "Nouveau Client")

**Méthodes appelées** :
```java
// HomeFragment.java
btnNewClient.setOnClickListener(v -> {
    // Navigation vers l'onglet Clients
    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
    bottomNav.setSelectedItemId(R.id.nav_clients);
    
    // Ouvre le formulaire d'ajout
    NavigationUtils.navigateToClientAjout(this);
});
```

**Redirection** : Vers l'onglet Clients → Formulaire d'ajout (voir [3.2](#32-action--créer-un-nouveau-client))

---

### 📝 Action : Créer une nouvelle commande

**Interface** : `HomeFragment` (Bouton "Nouvelle Commande")

**Méthodes appelées** :
```java
// HomeFragment.java
btnNewCommande.setOnClickListener(v -> {
    // Indique que la navigation vient de l'accueil
    commandesViewModel.setFromAccueil();
    
    // Navigation vers l'onglet Commandes
    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
    bottomNav.setSelectedItemId(R.id.nav_commandes);
});
```

**Redirection** : Vers l'onglet Commandes (voir [4.1](#41-action--créer-une-nouvelle-commande))

---

### ⏳ Action : Voir les données en attente d'envoi

**Interface** : `HomeFragment` (Bouton "Données en attente")

**Méthodes appelées** :
```java
// HomeFragment.java
btnPendingData.setOnClickListener(v -> {
    // Navigation vers l'onglet Liste d'Attente
    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
    bottomNav.setSelectedItemId(R.id.nav_en_attentes);
});
```

**Redirection** : Vers l'onglet Liste d'Attente (voir [5](#5-liste-dattente--synchronisation))

---

## 3. Gestion des Clients

### 📋 3.1. Action : Consulter la liste des clients

**Interface** : `ClientsFragment` (Onglet Clients)

**Méthodes appelées** :
```java
// ClientsFragment.java - onViewCreated()
ClientsFragmentViewModel viewModel = new ViewModelProvider(requireActivity()).get(ClientsFragmentViewModel.class);

// Charge tous les clients (locaux + API)
viewModel.chargerTousLesClients(requireContext());

// Observe les changements
viewModel.getListeClients().observe(getViewLifecycleOwner(), clients -> {
    if (clients != null) {
        clientAdapteur.updateClients(clients);
    }
});
```

**Flux d'exécution** :
```
ClientsFragment
    ↓
ClientsFragmentViewModel.chargerTousLesClients()
    ↓
GestionnaireStockageClient.loadClients() (fichier: clients.json)
    ↓
GestionnaireStockageClient.loadClients() (fichier: clients_api.json)
    ↓
Fusion des 2 listes
    ↓
ClientsFragmentViewModel.listeClients.postValue(tousLesClients)
    ↓
ClientsFragment: Mise à jour du RecyclerView via ClientAdapteur
```

**Classes impliquées** :
- `ClientsFragment`
- `ClientsFragmentViewModel`
- `GestionnaireStockageClient`
- `ClientAdapteur`

**Affichage** :
- Liste de tous les clients (locaux + récupérés de l'API)
- Distinction visuelle : clients locaux vs clients API (`fromApi` boolean)

---

### ➕ 3.2. Action : Créer un nouveau client

**Interface** : `ClientsAjoutFragment` → `ClientFormulaireFragment`

**Méthodes appelées** :
```java
// ClientFormulaireFragment.java
btnValider.setOnClickListener(v -> {
    // 1. Récupération des données du formulaire
    String nom = edtNom.getText().toString().trim();
    String adresse = edtAdresse.getText().toString().trim();
    String codePostal = edtCodePostal.getText().toString().trim();
    String ville = edtVille.getText().toString().trim();
    String email = edtEmail.getText().toString().trim();
    String telephone = edtTelephone.getText().toString().trim();
    
    // 2. Validation des champs
    if (nom.isEmpty()) {
        edtNom.setError("Le nom est obligatoire");
        return;
    }
    // ... autres validations
    
    // 3. Construction du client via Builder
    Client client = new Client.Builder()
        .setId(UUID.randomUUID().toString())
        .setNom(nom)
        .setAdresse(adresse)
        .setCodePostal(codePostal)
        .setVille(ville)
        .setAdresseMail(email)
        .setTelephone(telephone)
        .setUtilisateur(LoginActivity.getUsername(requireContext()))
        .setDateSaisie(new Date())
        .setFromApi(false) // Client local
        .build();
    
    // 4. Sauvegarde via ServiceClient
    ServiceClient.ajouterClient(requireContext(), client, new ServiceClient.OnClientAjouteListener() {
        @Override
        public void onClientAjoute(Client clientAjoute) {
            // Notifie le ViewModel
            viewModel.publierClientCree(clientAjoute);
            
            // Affiche un Toast de succès
            Toast.makeText(requireContext(), "Client créé avec succès", Toast.LENGTH_SHORT).show();
            
            // Ferme le fragment
            getParentFragmentManager().popBackStack();
        }
        
        @Override
        public void onErreur(String message) {
            Toast.makeText(requireContext(), "Erreur : " + message, Toast.LENGTH_LONG).show();
        }
    });
});
```

**Flux d'exécution** :
```
ClientFormulaireFragment (formulaire rempli)
    ↓
Client.Builder.build() (validation + construction)
    ↓
ServiceClient.ajouterClient()
    ↓
GestionnaireStockageClient.loadClients() (charge la liste existante)
    ↓
Ajoute le nouveau client à la liste
    ↓
GestionnaireStockageClient.saveClients() (sauvegarde dans clients.json)
    ↓
Callback: onClientAjoute(Client)
    ↓
ClientsAjoutFragmentViewModel.publierClientCree()
    ↓
ClientsFragment: Observer notifié → Recharge la liste
```

**Classes impliquées** :
- `ClientFormulaireFragment`
- `Client` + `Client.Builder`
- `ServiceClient`
- `GestionnaireStockageClient`
- `ClientsAjoutFragmentViewModel`

**Validations** :
```java
// Client.Builder.build()
- Nom obligatoire (non vide)
- Adresse obligatoire (non vide après trim)
- Code postal : 5 chiffres
- Email : format valide (regex)
- Téléphone : 10 chiffres
- Utilisateur obligatoire
```

**Fichiers JSON modifiés** :
- ✅ `clients.json` (ajout du nouveau client)

---

### 🔍 3.3. Action : Filtrer les clients

**Interface** : `ClientsFragment` (Bouton filtre)

**Méthodes appelées** :
```java
// ClientsFragment.java
btnFilter.setOnClickListener(v -> {
    // Ouvre un dialogue de filtre
    DialogFilterClients dialog = new DialogFilterClients();
    dialog.show(getChildFragmentManager(), "filter");
});

// DialogFilterClients.java
btnAppliquer.setOnClickListener(v -> {
    String nom = edtNom.getText().toString().trim();
    String ville = edtVille.getText().toString().trim();
    String codePostal = edtCodePostal.getText().toString().trim();
    
    // Applique les filtres
    List<Client> clientsFiltres = filtrerClients(tousLesClients, nom, ville, codePostal);
    
    // Met à jour l'adapteur
    callback.onFilterApplied(clientsFiltres);
});

private List<Client> filtrerClients(List<Client> clients, String nom, String ville, String cp) {
    List<Client> result = new ArrayList<>();
    for (Client client : clients) {
        boolean match = true;
        if (!nom.isEmpty() && !client.getNom().toLowerCase().contains(nom.toLowerCase())) {
            match = false;
        }
        if (!ville.isEmpty() && !client.getVille().toLowerCase().contains(ville.toLowerCase())) {
            match = false;
        }
        if (!cp.isEmpty() && !client.getCodePostal().contains(cp)) {
            match = false;
        }
        if (match) {
            result.add(client);
        }
    }
    return result;
}
```

**Classes impliquées** :
- `ClientsFragment`
- `DialogFilterClients` (dialogue personnalisé)
- `ClientAdapteur`

**Critères de filtrage** :
- Nom (recherche partielle, insensible à la casse)
- Ville (recherche partielle, insensible à la casse)
- Code postal (recherche partielle)

---

### 👁️ 3.4. Action : Consulter les détails d'un client

**Interface** : `ClientsFragment` (Clic sur un client dans la liste)

**Méthodes appelées** :
```java
// ClientsFragment.java
ClientAdapteur adapteur = new ClientAdapteur(clients, new ClientAdapteur.OnClientClickListener() {
    @Override
    public void onClientClick(Client client) {
        // Affiche un dialogue avec les détails
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(client.getNom());
        builder.setMessage(
            "Adresse : " + client.getAdresse() + "\n" +
            "Code Postal : " + client.getCodePostal() + "\n" +
            "Ville : " + client.getVille() + "\n" +
            "Email : " + client.getAdresseMail() + "\n" +
            "Téléphone : " + client.getTelephone() + "\n" +
            "Créé par : " + client.getUtilisateur() + "\n" +
            "Date : " + formatDate(client.getDateSaisie())
        );
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    
    @Override
    public boolean onClientLongClick(Client client) {
        // Affiche un menu contextuel (Modifier, Supprimer, Créer commande)
        return true;
    }
});
```

**Classes impliquées** :
- `ClientsFragment`
- `ClientAdapteur`
- `AlertDialog` (Android)

---

### 📝 3.5. Action : Créer une commande pour un client

**Interface** : `ClientsFragment` (Clic long sur un client → Menu → "Créer une commande")

**Méthodes appelées** :
```java
// ClientsFragment.java
@Override
public boolean onClientLongClick(Client client) {
    PopupMenu popup = new PopupMenu(requireContext(), view);
    popup.inflate(R.menu.menu_actions_client);
    
    popup.setOnMenuItemClickListener(item -> {
        if (item.getItemId() == R.id.action_create_commande) {
            // Indique que la navigation vient de la liste des clients
            commandesViewModel.setFromListeClients();
            
            // Pré-sélectionne le client
            commandesViewModel.startNouvelleCommandePour(client);
            
            // Navigation vers l'onglet Commandes
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
            bottomNav.setSelectedItemId(R.id.nav_commandes);
            
            return true;
        }
        return false;
    });
    
    popup.show();
    return true;
}
```

**Flux d'exécution** :
```
ClientsFragment (clic long sur client)
    ↓
Menu contextuel affiché
    ↓
Clic sur "Créer une commande"
    ↓
CommandesFragmentViewModel.setFromListeClients()
    ↓
CommandesFragmentViewModel.startNouvelleCommandePour(client)
    ↓
Navigation vers l'onglet Commandes
    ↓
CommandesFragment détecte la pré-sélection → Ouvre le formulaire
```

**Classes impliquées** :
- `ClientsFragment`
- `CommandesFragmentViewModel`
- `PopupMenu` (Android)

**Redirection** : Vers l'onglet Commandes avec client pré-sélectionné

---

## 4. Gestion des Commandes

### 📝 4.1. Action : Créer une nouvelle commande

**Interface** : `CommandesFragment` → `CommandeFormDialogFragment`

**Étape 1 : Ouverture du formulaire**
```java
// CommandesFragment.java - onViewCreated()
btnNouvelleCommande.setOnClickListener(v -> {
    CommandeFormDialogFragment dialog = new CommandeFormDialogFragment();
    dialog.show(getChildFragmentManager(), "commande_form");
});
```

**Étape 2 : Chargement des données (clients et produits)**
```java
// CommandeFormDialogFragment.java - onCreateDialog()
// Charge les clients (locaux + API)
viewModel.chargerTousLesClients(requireContext());
viewModel.getListeClients().observe(this, clients -> {
    if (clients != null) {
        // Remplit le spinner de sélection client
        ArrayAdapter<Client> adapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, clients);
        spinnerClient.setAdapter(adapter);
        
        // Pré-sélectionne le client si navigation depuis la liste
        Client clientPreselectionne = viewModel.getClientSelectionne().getValue();
        if (clientPreselectionne != null) {
            int position = clients.indexOf(clientPreselectionne);
            if (position >= 0) {
                spinnerClient.setSelection(position);
            }
        }
    }
});

// Charge les produits depuis le cache
viewModel.chargerProduitsDepuisCache(requireContext());
viewModel.getListeProduits().observe(this, produits -> {
    if (produits != null) {
        // Remplit l'AutoCompleteTextView de sélection produit
        ProduitAdapter adapter = new ProduitAdapter(requireContext(), produits);
        autoCompleteProduit.setAdapter(adapter);
    }
});
```

**Étape 3 : Ajout de lignes de commande**
```java
// CommandeFormDialogFragment.java
autoCompleteProduit.setOnItemClickListener((parent, view, position, id) -> {
    Produit produit = (Produit) parent.getItemAtPosition(position);
    
    // Ajoute la ligne via le ViewModel
    viewModel.addArticle(produit);
    
    // Ferme le dropdown
    autoCompleteProduit.setText("");
    autoCompleteProduit.dismissDropDown();
});

// Observer les lignes de commande
viewModel.getLignesCommande().observe(this, lignes -> {
    if (lignes != null) {
        // Met à jour le RecyclerView des lignes
        lignesAdapter.updateLignes(lignes);
        
        // Met à jour le total
        double total = viewModel.getTotal();
        textTotal.setText(String.format("%.2f €", total));
        
        // Compte les lignes non validées
        int nbNonValidees = 0;
        for (LigneCommande ligne : lignes) {
            if (!ligne.isValidee()) {
                nbNonValidees++;
            }
        }
        
        // Met à jour le texte du bouton
        if (nbNonValidees > 0) {
            btnEnregistrer.setText("Enregistrer (" + nbNonValidees + " ligne(s) à valider)");
            btnEnregistrer.setEnabled(false);
        } else {
            btnEnregistrer.setText("Enregistrer");
            btnEnregistrer.setEnabled(true);
        }
    }
});
```

**Étape 4 : Modification d'une ligne (quantité, remise)**
```java
// LignesCommandeAdapter.java (adapteur du RecyclerView)
edtQuantite.addTextChangedListener(new TextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
        if (!ligne.isValidee()) {
            try {
                int newQty = Integer.parseInt(s.toString());
                double remise = Double.parseDouble(edtRemise.getText().toString());
                
                // Met à jour via le ViewModel
                viewModel.updateLigne(ligne, newQty, remise);
            } catch (NumberFormatException e) {
                // Ignore les erreurs de format
            }
        }
    }
});

edtRemise.addTextChangedListener(/* même logique pour la remise */);
```

**Étape 5 : Validation d'une ligne**
```java
// LignesCommandeAdapter.java
btnValider.setOnClickListener(v -> {
    if (!ligne.isValidee()) {
        // Valide la ligne (elle devient non modifiable)
        viewModel.toggleValidationLigne(ligne);
        
        // L'UI se met à jour via l'Observer
    }
});

btnEdit.setOnClickListener(v -> {
    if (ligne.isValidee()) {
        // Dévalide la ligne (elle redevient modifiable)
        viewModel.toggleValidationLigne(ligne);
    }
});
```

**Étape 6 : Suppression d'une ligne**
```java
// LignesCommandeAdapter.java
btnSupprimer.setOnClickListener(v -> {
    // Supprime la ligne via le ViewModel
    viewModel.removeLigne(ligne);
});
```

**Étape 7 : Enregistrement de la commande**
```java
// CommandeFormDialogFragment.java
btnEnregistrer.setOnClickListener(v -> {
    // 1. Récupère le client sélectionné
    Client client = (Client) spinnerClient.getSelectedItem();
    if (client == null) {
        Toast.makeText(requireContext(), "Veuillez sélectionner un client", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // 2. Récupère la date
    String dateStr = edtDate.getText().toString();
    Date date;
    try {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
        date = sdf.parse(dateStr);
    } catch (ParseException e) {
        date = new Date();
    }
    
    // 3. Récupère les lignes de commande
    List<LigneCommande> lignes = viewModel.getLignesCommande().getValue();
    if (lignes == null || lignes.isEmpty()) {
        Toast.makeText(requireContext(), "Ajoutez au moins un produit", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // 4. Vérifie que toutes les lignes sont validées
    for (LigneCommande ligne : lignes) {
        if (!ligne.isValidee()) {
            Toast.makeText(requireContext(), 
                "Veuillez valider toutes les lignes avant d'enregistrer", 
                Toast.LENGTH_SHORT).show();
            return;
        }
    }
    
    // 5. Construit la commande via Builder
    Commande commande = new Commande.Builder()
        .setId(UUID.randomUUID().toString())
        .setClient(client)
        .setDateCommande(date)
        .setLignesCommande(lignes)
        .setUtilisateur(LoginActivity.getUsername(requireContext()))
        .build();
    
    // 6. Sauvegarde la commande
    GestionnaireStockageCommande gestionnaire = new GestionnaireStockageCommande(requireContext());
    List<Commande> commandes = gestionnaire.loadCommandes();
    commandes.add(commande);
    boolean success = gestionnaire.saveCommandes(commandes);
    
    if (success) {
        Toast.makeText(requireContext(), "Commande enregistrée avec succès", Toast.LENGTH_SHORT).show();
        
        // 7. Nettoie le ViewModel
        viewModel.clear();
        
        // 8. Ferme le dialogue
        dismiss();
        
        // 9. Recharge la liste des commandes dans le fragment parent
        ((CommandesFragment) getParentFragment()).rechargerCommandes();
    } else {
        Toast.makeText(requireContext(), "Erreur lors de l'enregistrement", Toast.LENGTH_LONG).show();
    }
});
```

**Flux d'exécution complet** :
```
CommandesFragment (clic sur "Nouvelle commande")
    ↓
CommandeFormDialogFragment.onCreateDialog()
    ↓
CommandesFragmentViewModel.chargerTousLesClients() → Charge clients.json + clients_api.json
    ↓
CommandesFragmentViewModel.chargerProduitsDepuisCache() → Charge produits.json
    ↓
Utilisateur remplit le formulaire :
  - Sélectionne un client
  - Sélectionne un produit → viewModel.addArticle(produit)
  - Modifie quantité/remise → viewModel.updateLigne(...)
  - Valide la ligne → viewModel.toggleValidationLigne(...)
  - Répète pour chaque produit
    ↓
Clic sur "Enregistrer"
    ↓
Commande.Builder.build() (validation + construction)
    ↓
GestionnaireStockageCommande.loadCommandes() (charge la liste existante)
    ↓
Ajoute la nouvelle commande à la liste
    ↓
GestionnaireStockageCommande.saveCommandes() (sauvegarde dans commandes.json)
    ↓
viewModel.clear() (réinitialise les données)
    ↓
CommandeFormDialogFragment.dismiss()
    ↓
CommandesFragment.rechargerCommandes() (rafraîchit l'affichage)
```

**Classes impliquées** :
- `CommandesFragment`
- `CommandeFormDialogFragment`
- `CommandesFragmentViewModel`
- `Commande` + `Commande.Builder`
- `LigneCommande`
- `GestionnaireStockageCommande`
- `GestionnaireStockageClient`
- `ProduitStorageManager`

**Validations** :
```java
// Commande.Builder.build()
- Client obligatoire (non null)
- Date par défaut si null
- Au moins une ligne de commande
- Utilisateur obligatoire

// LigneCommande
- Produit obligatoire (non null)
- Quantité > 0
- Remise entre 0 et 100
```

**Fichiers JSON modifiés** :
- ✅ `commandes.json` (ajout de la nouvelle commande)

**Fichiers JSON lus** :
- 📖 `clients.json` (clients locaux)
- 📖 `clients_api.json` (clients de l'API)
- 📖 `produits.json` (produits)

---

### 📋 4.2. Action : Consulter la liste des commandes

**Interface** : `CommandesFragment` (Onglet Commandes)

**Méthodes appelées** :
```java
// CommandesFragment.java - onViewCreated()
private void rechargerCommandes() {
    GestionnaireStockageCommande gestionnaire = new GestionnaireStockageCommande(requireContext());
    List<Commande> commandes = gestionnaire.loadCommandes();
    
    // Met à jour le RecyclerView
    commandesAdapter.updateCommandes(commandes);
}
```

**Classes impliquées** :
- `CommandesFragment`
- `GestionnaireStockageCommande`
- `CommandesAdapter` (adapteur personnalisé)

**Affichage** :
- Liste de toutes les commandes locales
- Pour chaque commande : client, date, montant total, nombre de lignes

---

### ✏️ 4.3. Action : Modifier une commande

**Interface** : `CommandesFragment` (Clic sur une commande)

**Méthodes appelées** :
```java
// CommandesFragment.java
commandesAdapter.setOnCommandeClickListener(commande -> {
    // Ouvre le dialogue de modification
    CommandeFormDialogFragment dialog = CommandeFormDialogFragment.newInstanceForEdit(commande);
    dialog.show(getChildFragmentManager(), "commande_edit");
});
```

**Flux** : Similaire à la création, mais avec les données pré-remplies

**Classes impliquées** :
- `CommandesFragment`
- `CommandeFormDialogFragment`
- `CommandesFragmentViewModel`

---

## 5. Liste d'Attente & Synchronisation

### 📋 5.1. Action : Consulter les clients en attente

**Interface** : `ListeAttenteFragment` → Onglet "CLIENTS"

**Méthodes appelées** :
```java
// TableauClientsFragment.java - onViewCreated()
private void chargerClients() {
    GestionnaireStockageClient gestionnaire = new GestionnaireStockageClient(requireContext());
    List<Client> clients = gestionnaire.loadClients(); // Charge clients.json (locaux uniquement)
    
    clientsAdapter.updateClients(clients);
}
```

**Classes impliquées** :
- `ListeAttenteFragment`
- `TableauClientsFragment`
- `GestionnaireStockageClient`
- `ClientsAttenteAdapteur`

**Affichage** :
- Liste des clients **locaux uniquement** (pas encore envoyés à Dolibarr)

---

### 📋 5.2. Action : Consulter les commandes en attente

**Interface** : `ListeAttenteFragment` → Onglet "COMMANDES"

**Méthodes appelées** :
```java
// TableauCommandesFragment.java - onViewCreated()
private void chargerCommandes() {
    GestionnaireStockageCommande gestionnaire = new GestionnaireStockageCommande(requireContext());
    List<Commande> commandes = gestionnaire.loadCommandes(); // Charge commandes.json (locales uniquement)
    
    commandesAdapter.updateCommandes(commandes);
}
```

**Classes impliquées** :
- `ListeAttenteFragment`
- `TableauCommandesFragment`
- `GestionnaireStockageCommande`
- `CommandesAttenteAdapteur`

**Affichage** :
- Liste des commandes **locales uniquement** (pas encore envoyées à Dolibarr)

---

### 🚀 5.3. Action : Envoyer les données vers Dolibarr

**Interface** : `ListeAttenteFragment` (Bouton "Envoyer vers Dolibarr")

**Méthodes appelées** :
```java
// ListeAttenteFragment.java
btnEnvoyer.setOnClickListener(v -> {
    // Affiche un dialogue de confirmation
    new AlertDialog.Builder(requireContext())
        .setTitle("Synchronisation complète")
        .setMessage("Voulez-vous envoyer tous les clients et leurs commandes vers Dolibarr ?")
        .setPositiveButton("Envoyer", (dialog, which) -> {
            envoyerToutVersDolibarr();
        })
        .setNegativeButton("Annuler", null)
        .show();
});

private void envoyerToutVersDolibarr() {
    // 1. Affiche un ProgressDialog
    ProgressDialog progressDialog = new ProgressDialog(requireContext());
    progressDialog.setMessage("Préparation de l'envoi...");
    progressDialog.setCancelable(false);
    progressDialog.show();
    
    // 2. Charge TOUS les clients (locaux + API)
    GestionnaireStockageClient storageLocal = new GestionnaireStockageClient(requireContext());
    GestionnaireStockageClient storageApi = new GestionnaireStockageClient(requireContext(), GestionnaireStockageClient.API_CLIENTS_FILE);
    List<Client> clientsLocaux = storageLocal.loadClients();
    List<Client> clientsApi = storageApi.loadClients();
    
    // 3. Charge toutes les commandes
    GestionnaireStockageCommande commandeStorage = new GestionnaireStockageCommande(requireContext());
    List<Commande> toutesCommandes = commandeStorage.loadCommandes();
    
    // 4. Identifie quels clients ont des commandes
    List<Client> clientsAEnvoyer = new ArrayList<>();
    
    // Ajoute tous les clients locaux (avec ou sans commandes)
    for (Client clientLocal : clientsLocaux) {
        if (!clientLocal.isFromApi()) {
            clientsAEnvoyer.add(clientLocal);
        }
    }
    
    // Ajoute les clients API qui ont des commandes
    for (Commande cmd : toutesCommandes) {
        Client clientCommande = cmd.getClient();
        if (clientCommande.isFromApi() && !clientsAEnvoyer.contains(clientCommande)) {
            clientsAEnvoyer.add(clientCommande);
        }
    }
    
    // 5. Lance l'envoi récursif
    ClientApiRepository clientRepo = new ClientApiRepository(requireContext());
    CommandeApiRepository commandeRepo = new CommandeApiRepository(requireContext());
    
    envoyerClientEtCommandesRecursif(clientsAEnvoyer, 0, clientRepo, commandeRepo, 
                                     storageLocal, commandeStorage, progressDialog);
}
```

**Étape 2 : Envoi récursif des clients et commandes**
```java
private void envoyerClientEtCommandesRecursif(List<Client> clients, int index, ...) {
    // Condition d'arrêt
    if (index >= clients.size()) {
        // Tous les clients traités → Re-synchronisation
        resynchroniserClients(clientStorage, progressDialog);
        return;
    }
    
    Client client = clients.get(index);
    progressDialog.setMessage("Traitement du client " + client.getNom() + " (" + (index + 1) + "/" + clients.size() + ")...");
    
    // Vérifie si le client provient de l'API
    if (client.isFromApi()) {
        // Client déjà dans Dolibarr → Envoie seulement les commandes
        envoyerCommandesDuClient(client, commandeRepo, commandeStorage, () -> {
            // Passe au client suivant
            envoyerClientEtCommandesRecursif(clients, index + 1, ...);
        });
    } else {
        // Client local → Envoie d'abord le client
        clientRepo.envoyerClient(client, new ClientApiRepository.ClientEnvoiCallback() {
            @Override
            public void onSuccess(String dolibarrId) {
                // Client créé dans Dolibarr avec l'ID retourné
                Client clientAvecId = new Client.Builder()
                    .setId(dolibarrId) // ID Dolibarr
                    .setNom(client.getNom())
                    .setAdresse(client.getAdresse())
                    .setCodePostal(client.getCodePostal())
                    .setVille(client.getVille())
                    .setAdresseMail(client.getAdresseMail())
                    .setTelephone(client.getTelephone())
                    .setUtilisateur(client.getUtilisateur())
                    .setDateSaisie(client.getDateSaisie())
                    .setFromApi(false)
                    .build();
                
                // Envoie les commandes de ce client
                envoyerCommandesDuClient(clientAvecId, commandeRepo, commandeStorage, () -> {
                    // Supprime le client local
                    clientStorage.deleteClient(client);
                    
                    // Passe au client suivant
                    envoyerClientEtCommandesRecursif(clients, index + 1, ...);
                });
            }
            
            @Override
            public void onError(String message) {
                // Convertit l'erreur en message convivial
                String messageConvivial = convertirErreurEnMessageConvivial(message);
                
                // Affiche un dialogue d'erreur
                new AlertDialog.Builder(requireContext())
                    .setTitle("❌ Erreur envoi client")
                    .setMessage("Client : " + client.getNom() + "\n\n" + messageConvivial)
                    .setPositiveButton("Continuer", (dialog, which) -> {
                        // Continue avec le client suivant
                        envoyerClientEtCommandesRecursif(clients, index + 1, ...);
                    })
                    .setNegativeButton("Abandonner", (dialog, which) -> {
                        progressDialog.dismiss();
                        naviguerVersAccueil();
                    })
                    .show();
            }
        });
    }
}
```

**Étape 3 : Envoi des commandes d'un client**
```java
private void envoyerCommandesDuClient(Client client, CommandeApiRepository repo, 
                                       GestionnaireStockageCommande storage, Runnable onTermine) {
    // Charge toutes les commandes
    List<Commande> toutesCommandes = storage.loadCommandes();
    
    // Filtre les commandes de ce client
    List<Commande> commandesDuClient = new ArrayList<>();
    for (Commande cmd : toutesCommandes) {
        if (cmd.getClient() != null && cmd.getClient().getNom().equals(client.getNom())) {
            // Remplace le client local par celui avec l'ID Dolibarr
            Commande commandeAvecClientId = new Commande.Builder()
                .setId(cmd.getId())
                .setClient(client) // Client avec ID Dolibarr
                .setDateCommande(cmd.getDateCommande())
                .setLignesCommande(cmd.getLignesCommande())
                .setUtilisateur(cmd.getUtilisateur())
                .build();
            commandesDuClient.add(commandeAvecClientId);
        }
    }
    
    if (commandesDuClient.isEmpty()) {
        onTermine.run();
        return;
    }
    
    // Envoie les commandes une par une
    envoyerCommandesRecursif(commandesDuClient, 0, repo, storage, onTermine);
}
```

**Étape 4 : Envoi récursif des commandes**
```java
private void envoyerCommandesRecursif(List<Commande> commandes, int index, 
                                       CommandeApiRepository repo, 
                                       GestionnaireStockageCommande storage, 
                                       Runnable onTermine) {
    // Condition d'arrêt
    if (index >= commandes.size()) {
        onTermine.run();
        return;
    }
    
    Commande commande = commandes.get(index);
    
    // ÉTAPE 1 : Envoie vers le module NATIF Dolibarr
    repo.envoyerCommandeVersModuleNatif(commande, new CommandeApiRepository.CommandeNativeEnvoiCallback() {
        @Override
        public void onSuccess(String dolibarrCommandeId) {
            // Commande créée dans Dolibarr avec l'ID retourné
            
            // ÉTAPE 2 : Envoie vers le module HISTORIQUE avec l'ID Dolibarr
            repo.envoyerCommandeVersHistoriqueAvecId(commande, dolibarrCommandeId, new CommandeApiRepository.CommandeEnvoiCallback() {
                @Override
                public void onSuccess(String historiqueId) {
                    // ÉTAPE 3 : Supprime la commande locale
                    storage.deleteCommande(commande.getId());
                    
                    // Passe à la commande suivante
                    envoyerCommandesRecursif(commandes, index + 1, repo, storage, onTermine);
                }
                
                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "Erreur historique : " + message, Toast.LENGTH_SHORT).show();
                    // Continue quand même
                    envoyerCommandesRecursif(commandes, index + 1, repo, storage, onTermine);
                }
            });
        }
        
        @Override
        public void onError(String message) {
            String messageConvivial = convertirErreurEnMessageConvivial(message);
            
            new AlertDialog.Builder(requireContext())
                .setTitle("❌ Erreur envoi commande")
                .setMessage(messageConvivial + "\n\nQue souhaitez-vous faire ?")
                .setPositiveButton("Continuer", (dialog, which) -> {
                    envoyerCommandesRecursif(commandes, index + 1, repo, storage, onTermine);
                })
                .setNegativeButton("Abandonner", (dialog, which) -> {
                    onTermine.run();
                })
                .show();
        }
    });
}
```

**Étape 5 : Re-synchronisation après envoi**
```java
private void resynchroniserClients(GestionnaireStockageClient storage, ProgressDialog progressDialog) {
    progressDialog.setMessage("Récupération des clients depuis Dolibarr...");
    
    ClientApiRepository repo = new ClientApiRepository(requireContext());
    GestionnaireStockageClient storageApi = new GestionnaireStockageClient(requireContext(), GestionnaireStockageClient.API_CLIENTS_FILE);
    
    repo.synchroniserDepuisApi(new ClientApiRepository.ClientCallback() {
        @Override
        public void onSuccess(List<Client> clients) {
            // Sauvegarde dans le fichier API
            storageApi.saveClients(clients);
            
            progressDialog.dismiss();
            
            Toast.makeText(getContext(), 
                "✅ Synchronisation terminée ! " + clients.size() + " clients récupérés", 
                Toast.LENGTH_LONG).show();
            
            // Navigation vers l'accueil
            naviguerVersAccueil();
        }
        
        @Override
        public void onError(String message) {
            progressDialog.dismiss();
            
            String messageConvivial = convertirErreurEnMessageConvivial(message);
            
            new AlertDialog.Builder(requireContext())
                .setTitle("❌ Erreur de synchronisation")
                .setMessage(messageConvivial)
                .setPositiveButton("OK", (dialog, which) -> naviguerVersAccueil())
                .setNegativeButton("Réessayer", (dialog, which) -> envoyerToutVersDolibarr())
                .show();
        }
    });
}
```

**Flux d'exécution complet** :
```
ListeAttenteFragment (clic sur "Envoyer vers Dolibarr")
    ↓
Dialogue de confirmation
    ↓
envoyerToutVersDolibarr()
    ↓
Charge clients.json + clients_api.json + commandes.json
    ↓
Identifie les clients à envoyer (locaux + API avec commandes)
    ↓
POUR CHAQUE CLIENT (récursif) :
    ├─ Si client.isFromApi() == true (client déjà dans Dolibarr) :
    │   └─ envoyerCommandesDuClient() → Envoie les commandes
    │
    └─ Si client.isFromApi() == false (client local) :
        ├─ POST /api/index.php/thirdparties (création client)
        ├─ POST /api/index.php/dolcustomersapi/clients (historique client)
        ├─ Récupère l'ID Dolibarr retourné
        ├─ envoyerCommandesDuClient() :
        │   └─ POUR CHAQUE COMMANDE du client (récursif) :
        │       ├─ POST /api/index.php/orders (module natif)
        │       ├─ Récupère l'ID commande Dolibarr
        │       ├─ POST /api/index.php/dolordersapi/fournisseurss (historique)
        │       └─ Supprime commandes.json (la commande envoyée)
        │
        └─ Supprime clients.json (le client envoyé)
    ↓
Tous les clients traités
    ↓
resynchroniserClients()
    ↓
GET /api/index.php/thirdparties (récupère tous les clients)
    ↓
Sauvegarde dans clients_api.json
    ↓
Navigation vers l'accueil
    ↓
Toast : "✅ Synchronisation terminée !"
```

**Classes impliquées** :
- `ListeAttenteFragment`
- `ClientApiRepository`
- `CommandeApiRepository`
- `GestionnaireStockageClient`
- `GestionnaireStockageCommande`

**Appels API (pour chaque client local)** :
1. **POST /api/index.php/users/login/{username}** → Récupère l'ID utilisateur
2. **POST /api/index.php/thirdparties** → Crée le client dans Dolibarr
3. **POST /api/index.php/dolcustomersapi/clients** → Ajoute à l'historique

**Appels API (pour chaque commande)** :
1. **POST /api/index.php/orders** → Crée la commande dans le module natif
2. **POST /api/index.php/dolordersapi/fournisseurss** → Ajoute à l'historique (1 ligne par produit)

**Fichiers JSON modifiés** :
- ✅ `clients.json` (suppression des clients envoyés)
- ✅ `commandes.json` (suppression des commandes envoyées)
- ✅ `clients_api.json` (mis à jour après re-synchronisation)

**Gestion d'erreurs** :
- Erreurs de connexion → Dialogue avec bouton "Réessayer"
- Erreur client → Dialogue "Continuer" ou "Abandonner"
- Erreur commande → Dialogue "Continuer" ou "Abandonner"
- Erreur historique → Toast d'avertissement, continue quand même

---

## 6. Déconnexion

### 🚪 Action : Se déconnecter de l'application

**Interface** : `MainActivity` (Menu utilisateur → "Déconnexion")

**Méthodes appelées** :
```java
// MainActivity.java - onOptionsItemSelected()
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_logout) {
        new AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Voulez-vous vraiment vous déconnecter ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                // Déconnexion via ServiceGestionSession
                ServiceGestionSession.logout(this);
                
                // Retour à l'écran de connexion
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Non", null)
            .show();
        return true;
    }
    return super.onOptionsItemSelected(item);
}
```

**Méthode de déconnexion** :
```java
// ServiceGestionSession.java
public static void logout(Context context) {
    try {
        MasterKey masterKey = new MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build();
        
        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
        
        // Supprime toutes les données sauvegardées
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        
        Log.d("ServiceGestionSession", "Déconnexion réussie");
    } catch (Exception e) {
        Log.e("ServiceGestionSession", "Erreur lors de la déconnexion", e);
    }
}
```

**Classes impliquées** :
- `MainActivity`
- `ServiceGestionSession`
- `LoginActivity`

**Données supprimées** :
- ❌ URL du serveur
- ❌ Nom d'utilisateur
- ❌ Clé API

**Notes** :
- Les fichiers JSON locaux (clients.json, commandes.json, produits.json) ne sont **pas supprimés**
- L'utilisateur peut se reconnecter avec d'autres identifiants

---

## 🔔 Indicateur de Connexion Internet

### 📶 Action : Surveillance de la connexion réseau

**Interface** : `MainActivity` (Point rouge/vert dans la toolbar)

**Méthodes appelées** :
```java
// MainActivity.java - onCreate()
private void setupConnectionMonitoring() {
    serviceConnexion = new ServiceConnexionInternet(this);
    
    serviceConnexion.startMonitoring(new ServiceConnexionInternet.ConnectionStatusListener() {
        @Override
        public void onConnectionStatusChanged(boolean isConnected) {
            runOnUiThread(() -> {
                updateConnectionIndicator(isConnected);
                
                if (isConnected) {
                    Toast.makeText(MainActivity.this, "Connexion rétablie", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Connexion perdue", Toast.LENGTH_SHORT).show();
                }
            });
        }
    });
    
    // État initial
    boolean isConnected = serviceConnexion.isConnected();
    updateConnectionIndicator(isConnected);
}

private void updateConnectionIndicator(boolean isConnected) {
    if (isConnected) {
        connectionIndicator.setBackgroundResource(R.drawable.ic_connection_online); // Point vert
    } else {
        connectionIndicator.setBackgroundResource(R.drawable.ic_connection_offline); // Point rouge
    }
}
```

**Classes impliquées** :
- `MainActivity`
- `ServiceConnexionInternet`

**Événements détectés** :
- ✅ Connexion disponible → Point vert + Toast "Connexion rétablie"
- ❌ Connexion perdue → Point rouge + Toast "Connexion perdue"

**Surveillance** :
- Temps réel via `ConnectivityManager.NetworkCallback`
- Actif pendant toute la durée de vie de l'application
- Arrêt automatique lors de la destruction de l'activité

---

## 📊 Récapitulatif des Fichiers JSON

### Fichiers utilisés par l'application

| Fichier | Description | Lecture | Écriture |
|---------|-------------|---------|----------|
| **clients.json** | Clients créés localement (pas encore envoyés à Dolibarr) | ✅ Home, Clients, ListeAttente, Commandes | ✅ ClientFormulaireFragment |
| **clients_api.json** | Clients récupérés depuis Dolibarr (via synchronisation) | ✅ Clients, Commandes | ✅ ClientApiRepository |
| **commandes.json** | Commandes créées localement (pas encore envoyées) | ✅ Home, Commandes, ListeAttente | ✅ CommandeFormDialogFragment |
| **produits.json** | Produits récupérés depuis Dolibarr (cache) | ✅ Commandes | ✅ ProduitRepository |

---

## 🔗 Récapitulatif des Appels API Dolibarr

### Endpoints utilisés

| Action | Méthode | Endpoint | Classe |
|--------|---------|----------|--------|
| **Récupérer clients** | GET | `/api/index.php/thirdparties` | ClientApiRepository |
| **Créer client** | POST | `/api/index.php/thirdparties` | ClientApiRepository |
| **Historique client** | POST | `/api/index.php/dolcustomersapi/clients` | ClientApiRepository |
| **Récupérer ID user** | GET | `/api/index.php/users/login/{username}` | ClientApiRepository |
| **Récupérer produits** | GET | `/api/index.php/products` | ProduitRepository |
| **Créer commande** | POST | `/api/index.php/orders` | CommandeApiRepository |
| **Historique commande** | POST | `/api/index.php/dolordersapi/fournisseurss` | CommandeApiRepository |

---

## 📚 Récapitulatif des ViewModels

### ViewModels utilisés et leurs responsabilités

| ViewModel | Responsabilités | Fragments associés |
|-----------|-----------------|-------------------|
| **ClientsFragmentViewModel** | Gestion des clients (liste, synchronisation API) | ClientsFragment, HomeFragment |
| **ClientsAjoutFragmentViewModel** | Gestion de la création de client | ClientsAjoutFragment, ClientFormulaireFragment |
| **CommandesFragmentViewModel** | Gestion des commandes (lignes, produits, clients) | CommandesFragment, CommandeFormDialogFragment, HomeFragment |

---

## 🎯 Conclusion

Ce document récapitule **toutes les actions utilisateur possibles** dans l'application DolOrders avec :
- ✅ Les interfaces (Fragments/Activities)
- ✅ Les méthodes appelées
- ✅ Le flux d'exécution complet
- ✅ Les classes impliquées
- ✅ Les fichiers JSON lus/modifiés
- ✅ Les appels API Dolibarr
- ✅ La gestion des erreurs

**Date de dernière mise à jour** : 05/02/2026

