package br.edu.fatecgru.glice.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.model.Receita;

/**
 * Repositório para gerenciar a persistência de receitas favoritas no Firebase Firestore.
 * Utiliza LiveData para observação em tempo real.
 */
public class ReceitaFavoritaRepository {

    private static final String TAG = "ReceitaFavoritaRepo";
    // Coleção principal no Firestore
    private static final String COLLECTION_RECEITAS = "receitasFavoritas";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // LiveData que será observado pelo ViewModel
    private final MutableLiveData<List<Receita>> receitasLiveData = new MutableLiveData<>();

    // Objeto para gerenciar o listener de tempo real
    private ListenerRegistration firestoreListener;

    public ReceitaFavoritaRepository() {
        // Inicializa a escuta de dados ao criar o repositório
        iniciarEscutaDeReceitas();
    }

    /**
     * Inicia a escuta em tempo real da coleção de receitas favoritas para o usuário logado.
     */
    public void iniciarEscutaDeReceitas() {
        // Remove qualquer listener anterior para evitar vazamentos de memória
        if (firestoreListener != null) {
            firestoreListener.remove();
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Usuário não autenticado. Não é possível carregar favoritos.");
            // Define a lista como vazia se não houver usuário logado
            receitasLiveData.setValue(new ArrayList<>());
            return;
        }

        // 1. Cria a referência à coleção
        CollectionReference receitasRef = db.collection(COLLECTION_RECEITAS);

        // 2. Cria a query: busca apenas receitas onde o campo 'userId' corresponde ao ID do usuário
        Query query = receitasRef.whereEqualTo("userId", user.getUid())
                .orderBy("nome", Query.Direction.ASCENDING);

        // 3. Inicia o listener em tempo real
        firestoreListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Erro ao ouvir a coleção de receitas favoritas: " + error.getMessage());
                return;
            }

            if (value != null) {
                List<Receita> receitas = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot document : value.getDocuments()) {
                    // Mapeia o documento para o objeto Receita
                    Receita receita = document.toObject(Receita.class);
                    if (receita != null) {
                        // O documentId é crucial para exclusão e edição
                        receita.setDocumentId(document.getId());
                        receitas.add(receita);
                    }
                }
                // Atualiza o LiveData, que notificará o ViewModel e, consequentemente, o Fragment
                receitasLiveData.setValue(receitas);
            }
        });
    }

    /**
     * Para uso em ViewModel, retorna o LiveData observável.
     */
    public LiveData<List<Receita>> getReceitasFavoritas() {
        return receitasLiveData;
    }

    /**
     * Remove uma receita favorita do Firestore.
     * @param receita A receita a ser removida (precisa ter o documentId).
     */
    public void delete(Receita receita) {
        if (receita.getDocumentId() != null) {
            db.collection(COLLECTION_RECEITAS).document(receita.getDocumentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Receita removida com sucesso: " + receita.getNome()))
                    .addOnFailureListener(e -> Log.e(TAG, "Erro ao remover receita: " + e.getMessage()));
        } else {
            Log.w(TAG, "Tentativa de excluir receita sem DocumentId.");
        }
    }

    /**
     * Deve ser chamado quando o Fragment/Activity que usa o repositório for destruído,
     * para liberar o listener de tempo real e evitar vazamentos.
     */
    public void removerListener() {
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    /**
     * Interface de callback para notificar quando os dados (Lista de Receitas) foram carregados com sucesso.
     */
    public interface OnDataLoadedListener {
        void onDataLoaded(List<Receita> receitas);
    }
}