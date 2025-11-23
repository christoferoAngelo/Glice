package br.edu.fatecgru.glice.dao;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import br.edu.fatecgru.glice.model.Usuario;


public class UsuarioDAO {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Retorna a Task pra caller poder adicionar listeners e ver o erro
    public Task<Void> salvarUsuario(String userId, Usuario usuario) {
        return db.collection("usuarios")
                .document(userId)
                .set(usuario);
    }
}