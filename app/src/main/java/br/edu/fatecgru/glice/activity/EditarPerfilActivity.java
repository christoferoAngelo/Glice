package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import br.edu.fatecgru.glice.R;

public class EditarPerfilActivity extends AppCompatActivity {

    private TextInputEditText edtNome;
    private TextInputEditText edtSenha;
    private TextView txtEmailAtual;
    private Button btnSalvar;
    private Button btnSair;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Se o usuário não estiver logado, redireciona para a tela de Login
            startActivity(new Intent(EditarPerfilActivity.this, LoginActivity.class));
            finish();
            return;
        }

        inicializarComponentes();
        carregarDadosUsuario();
    }

    private void inicializarComponentes() {
        edtNome = findViewById(R.id.edtNome);
        edtSenha = findViewById(R.id.edtSenha);
        txtEmailAtual = findViewById(R.id.txtEmailAtual);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnSair = findViewById(R.id.btnSair);
    }

    private void carregarDadosUsuario() {
        // Exibe o email atual (não editável)
        txtEmailAtual.setText("Email: " + currentUser.getEmail());

        // Carrega o nome do Firestore
        DocumentReference userRef = db.collection("usuarios").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nomeAtual = documentSnapshot.getString("nome");
                if (nomeAtual != null) {
                    edtNome.setText(nomeAtual);
                }
            } else {
                Toast.makeText(this, "Dados do usuário não encontrados no Firestore.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Método chamado pelo botão "SALVAR MUDANÇAS" (onClick="salvarAlteracoes")
    public void salvarAlteracoes(android.view.View view) {
        String novoNome = edtNome.getText().toString().trim();
        String novaSenha = edtSenha.getText().toString().trim();

        // 1. Atualizar Nome no Firestore
        if (!novoNome.isEmpty()) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("nome", novoNome);

            db.collection("usuarios").document(currentUser.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditarPerfilActivity.this, "Nome atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                        // O nome no Firebase Auth é opcional, mas vamos atualizar por segurança
                        currentUser.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(novoNome)
                                .build());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditarPerfilActivity.this, "Erro ao atualizar nome: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }

        // 2. Atualizar Senha no Firebase Auth (se a senha não estiver vazia)
        if (!novaSenha.isEmpty()) {
            if (novaSenha.length() < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_LONG).show();
                return;
            }

            // Requer reautenticação se a senha for alterada
            currentUser.updatePassword(novaSenha)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditarPerfilActivity.this, "Senha atualizada com sucesso! Novo login será necessário em breve.", Toast.LENGTH_LONG).show();
                            edtSenha.setText(""); // Limpa o campo após o sucesso
                        } else {
                            // Isso geralmente falha se o usuário não fez login recentemente.
                            Toast.makeText(EditarPerfilActivity.this, "Falha ao atualizar senha. Faça login novamente para tentar de novo.", Toast.LENGTH_LONG).show();
                        }
                    });
        }

        // Se ambos forem vazios/inalterados, apenas feedback
        if (novoNome.isEmpty() && novaSenha.isEmpty()) {
            Toast.makeText(this, "Nenhuma alteração detectada.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método chamado pelo botão "SAIR DA CONTA" (onClick="deslogarUsuario")
    public void deslogarUsuario(android.view.View view) {
        auth.signOut();
        Toast.makeText(this, "Usuário deslogado.", Toast.LENGTH_SHORT).show();

        // Redireciona para a tela de login
        Intent intent = new Intent(EditarPerfilActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa o histórico
        startActivity(intent);
        finish();
    }
}