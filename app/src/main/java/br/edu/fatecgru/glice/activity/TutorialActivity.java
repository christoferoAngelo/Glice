package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.WelcomeAdapter;
import br.edu.fatecgru.glice.model.Carrossel_Item;

public class TutorialActivity extends AppCompatActivity {

    private RecyclerView recyclerCarrossel;
    private WelcomeAdapter adapter;
    private List<Carrossel_Item> lista = new ArrayList<>();
    private Button btnPular, btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutorial);

        // Ajuste para bordas do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // === RecyclerView ===
        recyclerCarrossel = findViewById(R.id.recyclerCarrossel);

        // Layout horizontal
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerCarrossel.setLayoutManager(layoutManager);

        // Lista de itens
        lista.add(new Carrossel_Item(
                "Bem-vindo ao Glicê",
                "O seu companheiro fitness para receitas saudáveis e nutritivas. Alcance seus objetivos com alimentação balanceada!",
                R.drawable.welcome
        ));

        lista.add(new Carrossel_Item("Controle Glicêmico", "Glicê 1: Sem açúcar; baixo impacto glicêmico. " +
                "Glicê 2: Contém apenas açúcares naturais (frutas) ou carboidratos complexos." +
                "Glicê 3:Contém qualquer açúcar adicionado ou carboidratos simples (mel, açúcar,farinha branca); impacto glicêmico:  ", R.drawable.welcome));
       lista.add(new Carrossel_Item("Receitas Saudáveis", "Descubra receitas", R.drawable.welcome));

        // Adapter
        adapter = new WelcomeAdapter(lista);
        recyclerCarrossel.setAdapter(adapter);

        // === Botões ===
        btnPular = findViewById(R.id.btnPular);
        btnContinuar = findViewById(R.id.btnContinuar);

        btnPular.setOnClickListener(v -> finish());

        btnContinuar.setOnClickListener(v -> {
            int next = ((LinearLayoutManager) recyclerCarrossel.getLayoutManager()).findFirstVisibleItemPosition() + 1;
            if (next < lista.size()) {
                recyclerCarrossel.smoothScrollToPosition(next);
            } else {
                finish();
            }
        });

        btnPular.setOnClickListener(v -> {
            Intent intent = new Intent(TutorialActivity.this, ReceitasActivity.class);
            startActivity(intent);
            finish(); // Fecha o tutorial para não voltar ao voltar do MainActivity
        });

    }
}
