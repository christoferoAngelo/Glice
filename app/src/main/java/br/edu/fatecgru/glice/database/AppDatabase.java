package br.edu.fatecgru.glice.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.fatecgru.glice.dao.ReceitaPessoalDAO;
import br.edu.fatecgru.glice.model.ReceitaPessoal;

/**
 * Classe principal do banco de dados Room para o aplicativo Glice.
 * Esta classe é um Singleton e gerencia a criação e acesso ao banco.
 *
 * @version: A versão do banco de dados. Deve ser incrementada após alterações de esquema.
 * @entities: Lista das classes de modelo (entidades) que fazem parte deste banco.
 */
@Database(entities = {ReceitaPessoal.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // 1. Definição do método abstrato para acessar o DAO
    // Este é o método que o ReceitaRepository estava buscando!
    public abstract ReceitaPessoalDAO receitaDAO();

    // 2. Singleton para evitar múltiplas instâncias
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    // Executor Service para garantir que operações de banco de dados
    // (como insert, delete, update) sejam executadas em background.
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Método de acesso thread-safe para o Singleton
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "glice_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}