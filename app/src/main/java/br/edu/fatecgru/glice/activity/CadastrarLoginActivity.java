package br.edu.fatecgru.glice.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.dao.UsuarioDAO;
import br.edu.fatecgru.glice.model.Usuario;

public class CadastrarLoginActivity extends AppCompatActivity {

    private static final String TAG = "CadastrarLoginAct";

    private TextInputEditText edtNome, edtEmail, edtSenha, edtConfirmar;
    private Button btnCadastrar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_login);

        edtNome = findViewById(R.id.edtNomeText);
        edtEmail = findViewById(R.id.edtEmailText);
        edtSenha = findViewById(R.id.edtSenhaText);
        edtConfirmar = findViewById(R.id.edtConfirmarSenhaText);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        auth = FirebaseAuth.getInstance();

        btnCadastrar.setOnClickListener(v -> cadastrarUsuario());
    }

    private void cadastrarUsuario() {
        String nome = edtNome.getText() != null ? edtNome.getText().toString().trim() : "";
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String senha = edtSenha.getText() != null ? edtSenha.getText().toString().trim() : "";
        String confirmar = edtConfirmar.getText() != null ? edtConfirmar.getText().toString().trim() : "";

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmar)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCadastrar.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            Usuario usuario = new Usuario(nome, email);

                            UsuarioDAO dao = new UsuarioDAO();
                            dao.salvarUsuario(userId, usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnCadastrar.setEnabled(true);
                                        String mensagem = e.getMessage() != null ? e.getMessage() : "Erro desconhecido ao salvar usuario";
                                        Toast.makeText(this, "Erro ao salvar dados: " + mensagem, Toast.LENGTH_LONG).show();
                                        Log.e(TAG, "Erro salvando usuário no Firestore", e);
                                    });
                        } else {
                            btnCadastrar.setEnabled(true);
                            Toast.makeText(this, "Usuário criado, mas auth.getCurrentUser() retornou null", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "FirebaseUser é null após createUserWithEmailAndPassword");
                        }
                    } else {
                        btnCadastrar.setEnabled(true);
                        String mensagem = task.getException() != null ? task.getException().getMessage() : "Erro ao criar usuário";
                        Toast.makeText(this, "Erro ao criar usuário: " + mensagem, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "createUserWithEmailAndPassword falhou", task.getException());
                    }
                });
    }
}
