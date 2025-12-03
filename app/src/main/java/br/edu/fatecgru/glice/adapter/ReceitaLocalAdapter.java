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

import java.io.File;
import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.model.ReceitaPessoal;

public class ReceitaLocalAdapter extends RecyclerView.Adapter<ReceitaLocalAdapter.ReceitaViewHolder> {

    private List<ReceitaPessoal> receitas;
    private OnItemClickListener listener;

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
        ReceitaPessoal receita = receitas.get(position);

        // Nome/Título
        holder.txtTituloReceita.setText(receita.getTitulo());

        // Fonte ou "Sem fonte"
        holder.txtFonteReceita.setText(
                (receita.getFonte() == null || receita.getFonte().isEmpty()) ?
                        "Fonte: Sem fonte" : "Fonte: " + receita.getFonte()
        );

        // Imagem
        if (receita.getImageUrl() != null && !receita.getImageUrl().isEmpty()) {
            File arquivo = new File(receita.getImageUrl());
            Glide.with(holder.itemView.getContext())
                    .load(arquivo.exists() ? arquivo : R.drawable.ic_default_recipe)
                    .placeholder(R.drawable.ic_default_recipe)
                    .error(R.drawable.ic_default_recipe)
                    .into(holder.imgReceitaThumbnail);
        } else {
            holder.imgReceitaThumbnail.setImageResource(R.drawable.ic_default_recipe);
        }
    }

    @Override
    public int getItemCount() {
        return receitas.size();
    }

    class ReceitaViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgReceitaThumbnail;
        final TextView txtTituloReceita;
        final TextView txtFonteReceita;
        final ImageButton btnExcluir;

        public ReceitaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReceitaThumbnail = itemView.findViewById(R.id.imgReceitaThumbnail);
            txtTituloReceita = itemView.findViewById(R.id.txtTituloReceita);
            txtFonteReceita = itemView.findViewById(R.id.txtFonteReceita);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(receitas.get(position));
                }
            });

            btnExcluir.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(receitas.get(position));
                }
            });
        }
    }
}
