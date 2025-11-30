package br.edu.fatecgru.glice.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import br.edu.fatecgru.glice.R;

public class EditarPerfilActivity extends AppCompatActivity {

    private TextInputEditText edtNome, edtEmail;
    private Button btnSalvar;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editar_perfil);

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        btnSalvar = findViewById(R.id.btnSalvar);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        carregarDados();

        btnSalvar.setOnClickListener(v -> salvarAlteracoes());


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregarDados() {
        if (user == null) return;

        // Pega nome + email do Firestore
        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        edtNome.setText(doc.getString("nome"));
                        edtEmail.setText(doc.getString("email")); // email também fica lá
                    }
                });
    }

    private void salvarAlteracoes() {
        String novoNome = edtNome.getText().toString().trim();
        String novoEmail = edtEmail.getText().toString().trim();

        if (novoNome.isEmpty() || novoEmail.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSalvar.setEnabled(false);

        String emailAtual = user.getEmail();

        if (!novoEmail.equals(emailAtual)) {
            // Se mudou email → é obrigatório reautenticar!
            pedirSenhaParaReautenticar(novoNome, novoEmail);
        } else {
            atualizarFirestore(novoNome, novoEmail);
        }
    }

    private void pedirSenhaParaReautenticar(String novoNome, String novoEmail) {
        // Exige senha atual — Firebase regra de segurança
        Toast.makeText(this, "Para mudar o email, digite a senha novamente.", Toast.LENGTH_LONG).show();

        // Aqui você pode abrir um dialog pedindo senha
        // Para simplificar, pedirei via dialog básico

        androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(this);
        dialog.setTitle("Confirmar senha");

        final TextInputEditText edtSenha = new TextInputEditText(this);
        edtSenha.setHint("Senha atual");
        dialog.setView(edtSenha);

        dialog.setPositiveButton("Confirmar", (d, w) -> {
            String senha = edtSenha.getText().toString();

            if (senha.isEmpty()) {
                Toast.makeText(this, "Digite a senha!", Toast.LENGTH_SHORT).show();
                btnSalvar.setEnabled(true);
                return;
            }

            reautenticarEAtualizarEmail(senha, novoNome, novoEmail);
        });

        dialog.setNegativeButton("Cancelar", (d, w) -> btnSalvar.setEnabled(true));

        dialog.show();
    }

    private void reautenticarEAtualizarEmail(String senha, String novoNome, String novoEmail) {

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), senha))
                .addOnSuccessListener(aVoid -> {

                    // Agora posso atualizar o email
                    user.updateEmail(novoEmail)
                            .addOnSuccessListener(unused -> atualizarFirestore(novoNome, novoEmail))
                            .addOnFailureListener(e -> {
                                btnSalvar.setEnabled(true);
                                Toast.makeText(this, "Erro ao atualizar email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });

                })
                .addOnFailureListener(e -> {
                    btnSalvar.setEnabled(true);
                    Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
                });
    }

    private void atualizarFirestore(String nome, String email) {

        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", nome);
        dados.put("email", email);

        db.collection("usuarios")
                .document(user.getUid())
                .update(dados)
                .addOnSuccessListener(unused -> {
                    btnSalvar.setEnabled(true);
                    Toast.makeText(this, "Dados atualizados!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSalvar.setEnabled(true);
                    Toast.makeText(this, "Erro ao salvar no Firestore", Toast.LENGTH_LONG).show();
                });

        Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
