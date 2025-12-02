package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
                R.drawable.gate
        ));

        lista.add(new Carrossel_Item("Controle Glicêmico", "Receitas classificadas com índice glicêmico 1, 2 ou 3, para você comer com segurança.", R.drawable.vegetables));
       lista.add(new Carrossel_Item("Receitas Saudáveis", "Publique e descubra receitas criativas com ingredientes que você já tem na geladeira!", R.drawable.recipes));

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
            }
                else if(next==lista.size()){
                    mostrarDisclaimer();
            }
            else {
                finish();
            }
        });

        btnPular.setOnClickListener(v -> {
            Intent intent = new Intent(TutorialActivity.this, ReceitasActivity.class);
            startActivity(intent);
            finish(); // Fecha o tutorial para não voltar ao voltar do MainActivity
        });

    }
      // TERMOS E CONDIÇÕES - MODEO PADRÃO
    private void mostrarDisclaimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Termos de Uso");

        builder.setMessage("As classificações Glicê e as informações nutricionais contidas neste aplicativo " +
                "são geradas por Inteligência Artificial com base em regras predefinidas e não substituem o " +
                "aconselhamento de um médico, endocrinologista ou nutricionista. Sempre consulte um profissional " +
                "de saúde antes de fazer alterações significativas em sua dieta.");

        builder.setPositiveButton("Concordo", (dialog, which) -> {
            Intent intent = new Intent(TutorialActivity.this, ReceitasActivity.class);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

}
