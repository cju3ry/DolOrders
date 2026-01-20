package com.example.dolorders.ui;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolorders.Client;
import com.example.dolorders.R;

import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private final List<Client> clients;

    public ClientAdapter(List<Client> clients) {
        this.clients = clients;
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

        popup.setOnMenuItemClickListener(item -> handleMenuClick(item, anchor, client));

        popup.show();
    }

    private boolean handleMenuClick(MenuItem item, View anchor, Client client) {
        int id = item.getItemId();

        if (id == R.id.action_details) {
            // TODO: appeler la navigation / ouvrir écran détails
            Toast.makeText(anchor.getContext(),
                    "Détails: " + client.getNom(),
                    Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_modifier) {
            // TODO: ouvrir écran modification
            Toast.makeText(anchor.getContext(),
                    "Modifier: " + client.getNom(),
                    Toast.LENGTH_SHORT).show();
            return true;

        } else if (id == R.id.action_nouvelle_commande) {
            // TODO: ouvrir écran nouvelle commande
            Toast.makeText(anchor.getContext(),
                    "Nouvelle commande: " + client.getNom(),
                    Toast.LENGTH_SHORT).show();
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
