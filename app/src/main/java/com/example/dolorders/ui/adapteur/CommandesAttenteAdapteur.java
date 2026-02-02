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
import com.example.dolorders.objet.Commande;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommandesAttenteAdapteur extends RecyclerView.Adapter<CommandesAttenteAdapteur.ViewHolder> {

    private final List<Commande> commandes;
    private final OnCommandeActionListener listener; // Interface de callback

    // Définition de l'interface pour communiquer avec le Fragment
    public interface OnCommandeActionListener {
        void onEdit(Commande commande);

        void onDelete(Commande commande);
    }

    public CommandesAttenteAdapteur(List<Commande> commandes, OnCommandeActionListener listener) {
        this.commandes = commandes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_commande_attente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Commande cmd = commandes.get(position);

        // Affichage des données (ID, Nom Client, Date)
        holder.tvId.setText(cmd.getId());
        holder.tvNom.setText(cmd.getClient().getNom());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        holder.tvDate.setText(sdf.format(cmd.getDateCommande()));

        // GESTION DU MENU 3 POINTS (Popup)
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);

            // On réutilise le même fichier de menu que pour les clients
            // Assure-toi que res/menu/menu_options_item.xml existe bien
            popup.inflate(R.menu.menu_options_item);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    // On appelle la méthode onEdit du fragment
                    if (listener != null) listener.onEdit(cmd);
                    return true;
                } else if (id == R.id.action_delete) {
                    // On appelle la méthode onDelete du fragment
                    if (listener != null) listener.onDelete(cmd);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return commandes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId;
        TextView tvNom;
        TextView tvDate;
        ImageView btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_id_commande);
            tvNom = itemView.findViewById(R.id.tv_nom_client);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnMore = itemView.findViewById(R.id.btn_more_options);
        }
    }
}