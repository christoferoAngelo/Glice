package br.edu.fatecgru.glice.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
    int ultimoSlide = lista.size()-1; //Guardar tamanho para descobrir em qual slide o user tá

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

        lista.add(new Carrossel_Item("Controle Glicêmico", "Receitas classificadas com índice glicêmico 1, 2 ou 3, informado na própria receita. Saiba mais sobre os índices na próxima tela!", R.drawable.vegetables));
        lista.add(new Carrossel_Item("Glicê 01", "Sem Açúcar adicionado, usa adoçantes próprios para diabetes, baixo impacto glicêmico.", R.drawable.fruit));
        lista.add(new Carrossel_Item("Glicê 02", "Contém apenas açúcares naturais ou carboidratos complexos; baixo a moderado impacto.", R.drawable.rice));
        lista.add(new Carrossel_Item("Glicê 03", "Contém qualquer açúcar adicionado ou carboidratos simples.", R.drawable.sugar));
        lista.add(new Carrossel_Item("Queremos Ouvir Você!", "Caso alguma receita esteja classificada com um índice incorreto,você pode contestar.", R.drawable.mark));
        lista.add(new Carrossel_Item("Seu Livro de Receitas", "Alimente o seu perfil com receitas criativas!", R.drawable.recipes));
        lista.add(new Carrossel_Item("Publique Receitas", "Envie suas receitas para o e-mail glice.oficial@gmail.com, para serem verificadas e postadas!", R.drawable.email));

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
            mostrarDisclaimer();
        });

    }

    private void mostrarDisclaimer() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_disclaimer);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        Button btnOk = dialog.findViewById(R.id.btnOk);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);

        btnOk.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("glice_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("tutorial_visto", true);
            editor.apply();

            Intent intent = new Intent(TutorialActivity.this, ReceitasActivity.class);
            startActivity(intent);
            dialog.dismiss();
            finish();
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

}
