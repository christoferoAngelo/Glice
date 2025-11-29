package br.edu.fatecgru.glice.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;

import java.security.AlgorithmParameterGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.ReceitaAdapter;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

public class ReceitasActivity extends AppCompatActivity implements ReceitaAdapter.OnReceitaClickListener {

    private RecyclerView recycler;
    private ReceitaAdapter adapter;
    private List<Receita> lista = new ArrayList<>();
    private ReceitaDAO receitaDao;

    private ImageView imgPerfil;

    private CardView cardDetalheReceita; // Novo
    private ImageView imgDetalheReceita; // Novo
    private TextView txtNomeDetalhe;     // Novo
    private TextView txtIndiceDetalhe;   // Novo
    private TextView txtFonteDetalhe;    // Novo
    private TextView txtIngredientes, txtPreparo;

    private ImageButton btnFecharDetalhe; // Novo
    private static Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        //coisos
        imgPerfil = findViewById(R.id.imgPerfil);
        cardDetalheReceita = findViewById(R.id.cardDetalheReceita);
        imgDetalheReceita = findViewById(R.id.imgDetalheReceita);
        txtNomeDetalhe = findViewById(R.id.txtNomeDetalhe);
        txtIndiceDetalhe = findViewById(R.id.txtIndiceDetalhe);
        txtFonteDetalhe = findViewById(R.id.txtFonteDetalhe);
        txtIngredientes = findViewById(R.id.txtIngredientes);
        txtPreparo = findViewById(R.id.txtPreparo);



        Map config = new HashMap();
        config.put("cloud_name", "de4j4ibb6");
        config.put("secure", true);
        MediaManager.init(this, config);



        recycler = findViewById(R.id.recyclerReceitas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReceitaAdapter((Context) this, lista, (ReceitaAdapter.OnReceitaClickListener) this);
        recycler.setAdapter(adapter);

        receitaDao = new ReceitaDAO();

        carregarReceitas();

        imgPerfil.setOnClickListener(v-> {
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
        });

    }


    private void carregarReceitas() {
        receitaDao.getReceitas(new ReceitaDAO.BuscarReceitasCallback() {
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




    // 1. Implemente o método da interface (para abrir o detalhe)

    public void onReceitaClick(Receita receita) {
        exibirDetalheReceita(receita);
    }

    // 2. Método para popular e mostrar o CardView de Detalhes
    private void exibirDetalheReceita(Receita receita) {
        // 2.1. Popule os dados
        txtNomeDetalhe.setText(receita.getNome());
        txtIndiceDetalhe.setText("Índice Glicêmico: " + receita.getIndiceGlicemico()); // Supondo getIndiceGlicemico()
        txtFonteDetalhe.setText("Fonte: " + receita.getFonte());
        txtIngredientes.setText(receita.getIngredientes());
        txtPreparo.setText(receita.getPreparo());

        Glide.with(this)
            .load(receita.getUrlImagem())
            .placeholder(R.drawable.ic_launcher_background)
            .into(imgDetalheReceita);


        // 2.3. Mude a visibilidade para VISIBLE
        cardDetalheReceita.setVisibility(View.VISIBLE);
    }

    // 3. Método para fechar o CardView de Detalhes (chamado pelo onClick no XML)
    public void fecharDetalhe(View view) {
        cardDetalheReceita.setVisibility(View.GONE);
    }
}
