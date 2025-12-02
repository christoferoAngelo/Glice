package br.edu.fatecgru.glice.dao;

import android.util.Log;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.edu.fatecgru.glice.model.Receita;

public class ReceitaDAO {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ReceitaDAO() {
    }

    // 🔹 BUSCAR TODAS AS RECEITAS E VERIFICAR FAVORITOS
    public void getReceitas(String userId, BuscarReceitasCallback callback) {

        db.collection("receita")
                .get()
                .addOnCompleteListener(taskReceitas -> {
                    if (taskReceitas.isSuccessful()) {
                        List<Receita> receitas = new ArrayList<>();

                        for (QueryDocumentSnapshot document : taskReceitas.getResult()) {
                            Receita receita = document.toObject(Receita.class);
                            receita.setDocumentId(document.getId());
                            receitas.add(receita);
                        }

                        if (userId == null || userId.isEmpty()) {
                            callback.onSuccess(receitas);
                            return;
                        }

                        db.collection("usuarios").document(userId).get()
                                .addOnCompleteListener(taskUsuario -> {
                                    if (taskUsuario.isSuccessful() && taskUsuario.getResult().exists()) {
                                        DocumentSnapshot userDoc = taskUsuario.getResult();
                                        List<String> favoritosIds = (List<String>) userDoc.get("favoritos");

                                        if (favoritosIds != null) {
                                            for (Receita r : receitas) {
                                                if (favoritosIds.contains(r.getDocumentId())) {
                                                    r.setFavorita(true);
                                                }
                                            }
                                        }
                                    } else {
                                        Log.w("ReceitaDAO", "Usuário não encontrado ou erro ao buscar favoritos: ", taskUsuario.getException());
                                    }
                                    callback.onSuccess(receitas);
                                });

                    } else {
                        Log.e("ReceitaDAO", "Erro ao buscar receitas: ", taskReceitas.getException());
                        callback.onError("Falha ao carregar dados do Firestore.");
                    }
                });
    }

    // 🔹 SALVAR NOVA RECEITA
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

    // 🔹 ATUALIZAR FAVORITO
    public void atualizarFavorito(String userId, String receitaId, boolean isFavorite, UpdateFavoriteCallback callback) {

        FieldValue updateOperation = isFavorite
                ? FieldValue.arrayUnion(receitaId)
                : FieldValue.arrayRemove(receitaId);

        // Cria um mapa para representar a atualização
        // O FieldValue.arrayUnion/arrayRemove só pode ser aplicado a campos do tipo Array (List<String>)
        Map<String, Object> updateData = new java.util.HashMap<>();
        updateData.put("favoritos", updateOperation);

        db.collection("usuarios").document(userId)
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("ReceitaDAO", "Favorito atualizado para o usuário: " + userId + " | Receita: " + receitaId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("ReceitaDAO", "Falha ao atualizar favoritos para o usuário: " + userId, e);
                    callback.onError("Falha ao salvar favorito: " + e.getMessage());
                });
    }
    // 🔹 BUSCAR RECEITAS FAVORITAS
    public void getReceitasFavoritas(String userId, BuscarReceitasCallback callback) {

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

                    db.collection("receita")
                            .whereIn(FieldPath.documentId(), favoritosIds) // Corrigido para usar FieldPath.documentId()
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

    // 🔹 BUSCAR RECEITAS POR INGREDIENTES
    public void buscarReceitasPorIngredientes(String userId, List<String> ingredientesBusca, BuscarReceitasCallback callback) {

        if (ingredientesBusca == null || ingredientesBusca.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection("receita")
                .whereArrayContainsAny("nomesIngredientes", ingredientesBusca)
                .get()
                .addOnCompleteListener(taskReceitas -> {
                    if (taskReceitas.isSuccessful()) {
                        List<Receita> receitasEncontradas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : taskReceitas.getResult()) {
                            Receita receita = document.toObject(Receita.class);
                            receita.setDocumentId(document.getId());
                            receitasEncontradas.add(receita);
                        }

                        if (userId == null || userId.isEmpty()) {
                            callback.onSuccess(receitasEncontradas);
                            return;
                        }

                        db.collection("usuarios").document(userId).get()
                                .addOnCompleteListener(taskUsuario -> {
                                    if (taskUsuario.isSuccessful() && taskUsuario.getResult().exists()) {
                                        DocumentSnapshot userDoc = taskUsuario.getResult();
                                        List<String> favoritosIds = (List<String>) userDoc.get("favoritos");

                                        if (favoritosIds != null) {
                                            for (Receita r : receitasEncontradas) {
                                                if (favoritosIds.contains(r.getDocumentId())) {
                                                    r.setFavorita(true);
                                                }
                                            }
                                        }
                                    }
                                    callback.onSuccess(receitasEncontradas);
                                });

                    } else {
                        Log.e("ReceitaDAO", "Erro ao buscar receitas por ingrediente: ", taskReceitas.getException());
                        callback.onError("Falha ao carregar resultados da pesquisa.");
                    }
                });
    }

    // 🔹 BUSCAR TODAS RECEITAS (ADMIN)
    public void buscarTodasReceitas(BuscarReceitasCallback callback) {
        db.collection("receita")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Receita> receitas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Receita receita = document.toObject(Receita.class);
                            receita.setDocumentId(document.getId());
                            receitas.add(receita);
                        }
                        callback.onSuccess(receitas);
                    } else {
                        Log.e("ReceitaDAO", "Erro ao buscar todas as receitas para Admin: ", task.getException());
                        callback.onError("Falha ao carregar dados do Firestore para administração.");
                    }
                });
    }

    // 🔹 EXCLUIR RECEITA
    public void excluirReceita(String receitaId, ExcluirReceitaCallback callback) {
        if (receitaId == null || receitaId.isEmpty()) {
            callback.onError("ID da receita não pode ser nulo ou vazio.");
            return;
        }

        db.collection("receita").document(receitaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("ReceitaDAO", "Receita excluída com ID: " + receitaId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("ReceitaDAO", "Erro ao excluir receita: " + receitaId, e);
                    callback.onError("Falha ao excluir receita: " + e.getMessage());
                });
    }

    // 🔹 ATUALIZAR RECEITA
    public void atualizarReceita(Receita receita, AtualizarReceitaCallback callback) {
        String receitaId = receita.getDocumentId();

        if (receitaId == null || receitaId.isEmpty()) {
            callback.onError("ID do documento ausente. Impossível atualizar.");
            return;
        }

        Map<String, Object> receitaMap = receita.toMap();

        // Garante que o ID numérico "id" não seja enviado/sobrescrito no Firestore.
        receitaMap.remove("id");

        db.collection("receita").document(receitaId)
                .update(receitaMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ReceitaDAO", "Receita atualizada com sucesso. ID: " + receitaId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("ReceitaDAO", "Erro ao atualizar receita: " + receitaId, e);
                    callback.onError("Falha ao atualizar receita no Firestore: " + e.getMessage());
                });
    }


    // --- Interfaces de callback ---

    public interface BuscarReceitasCallback {
        void onSuccess(List<Receita> receitas);
        void onError(String msg);
    }
    public interface ExcluirReceitaCallback {
        void onSuccess();
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
    public interface AtualizarReceitaCallback {
        void onSuccess();
        void onError(String msg);
    }
}