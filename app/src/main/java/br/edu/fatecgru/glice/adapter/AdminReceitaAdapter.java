package br.edu.fatecgru.glice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Para os ícones de ação
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.model.Receita;

public class AdminReceitaAdapter extends RecyclerView.Adapter<AdminReceitaAdapter.AdminReceitaViewHolder> {

    private final List<Receita> receitaList;
    private final Context context;
    private final OnAdminActionListener adminListener;

    // Construtor: Recebe o Listener de Admin
    public AdminReceitaAdapter(Context context, List<Receita> receitaList, OnAdminActionListener listener) {
        this.context = context;
        this.receitaList = receitaList;
        this.adminListener = listener;
    }

    @NonNull
    @Override
    public AdminReceitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usa o NOVO layout específico para o administrador
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receita_admin, parent, false);
        return new AdminReceitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminReceitaViewHolder holder, int position) {
        Receita r = receitaList.get(position);

        // --- EXIBIÇÃO DE DADOS BÁSICOS (Similar ao adapter de usuário) ---
        holder.txtNome.setText(r.getNome());
        holder.txtIndice.setText("Glicê: " + r.getIndiceGlicemico());
        holder.txtFonte.setText("Fonte: " + r.getFonte());

        String imageUrl = r.getUrlImagem();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder temporário
                    .into(holder.img);
        } else {
            holder.img.setImageResource(R.drawable.ic_launcher_background); // Ícone padrão sem foto
        }

        // --- LISTENERS DE AÇÃO DE ADMIN ---

        // 1. Clique no botão de EDIÇÃO
        holder.btnEdit.setOnClickListener(v -> {
            if (adminListener != null) {
                adminListener.onEditClick(r);
            }
        });

        // 2. Clique no botão de EXCLUSÃO
        holder.btnDelete.setOnClickListener(v -> {
            if (adminListener != null) {
                adminListener.onDeleteClick(r);
            }
        });

        // Clique no item (para visualizar detalhes se necessário, ou apenas para evitar cliques vazios)
        holder.itemView.setOnClickListener(v -> {
            // Opcional: Aqui você pode chamar o onEditClick ou abrir a visualização
            if (adminListener != null) {
                adminListener.onEditClick(r); // Usar clique em qualquer lugar para editar
            }
        });
    }

    @Override
    public int getItemCount() {
        return receitaList.size();
    }

    // --- VIEWHOLDER ---
    public static class AdminReceitaViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView txtNome, txtIndice, txtFonte;
        ImageButton btnEdit; // Botão de edição
        ImageButton btnDelete; // Botão de exclusão

        public AdminReceitaViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.imageView_admin); // ID de Imagem do item admin
            txtNome = itemView.findViewById(R.id.txtNomeReceita_admin);
            txtIndice = itemView.findViewById(R.id.txtIndiceGlice_admin);
            txtFonte = itemView.findViewById(R.id.txtFonte_admin);

            // IDs dos botões que devem estar em item_receita_admin.xml
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    // --- INTERFACE DE AÇÃO PARA A ACTIVITY ---
    public interface OnAdminActionListener {
        void onEditClick(Receita receita);
        void onDeleteClick(Receita receita);
    }
}