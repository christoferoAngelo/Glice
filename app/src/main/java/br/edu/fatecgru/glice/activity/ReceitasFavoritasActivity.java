package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.ReceitaFavoritaAdapter;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

public class ReceitasFavoritasActivity extends AppCompatActivity
        implements ReceitaFavoritaAdapter.OnReceitaClickListener,
        ReceitaFavoritaAdapter.OnReceitaFavoriteListener {

    private RecyclerView recycler;
    private ReceitaFavoritaAdapter adapter;
    private List<Receita> lista = new ArrayList<>();
    private ReceitaDAO dao;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas_favoritas);

        recycler = findViewById(R.id.recyclerFavoritos);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Adapter ajustado com ambos os listeners
        adapter = new ReceitaFavoritaAdapter(lista, this, this);
        recycler.setAdapter(adapter);

        dao = new ReceitaDAO();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Faça login para ver favoritos!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = user.getUid();
        carregarFavoritos();
    }

    private void carregarFavoritos() {
        dao.getReceitasFavoritas(userId, new ReceitaDAO.BuscarReceitasCallback() {
            @Override
            public void onSuccess(List<Receita> receitas) {
                lista.clear();
                lista.addAll(receitas);

                // Debug: Verificar se as receitas estão marcadas como favoritas
                for (Receita r : receitas) {
                    Log.d("FavoritosActivity", "Receita: " + r.getNome() + " - Favorita: " + r.isFavorita());
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(ReceitasFavoritasActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Clique no item → abre detalhes da receita (na ReceitasActivity)
    @Override
    public void onReceitaClick(Receita receita) {
        Intent i = new Intent(this, ReceitasActivity.class);
        i.putExtra("abrir_receita_id", receita.getDocumentId());
        startActivity(i);
    }

    // Clique no favorito → alterna favorito/desfavorito
    @Override
    public void onReceitaFavoriteClick(Receita receita, int position) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Faça login para favoritar receitas!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // Alterna estado
        boolean novoStatus = !receita.isFavorita();
        receita.setFavorita(novoStatus);
        adapter.notifyItemChanged(position);

        // Persiste no banco principal (opcional, dependendo da sua regra de negócio, mas mantido por segurança)
        dao.atualizarFavorito(
                userId,
                receita.getDocumentId(),
                receita.isFavorita(),
                new ReceitaDAO.UpdateFavoriteCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ReceitasFavoritasActivity.this, receita.getNome() + (novoStatus ? " favoritada!" : " desfavoritada!"), Toast.LENGTH_SHORT).show();
                        // Recarregar favoritos para atualizar a lista (fundamental: remove se desfavoritar)
                        carregarFavoritos();
                    }

                    @Override
                    public void onError(String msg) {
                        // Reverte em caso de erro
                        receita.setFavorita(!novoStatus);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(ReceitasFavoritasActivity.this, "Erro ao salvar favorito: " + msg, Toast.LENGTH_LONG).show();
                    }
                }
        );

        // **Lógica Central para Adicionar/Remover da LISTA DE FAVORITOS DO USUÁRIO**
        if (novoStatus) {
            // Se favoritou (novoStatus é true)
            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(userId)
                    .collection("favoritos")
                    .document(receita.getDocumentId())
                    .set(receita.toMap()); // Adiciona a receita na subcoleção
        } else {
            // Se desfavoritou (novoStatus é false)
            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(userId)
                    .collection("favoritos")
                    .document(receita.getDocumentId())
                    .delete(); // **REMOVE a receita da subcoleção**
        }
    }}