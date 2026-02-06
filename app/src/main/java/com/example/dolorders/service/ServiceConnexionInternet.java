package com.example.dolorders.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Service pour surveiller l'état de la connexion Internet.
 * Utilise ConnectivityManager.NetworkCallback pour détecter les changements en temps réel.
 */
public class ServiceConnexionInternet {
    private static final String TAG = "ServiceConnexion";
    private final ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectionStatusListener listener;
    private boolean isConnected = false;

    /**
     * Interface pour être notifié des changements de connexion
     */
    public interface ConnectionStatusListener {
        void onConnectionStatusChanged(boolean isConnected);
    }

    public ServiceConnexionInternet(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Vérifier l'état initial
        isConnected = isInternetAvailable();
    }

    /**
     * Démarre la surveillance de la connexion Internet
     *
     * @param listener Callback appelé lors des changements d'état
     */
    public void startMonitoring(ConnectionStatusListener listener) {
        this.listener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // API 24+ : Utilisation de registerDefaultNetworkCallback
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    Log.d(TAG, "✅ Connexion Internet disponible");
                    isConnected = true;
                    if (ServiceConnexionInternet.this.listener != null) {
                        ServiceConnexionInternet.this.listener.onConnectionStatusChanged(true);
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    Log.d(TAG, "❌ Connexion Internet perdue");
                    isConnected = false;
                    if (ServiceConnexionInternet.this.listener != null) {
                        ServiceConnexionInternet.this.listener.onConnectionStatusChanged(false);
                    }
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                    if (isConnected != hasInternet) {
                        isConnected = hasInternet;
                        Log.d(TAG, "🔄 Statut Internet changé: " + (hasInternet ? "Connecté" : "Déconnecté"));
                        if (ServiceConnexionInternet.this.listener != null) {
                            ServiceConnexionInternet.this.listener.onConnectionStatusChanged(hasInternet);
                        }
                    }
                }
            };
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            // API < 24 : Utilisation de registerNetworkCallback avec NetworkRequest
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    Log.d(TAG, "✅ Connexion Internet disponible");
                    isConnected = true;
                    if (ServiceConnexionInternet.this.listener != null) {
                        ServiceConnexionInternet.this.listener.onConnectionStatusChanged(true);
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    Log.d(TAG, "❌ Connexion Internet perdue");
                    isConnected = false;
                    if (ServiceConnexionInternet.this.listener != null) {
                        ServiceConnexionInternet.this.listener.onConnectionStatusChanged(false);
                    }
                }
            };
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        }
        Log.d(TAG, "🔍 Surveillance de la connexion démarrée");
    }

    /**
     * Arrête la surveillance de la connexion Internet
     */
    public void stopMonitoring() {
        if (networkCallback != null && connectivityManager != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                Log.d(TAG, "🛑 Surveillance de la connexion arrêtée");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "NetworkCallback déjà désinscrit", e);
            }
            networkCallback = null;
        }
        listener = null;
    }

    /**
     * Vérifie si Internet est disponible à l'instant T
     *
     * @return true si connecté, false sinon
     */
    public boolean isInternetAvailable() {
        if (connectivityManager == null) {
            return false;
        }
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            return false;
        }
        // Vérifier que le réseau a une connexion Internet validée
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * Retourne l'état actuel de la connexion
     *
     * @return true si connecté, false sinon
     */
    public boolean isConnected() {
        return isConnected;
    }
}
