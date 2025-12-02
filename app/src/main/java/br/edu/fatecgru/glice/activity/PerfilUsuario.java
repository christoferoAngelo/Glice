package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

/**
 * Activity que representa a tela de Perfil do usuário.
 * Contém informações do usuário no topo e usa TabLayout/ViewPager2 para navegar
 * entre as seções Livro de Receitas (local) e Receitas Favoritas (FireStore).
 */
public class PerfilUsuario extends AppCompatActivity {

    private TextView txtNomeUsuario;
    private ImageView imgFotoPerfil;
    private ImageButton btnEditarPerfilIcone;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    // Campos do Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_usuario);

        // --- Configuração do Firebase ---
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Se o usuário não estiver logado, redireciona para a tela de Login
            startActivity(new Intent(PerfilUsuario.this, LoginActivity.class));
            finish();
            return;
        }

        userId = currentUser.getUid();

        // --- Inicializa Elementos de Perfil e Firebase ---
        txtNomeUsuario = findViewById(R.id.txtNomeUsuario);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        btnEditarPerfilIcone = findViewById(R.id.btnEditarPerfilIcone);

        // Adiciona Listeners para os botões do Perfil
        btnEditarPerfilIcone.setOnClickListener(this::abrirTelaEdicao);
        // O método selecionarFotoPerfil já está definido como onClick no XML
        // imgFotoPerfil.setOnClickListener(this::selecionarFotoPerfil); // Pode ser removido se já estiver no XML

        carregarInformacoesUsuario();

        // --- Configuração das Abas Deslizantes (ViewPager2 e TabLayout) ---
        tabLayout = findViewById(R.id.tabLayoutPerfil);
        viewPager = findViewById(R.id.viewPagerPerfil);

        // 1. Configura o adapter do ViewPager2
        PerfilPageAdapter adapter = new PerfilPageAdapter(this);
        viewPager.setAdapter(adapter);

        // 2. Liga o TabLayout com o ViewPager2, definindo os títulos e ícones das abas
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Meu Livro");
                    // Adiciona o Ícone para "Meu Livro de Receitas"
                    tab.setIcon(R.drawable.sharp_book_24);
                    break;
                case 1:
                    tab.setText("Favoritos");
                    // Adiciona o Ícone para "Receitas Favoritas"
                    tab.setIcon(R.drawable.baseline_favorite_24);
                    break;
            }
        }).attach();
    }

    /**
     * Carrega Nome (Firestore) e Foto/Email (Auth).
     */
    private void carregarInformacoesUsuario() {
        // Carrega foto do Firebase Auth (se existir)
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_profile) // Use o recurso de ícone padrão
                    .error(R.drawable.ic_default_profile) // Use o recurso de ícone padrão
                    .into(imgFotoPerfil);
        }

        // Carrega o nome do Firestore
        DocumentReference userRef = db.collection("usuarios").document(userId);
        userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null || documentSnapshot == null || !documentSnapshot.exists()) {
                // Se houver erro ou não existir, usa o nome do Auth
                txtNomeUsuario.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuário GLICE");
                return;
            }

            String nome = documentSnapshot.getString("nome");
            if (nome != null && !nome.isEmpty()) {
                txtNomeUsuario.setText(nome);
            }
        });
    }

    // Métodos onClick do layout

    public void selecionarFotoPerfil(View view) {
        // Lógica para selecionar foto
        Toast.makeText(this, "Funcionalidade de upload de foto pendente.", Toast.LENGTH_SHORT).show();
    }

    public void abrirTelaEdicao(View view) {
        // Lógica para abrir tela de edição (como na sua versão antiga)
        Intent intent = new Intent(PerfilUsuario.this, EditarPerfilActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            // Garante que o nome é atualizado após a edição
            carregarInformacoesUsuario();
        }
    }
}