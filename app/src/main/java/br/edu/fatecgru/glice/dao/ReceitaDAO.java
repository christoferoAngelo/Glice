package br.edu.fatecgru.glice.dao;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot; // Necessário para buscar o documento do usuário

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.model.Receita;

public class ReceitaDAO {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ReceitaDAO() {
    }

    // 🔹 BUSCAR TODAS AS RECEITAS E VERIFICAR FAVORITOS (MÉTODO CORRIGIDO)
    // Agora aceita a String userId para verificar os favoritos
    public void getReceitas(String userId, BuscarReceitasCallback callback) {

        // 1. Busca todas as receitas
        db.collection("receita")
                .get()
                .addOnCompleteListener(taskReceitas -> {
                    if (taskReceitas.isSuccessful()) {
                        List<Receita> receitas = new ArrayList<>();

                        // Mapeia todas as receitas
                        for (QueryDocumentSnapshot document : taskReceitas.getResult()) {
                            Receita receita = document.toObject(Receita.class);
                            receita.setDocumentId(document.getId());
                            receitas.add(receita);
                        }

                        // 2. Se não houver userId logado, retorna a lista sem favoritos marcados
                        if (userId == null || userId.isEmpty()) {
                            callback.onSuccess(receitas);
                            return;
                        }

                        // 3. Busca a lista de favoritos do usuário logado
                        db.collection("usuarios").document(userId).get()
                                .addOnCompleteListener(taskUsuario -> {
                                    if (taskUsuario.isSuccessful() && taskUsuario.getResult().exists()) {
                                        DocumentSnapshot userDoc = taskUsuario.getResult();

                                        // Verifica se o campo 'favoritos' existe e é uma List<String>
                                        List<String> favoritosIds = (List<String>) userDoc.get("favoritos");

                                        if (favoritosIds != null) {
                                            // 4. Marca as receitas como favoritas
                                            for (Receita r : receitas) {
                                                if (favoritosIds.contains(r.getDocumentId())) {
                                                    r.setFavorita(true);
                                                }
                                            }
                                        }
                                    } else {
                                        Log.w("ReceitaDAO", "Usuário não encontrado ou erro ao buscar favoritos: ", taskUsuario.getException());
                                        // Continua, mas com os corações desmarcados
                                    }

                                    // Retorna a lista de receitas
                                    callback.onSuccess(receitas);
                                });

                    } else {
                        Log.e("ReceitaDAO", "Erro ao buscar receitas: ", taskReceitas.getException());
                        callback.onError("Falha ao carregar dados do Firestore.");
                    }
                });
    }

    // Salva um novo objeto Receita no Firebase Firestore.
    public void salvarNovaReceita(Receita receita, SalvarReceitaCallback callback) {
        db.collection("receita")
                .add(receita)
                .addOnSuccessListener(documentReference -> {
                    Log.d("ReceitaDAO", "Receita adicionada com ID: " + documentReference.getId());
                    callback.onSuccess(receita);
                })
                .addOnFailureListener(e -> {
                    Log.e("ReceitaDAO", "Erro ao salvar receita no Firestore.", e);
                    callback.onError("Falha ao salvar no Firestore: " + e.getMessage());
                });
    }

    // Método para ADICIONAR ou REMOVER o ID da Receita da lista de favoritos do usuário
    public void atualizarFavorito(String userId, String receitaId, boolean isFavorite, UpdateFavoriteCallback callback) {

        FieldValue updateOperation = isFavorite
                ? FieldValue.arrayUnion(receitaId)
                : FieldValue.arrayRemove(receitaId);

        db.collection("usuarios").document(userId)
                .update("favoritos", updateOperation)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ReceitaDAO", "Favorito atualizado para o usuário: " + userId + " | Receita: " + receitaId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("ReceitaDAO", "Falha ao atualizar favoritos para o usuário: " + userId, e);
                    callback.onError("Falha ao salvar favorito: " + e.getMessage());
                });
    }


    // --- Interfaces de callback ---

    public interface BuscarReceitasCallback {
        void onSuccess(List<Receita> receitas);
        void onError(String msg);
    }

    public interface SalvarReceitaCallback {
        void onSuccess(Receita receitaSalva);
        void onError(String msg);
    }

    public interface UpdateFavoriteCallback {
        void onSuccess();
        void onError(String msg);
    }

    public void getReceitasFavoritas(String userId, BuscarReceitasCallback callback) {

        // 1. Pega o documento do usuário com os IDs favoritos
        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<String> favoritosIds = (List<String>) userDoc.get("favoritos");

                    if (favoritosIds == null || favoritosIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // 2. Busca só os documentos que estão na lista de favoritos
                    db.collection("receita")
                            .whereIn("__name__", favoritosIds)
                            .get()
                            .addOnSuccessListener(task -> {
                                List<Receita> lista = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : task) {
                                    Receita r = doc.toObject(Receita.class);
                                    r.setDocumentId(doc.getId());
                                    r.setFavorita(true);
                                    lista.add(r);
                                }
                                callback.onSuccess(lista);
                            })
                            .addOnFailureListener(e -> callback.onError("Falha ao carregar favoritos"));
                })
                .addOnFailureListener(e -> callback.onError("Erro ao buscar usuário"));
    }

}