package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import br.edu.fatecgru.glice.MainActivity;
import br.edu.fatecgru.glice.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtSenha;
    private Button btnLogin, btnLoginGoogle;
    private TextView txtCadastrar;
    private FirebaseAuth auth;

    // Cliente do Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;

    // Launcher para a Intent do Google Sign-In
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inicialização do Firebase
        auth = FirebaseAuth.getInstance();

        // 2. Configuração do Google Sign-In
        configurarGoogleSignIn();

        // 3. Registro do Launcher (para receber o resultado do login do Google)
        registrarSignInLauncher();

        // 4. Inicialização dos Componentes (IDs CORRIGIDOS)
        // Os IDs foram corrigidos para: edtLoginEmail, edtLoginSenha, e btnLogin
        edtEmail = findViewById(R.id.edtLoginEmail); // CORRIGIDO (Era edtEmailText)
        edtSenha = findViewById(R.id.edtLoginSenha); // CORRIGIDO (Era edtSenhaText)
        btnLogin = findViewById(R.id.btnLogin);      // CORRIGIDO (Era btnCadastrar)

        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        txtCadastrar = findViewById(R.id.txvCadastrar);

        // 5. Estado Inicial do Botão
        // O botão AGORA não será nulo, pois o ID foi corrigido.
        btnLogin.setEnabled(false);

        // 6. Lógica de Habilitação do Botão (Text Watcher)
        addTextWatchers();

        // 7. Configuração dos Listeners (Cliques)

        // Clicar em LOGIN (Email/Senha)
        btnLogin.setOnClickListener(v -> fazerLogin());

        // Clicar em LOGIN GOOGLE
        btnLoginGoogle.setOnClickListener(v -> signInGoogle());

        // Clicar em CADASTRAR
        txtCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastrarLoginActivity.class);
            startActivity(intent);
        });

        // 8. Checagem de Sessão Existente
        checarSessaoExistente();
    }
    // =========================================================================
    // Lógica de Configuração do Google Sign-In
    // =========================================================================
    private void configurarGoogleSignIn() {
        // Solicita o ID e perfis básicos. DEFAULT_SIGN_IN é suficiente.
        // O método 'requestIdToken' é crucial para integrar com o Firebase.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Deve ser o ID do cliente web do seu Firebase
                .requestEmail()
                .build();

        // Constrói um GoogleSignInClient com as opções especificadas.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // =========================================================================
    // Lógica de Inicialização do Launcher
    // =========================================================================
    private void registrarSignInLauncher() {
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Toast.makeText(this, "Login com Google cancelado ou falhou.", Toast.LENGTH_SHORT).show();                    }
                }
        );
    }

    // =========================================================================
    // Inicia o fluxo de login do Google
    // =========================================================================
    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    // =========================================================================
    // Lida com o resultado do Google Sign-In e autentica no Firebase
    // =========================================================================
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Se o login com Google for bem-sucedido, autentica com o Firebase
            firebaseAuthWithGoogle(account.getIdToken());

        } catch (ApiException e) {
            // ApiException significa que houve um erro no lado do Google
            Toast.makeText(this, "Falha no Login Google: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    // =========================================================================
    // Autenticação no Firebase usando o Token do Google
    // =========================================================================
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sucesso na autenticação Firebase
                        Toast.makeText(this, "Login com Google realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Falha na autenticação Firebase
                        Toast.makeText(this, "Falha na autenticação Firebase com Google.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =========================================================================
    // Métodos Originais (mantidos)
    // =========================================================================
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
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Email ou senha inválidos!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addTextWatchers() {
        // ... (Lógica do TextWatcher mantida)
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

    private void checarSessaoExistente() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Usando MainActivity por enquanto, como no seu método fazerLogin().
            // Se PerfilUsuario for o local correto, use-o.
            Intent it = new Intent(this, PerfilUsuario.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(it);
            finish();
        }
    }
}