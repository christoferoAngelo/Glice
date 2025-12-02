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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import br.edu.fatecgru.glice.R;
// Importação simulada para navegação
import br.edu.fatecgru.glice.activity.DetalheReceitaActivity;
import br.edu.fatecgru.glice.adapter.ReceitaFavoritaAdapter;
import br.edu.fatecgru.glice.model.Receita;
import br.edu.fatecgru.glice.viewmodel.ReceitasFavoritasViewModel;

/**
 * Fragment para exibir a lista de receitas salvas como favoritas no Firebase Firestore.
 * Implementa ReceitaFavoritaAdapter.OnItemClickListener para lidar com eventos de clique.
 */
public class MeusFavoritosFragment extends Fragment implements ReceitaFavoritaAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private TextView txtListaVazia;
    private ReceitaFavoritaAdapter adapter;
    private ReceitasFavoritasViewModel viewModel;
    private FirebaseAuth auth;

    public static final String EXTRA_RECEITA_FAVORITA = "br.edu.fatecgru.glice.EXTRA_RECEITA_FAVORITA";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_livro_receitas, container, false); // Reutilizando o mesmo layout do livro local
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicialização de Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // 2. Inicialização de Views
        recyclerView = view.findViewById(R.id.recyclerViewLivroReceitas);
        txtListaVazia = view.findViewById(R.id.txtListaVazia);
        txtListaVazia.setText("Você ainda não adicionou nenhuma receita aos favoritos.");

        // 3. Verifica autenticação antes de carregar dados
        if (user == null) {
            txtListaVazia.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Por favor, faça login para ver seus favoritos.", Toast.LENGTH_LONG).show();
            return;
        }

        // 4. Configuração do RecyclerView e Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new ReceitaFavoritaAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        // 5. Inicializa o ViewModel e Observa LiveData
        viewModel = new ViewModelProvider(this).get(ReceitasFavoritasViewModel.class);

        // CORREÇÃO: Observando o campo 'favoritas' do ViewModel, que é o LiveData exposto.
        viewModel.favoritas.observe(getViewLifecycleOwner(), receitas -> {
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

    // Implementação da interface ReceitaFavoritaAdapter.OnItemClickListener

    @Override
    public void onItemClick(Receita receita) {
        Toast.makeText(getContext(), "Abrindo detalhes de: " + receita.getNome(), Toast.LENGTH_SHORT).show();

        // Simulação da navegação:
        /*
        Intent intent = new Intent(getContext(), DetalheReceitaActivity.class);
        intent.putExtra(EXTRA_RECEITA_FAVORITA, receita);
        startActivity(intent);
        */
    }

    @Override
    public void onDeleteClick(Receita receita) {
        mostrarDialogoDeExclusao(receita);
    }

    /**
     * Exibe um diálogo de confirmação antes de remover dos favoritos.
     */
    private void mostrarDialogoDeExclusao(Receita receita) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Remoção")
                .setMessage("Tem certeza que deseja remover a receita \"" + receita.getNome() + "\" dos seus Favoritos?")
                .setPositiveButton("Remover", (dialog, which) -> {
                    // CORREÇÃO: Chamando o método 'removerFavorita' do ViewModel
                    viewModel.removerFavorita(receita);
                    Toast.makeText(getContext(), "Receita removida dos favoritos: " + receita.getNome(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}