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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.ReceitaAdapter;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

// 1. CORREÇÃO: Implementar o novo Listener de Favorito
public class ReceitasActivity extends AppCompatActivity
        implements ReceitaAdapter.OnReceitaClickListener,
        ReceitaAdapter.OnReceitaFavoriteListener {

    private RecyclerView recycler;
    private ReceitaAdapter adapter;
    private List<Receita> lista = new ArrayList<>();
    private ReceitaDAO receitaDao;

    private ImageView imgPerfil;

    private CardView cardDetalheReceita;
    private ImageView imgDetalheReceita;
    private TextView txtNomeDetalhe;
    private TextView txtIndiceDetalhe;
    private TextView txtFonteDetalhe;
    private TextView txtIngredientes, txtPreparo;

    private ImageButton btnFecharDetalhe;
    private ImageButton btnFavoriteDetalhe; // NOVO: Botão de favorito no detalhe

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
        btnFavoriteDetalhe = findViewById(R.id.btnFavoriteDetalhe); // Busca do novo botão de favorito no detalhe


        Map config = new HashMap();
        config.put("cloud_name", "de4j4ibb6");
        config.put("secure", true);
        MediaManager.init(this, config);



        recycler = findViewById(R.id.recyclerReceitas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // 2. CORREÇÃO: Passar o 4º argumento (this, que agora é OnReceitaFavoriteListener)
        adapter = new ReceitaAdapter(this, lista, this, this);
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


    // 3. Implementação do clique na receita (abre detalhe)
    @Override
    public void onReceitaClick(Receita receita) {
        exibirDetalheReceita(receita);
    }

    // 4. Implementação do clique no ícone de FAVORITO (na lista)
    @Override
    public void onReceitaFavoriteClick(Receita receita, int position) {
        // Inverte o estado
        receita.setFavorita(!receita.isFavorita());

        // Atualiza a visualização APENAS do item modificado
        adapter.notifyItemChanged(position);

        Toast.makeText(this, receita.getNome() + (receita.isFavorita() ? " favoritada!" : " desfavoritada!"), Toast.LENGTH_SHORT).show();
        // TODO: Adicionar aqui a lógica de persistência (salvar no banco/prefs)
    }


    // Método para popular e mostrar o CardView de Detalhes
    private void exibirDetalheReceita(Receita receita) {
        // Popule os dados
        txtNomeDetalhe.setText(receita.getNome());
        txtIndiceDetalhe.setText("Índice Glicêmico: " + receita.getIndiceGlicemico());
        txtFonteDetalhe.setText("Fonte: " + receita.getFonte());
        txtIngredientes.setText(receita.getIngredientes());
        txtPreparo.setText(receita.getPreparo());

        Glide.with(this)
                .load(receita.getUrlImagem())
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgDetalheReceita);

        // NOVO: Configurar o estado inicial do ícone de favorito no detalhe
        atualizarIconeFavoritoDetalhe(receita);

        // NOVO: Lógica de clique do botão de favorito na tela de Detalhes
        btnFavoriteDetalhe.setOnClickListener(v -> {
            // Inverte o estado de favorito
            receita.setFavorita(!receita.isFavorita());

            // Atualiza o ícone de favorito do detalhe
            atualizarIconeFavoritoDetalhe(receita);

            // É importante notificar o adapter da lista para que o ícone na lista também atualize se o detalhe for fechado
            int index = lista.indexOf(receita);
            if(index != -1) {
                adapter.notifyItemChanged(index);
            }

            Toast.makeText(this, receita.getNome() + (receita.isFavorita() ? " favoritada!" : " desfavoritada!"), Toast.LENGTH_SHORT).show();
            // TODO: Adicionar aqui a lógica de persistência (salvar no banco/prefs)
        });


        // Mude a visibilidade para VISIBLE
        cardDetalheReceita.setVisibility(View.VISIBLE);
    }

    // NOVO: Método auxiliar para atualizar o ícone no Detalhe
    private void atualizarIconeFavoritoDetalhe(Receita receita) {
        if (receita.isFavorita()) {
            btnFavoriteDetalhe.setImageResource(R.drawable.baseline_favorite_24); // Coração preenchido
        } else {
            btnFavoriteDetalhe.setImageResource(R.drawable.baseline_favorite_border_24); // Coração borda
        }
    }


    // Método para fechar o CardView de Detalhes (chamado pelo onClick no XML)
    public void fecharDetalhe(View view) {
        cardDetalheReceita.setVisibility(View.GONE);
    }
}