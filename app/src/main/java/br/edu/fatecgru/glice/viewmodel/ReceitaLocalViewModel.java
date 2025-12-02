package br.edu.fatecgru.glice.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import br.edu.fatecgru.glice.model.ReceitaPessoal;
import br.edu.fatecgru.glice.repository.ReceitaRepository;

/**
 * ViewModel que fornece dados ao LivroReceitasFragment.
 * O LiveData garante que a UI seja atualizada automaticamente.
 */
public class ReceitaLocalViewModel extends AndroidViewModel {

    private ReceitaRepository repository;
    private LiveData<List<ReceitaPessoal>> allReceitas;

    // Construtor: O ViewModel recebe o Application e inicializa o Repositório
    public ReceitaLocalViewModel(@NonNull Application application) {
        super(application);
        repository = new ReceitaRepository(application);
        allReceitas = repository.getAllReceitas();
    }

    // Getter para o LiveData (o Fragment o observará)
    public LiveData<List<ReceitaPessoal>> getAllReceitas() {
        return allReceitas;
    }

    // Métodos wrappers para as operações do banco de dados (chamados pelo Fragment)
    public void insert(ReceitaPessoal receita) {
        repository.insert(receita);
    }

    public void update(ReceitaPessoal receita) {
        repository.update(receita);
    }

    public void delete(ReceitaPessoal receita) {
        repository.delete(receita);
    }
}