package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.PerfilPageAdapter;
import android.content.SharedPreferences;

/**
 * Activity da tela de Perfil do usuário.
 * Contém informações do usuário e TabLayout/ViewPager2 para Livro de Receitas e Favoritos.
 */
public class PerfilUsuario extends AppCompatActivity {

    private TextView txtNomeUsuario;
    private ImageView imgFotoPerfil;
    private ImageView imgEditarFoto;
    private ImageButton btnEditarPerfilIcone;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;

    // Foto de perfil
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<String> mGetContent;
    private static final String PREFS_NAME = "PerfilPrefs";
    private static final String KEY_PROFILE_PIC_URI = "profilePicUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_usuario);

        // --- Firebase ---
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        userId = currentUser.getUid();

        // --- SharedPreferences para foto de perfil ---
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        configurarSelecaoFotoLauncher();

        // --- Inicializa elementos do layout ---
        txtNomeUsuario = findViewById(R.id.txtNomeUsuario);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        imgEditarFoto = findViewById(R.id.imgEditarFoto);
        btnEditarPerfilIcone = findViewById(R.id.btnEditarPerfilIcone);

        // Clique na foto ou ícone de editar foto abre a galeria
        imgFotoPerfil.setOnClickListener(this::selecionarFotoPerfil);
        imgEditarFoto.setOnClickListener(this::selecionarFotoPerfil);

        // Clique no botão de editar perfil abre a tela de edição
        btnEditarPerfilIcone.setOnClickListener(this::abrirTelaEdicao);

        // Carrega informações do usuário
        carregarInformacoesUsuario();

        // --- Configuração das Abas ---
        tabLayout = findViewById(R.id.tabLayoutPerfil);
        viewPager = findViewById(R.id.viewPagerPerfil);

        PerfilPageAdapter adapter = new PerfilPageAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Meu Livro");
                    tab.setIcon(R.drawable.sharp_book_24);
                    break;
                case 1:
                    tab.setText("Favoritos");
                    tab.setIcon(R.drawable.baseline_favorite_24);
                    break;
            }
        }).attach();
    }

    /**
     * Carrega foto de perfil e nome do usuário.
     */
    private void carregarInformacoesUsuario() {
        // Foto local
        String localUriString = sharedPreferences.getString(KEY_PROFILE_PIC_URI, null);
        Uri imageUri = localUriString != null ? Uri.parse(localUriString) : null;

        if (imageUri != null) {
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .into(imgFotoPerfil);
        } else if (currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_profile)
                    .error(R.drawable.ic_default_profile)
                    .into(imgFotoPerfil);
        } else {
            imgFotoPerfil.setImageResource(R.drawable.ic_default_profile);
        }

        // Nome do usuário Firestore
        DocumentReference userRef = db.collection("usuarios").document(userId);
        userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null || documentSnapshot == null || !documentSnapshot.exists()) {
                txtNomeUsuario.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário GLICE");
                return;
            }
            String nome = documentSnapshot.getString("nome");
            txtNomeUsuario.setText(nome != null && !nome.isEmpty() ? nome : "Usuário GLICE");
        });
    }

    /**
     * Configura ActivityResultLauncher para selecionar a foto da galeria.
     */
    private void configurarSelecaoFotoLauncher() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                persistirUriLocal(uri);

                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_default_profile)
                        .error(R.drawable.ic_default_profile)
                        .into(imgFotoPerfil);

                Toast.makeText(this, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Persiste URI da imagem selecionada.
     */
    private void persistirUriLocal(Uri uri) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_PIC_URI, uri.toString());
        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        editor.apply();
    }

    /**
     * Abre seletor de imagens da galeria.
     */
    public void selecionarFotoPerfil(View view) {
        mGetContent.launch("image/*");
    }

    /**
     * Abre tela de edição do perfil (nome, senha...).
     */
    public void abrirTelaEdicao(View view) {
        Intent intent = new Intent(this, EditarPerfilActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarInformacoesUsuario();
    }
}
