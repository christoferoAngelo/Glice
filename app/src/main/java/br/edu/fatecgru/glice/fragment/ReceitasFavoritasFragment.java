package br.edu.fatecgru.glice.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.activity.DetalheReceitaActivity;
import br.edu.fatecgru.glice.adapter.ReceitaFavoritaAdapter;
import br.edu.fatecgru.glice.model.Receita;
import br.edu.fatecgru.glice.viewmodel.ReceitasFavoritasViewModel; // CORRIGIDO: Nome do ViewModel

/**
 * Fragmento para exibir a lista de receitas marcadas como favoritas (dados do Firestore).
 * Utiliza o ReceitaFavoritaAdapter e o ReceitaFavoritasViewModel.
 */
public class ReceitasFavoritasFragment extends Fragment implements ReceitaFavoritaAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private TextView txtListaVazia;
    private ReceitaFavoritaAdapter adapter;
    private ReceitasFavoritasViewModel viewModel; // CORRIGIDO: Tipo do ViewModel

    // Chave para passar a receita para a Activity de Detalhes
    public static final String EXTRA_RECEITA_FAVORITA = "br.edu.fatecgru.glice.EXTRA_RECEITA_FAVORITA";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Reutiliza o layout, mas se fosse diferente, usaria fragment_receitas_favoritas
        return inflater.inflate(R.layout.fragment_livro_receitas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicialização de Views (reutilizando IDs do layout de livro)
        // OBS: Se você quiser IDs mais específicos para este fragment, crie um novo XML de fragmento.
        recyclerView = view.findViewById(R.id.recyclerViewLivroReceitas);
        txtListaVazia = view.findViewById(R.id.txtListaVazia);
        txtListaVazia.setText("Você ainda não favoritou nenhuma receita!");

        // 2. Configuração do RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // 3. Inicializa o Adapter e define o Listener
        adapter = new ReceitaFavoritaAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        // 4. Inicializa o ViewModel
        viewModel = new ViewModelProvider(this).get(ReceitasFavoritasViewModel.class); // CORRIGIDO: Classe do ViewModel

        // 5. Observa a lista de receitas favoritas (LiveData)
        viewModel.favoritas.observe(getViewLifecycleOwner(), receitas -> {
            // Atualiza a lista no Adapter
            adapter.setReceitas(receitas);

            // Gerencia a exibição da mensagem de lista vazia
            if (receitas != null && receitas.isEmpty()) {
                txtListaVazia.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                txtListaVazia.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    // --- Implementação da interface ReceitaFavoritaAdapter.OnItemClickListener ---

    /**
     * Abre a Activity de Detalhes da Receita (para receitas da API).
     */
    @Override
    public void onItemClick(Receita receita) {
        Toast.makeText(getContext(), "Detalhes da Receita: " + receita.getNome(), Toast.LENGTH_SHORT).show();
        // Lógica de navegação para DetalheReceitaActivity (se necessário)
        /*
        Intent intent = new Intent(getContext(), DetalheReceitaActivity.class);
        intent.putExtra(EXTRA_RECEITA_FAVORITA, receita); // Receita precisa ser Parcelable/Serializable
        startActivity(intent);
        */
    }

    /**
     * Lógica chamada quando o botão de exclusão/remover favorito é clicado.
     */
    @Override
    public void onDeleteClick(Receita receita) {
        mostrarDialogoDeRemocao(receita);
    }

    /**
     * Exibe um diálogo de confirmação antes de remover dos favoritos.
     */
    private void mostrarDialogoDeRemocao(Receita receita) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remover Favorito")
                .setMessage("Tem certeza que deseja remover \"" + receita.getNome() + "\" dos seus favoritos?")
                .setPositiveButton("Remover", (dialog, which) -> {
                    // 1. Chama o método do ViewModel para remover do Firestore
                    viewModel.removerFavorita(receita);
                    // 2. O LiveData garante que a lista na UI será atualizada automaticamente
                    Toast.makeText(getContext(), "Removido dos favoritos: " + receita.getNome(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}