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
    private OnReceitaFavoriteListener favoriteListener;

    // CONSTRUTOR
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

        // 1. Título
        holder.txtNome.setText(r.getNome());

        // int indiceGlicemico = r.getIndiceGlicemico(); // Opcional: mantém o valor numérico para lógica futura
        String indiceString = String.valueOf(r.getIndiceGlicemico());

        // Como 'indiceGlicemico' é um 'int', ele nunca é null. Usamos diretamente 'indiceString'.
        holder.txtIndice.setText("Índice Glicê: " + indiceString);


        // 2. Fonte
        holder.txtFonte.setText("Fonte: " + r.getFonte());


        // 3. Imagem
        String imageUrl = r.getUrlImagem();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_image_24)
                    .error(R.drawable.baseline_broken_image_24)
                    .into(holder.img);
        } else {
            // Ícone padrão se não houver URL
            holder.img.setImageResource(R.drawable.baseline_image_24);
        }

        // 4. Favoritos
        if (r.isFavorita()) {
            holder.imgFavoriteList.setImageResource(R.drawable.baseline_favorite_24); // Coração Cheio
        } else {
            holder.imgFavoriteList.setImageResource(R.drawable.baseline_favorite_border_24); // Coração Vazio
        }


        // 5. Configura o clique no ícone de favorito
        holder.imgFavoriteList.setOnClickListener(v -> {
            if (favoriteListener != null) {
                favoriteListener.onReceitaFavoriteClick(r, position);
            }
        });

        // 6. CLIQUE NO ITEM COMPLETO para abrir detalhes
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
        ImageView imgFavoriteList;
        TextView txtNome, txtIndice, txtFonte;

        public ReceitaViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.imageView);
            txtNome = itemView.findViewById(R.id.txtNomeReceita);
            txtIndice = itemView.findViewById(R.id.txtIndiceGlice); // Mapeamento do Índice Glicêmico
            txtFonte = itemView.findViewById(R.id.txtFonte);

            imgFavoriteList = itemView.findViewById(R.id.imgFavoriteList);
        }
    }

    // Interfaces de Clique e Favoritos (Sem Alterações)
    public interface OnReceitaClickListener {
        void onReceitaClick(Receita receita);
    }

    public interface OnReceitaFavoriteListener {
        void onReceitaFavoriteClick(Receita receita, int position);
    }

    public void atualizarLista(List<Receita> novaLista) {
        this.receitaList = novaLista;
        notifyDataSetChanged();
    }
}