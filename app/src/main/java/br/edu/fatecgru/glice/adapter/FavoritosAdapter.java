package br.edu.fatecgru.glice.adapter;

import android.content.Context;
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

public class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.ViewHolder> {

    public interface OnFavoritoClickListener {
        void onReceitaClick(Receita receita);
        void onRemoverFavorito(Receita receita, int position);
    }

    private final Context context;
    private final List<Receita> lista;
    private final OnFavoritoClickListener listener;

    public FavoritosAdapter(Context context, List<Receita> lista, OnFavoritoClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Receita r = lista.get(position);

        h.nome.setText(r.getNome());
        Glide.with(context).load(r.getUrlImagem()).into(h.img);

        h.itemView.setOnClickListener(v -> listener.onReceitaClick(r));

        h.btnRemover.setOnClickListener(v ->
                listener.onRemoverFavorito(r, position)
        );
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView nome;
        ImageButton btnRemover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgFavorito);
            nome = itemView.findViewById(R.id.txtNomeFavorito);
            btnRemover = itemView.findViewById(R.id.btnRemoverFavorito);
        }
    }
}
