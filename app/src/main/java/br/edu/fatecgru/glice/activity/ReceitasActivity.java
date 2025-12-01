package br.edu.fatecgru.glice.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.ReceitaAdapter;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

// A classe agora implementa as duas interfaces de Listener
public class ReceitasActivity extends AppCompatActivity
        implements ReceitaAdapter.OnReceitaClickListener,
        ReceitaAdapter.OnReceitaFavoriteListener {

    private RecyclerView recycler;
    private ReceitaAdapter adapter;
    private SearchView searchPesquisa;
    private List<Receita> lista = new ArrayList<>();
    private ReceitaDAO receitaDao;

    private ImageView imgPerfil;

    private CardView cardDetalheReceita;
    private ImageView imgDetalheReceita;
    private TextView txtNomeDetalhe;
    private TextView txtIndiceDetalhe;
    private TextView txtFonteDetalhe;
    private TextView txtIngredientes, txtPreparo;

    private FirebaseAuth auth;
    private String currentUserId; // Armazenará o UID do usuário (será null se deslogado)

    private ImageButton btnFecharDetalhe;
    private ImageButton btnFavoriteDetalhe;

    private static Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        // Inicialização do Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Busca de Views
        imgPerfil = findViewById(R.id.imgPerfil);
        cardDetalheReceita = findViewById(R.id.cardDetalheReceita);
        imgDetalheReceita = findViewById(R.id.imgDetalheReceita);
        searchPesquisa = findViewById(R.id.searchPesquisa);
        txtNomeDetalhe = findViewById(R.id.txtNomeDetalhe);
        txtIndiceDetalhe = findViewById(R.id.txtIndiceDetalhe);
        txtFonteDetalhe = findViewById(R.id.txtFonteDetalhe);
        txtIngredientes = findViewById(R.id.txtIngredientes);
        txtPreparo = findViewById(R.id.txtPreparo);
        // Garanta que o ID R.id.btnFavoriteDetalhe existe no seu XML de detalhe!
        btnFavoriteDetalhe = findViewById(R.id.btnFavoriteDetalhe);

        // Configuração do Cloudinary
        Map config = new HashMap();
        config.put("cloud_name", "de4j4ibb6");
        config.put("secure", true);
        MediaManager.init(this, config);

        // Configuração do RecyclerView
        recycler = findViewById(R.id.recyclerReceitas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Instanciação do Adapter com os quatro argumentos
        adapter = new ReceitaAdapter(this, lista, this, this);
        recycler.setAdapter(adapter);

        receitaDao = new ReceitaDAO();

        // A chamada carregarReceitas() foi movida para o onResume()

        imgPerfil.setOnClickListener(v-> {
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
        });

        configurarPesquisa();
    }

    // --- Lógica de Sincronização e Favoritos ---

    @Override
    protected void onResume() {
        super.onResume();

        // 1. ATUALIZA o UID do usuário logado (será null se não estiver logado)
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            currentUserId = null;
        }

        // 2. CHAMA a lógica de carregamento.
        // Isso garante que a lista de favoritos será atualizada se o usuário acabou de logar
        // ou deslogar.
        carregarReceitas(() -> verificarIntentDeAbrirReceita());
    }

    // MÉTODO ATUALIZADO para receber o ID do usuário
    private void carregarReceitas() {
        // O ReceitaDAO agora precisa do ID do usuário para verificar os favoritos.
        receitaDao.getReceitas(currentUserId, new ReceitaDAO.BuscarReceitasCallback() {
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

    private void carregarReceitas(Runnable callback) {
        receitaDao.getReceitas(currentUserId, new ReceitaDAO.BuscarReceitasCallback() {
            @Override
            public void onSuccess(List<Receita> receitas) {
                lista.clear();
                lista.addAll(receitas);
                adapter.notifyDataSetChanged();

                if (callback != null) callback.run();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(ReceitasActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Implementação do OnReceitaClickListener (abre detalhe)
    @Override
    public void onReceitaClick(Receita receita) {
        exibirDetalheReceita(receita);
    }

    // Implementação do OnReceitaFavoriteListener (persiste o favorito)
    @Override
    public void onReceitaFavoriteClick(Receita receita, int position) {

        // 🚨 PASSO 1: VERIFICAÇÃO DE LOGIN
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Faça login para favoritar receitas!", Toast.LENGTH_LONG).show();

            // Redireciona para a tela de Login
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);

            return; // Sai do método
        }

        // PASSO 2: Atualização Visual Imediata
        receita.setFavorita(!receita.isFavorita());
        adapter.notifyItemChanged(position); // Atualiza o ícone na lista

        // PASSO 3: Persistência no Firebase
        if (receita.getDocumentId() != null) {
            receitaDao.atualizarFavorito(
                    currentUserId,
                    receita.getDocumentId(),
                    receita.isFavorita(), // Se está favoritado (true), o DAO adiciona
                    new ReceitaDAO.UpdateFavoriteCallback() {
                        @Override
                        public void onSuccess() {
                            // Não precisa de Toast aqui, pois já foi dado um feedback rápido
                        }

                        @Override
                        public void onError(String msg) {
                            // Se o banco falhar, REVERTE o estado visual para o usuário não ser enganado.
                            receita.setFavorita(!receita.isFavorita());
                            adapter.notifyItemChanged(position);
                            Toast.makeText(ReceitasActivity.this, "Erro ao salvar favorito: " + msg, Toast.LENGTH_LONG).show();
                        }
                    }
            );

            // Feedback imediato
            Toast.makeText(this, receita.getNome() + (receita.isFavorita() ? " favoritada!" : " desfavoritada!"), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro: Receita sem ID de documento.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para popular e mostrar o CardView de Detalhes
    private void exibirDetalheReceita(Receita receita) {
        // Popule os dados
        txtNomeDetalhe.setText(receita.getNome());
        txtIndiceDetalhe.setText("Índice Glicêmico: " + receita.getIndiceGlicemico());
        txtFonteDetalhe.setText("Fonte: " + receita.getFonte());
        List<String> ingredientesList = receita.getIngredientesDetalhe();
        if (ingredientesList != null) {
            // Usa String.join("\n", lista) para colocar cada item da lista em uma nova linha.
            txtIngredientes.setText(String.join("\n", ingredientesList));
        } else {
            txtIngredientes.setText("Ingredientes não disponíveis.");
        }
        txtPreparo.setText(receita.getPreparo());

        Glide.with(this)
                .load(receita.getUrlImagem())
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgDetalheReceita);

        // Configurar o estado inicial do ícone de favorito no detalhe
        atualizarIconeFavoritoDetalhe(receita);

        // Lógica de clique do botão de favorito na tela de Detalhes
        btnFavoriteDetalhe.setOnClickListener(v -> {

            // Reutiliza a mesma lógica de persistência e verificação de login do clique na lista
            // Note que o onReceitaFavoriteClick exige a posição do item na lista,
            // mas aqui precisamos apenas da receita. Vamos chamar a lógica de persistência
            // diretamente para simplificar.

            // 🚨 VERIFICAÇÃO DE LOGIN
            if (currentUserId == null || currentUserId.isEmpty()) {
                Toast.makeText(this, "Faça login para favoritar receitas!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            // 1. Inverte o estado de favorito
            receita.setFavorita(!receita.isFavorita());

            // 2. Atualiza o ícone de favorito do detalhe
            atualizarIconeFavoritoDetalhe(receita);

            // 3. Persistência e atualização da lista
            int index = lista.indexOf(receita); // Encontra a posição do item na lista
            if(index != -1) {
                // Notifica a lista para atualizar o coração na tela principal
                adapter.notifyItemChanged(index);
            }

            // Lógica de Persistência (duplicada do onReceitaFavoriteClick para simplificar)
            if (receita.getDocumentId() != null) {
                receitaDao.atualizarFavorito(
                        currentUserId,
                        receita.getDocumentId(),
                        receita.isFavorita(),
                        new ReceitaDAO.UpdateFavoriteCallback() {
                            @Override
                            public void onSuccess() {}
                            @Override
                            public void onError(String msg) {
                                // Reverter o estado visual
                                receita.setFavorita(!receita.isFavorita());
                                if(index != -1) adapter.notifyItemChanged(index);
                                atualizarIconeFavoritoDetalhe(receita); // Reverte no detalhe também
                                Toast.makeText(ReceitasActivity.this, "Erro ao salvar favorito: " + msg, Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }

            Toast.makeText(this, receita.getNome() + (receita.isFavorita() ? " favoritada!" : " desfavoritada!"), Toast.LENGTH_SHORT).show();
        });


        // Muda a visibilidade para VISIBLE
        cardDetalheReceita.setVisibility(View.VISIBLE);
    }

    // Método auxiliar para atualizar o ícone no Detalhe
    private void atualizarIconeFavoritoDetalhe(Receita receita) {
        if (receita.isFavorita()) {
            // Garanta que você tem R.drawable.baseline_favorite_24 (coração cheio)
            btnFavoriteDetalhe.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            // Garanta que você tem R.drawable.baseline_favorite_border_24 (coração vazio)
            btnFavoriteDetalhe.setImageResource(R.drawable.baseline_favorite_border_24);
        }
    }

    private void verificarIntentDeAbrirReceita() {
        String id = getIntent().getStringExtra("abrir_receita_id");
        if (id == null) return;

        // evitar reabrir ao voltar
        getIntent().removeExtra("abrir_receita_id");

        for (Receita r : lista) {
            if (r.getDocumentId().equals(id)) {
                exibirDetalheReceita(r);
                break;
            }
        }
    }

    // Método para fechar o CardView de Detalhes
    public void fecharDetalhe(View view) {
        cardDetalheReceita.setVisibility(View.GONE);
    }

    private void configurarPesquisa() {
        searchPesquisa.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarReceitas(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarReceitas(newText);
                return true;
            }
        });
    }

    private void filtrarReceitas(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            // Se o texto estiver vazio, restaura a lista completa
            adapter.atualizarLista(lista);
            return;
        }

        String pesquisa = texto.toLowerCase().trim();

        List<Receita> listaFiltrada = new ArrayList<>();

        for (Receita r : lista) {
            if (r.getNome() != null && r.getNome().toLowerCase().contains(pesquisa)) {
                listaFiltrada.add(r);
            }
        }

        adapter.atualizarLista(listaFiltrada);
    }

}