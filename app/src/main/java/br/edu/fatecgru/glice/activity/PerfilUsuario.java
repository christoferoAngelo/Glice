package br.edu.fatecgru.glice.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.edu.fatecgru.glice.R;

public class PerfilUsuario extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView txtUserName;
    private TextView txtUserEmail;
    private Button btnEditarInfo;
    private Button btnVerFavoritos;
    private EditText edtIngrediente;
    private Button btnAdicionarIngrediente;
    private ListView listIngredientes;
    private ScrollView scrollMain;

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

        imgProfile = findViewById(R.id.imgProfile);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        btnEditarInfo = findViewById(R.id.btnEditarInfo);
        btnVerFavoritos = findViewById(R.id.btnVerFavoritos);
        edtIngrediente = findViewById(R.id.edtIngrediente);
        btnAdicionarIngrediente = findViewById(R.id.btnAdicionarIngrediente);
        listIngredientes = findViewById(R.id.listIngredientes);
    }
}