package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.FavoritosAdapter;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

public class ReceitasFavoritasActivity extends AppCompatActivity
        implements FavoritosAdapter.OnFavoritoClickListener {

    private RecyclerView recycler;
    private FavoritosAdapter adapter;
    private List<Receita> lista = new ArrayList<>();
    private ReceitaDAO dao;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas_favoritas);

        recycler = findViewById(R.id.recyclerFavoritos);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FavoritosAdapter(this, lista, this);
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(ReceitasFavoritasActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Clique para abrir a tela de detalhes da receita original
    @Override
    public void onReceitaClick(Receita receita) {
        Intent i = new Intent(this, ReceitasActivity.class);
        i.putExtra("abrir_receita_id", receita.getDocumentId());
        startActivity(i);
    }

    // Remover dos favoritos
    @Override
    public void onRemoverFavorito(Receita receita, int pos) {
        dao.atualizarFavorito(userId, receita.getDocumentId(), false, new ReceitaDAO.UpdateFavoriteCallback() {
            @Override
            public void onSuccess() {
                lista.remove(pos);
                adapter.notifyItemRemoved(pos);
                Toast.makeText(ReceitasFavoritasActivity.this, "Removido dos favoritos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(ReceitasFavoritasActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
