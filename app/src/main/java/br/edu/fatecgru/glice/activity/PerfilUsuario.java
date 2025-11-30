package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.edu.fatecgru.glice.R;

public class PerfilUsuario extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView txtUserName, txtUserEmail;
    private Button btnEditarInfo, btnVerFavoritos, btnLogout;
    private EditText edtIngrediente;
    private Button btnAdicionarIngrediente;
    private ListView listIngredientes;
    private ScrollView scrollMain;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ArrayAdapter<String> adapterIngredientes;
    private ArrayList<String> listaIngredientes = new ArrayList<>();

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_usuario);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        // Views
        imgProfile = findViewById(R.id.imgProfile);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        btnEditarInfo = findViewById(R.id.btnEditarInfo);
        btnVerFavoritos = findViewById(R.id.btnVerFavoritos);
        edtIngrediente = findViewById(R.id.edtIngrediente);
        btnAdicionarIngrediente = findViewById(R.id.btnAdicionarIngrediente);
        listIngredientes = findViewById(R.id.listIngredientes);
        btnLogout = findViewById(R.id.btnLogout);

        // Adapter ListView
        adapterIngredientes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaIngredientes);
        listIngredientes.setAdapter(adapterIngredientes);

        // Carregar dados do usuário
        carregarInformacoesUsuario();

        // Botão para adicionar ingrediente
        btnAdicionarIngrediente.setOnClickListener(v -> adicionarIngrediente());

        // EventListener realtime da geladeira
        observarMudancasGeladeira();

        btnEditarInfo.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilUsuario.this, EditarPerfilActivity.class);
            startActivity(intent);
        });

        btnVerFavoritos.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilUsuario.this, ReceitasFavoritasActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Desloga o usuário

            Intent i = new Intent(PerfilUsuario.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

    }

    // 🔥 Carregar dados do usuário
    private void carregarInformacoesUsuario() {
        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        txtUserName.setText(document.getString("nome"));
                        txtUserEmail.setText(document.getString("email"));

                        // Carregar ingredientes se existir campo "geladeira"
                        List<String> lista = (List<String>) document.get("geladeira");
                        if (lista != null) {
                            listaIngredientes.clear();
                            listaIngredientes.addAll(lista);
                            adapterIngredientes.notifyDataSetChanged();
                        }
                    }
                });
    }

    // 🔥 Adicionar item da geladeira no Firestore
    private void adicionarIngrediente() {
        String ingrediente = edtIngrediente.getText().toString().trim();

        if (ingrediente.isEmpty()) {
            Toast.makeText(this, "Digite um ingrediente!", Toast.LENGTH_SHORT).show();
            return;
        }

        listaIngredientes.add(ingrediente);
        adapterIngredientes.notifyDataSetChanged();
        edtIngrediente.setText("");

        // Salvar no Firestore
        db.collection("usuarios")
                .document(userId)
                .update("geladeira", listaIngredientes)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Ingrediente adicionado!", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDadosUsuario(); // Atualiza sempre que volta pra tela
    }

    // 🔥 Atualiza automaticamente quando mudar no Firestore
    private void observarMudancasGeladeira() {
        db.collection("usuarios")
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;

                    List<String> lista = (List<String>) snapshot.get("geladeira");
                    if (lista != null) {
                        listaIngredientes.clear();
                        listaIngredientes.addAll(lista);
                        adapterIngredientes.notifyDataSetChanged();
                    }
                });
    }

    private void carregarDadosUsuario() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        txtUserName.setText(doc.getString("nome"));
                        txtUserEmail.setText(doc.getString("email"));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                });
    }
}
