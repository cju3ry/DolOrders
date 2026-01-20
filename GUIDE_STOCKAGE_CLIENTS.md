# Guide d'utilisation du système de stockage des clients

## 📁 Architecture créée

Le système de stockage des clients a été mis en place avec les composants suivants :

### 1. **ClientStorageManager.java**
Gestionnaire principal pour la sauvegarde et le chargement des clients dans un fichier JSON.

**Emplacement** : `app/src/main/java/com/example/dolorders/data/storage/ClientStorageManager.java`

**Méthodes disponibles** :
- `saveClients(List<Client>)` : Sauvegarde la liste complète (écrase l'ancienne)
- `loadClients()` : Charge tous les clients depuis le fichier
- `addClient(Client)` : Ajoute un client à la liste existante
- `hasStoredClients()` : Vérifie si des clients sont sauvegardés
- `clearClients()` : Supprime tous les clients du fichier
- `getClientCount()` : Retourne le nombre de clients stockés

### 2. **ClientTypeAdapter.java**
Adaptateur Gson personnalisé pour sérialiser/désérialiser la classe `Client` qui utilise le pattern Builder.

**Emplacement** : `app/src/main/java/com/example/dolorders/data/storage/ClientTypeAdapter.java`

---

## 🚀 Utilisation

### Dans MainActivity

#### Récupérer les clients depuis l'API et les sauvegarder automatiquement

```java
private void chargerLesClients() {
    ClientRepository repository = new ClientRepository(this);
    ClientStorageManager storageManager = new ClientStorageManager(this);

    repository.recupererClientsDolibarr(new ClientRepository.ClientCallback() {
        @Override
        public void onSuccess(List<Client> clients) {
            // Sauvegarde automatique dans le fichier
            boolean saved = storageManager.saveClients(clients);
            
            if (saved) {
                Toast.makeText(MainActivity.this, 
                    clients.size() + " clients sauvegardés", 
                    Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(String message) {
            Toast.makeText(MainActivity.this, 
                "Erreur : " + message, 
                Toast.LENGTH_LONG).show();
        }
    });
}
```

#### Charger les clients depuis le fichier local

```java
private List<Client> chargerClientsLocaux() {
    ClientStorageManager storageManager = new ClientStorageManager(this);
    List<Client> clients = storageManager.loadClients();
    
    Log.d("STORAGE", "Clients chargés : " + clients.size());
    return clients;
}
```

### Dans un Fragment (ex: ClientsFragment)

```java
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    // Charger les clients au démarrage du fragment
    ClientStorageManager storageManager = new ClientStorageManager(requireContext());
    List<Client> clients = storageManager.loadClients();
    
    if (clients.isEmpty()) {
        Toast.makeText(getContext(), 
            "Aucun client en local. Synchronisez d'abord.", 
            Toast.LENGTH_SHORT).show();
    } else {
        // Afficher les clients dans un RecyclerView
        // monAdapter.setClients(clients);
    }
}
```

### Ajouter un nouveau client manuellement

```java
Client nouveauClient = new Client.Builder()
    .setId("123")
    .setNom("Dupont")
    .setAdresse("1 rue de Paris")
    .setCodePostal("75001")
    .setVille("Paris")
    .setAdresseMail("dupont@example.com")
    .setTelephone("0612345678")
    .setUtilisateur("admin")
    .setDateSaisie(new Date())
    .setUtilisateurEnvoie("admin")
    .setDateEnvoie(new Date())
    .setDateMiseAJour(new Date())
    .build();

ClientStorageManager storageManager = new ClientStorageManager(this);
boolean added = storageManager.addClient(nouveauClient);

if (added) {
    Toast.makeText(this, "Client ajouté et sauvegardé", Toast.LENGTH_SHORT).show();
}
```

---

## 💾 Caractéristiques du stockage

### Fichier utilisé
- **Nom** : `clients_data.json`
- **Emplacement** : Répertoire interne de l'application (`context.getFilesDir()`)
- **Format** : JSON

### Persistance
- ✅ Les données persistent entre les redémarrages de l'application
- ✅ Les données sont supprimées automatiquement si l'app est désinstallée
- ✅ Les données sont privées à votre application (non accessibles par d'autres apps)

### Sécurité
- Le fichier est stocké dans le répertoire privé de l'app
- Aucune autre application ne peut y accéder (sauf avec root)
- Pas de chiffrement par défaut (à ajouter si nécessaire)

---

## 🔄 Workflow recommandé

### 1. Au démarrage de l'application
```java
ClientStorageManager storageManager = new ClientStorageManager(this);

if (storageManager.hasStoredClients()) {
    // Charger les clients locaux pour un affichage rapide
    List<Client> clients = storageManager.loadClients();
    // Afficher dans l'UI
} else {
    // Première utilisation : inviter l'utilisateur à synchroniser
    Toast.makeText(this, "Synchronisez pour charger les clients", Toast.LENGTH_SHORT).show();
}
```

### 2. Lors de la synchronisation (bouton "Synchro")
- Appeler l'API Dolibarr via `ClientRepository`
- Sauvegarder automatiquement avec `storageManager.saveClients()`
- Mettre à jour l'affichage

### 3. Mode hors ligne
- Les clients restent accessibles via `loadClients()`
- L'utilisateur peut consulter les données même sans connexion

---

## 📊 Exemple d'intégration avec RecyclerView

### Dans ClientsFragment

```java
public class ClientsFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private ClientAdapter adapter;
    private ClientStorageManager storageManager;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        storageManager = new ClientStorageManager(requireContext());
        
        // Configuration du RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewClients);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Chargement des clients
        List<Client> clients = storageManager.loadClients();
        adapter = new ClientAdapter(clients);
        recyclerView.setAdapter(adapter);
        
        // Rafraîchissement à tirer (Pull to refresh)
        swipeRefreshLayout.setOnRefreshListener(() -> {
            synchroniserClients();
        });
    }
    
    private void synchroniserClients() {
        ClientRepository repository = new ClientRepository(requireContext());
        
        repository.recupererClientsDolibarr(new ClientRepository.ClientCallback() {
            @Override
            public void onSuccess(List<Client> clients) {
                // Sauvegarder
                storageManager.saveClients(clients);
                
                // Mettre à jour l'adapter
                adapter.setClients(clients);
                adapter.notifyDataSetChanged();
                
                swipeRefreshLayout.setRefreshing(false);
            }
            
            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
```

---

## 🐛 Debugging

Pour vérifier le contenu du fichier JSON :

```java
ClientStorageManager storageManager = new ClientStorageManager(this);

// Afficher le nombre de clients
int count = storageManager.getClientCount();
Log.d("STORAGE", "Nombre de clients : " + count);

// Afficher tous les clients
List<Client> clients = storageManager.loadClients();
for (Client client : clients) {
    Log.d("STORAGE", "Client: " + client.getNom() + " - " + client.getAdresseMail());
}
```

---

## ⚙️ Configuration actuelle dans MainActivity

Le bouton **btnSyncroClient** déclenche :
1. Appel API vers Dolibarr (`/thirdparties`)
2. Mapping des DTOs vers objets Client
3. **Sauvegarde automatique** dans `clients_data.json`
4. Affichage d'un Toast de confirmation

```java
btnSyncroClient.setOnClickListener(v -> {
    chargerLesClients();
});
```

---

## 🎯 Prochaines étapes recommandées

1. ✅ **Créer un RecyclerView** pour afficher la liste des clients
2. ✅ **Ajouter un Pull-to-Refresh** pour synchroniser facilement
3. ✅ **Gérer le mode hors ligne** avec les clients stockés
4. ⚠️ **Ajouter un système de cache** avec timestamp pour savoir quand re-synchroniser
5. ⚠️ **Gérer les conflits** si un client est modifié localement et à distance

---

## 📝 Notes importantes

- Le fichier `clients_data.json` est **écrasé complètement** à chaque sauvegarde
- Pour une version production, considérez **Room Database** pour des opérations plus complexes
- Actuellement, pas de gestion des modifications locales non synchronisées
- Les clients sont identifiés par leur `id` de Dolibarr


