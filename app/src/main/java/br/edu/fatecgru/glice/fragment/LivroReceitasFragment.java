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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import br.edu.fatecgru.glice.R;
// Remova a importação de DetalheReceitaActivity, pois não usaremos mais aqui
// import br.edu.fatecgru.glice.activity.DetalheReceitaActivity;
import br.edu.fatecgru.glice.activity.AdicionarReceitaLocalActivity;  // NOVO: Import para a Activity de adicionar/editar
import br.edu.fatecgru.glice.adapter.ReceitaLocalAdapter;
import br.edu.fatecgru.glice.model.ReceitaPessoal;
import br.edu.fatecgru.glice.viewmodel.ReceitaLocalViewModel;

/**
 * Fragmento para exibir a lista de receitas salvas localmente (Livro de Receitas),
 * utilizando RecyclerView e Room/ViewModel para dados reativos (MVVM).
 * Implementa ReceitaLocalAdapter.OnItemClickListener para lidar com eventos de clique.
 */
public class LivroReceitasFragment extends Fragment implements ReceitaLocalAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private TextView txtListaVazia;
    private ReceitaLocalAdapter adapter;
    private ReceitaLocalViewModel viewModel;
    private FloatingActionButton fabAdicionarReceita;

    // Chave para passar a receita para a Activity de edição (substitui o EXTRA para detalhes)
    public static final String EXTRA_RECEITA_PARA_EDITAR = "receita_para_editar";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout para este fragment (fragment_livro_receitas)
        return inflater.inflate(R.layout.fragment_livro_receitas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicialização de Views
        recyclerView = view.findViewById(R.id.recyclerViewLivroReceitas);
        txtListaVazia = view.findViewById(R.id.txtListaVazia);

        // 2. Configuração do RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // 3. Inicializa o Adapter e define o Listener
        adapter = new ReceitaLocalAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        // 4. Inicializa o ViewModel
        viewModel = new ViewModelProvider(this).get(ReceitaLocalViewModel.class);

        // 5. Observa a lista de receitas do Room Database (LiveData)
        viewModel.getAllReceitas().observe(getViewLifecycleOwner(), receitas -> {
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

        fabAdicionarReceita = view.findViewById(R.id.fabAdicionarReceita);
        fabAdicionarReceita.setOnClickListener(v -> abrirTelaAdicionarReceita());
    }

    // Implementação da interface ReceitaLocalAdapter.OnItemClickListener

    /**
     * Lógica para abrir a tela de edição da receita quando o item é clicado.
     * Passa a receita selecionada via Intent para a AdicionarReceitaLocalActivity.
     * @param receita O objeto ReceitaPessoal clicado.
     */
    @Override
    public void onItemClick(ReceitaPessoal receita) {
        // Navega para a tela de edição, passando a receita
        Intent intent = new Intent(getContext(), AdicionarReceitaLocalActivity.class);
        intent.putExtra(EXTRA_RECEITA_PARA_EDITAR, receita);  // Passa a receita via Parcelable
        startActivity(intent);

    }

    /**
     * Lógica chamada quando o botão de exclusão é clicado.
     * @param receita O objeto ReceitaPessoal a ser excluído.
     */
    @Override
    public void onDeleteClick(ReceitaPessoal receita) {
        mostrarDialogoDeExclusao(receita);
    }

    /**
     * Exibe um diálogo de confirmação antes de excluir a receita.
     * @param receita A receita a ser excluída.
     */
    private void mostrarDialogoDeExclusao(ReceitaPessoal receita) {
        // Uso de requireContext() garante que o contexto não é nulo
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja remover a receita \"" + receita.getTitulo() + "\" do seu Livro de Receitas?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    // 1. Chama o método delete do ViewModel para iniciar a exclusão assíncrona
                    viewModel.delete(receita);
                    // 2. O LiveData garante que a lista na UI será atualizada
                    Toast.makeText(getContext(), "Receita excluída: " + receita.getTitulo(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null) // Fecha o diálogo sem ação
                .show();
    }

    /**
     * Abre a tela para adicionar uma nova receita (chamado pelo FAB).
     * Não passa nenhuma receita, então a Activity detectará como "nova".
     */
    private void abrirTelaAdicionarReceita() {
        Intent intent = new Intent(getContext(), AdicionarReceitaLocalActivity.class);
        startActivity(intent);
    }
}