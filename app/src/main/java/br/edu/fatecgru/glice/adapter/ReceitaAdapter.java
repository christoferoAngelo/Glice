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

    public ReceitaAdapter(Context context, List<Receita> receitaList, OnReceitaClickListener listener) {
        this.context = context;
        this.receitaList = receitaList;
        this.clickListener = listener; // Inicialize o listener
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

        holder.txtNome.setText(r.nome);
        holder.txtIndice.setText("Índice: " + r.indice);
        holder.txtFonte.setText("Fonte: " + r.fonte);

        // Se tiver imagem, usa Glide
        if (r.foto_url != null && !r.foto_url.isEmpty()) {
            Glide.with(context)
                    .load(r.foto_url)
                    .into(holder.img);
        } else {
            holder.img.setImageResource(R.drawable.ic_launcher_background); // alterar  ícone padrão
        }

        holder.itemView.setOnClickListener(v -> {
            // Quando um item é clicado, chame o método da interface
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
        TextView txtNome, txtIndice, txtFonte;

        public ReceitaViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.imageView);
            txtNome = itemView.findViewById(R.id.txtNomeReceita);
            txtIndice = itemView.findViewById(R.id.txtIndiceGlice);
            txtFonte = itemView.findViewById(R.id.txtFonte);
        }
    }

    public interface OnReceitaClickListener {
        void onReceitaClick(Receita receita);
    }

    private OnReceitaClickListener clickListener;
}
