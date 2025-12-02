package br.edu.fatecgru.glice.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // Import adicionado para desfavoritar

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.activity.LoginActivity;
import br.edu.fatecgru.glice.adapter.ReceitaFavoritaAdapter;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

public class ReceitasFavoritasFragment extends Fragment
        implements ReceitaFavoritaAdapter.OnReceitaFavoriteListener { // Implementação adicionada

    private RecyclerView recyclerView;
    private TextView txtListaVazia;

    private ReceitaFavoritaAdapter adapter;
    private ArrayList<Receita> listaFavoritos = new ArrayList<>();

    private FirebaseAuth auth;
    private ReceitaDAO receitaDAO;

    // Novas views para o detalhe (overlay)
    private CardView cardDetalheReceita;
    private ImageView imgDetalheReceita;
    private TextView txtNomeDetalhe;
    private TextView txtIndiceDetalhe;
    private TextView txtFonteDetalhe;
    private TextView txtIngredientes;
    private TextView txtPreparo;
    private TextView txtResumoDetalhe;
    private TextView txtTempoDetalhe;
    private TextView txtLinkDetalhe;
    private ImageButton btnFecharDetalhe;
    private ImageButton btnFavoriteDetalhe;
    private ImageButton btnInfoGlice;

    public ReceitasFavoritasFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_receitas_favoritas, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLivroReceitas);
        txtListaVazia = view.findViewById(R.id.txtListaVazia);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // CORREÇÃO: Passando 'this' como favoriteListener
        adapter = new ReceitaFavoritaAdapter(listaFavoritos, receita -> {
            exibirDetalheReceita(receita);
        }, this);

        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        receitaDAO = new ReceitaDAO();

        // Inicializar as views do detalhe
        cardDetalheReceita = view.findViewById(R.id.cardDetalheReceita);
        imgDetalheReceita = view.findViewById(R.id.imgDetalheReceita);
        txtNomeDetalhe = view.findViewById(R.id.txtNomeDetalhe);
        txtIndiceDetalhe = view.findViewById(R.id.txtIndiceDetalhe);
        txtFonteDetalhe = view.findViewById(R.id.txtFonteDetalhe);
        txtIngredientes = view.findViewById(R.id.txtIngredientes);
        txtPreparo = view.findViewById(R.id.txtPreparo);
        txtResumoDetalhe = view.findViewById(R.id.txtResumoDetalhe);
        txtTempoDetalhe = view.findViewById(R.id.txtTempoDetalhe);
        txtLinkDetalhe = view.findViewById(R.id.txtLinkDetalhe);
        btnFecharDetalhe = view.findViewById(R.id.btnFecharDetalhe);
        btnFavoriteDetalhe = view.findViewById(R.id.btnFavoriteDetalhe);
        btnInfoGlice = view.findViewById(R.id.btnInfoGlice);

        // Configurar clique para fechar o detalhe
        btnFecharDetalhe.setOnClickListener(v -> fecharDetalhe());

        carregarFavoritos();

        return view;
    }

    // =================================================================================
    // NOVO MÉTODO OBRIGATÓRIO (para corrigir o problema de clique no coração da lista)
    // =================================================================================

    @Override
    public void onReceitaFavoriteClick(Receita receita, int position) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Faça login para desfavoritar receitas!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // No contexto da lista de favoritos, clicar no coração significa DESFAVORITAR (novoStatus = false)
        boolean novoStatus = false;
        receita.setFavorita(novoStatus);

        // Atualiza a visualização rapidamente
        adapter.notifyItemChanged(position);

        // 1. Atualizar no banco de dados principal
        receitaDAO.atualizarFavorito(
                userId,
                receita.getDocumentId(),
                novoStatus,
                new ReceitaDAO.UpdateFavoriteCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), receita.getNome() + " desfavoritada!", Toast.LENGTH_SHORT).show();
                        // 2. Recarregar favoritos para REMOVER o item da lista
                        carregarFavoritos();
                    }

                    @Override
                    public void onError(String msg) {
                        // Reverte o status em caso de erro
                        receita.setFavorita(true);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(getContext(), "Erro ao desfavoritar: " + msg, Toast.LENGTH_LONG).show();
                    }
                }
        );

        // 3. Remover da subcoleção de favoritos do usuário (necessário para a consistência da lista)
        // Se esta lógica já estiver dentro do ReceitaDAO.atualizarFavorito, remova este bloco.
        // Caso contrário, use:
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .collection("favoritos")
                .document(receita.getDocumentId())
                .delete()
                .addOnFailureListener(e -> Log.e("Favoritos", "Erro ao remover da subcoleção: " + e.getMessage()));
    }

    // =================================================================================
    // MÉTODOS EXISTENTES ABAIXO (MANTIDOS SEM ALTERAÇÃO na lógica principal)
    // =================================================================================

    private void carregarFavoritos() {
        if (auth.getCurrentUser() == null) {
            mostrarListaVazia(true);
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        receitaDAO.getReceitasFavoritas(userId, new ReceitaDAO.BuscarReceitasCallback() {
            @Override
            public void onSuccess(List<Receita> receitas) {

                listaFavoritos.clear();
                listaFavoritos.addAll(receitas);

                // Debug: Verificar se as receitas estão marcadas como favoritas
                for (Receita r : receitas) {
                    Log.d("Favoritos", "Receita: " + r.getNome() + " - Favorita: " + r.isFavorita());
                }

                adapter.notifyDataSetChanged();

                mostrarListaVazia(receitas.isEmpty());
            }

            @Override
            public void onError(String msg) {
                mostrarListaVazia(true);
            }
        });
    }

    private void mostrarListaVazia(boolean vazio) {
        txtListaVazia.setVisibility(vazio ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    private void exibirDetalheReceita(Receita receita) {
        if (receita == null) return;

        // Preencher os campos
        txtNomeDetalhe.setText(receita.getNome());
        txtFonteDetalhe.setText("Fonte: " + receita.getFonte());

        // Adicionando títulos em negrito aos campos de texto
        txtPreparo.setText(Html.fromHtml("<b>Preparo:</b><br/>" + receita.getPreparo()));
        txtResumoDetalhe.setText(Html.fromHtml("<b>Resumo:</b><br/>" + receita.getResumo()));
        txtTempoDetalhe.setText(Html.fromHtml("<b>Tempo:</b> " + receita.getTempoPreparo() + " minutos"));

        // Link da receita (ajustado para melhor manipulação)
        String link = receita.getLinkReceita();
        if (link != null && !link.isEmpty()) {
            txtLinkDetalhe.setText(Html.fromHtml("<b>Link:</b> " + link));
            txtLinkDetalhe.setVisibility(View.VISIBLE);
        } else {
            txtLinkDetalhe.setVisibility(View.GONE);
        }

        // Ingredientes (formatar com bullets)
        List<String> ingredientesList = receita.getIngredientesDetalhe();
        if (ingredientesList != null && !ingredientesList.isEmpty()) {
            String ingredientesFormatados = formatarListaComBullet(ingredientesList);
            txtIngredientes.setText(Html.fromHtml("<b>Ingredientes:</b><br/>" + ingredientesFormatados));
        } else {
            txtIngredientes.setText(Html.fromHtml("<b>Ingredientes:</b><br/>" + "Não disponíveis."));
        }

        // Índice Glicêmico com cor
        int indice = receita.getIndiceGlicemico();
        String textoIndice = "Índice Glicê " + indice;
        txtIndiceDetalhe.setText(textoIndice);

        int cor = R.color.preto;
        if (indice == 1) {
            cor = R.color.rosa4;
        } else if (indice == 2) {
            cor = R.color.rosa2;
        } else if (indice == 3) {
            cor = R.color.rosa3;
        }
        txtIndiceDetalhe.setTextColor(ContextCompat.getColor(requireContext(), cor));

        // Imagem
        Glide.with(requireContext())
                .load(receita.getUrlImagem())
                .placeholder(R.drawable.recipes)
                .into(imgDetalheReceita);

        // Atualizar ícone do favorito
        atualizarIconeFavoritoDetalhe(receita);

        // Configurar clique no botão favorito
        btnFavoriteDetalhe.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "Faça login para favoritar receitas!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            boolean novoStatus = !receita.isFavorita();
            receita.setFavorita(novoStatus);
            atualizarIconeFavoritoDetalhe(receita);

            // Atualizar na lista e notificar adapter
            int index = listaFavoritos.indexOf(receita);
            if (index != -1) {
                adapter.notifyItemChanged(index);
            }

            // Persistir no Firebase
            receitaDAO.atualizarFavorito(
                    userId,
                    receita.getDocumentId(),
                    receita.isFavorita(),
                    new ReceitaDAO.UpdateFavoriteCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), receita.getNome() + (novoStatus ? " favoritada!" : " desfavoritada!"), Toast.LENGTH_SHORT).show();
                            // Recarregar favoritos para atualizar a lista (ex.: remover se desfavoritar)
                            carregarFavoritos();
                        }

                        @Override
                        public void onError(String msg) {
                            // Reverte o status em caso de erro
                            receita.setFavorita(!novoStatus);
                            if (index != -1) adapter.notifyItemChanged(index);
                            atualizarIconeFavoritoDetalhe(receita);
                            Toast.makeText(getContext(), "Erro ao salvar favorito: " + msg, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });

        // Configurar clique no botão Info Glice (para abrir pop-up)
        btnInfoGlice.setOnClickListener(v -> exibirInfoGlice(receita));

        // Mostrar o card
        cardDetalheReceita.setVisibility(View.VISIBLE);
    }

    private void fecharDetalhe() {
        cardDetalheReceita.setVisibility(View.GONE);
    }

    private String formatarListaComBullet(List<String> lista) {
        StringBuilder sb = new StringBuilder();
        for (String item : lista) {
            sb.append("• ").append(item).append("<br/>");
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
    private void exibirInfoGlice(Receita receita) {
        // 1. Inflar o layout do diálogo (R.layout.dialog_glice_info deve existir)
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        String textoIndice = "Índice Glicê: " + indice;
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
        txtIndiceInfo.setTextColor(ContextCompat.getColor(requireContext(), cor));

        // Aplica o formato HTML nas Justificativas e Substituições
        txtJustificativaInfo.setText(formatarComNegrito(receita.getJustificativaGlice()));
        txtSubstituicoesInfo.setText(formatarComNegrito(receita.getSubstituicoes()));

        // Explicação padrão do Índice Glicê (APRIMORADA)
        String explicacao = "O **Índice Glice** do GLICE classifica o impacto potencial da receita na glicemia:\n\n" +
                "• **Glice 1 (Baixo):** Receitas sem adição de açúcar, frequentemente usando adoçantes dietéticos e ingredientes de baixo carboidrato. Ideal para controle estrito.\n" +
                "• **Glice 2 (Moderado):** Contém carboidratos complexos ou açúcares naturais de frutas/vegetais. Impacto controlado, adequado com moderação.\n" +
                "• **Glice 3 (Alto):** Contém açúcar adicionado (mel, açúcar refinado, leite condensado) ou alto teor de farinhas brancas. Deve ser consumido com cautela.";

        // Usa o formatador para aplicar negrito e quebras de linha.
        txtOQueEGlice.setText(formatarComNegrito(explicacao));

        btnFecharInfo.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private CharSequence formatarComNegrito(String texto) {
            // Substitui **palavra** por <b>palavra</b> e \n por <br/>
            String htmlText = texto.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                    .replaceAll("\n", "<br/>");


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT);
            } else {
                return Html.fromHtml(htmlText);
            }
        }
    }
