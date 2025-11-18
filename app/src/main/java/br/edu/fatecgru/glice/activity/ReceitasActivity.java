package br.edu.fatecgru.glice.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.ReceitaAdapter;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

public class ReceitasActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ReceitaAdapter adapter;
    private List<Receita> lista = new ArrayList<>();
    private ReceitaDAO receitaDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        recycler = findViewById(R.id.recyclerReceitas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReceitaAdapter(this, lista);
        recycler.setAdapter(adapter);

        receitaDao = new ReceitaDAO();

        carregarReceitas();
    }

    private void carregarReceitas() {
        receitaDao.getReceitas(new ReceitaDAO.ReceitasCallback() {
            @Override
            public void onSuccess(List<Receita> receitas) {
                lista.clear();
                lista.addAll(receitas);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(ReceitasActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
