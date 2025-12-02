package br.edu.fatecgru.glice.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {

    private final List<String> ingredientes;

    public IngredienteAdapter(List<String> ingredientes) {
        this.ingredientes = ingredientes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtIngrediente.setText(ingredientes.get(position));
    }

    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtIngrediente;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtIngrediente = itemView.findViewById(android.R.id.text1);
        }
    }
}
