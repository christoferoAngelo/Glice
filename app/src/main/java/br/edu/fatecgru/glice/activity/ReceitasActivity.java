package br.edu.fatecgru.glice.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

    // Views do Detalhe
    private CardView cardDetalheReceita;
    private ImageView imgDetalheReceita;
    private TextView txtNomeDetalhe;
    private TextView txtIndiceDetalhe;
    private TextView txtFonteDetalhe;
    private TextView txtIngredientes, txtPreparo;

    private TextView txtResumoDetalhe;
    private TextView txtTempoDetalhe;
    private TextView txtJustificativaDetalhe;
    private TextView txtSubstituicoesDetalhe;
    private TextView txtLinkDetalhe;

    private FirebaseAuth auth;
    private String currentUserId;

    private ImageButton btnFecharDetalhe;
    private ImageButton btnFavoriteDetalhe;
    private ImageButton btnInfoGlice;

    private static Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        // Inicialização do Firebase Auth
        auth = FirebaseAuth.getInstance();

        // --- Busca de Views da TELA PRINCIPAL ---
        imgPerfil = findViewById(R.id.imgPerfil);
        searchPesquisa = findViewById(R.id.searchPesquisa);
        recycler = findViewById(R.id.recyclerReceitas);

        // --- Busca de Views do DETALHE ---
        cardDetalheReceita = findViewById(R.id.cardDetalheReceita);
        imgDetalheReceita = findViewById(R.id.imgDetalheReceita);
        txtNomeDetalhe = findViewById(R.id.txtNomeDetalhe);
        txtIndiceDetalhe = findViewById(R.id.txtIndiceDetalhe);
        txtFonteDetalhe = findViewById(R.id.txtFonteDetalhe);
        txtIngredientes = findViewById(R.id.txtIngredientes);
        txtPreparo = findViewById(R.id.txtPreparo);
        btnFavoriteDetalhe = findViewById(R.id.btnFavoriteDetalhe);
        btnFecharDetalhe = findViewById(R.id.btnFecharDetalhe);

        // --- Busca de Views de INTERAÇÃO (Botão de Info) ---
        btnInfoGlice = findViewById(R.id.btnInfoGlice);

        // --- Inicializa as variáveis de Dados da Receita (ELIMINEI O findViewById para os itens removidos do XML) ---
        // Estas variáveis agora apenas guardam o dado da Receita no método exibirDetalheReceita
        txtResumoDetalhe = new TextView(this); // Inicialização dummy (se necessário, senão o null check basta)
        txtTempoDetalhe = new TextView(this);
        txtJustificativaDetalhe = new TextView(this);
        txtSubstituicoesDetalhe = new TextView(this);
        txtLinkDetalhe = new TextView(this);


        // Configuração do Cloudinary
        Map config = new HashMap();
        config.put("cloud_name", "de4j4ibb6");
        config.put("secure", true);
        MediaManager.init(this, config);

        // --- Configuração do RecyclerView ---
        if (recycler != null) {
            recycler.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ReceitaAdapter(this, lista, this, this);
            recycler.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Erro: RecyclerView não encontrado no layout.", Toast.LENGTH_LONG).show();
            return;
        }

        receitaDao = new ReceitaDAO();

        // Configura o clique no ícone de perfil para ir para o Login
        if (imgPerfil != null) {
            imgPerfil.setOnClickListener(v-> {
                Intent it = new Intent(this, LoginActivity.class);
                startActivity(it);
            });
        }

        configurarPesquisa();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1. ATUALIZA o UID do usuário logado
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            currentUserId = null;
        }

        // 2. Carrega as receitas e verifica se precisa abrir um detalhe via Intent
        carregarReceitas(() -> verificarIntentDeAbrirReceita());
    }

    private void carregarReceitas(Runnable callback) {
        receitaDao.getReceitas(currentUserId, new ReceitaDAO.BuscarReceitasCallback() {
            @Override
            public void onSuccess(List<Receita> receitas) {
                lista.clear();
                lista.addAll(receitas);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

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

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Faça login para favoritar receitas!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // Alterna estado
        receita.setFavorita(!receita.isFavorita());
        adapter.notifyItemChanged(position);

        // Persiste no banco principal
        receitaDao.atualizarFavorito(
                currentUserId,
                receita.getDocumentId(),
                receita.isFavorita(),
                new ReceitaDAO.UpdateFavoriteCallback() {
                    @Override
                    public void onSuccess() {}

                    @Override
                    public void onError(String msg) {
                        receita.setFavorita(!receita.isFavorita());
                        adapter.notifyItemChanged(position);
                        Toast.makeText(ReceitasActivity.this, "Erro ao salvar favorito: " + msg, Toast.LENGTH_LONG).show();
                    }
                }
        );

        // SALVA OU REMOVE NA LISTA DE FAVORITOS DO USUÁRIO (necessário para o Fragment)
        if (receita.isFavorita()) {
            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(currentUserId)
                    .collection("favoritos")
                    .document(receita.getDocumentId())
                    .set(receita.toMap());
        } else {
            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(currentUserId)
                    .collection("favoritos")
                    .document(receita.getDocumentId())
                    .delete();
        }
    }


    // Método para popular e mostrar o CardView de Detalhes
    private void exibirDetalheReceita(Receita receita) {

        // --- PREENCHIMENTO DOS CAMPOS DE TEXTO ---
        txtNomeDetalhe.setText(receita.getNome());

        // Armazena os dados da receita (Mesmo que não estejam no layout principal)
        txtResumoDetalhe.setText(receita.getResumo());
        txtTempoDetalhe.setText(receita.getTempoPreparo() + " minutos");
        txtJustificativaDetalhe.setText(receita.getJustificativaGlice());
        txtSubstituicoesDetalhe.setText(receita.getSubstituicoes());

        txtPreparo.setText(receita.getPreparo());
        txtFonteDetalhe.setText("Fonte: " + receita.getFonte());

        // --- PREENCHIMENTO DO LINK ---
        String link = receita.getLinkReceita();
        if (link != null && !link.isEmpty()) {
            txtLinkDetalhe.setText(link);
            txtLinkDetalhe.setVisibility(View.VISIBLE);
        } else {
            txtLinkDetalhe.setVisibility(View.GONE);
        }

        // --- PREENCHIMENTO DA LISTA DE INGREDIENTES ---
        List<String> ingredientesList = receita.getIngredientesDetalhe();
        if (ingredientesList != null && !ingredientesList.isEmpty()) {
            String ingredientesFormatados = formatarListaComBullet(ingredientesList);
            txtIngredientes.setText(ingredientesFormatados);
        } else {
            txtIngredientes.setText("Ingredientes não disponíveis.");
        }

        // --- PREENCHIMENTO DO ÍNDICE GLICÊMICO E COR ---
        int indice = receita.getIndiceGlicemico();
        String textoIndice = "Glicê " + indice;
        txtIndiceDetalhe.setText(textoIndice);

        int cor = R.color.preto;
        if (indice == 1) {
            cor = R.color.rosa4;
        } else if (indice == 2) {
            cor = R.color.rosa2;
        } else if (indice == 3) {
            cor = R.color.rosa3;
        }
        txtIndiceDetalhe.setTextColor(ContextCompat.getColor(this, cor));


        // --- CARREGAMENTO DA IMAGEM ---
        Glide.with(this)
                .load(receita.getUrlImagem())
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgDetalheReceita);

        // --- LÓGICA DO BOTÃO FAVORITO NO DETALHE ---
        atualizarIconeFavoritoDetalhe(receita);

        btnFavoriteDetalhe.setOnClickListener(v -> {
            if (currentUserId == null || currentUserId.isEmpty()) {
                Toast.makeText(this, "Faça login para favoritar receitas!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            receita.setFavorita(!receita.isFavorita());
            atualizarIconeFavoritoDetalhe(receita);

            int index = lista.indexOf(receita);
            if(index != -1) {
                adapter.notifyItemChanged(index);
            }

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
                                receita.setFavorita(!receita.isFavorita());
                                if(index != -1) adapter.notifyItemChanged(index);
                                atualizarIconeFavoritoDetalhe(receita);
                                Toast.makeText(ReceitasActivity.this, "Erro ao salvar favorito: " + msg, Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }

            Toast.makeText(this, receita.getNome() + (receita.isFavorita() ? " favoritada!" : " desfavoritada!"), Toast.LENGTH_SHORT).show();
        });

        // Configuração do clique para abrir o Pop-up de informações
        if (btnInfoGlice != null) {
            btnInfoGlice.setOnClickListener(v -> {
                exibirInfoGlice(receita);
            });
        }


        // Muda a visibilidade para VISIBLE
        cardDetalheReceita.setVisibility(View.VISIBLE);
    }

    // Método auxiliar para formatar a lista com bullets
    private String formatarListaComBullet(List<String> lista) {
        StringBuilder sb = new StringBuilder();
        for (String item : lista) {
            sb.append("• ").append(item).append("\n");
        }
        return sb.toString().trim();
    }


    private void atualizarIconeFavoritoDetalhe(Receita receita) {
        if (receita.isFavorita()) {
            btnFavoriteDetalhe.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            btnFavoriteDetalhe.setImageResource(R.drawable.baseline_favorite_border_24);
        }
    }

    private void verificarIntentDeAbrirReceita() {
        String id = getIntent().getStringExtra("abrir_receita_id");
        if (id == null) return;

        getIntent().removeExtra("abrir_receita_id");

        for (Receita r : lista) {
            if (r.getDocumentId().equals(id)) {
                exibirDetalheReceita(r);
                break;
            }
        }
    }

    public void fecharDetalhe(View view) {
        cardDetalheReceita.setVisibility(View.GONE);
    }

    private void configurarPesquisa() {
        // Verifica se o searchPesquisa foi encontrado para evitar NullPointerException
        if (searchPesquisa == null) return;

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
        // Verifica se o adapter foi inicializado
        if (adapter == null) return;

        if (texto == null || texto.trim().isEmpty()) {
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

    // ************************************************************
    // MÉTODOS PARA O POP-UP DE INFORMAÇÕES GLICÊ
    // ************************************************************

    // Método auxiliar para formatar a string de justificativa com negrito
    private CharSequence formatarComNegrito(String texto) {
        // Substitui **palavra** por <b>palavra</b> para que o Html.fromHtml renderize em negrito.
        String htmlText = texto.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(htmlText);
        }
    }


    // Método para exibir o pop-up com Justificativa, Substituições e Explicação do IG
    private void exibirInfoGlice(Receita receita) {
        // 1. Inflar o layout do diálogo (R.layout.dialog_glice_info deve existir)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_glice_info, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // 2. Encontrar as views do diálogo
        TextView txtIndiceInfo = dialogView.findViewById(R.id.txtIndiceInfo);
        TextView txtJustificativaInfo = dialogView.findViewById(R.id.txtJustificativaInfo);
        TextView txtSubstituicoesInfo = dialogView.findViewById(R.id.txtSubstituicoesInfo);
        TextView txtOQueEGlice = dialogView.findViewById(R.id.txtOQueEGlice);
        Button btnFecharInfo = dialogView.findViewById(R.id.btnFecharInfo);

        // 3. Preencher os dados
        int indice = receita.getIndiceGlicemico();
        String textoIndice = "Índice Glicê: Glicê " + indice;
        txtIndiceInfo.setText(textoIndice);

        // Ajusta a cor do índice no pop-up
        int cor = R.color.preto;
        if (indice == 1) {
            cor = R.color.rosa4;
        } else if (indice == 2) {
            cor = R.color.rosa2;
        } else if (indice == 3) {
            cor = R.color.rosa3;
        }
        txtIndiceInfo.setTextColor(ContextCompat.getColor(this, cor));


        // Usa a função para aplicar negrito (dados são obtidos diretamente do objeto 'receita')
        txtJustificativaInfo.setText(formatarComNegrito(receita.getJustificativaGlice()));
        txtSubstituicoesInfo.setText(formatarComNegrito(receita.getSubstituicoes()));

        // Explicação padrão do Índice Glicê (pode ser movida para strings.xml se preferir)
        String explicacao = "O Índice Glice do GLICE classifica o impacto potencial da receita na glicemia: " +
                "Glice 1 (Baixo), Glice 2 (Moderado) e Glice 3 (Alto). " +
                "O objetivo é auxiliar na escolha de refeições mais estáveis.";
        txtOQueEGlice.setText(explicacao);

        // 4. Configurar o botão de fechar
        btnFecharInfo.setOnClickListener(v -> dialog.dismiss());

        // 5. Exibir o diálogo
        dialog.show();
    }
}