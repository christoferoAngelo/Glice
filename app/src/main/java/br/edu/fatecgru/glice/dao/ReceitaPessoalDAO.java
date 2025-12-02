package br.edu.fatecgru.glice.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.edu.fatecgru.glice.model.ReceitaPessoal;

/**
 * Data Access Object (DAO) para interagir com a entidade ReceitaLocal.
 * Define métodos para operações de banco de dados.
 */
@Dao
public interface ReceitaPessoalDAO {

        // Insere uma nova receita no Livro de Receitas
        @Insert
        void insert(ReceitaPessoal receita);

        // Atualiza uma receita existente
        @Update
        void update(ReceitaPessoal receita);

        // Remove uma receita
        @Delete
        void delete(ReceitaPessoal receita);

        // Retorna todas as receitas ordenadas pelo título.
        // O LiveData permite que a UI observe mudanças em tempo real.
        @Query("SELECT * FROM receitas_pessoais ORDER BY titulo ASC")
        LiveData<List<ReceitaPessoal>> getAllReceitas();

        // Você pode adicionar outras consultas como buscar por ID ou por título.
        // @Query("SELECT * FROM receitas WHERE id = :id")
        // ReceitaLocal getReceitaById(int id);
}