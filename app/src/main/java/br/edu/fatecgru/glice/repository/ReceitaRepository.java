package br.edu.fatecgru.glice.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.fatecgru.glice.dao.ReceitaPessoalDAO_Impl;
import br.edu.fatecgru.glice.database.AppDatabase;
import br.edu.fatecgru.glice.dao.ReceitaPessoalDAO;
import br.edu.fatecgru.glice.model.ReceitaPessoal;

/**
 * Repositório para abstrair o acesso à fonte de dados do Room.
 * Fornece métodos síncronos (LiveData) e assíncronos (ExecutorService) para o ViewModel.
 */
public class ReceitaRepository {

    private ReceitaPessoalDAO receitaDAO;
    private LiveData<List<ReceitaPessoal>> allReceitas;

    // Executor para rodar operações de I/O em um thread separado (inserir, deletar, etc.)
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Construtor: obtém o DAO e a lista de receitas do banco de dados.
    public ReceitaRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        receitaDAO = db.receitaDAO();
        allReceitas = receitaDAO.getAllReceitas();
    }

    // Retorna todas as receitas (LiveData para observação reativa)
    public LiveData<List<ReceitaPessoal>> getAllReceitas() {
        return allReceitas;
    }

    // Insere uma receita de forma assíncrona (boa prática para I/O)
    public void insert(ReceitaPessoal receita) {
        databaseWriteExecutor.execute(() -> {
            receitaDAO.insert(receita);
        });
    }

    // Deleta uma receita de forma assíncrona
    public void delete(ReceitaPessoal receita) {
        databaseWriteExecutor.execute(() -> {
            receitaDAO.delete(receita);
        });
    }

    // Atualiza uma receita de forma assíncrona
    public void update(ReceitaPessoal receita) {
        databaseWriteExecutor.execute(() -> {
            receitaDAO.update(receita);
        });
    }
}