package br.edu.fatecgru.glice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.model.Receita;

public class ReceitaAdapter extends RecyclerView.Adapter<ReceitaAdapter.ReceitaViewHolder> {

    private List<Receita> receitaList;
    private Context context;
    private OnReceitaClickListener clickListener;
    private OnReceitaFavoriteListener favoriteListener; // NOVO: Listener para o clique no favorito

    // CONSTRUTOR ATUALIZADO para 4 argumentos
    public ReceitaAdapter(Context context, List<Receita> receitaList,
                          OnReceitaClickListener listener,
                          OnReceitaFavoriteListener favoriteListener) {
        this.context = context;
        this.receitaList = receitaList;
        this.clickListener = listener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public ReceitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receita, parent, false);
        return new ReceitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceitaViewHolder holder, int position) {
        Receita r = receitaList.get(position);

        // CORREÇÃO 1: Usando getters corretos e consistentes
        holder.txtNome.setText(r.getNome());
        holder.txtIndice.setText("Índice: " + r.getIndiceGlicemico()); // CORRIGIDO: usa getIndiceGlicemico()
        holder.txtFonte.setText("Fonte: " + r.getFonte());

        // CORREÇÃO 2: Usando o getter correto para URL da imagem
        String imageUrl = r.getUrlImagem();

        // Se tiver imagem, usa Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.img);
        } else {
            holder.img.setImageResource(R.drawable.ic_launcher_background); // alterar  ícone padrão
        }

        // NOVO: Lógica visual do ícone de favorito
        if (r.isFavorita()) {
            holder.imgFavoriteList.setImageResource(R.drawable.baseline_favorite_24); // Coração Cheio
        } else {
            holder.imgFavoriteList.setImageResource(R.drawable.baseline_favorite_border_24); // Coração Vazio
        }


        // NOVO: Configura o clique no ícone de favorito
        holder.imgFavoriteList.setOnClickListener(v -> {
            if (favoriteListener != null) {
                // Passa a receita e a posição para a Activity
                favoriteListener.onReceitaFavoriteClick(r, position);
            }
        });

        // Clique no item (área não-favorito) para abrir detalhes
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReceitaClick(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return receitaList.size();
    }

    public static class ReceitaViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        ImageView imgFavoriteList; // NOVO: Ícone de Favorito
        TextView txtNome, txtIndice, txtFonte;

        public ReceitaViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.imageView);
            txtNome = itemView.findViewById(R.id.txtNomeReceita);
            txtIndice = itemView.findViewById(R.id.txtIndiceGlice);
            txtFonte = itemView.findViewById(R.id.txtFonte);

            // NOVO: Encontrar a view do ícone de favorito (ID do XML item_receita)
            imgFavoriteList = itemView.findViewById(R.id.imgFavoriteList);
        }
    }

    public interface OnReceitaClickListener {
        void onReceitaClick(Receita receita);
    }

    // NOVA INTERFACE: Para tratar o clique no botão de Favorito
    public interface OnReceitaFavoriteListener {
        void onReceitaFavoriteClick(Receita receita, int position);
    }

    public void atualizarLista(List<Receita> novaLista) {
        this.receitaList = novaLista;
        notifyDataSetChanged();
    }
}