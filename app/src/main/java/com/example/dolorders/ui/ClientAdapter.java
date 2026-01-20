package com.example.dolorders.ui;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolorders.Client;
import com.example.dolorders.R;

import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    // ✅ 1) INTERFACE (callback vers le Fragment)
    public interface OnClientActionListener {
        void onDetails(Client client);
        void onModifier(Client client);
        void onNouvelleCommande(Client client);
    }

    // ✅ 2) CHAMPS MEMBRES
    private final List<Client> clients;
    private final OnClientActionListener listener;

    // ✅ 3) CONSTRUCTEUR
    public ClientAdapter(List<Client> clients, OnClientActionListener listener) {
        this.clients = clients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = clients.get(position);

        holder.txtNom.setText(client.getNom());
        holder.txtTelephone.setText(client.getTelephone());
        holder.txtVille.setText(client.getVille());

        holder.btnActions.setOnClickListener(v -> showActionsMenu(v, client));
    }

    private void showActionsMenu(View anchor, Client client) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_actions_client, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> handleMenuClick(item, client));
        popup.show();
    }

    // ✅ 4) UTILISATION DU listener (là où tu avais l’erreur)
    private boolean handleMenuClick(MenuItem item, Client client) {
        int id = item.getItemId();

        if (id == R.id.action_details) {
            if (listener != null) listener.onDetails(client);
            return true;

        } else if (id == R.id.action_modifier) {
            if (listener != null) listener.onModifier(client);
            return true;

        } else if (id == R.id.action_nouvelle_commande) {
            if (listener != null) listener.onNouvelleCommande(client);
            return true;
        }

        return false;
    }

    @Override
    public int getItemCount() {
        return clients != null ? clients.size() : 0;
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {

        TextView txtNom, txtTelephone, txtVille;
        ImageView btnActions;

        ClientViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNom = itemView.findViewById(R.id.txtNom);
            txtTelephone = itemView.findViewById(R.id.txtTelephone);
            txtVille = itemView.findViewById(R.id.txtVille);
            btnActions = itemView.findViewById(R.id.btnActions);
        }
    }
}
