package br.edu.fatecgru.glice.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.AdminReceitaAdapter; // NOVO ADAPTER
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

// A atividade implementa o Listener do novo Adapter
public class AdminListActivity extends AppCompatActivity implements AdminReceitaAdapter.OnAdminActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddReceita;

    private AdminReceitaAdapter adapter;
    private List<Receita> receitaList = new ArrayList<>();
    private ReceitaDAO receitaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        // 1. Inicialização
        recyclerView = findViewById(R.id.recycler_admin_receitas);
        progressBar = findViewById(R.id.progress_bar_admin);
        fabAddReceita = findViewById(R.id.fab_add_receita);
        receitaDAO = new ReceitaDAO(); // Assumindo que você tem uma instância de ReceitaDAO

        // 2. Configurar RecyclerView e Adapter
        adapter = new AdminReceitaAdapter(this, receitaList, this); // 'this' é o OnAdminActionListener
        recyclerView.setAdapter(adapter);

        // 3. Listener do FAB (leva para a tela de Criação)
        fabAddReceita.setOnClickListener(v -> {
            // Abre a tela de Upload/Edição para criar uma nova receita
            startActivity(new Intent(AdminListActivity.this, UploadReceitaActivity.class));
        });

        // 4. Carregar Dados
        carregarReceitas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega os dados sempre que a Activity retornar (após Edição/Exclusão)
        carregarReceitas();
    }

    private void carregarReceitas() {
        progressBar.setVisibility(View.VISIBLE);

        // O ReceitaDAO precisa de um método que busca todas as receitas
        receitaDAO.buscarTodasReceitas(new ReceitaDAO.BuscarReceitasCallback() {
            @Override
            public void onSuccess(List<Receita> receitas) { // <-- CORRIGIDO!
                // Sucesso na busca
                receitaList.clear();
                receitaList.addAll(receitas);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                // Erro na busca
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminListActivity.this, "Erro ao carregar receitas: " + error, Toast.LENGTH_LONG).show();
                Log.e("AdminListActivity", "Erro Firestore: " + error);
            }
        });
    }
    // --- IMPLEMENTAÇÃO DOS LISTENERS DE ADMIN (AdminReceitaAdapter.OnAdminActionListener) ---

    @Override
    public void onEditClick(Receita receita) {
        // Ação de EDIÇÃO
        Toast.makeText(this, "Editando: " + receita.getNome(), Toast.LENGTH_SHORT).show();

        // 1. Abre a tela de Upload/Edição
        Intent intent = new Intent(this, UploadReceitaActivity.class);
        // 2. Passa o objeto Receita completo (necessário o ID do Firestore no objeto Receita)
        intent.putExtra("RECEITA_PARA_EDITAR", receita);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Receita receita) {
        // Ação de EXCLUSÃO
        Toast.makeText(this, "Excluindo: " + receita.getNome(), Toast.LENGTH_SHORT).show();

        // Implementação real da exclusão (precisa do ID do Firestore e de um método no ReceitaDAO)
        // Implementar diálogo de confirmação ANTES de executar a exclusão
        confirmarEexcluirReceita(receita);
    }

    private void confirmarEexcluirReceita(Receita receita) {
        // Lógica de exclusão aqui. Exemplo:
        // 1. Obter o ID do documento da Receita (Se a sua classe Receita tiver o campo ID)
        String receitaId = receita.getDocumentId(); // Ex: 'receita.getId()'

        if (receitaId != null) {
            receitaDAO.excluirReceita(receitaId, new ReceitaDAO.ExcluirReceitaCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AdminListActivity.this, receita.getNome() + " excluída com sucesso!", Toast.LENGTH_SHORT).show();
                    carregarReceitas(); // Recarrega a lista
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(AdminListActivity.this, "Falha ao excluir: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "ID da receita não encontrado.", Toast.LENGTH_SHORT).show();
        }
    }
}