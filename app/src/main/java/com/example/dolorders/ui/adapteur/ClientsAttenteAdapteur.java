package com.example.dolorders.ui.adapteur;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolorders.R;
import com.example.dolorders.objet.Client;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ClientsAttenteAdapteur extends RecyclerView.Adapter<ClientsAttenteAdapteur.ViewHolder> {

    private final List<Client> clients;
    private final OnClientActionListener listener; // Interface de callback

    // Interface pour communiquer avec le Fragment
    public interface OnClientActionListener {
        void onEdit(Client client);

        void onDelete(Client client);
    }

    public ClientsAttenteAdapteur(List<Client> clients, OnClientActionListener listener) {
        this.clients = clients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_attente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Client client = clients.get(position);

        holder.tvNom.setText(client.getNom());
        holder.tvEmail.setText(client.getAdresseMail());
        holder.tvVille.setText(client.getVille());

        if (client.getDateSaisie() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.FRANCE);
            holder.tvDate.setText(sdf.format(client.getDateSaisie()));
        } else {
            holder.tvDate.setText("-");
        }

        // GESTION DU MENU 3 POINTS
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.menu_options_item); // On charge le menu XML créé

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    listener.onEdit(client); // On prévient le fragment qu'on veut modifier
                    return true;
                } else if (id == R.id.action_delete) {
                    listener.onDelete(client); // On prévient le fragment qu'on veut supprimer
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom;

        TextView tvEmail;

        TextView tvVille;

        TextView tvDate;
        ImageView btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tv_nom_client);
            tvEmail = itemView.findViewById(R.id.tv_email_client);
            tvVille = itemView.findViewById(R.id.tv_ville_client);
            tvDate = itemView.findViewById(R.id.tv_date_client);
            btnMore = itemView.findViewById(R.id.btn_more_options);
        }
    }
}