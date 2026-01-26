package com.example.dolorders.ui.adapteur;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dolorders.R;
import com.example.dolorders.objet.Commande;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommandesAttenteAdapteur extends RecyclerView.Adapter<CommandesAttenteAdapteur.ViewHolder> {

    private final List<Commande> commandes;

    public CommandesAttenteAdapteur(List<Commande> commandes) {
        this.commandes = commandes;
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

        holder.tvId.setText(cmd.getId());
        holder.tvNom.setText(cmd.getClient().getNom());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        holder.tvDate.setText(sdf.format(cmd.getDateCommande()));

        // Gestion du menu 3 points
        holder.btnMore.setOnClickListener(v -> {
            // Ici tu pourras implémenter un PopupMenu (Modifier, Supprimer)
            Toast.makeText(v.getContext(), "Options pour " + cmd.getId(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return commandes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvNom, tvDate;
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