package br.edu.fatecgru.glice.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.model.Receita;

/**
 * Adapter para exibir a lista de Receitas Favoritas do Firestore no RecyclerView.
 * Reutiliza o layout item_receita_local, mas usa o modelo Receita.
 */
public class ReceitaFavoritaAdapter extends RecyclerView.Adapter<ReceitaFavoritaAdapter.ReceitaViewHolder> {

    private List<Receita> receitas;
    private OnItemClickListener listener;

    // Interface para manipular cliques (para abrir detalhes ou excluir)
    public interface OnItemClickListener {
        void onItemClick(Receita receita);
        void onDeleteClick(Receita receita); // Para remover dos favoritos
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ReceitaFavoritaAdapter(List<Receita> receitas) {
        this.receitas = receitas;
    }

    // Usado pelo ViewModel para atualizar a lista reativamente
    public void setReceitas(List<Receita> novasReceitas) {
        this.receitas = novasReceitas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReceitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receita_local, parent, false);
        return new ReceitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceitaViewHolder holder, int position) {
        Receita currentReceita = receitas.get(position);

        holder.txtTituloReceita.setText(currentReceita.getNome());

        // Exibe o resumo da receita (usando o campo 'resumo' da model Receita)
        holder.txtDescricaoReceita.setText(currentReceita.getResumo());

        // Carrega a imagem se o URL estiver disponível (usando o campo 'foto_url' da model Receita)
        if (currentReceita.getUrlImagem() != null && !currentReceita.getUrlImagem().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentReceita.getUrlImagem())
                    .placeholder(R.drawable.ic_default_recipe)
                    .error(R.drawable.ic_default_recipe)
                    .into(holder.imgReceitaThumbnail);
        } else {
            holder.imgReceitaThumbnail.setImageResource(R.drawable.ic_default_recipe); // Ícone padrão
        }
    }

    @Override
    public int getItemCount() {
        return receitas.size();
    }

    // ViewHolder interno
    class ReceitaViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgReceitaThumbnail;
        final TextView txtTituloReceita;
        final TextView txtDescricaoReceita;
        final ImageButton btnExcluir; // Será usado para "remover dos favoritos"

        public ReceitaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReceitaThumbnail = itemView.findViewById(R.id.imgReceitaThumbnail);
            txtTituloReceita = itemView.findViewById(R.id.txtTituloReceita);
            txtDescricaoReceita = itemView.findViewById(R.id.txtDescricaoReceita);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);

            // Listener de clique no item completo
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(receitas.get(position));
                }
            });

            // Listener de clique no botão de excluir (remover dos favoritos)
            btnExcluir.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    // Chama a interface do Fragment
                    listener.onDeleteClick(receitas.get(position));
                }
            });
        }
    }
}