package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import br.edu.fatecgru.glice.MainActivity;
import br.edu.fatecgru.glice.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtSenha;
    private Button btnLogin;
    private TextView txtCadastrar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase
        auth = FirebaseAuth.getInstance();

        // Componentes
        edtEmail = findViewById(R.id.edtLoginEmail);
        edtSenha = findViewById(R.id.edtLoginSenha);
        btnLogin = findViewById(R.id.btnLogin); // no XML o botão chama btnCadastrar
        txtCadastrar = findViewById(R.id.txvCadastrar);

        // Botão começa desabilitado
        btnLogin.setEnabled(false);

        // Habilitar/desabilitar o botão automaticamente
        addTextWatchers();

        // Clicar em LOGIN
        btnLogin.setOnClickListener(v -> fazerLogin());

        // Clicar em CADASTRAR
        txtCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastrarLoginActivity.class);
            startActivity(intent);
        });


    }

    private void fazerLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                        // Ir para a próxima tela (troque depois)
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this, "Email ou senha inválidos!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Habilita o botão só quando os campos tiverem texto
    private void addTextWatchers() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = edtEmail.getText().toString().trim();
                String senha = edtSenha.getText().toString().trim();

                btnLogin.setEnabled(!email.isEmpty() && !senha.isEmpty());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };

        edtEmail.addTextChangedListener(watcher);
        edtSenha.addTextChangedListener(watcher);
    }
}