package br.edu.fatecgru.glice.dao;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.model.Receita;

// Não precisamos mais de SupabaseApi, SupabaseClient, List, Map, Retrofit, etc.
// Os imports não utilizados foram removidos para a versão final.

public class ReceitaDAO {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Construtor padrão (não precisa mais de API do Supabase)
    public ReceitaDAO() {
    }

    // ⛔️ Métodos antigos do Supabase (getReceitas e insertReceita) foram removidos.
    // ⛔️ A lógica de 'salvarReceita(String userId, Usuario usuario)' foi removida/substituída.

    /**
     * 🔹 Salva um novo objeto Receita no Firebase Firestore.
     * O Firebase gera um ID de documento único automaticamente.
     *
     * @param receita O objeto Receita (contendo a foto_url do Cloudinary).
     * @param callback Interface para notificar o sucesso ou falha.
     */
    // 🔹 Buscar todas receitas do Firebase Firestore
    public void getReceitas(BuscarReceitasCallback callback) {
        db.collection("receita")
                .get() // Busca todos os documentos na coleção "receita"
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Receita> receitas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Converte cada documento do Firestore para o objeto Receita
                            Receita receita = document.toObject(Receita.class);
                            receitas.add(receita);
                        }
                        callback.onSuccess(receitas);
                    } else {
                        Log.e("ReceitaDAO", "Erro ao buscar receitas: ", task.getException());
                        callback.onError("Falha ao carregar dados do Firestore.");
                    }
                });
    }

    // 🔹 Salva um novo objeto Receita no Firebase Firestore.
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


    // --- Interfaces de callback ---

    public interface BuscarReceitasCallback {
        void onSuccess(List<Receita> receitas);
        void onError(String msg);
    }

    public interface SalvarReceitaCallback {
        void onSuccess(Receita receitaSalva);
        void onError(String msg);
    }
}