package br.edu.fatecgru.glice.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.model.Receita;

public class ReceitaFavoritaAdapter extends RecyclerView.Adapter<ReceitaFavoritaAdapter.MyViewHolder> {

    private List<Receita> lista;
    private OnReceitaClickListener clickListener;
    private OnReceitaFavoriteListener favoriteListener;

    // Construtor ajustado: favoriteListener pode ser null (opcional)
    public ReceitaFavoritaAdapter(List<Receita> lista, OnReceitaClickListener clickListener, OnReceitaFavoriteListener favoriteListener) {
        this.lista = lista;
        this.clickListener = clickListener;
        this.favoriteListener = favoriteListener;
    }

    // Interface de clique no item
    public interface OnReceitaClickListener {
        void onReceitaClick(Receita receita);
    }

    // Interface de clique no favorito
    public interface OnReceitaFavoriteListener {
        void onReceitaFavoriteClick(Receita receita, int position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receita, parent, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Receita receita = lista.get(position);

        holder.txtNome.setText(receita.getNome());
        holder.txtGlice.setText("Índice Glicê: " + receita.getIndiceGlicemico());
        holder.txtFonte.setText("Fonte: " + receita.getFonte());

        Glide.with(holder.itemView.getContext())
                .load(receita.getUrlImagem())
                .placeholder(R.drawable.recipes)
                .into(holder.imgThumbnail);

        // Atualizar ícone do favorito na lista
        atualizarIconeFavorito(holder, receita);

        // Clique no item → abrir detalhes
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onReceitaClick(receita);
        });

        // Clique no coração → favoritar/desfavoritar (apenas se favoriteListener não for null)
        holder.imgFavorite.setOnClickListener(v -> {
            if (favoriteListener != null) favoriteListener.onReceitaFavoriteClick(receita, position);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // Método auxiliar para atualizar ícone do favorito
    private void atualizarIconeFavorito(MyViewHolder holder, Receita receita) {
        if (receita.isFavorita()) {
            holder.imgFavorite.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            holder.imgFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtNome, txtGlice, txtFonte;
        ImageView imgThumbnail, imgFavorite;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeReceita);
            txtGlice = itemView.findViewById(R.id.txtIndiceGlice);
            txtFonte = itemView.findViewById(R.id.txtFonte);

            imgThumbnail = itemView.findViewById(R.id.imgReceitaThumbnail);
            imgFavorite = itemView.findViewById(R.id.imgFavoriteList);
        }
    }
}