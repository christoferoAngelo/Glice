package br.edu.fatecgru.glice.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.model.ReceitaPessoal;
import br.edu.fatecgru.glice.viewmodel.ReceitaLocalViewModel;

/**
 * Adapter para exibir a lista de Receitas Locais no RecyclerView.
 */
public class ReceitaLocalAdapter extends RecyclerView.Adapter<ReceitaLocalAdapter.ReceitaViewHolder> {

    private List<ReceitaPessoal> receitas;
    private OnItemClickListener listener;

    // Interface para manipular cliques (para abrir detalhes ou excluir)
    public interface OnItemClickListener {
        void onItemClick(ReceitaPessoal receita);
        void onDeleteClick(ReceitaPessoal receita);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ReceitaLocalAdapter(List<ReceitaPessoal> receitas) {
        this.receitas = receitas;
    }

    // Usado pelo ViewModel para atualizar a lista reativamente
    public void setReceitas(List<ReceitaPessoal> novasReceitas) {
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
        ReceitaPessoal currentReceita = receitas.get(position);

        holder.txtTituloReceita.setText(currentReceita.getTitulo());

        // Exibe um resumo do conteúdo
        String resumo = (currentReceita.getIngredientes() != null ? currentReceita.getIngredientes() + "\n" : "") + currentReceita.getPreparo();
        holder.txtDescricaoReceita.setText(resumo.length() > 80 ? resumo.substring(0, 80) + "..." : resumo);

        // Carrega a imagem se o URL estiver disponível
        if (currentReceita.getImageUrl() != null && !currentReceita.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentReceita.getImageUrl())
                    .placeholder(R.drawable.ic_default_recipe) // Placeholder (assumindo que você tem um ícone padrão)
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
        final ImageButton btnExcluir;

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

            // Listener de clique no botão de excluir
            btnExcluir.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(receitas.get(position));
                }
            });
        }
    }
}