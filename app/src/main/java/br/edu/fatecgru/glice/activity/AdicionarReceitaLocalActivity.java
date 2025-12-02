package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.model.ReceitaPessoal;
import br.edu.fatecgru.glice.viewmodel.ReceitaLocalViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdicionarReceitaLocalActivity extends AppCompatActivity {

    private TextInputEditText editTitulo, editIngredientes, editPreparo, editAnotacoes, editFonte, editLink;
    private MaterialButton btnSalvar;
    private ReceitaLocalViewModel viewModel;
    private ReceitaPessoal receitaParaEditar;  // Receita a editar (null se for nova)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_receita_local);  // Atualizado o nome do layout

        // Inicializa Views
        editTitulo = findViewById(R.id.editTitulo);
        editIngredientes = findViewById(R.id.editIngredientes);
        editPreparo = findViewById(R.id.editPreparo);
        editAnotacoes = findViewById(R.id.editAnotacoes);
        editFonte = findViewById(R.id.editFonte);
        editLink = findViewById(R.id.editLink);
        btnSalvar = findViewById(R.id.btnSalvar);

        // Inicializa ViewModel
        viewModel = new ViewModelProvider(this).get(ReceitaLocalViewModel.class);

        // Verifica se uma receita foi passada para edição
        Intent intent = getIntent();
        if (intent.hasExtra("receita_para_editar")) {
            receitaParaEditar = (ReceitaPessoal) intent.getParcelableExtra("receita_para_editar");
            preencherCamposParaEdicao();
            setTitle("Editar Receita");  // Muda o título da ActionBar
            btnSalvar.setText("Salvar Alterações");  // Muda o texto do botão
        } else {
            setTitle("Adicionar Receita");
            btnSalvar.setText("Salvar Receita");
        }

        // Listener do botão Salvar
        btnSalvar.setOnClickListener(v -> salvarReceita());
    }

    private void preencherCamposParaEdicao() {
        if (receitaParaEditar != null) {
            editTitulo.setText(receitaParaEditar.getTitulo());
            editIngredientes.setText(receitaParaEditar.getIngredientes());
            editPreparo.setText(receitaParaEditar.getPreparo());
            editAnotacoes.setText(receitaParaEditar.getAnotacoes());
            editFonte.setText(receitaParaEditar.getFonte());
            editLink.setText(receitaParaEditar.getLink());
        }
    }

    private void salvarReceita() {
        String titulo = editTitulo.getText().toString().trim();
        String preparo = editPreparo.getText().toString().trim();

        if (titulo.isEmpty() || preparo.isEmpty()) {
            Toast.makeText(this, "Título e preparo são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (receitaParaEditar != null) {
            // Edição: Atualiza a receita existente
            receitaParaEditar.setTitulo(titulo);
            receitaParaEditar.setIngredientes(editIngredientes.getText().toString().trim());
            receitaParaEditar.setPreparo(preparo);
            receitaParaEditar.setAnotacoes(editAnotacoes.getText().toString().trim());
            receitaParaEditar.setFonte(editFonte.getText().toString().trim());
            receitaParaEditar.setLink(editLink.getText().toString().trim());
            viewModel.update(receitaParaEditar);
            Toast.makeText(this, "Receita atualizada!", Toast.LENGTH_SHORT).show();
        } else {
            // Nova receita: Cria e insere
            ReceitaPessoal novaReceita = new ReceitaPessoal(titulo, preparo);
            novaReceita.setIngredientes(editIngredientes.getText().toString().trim());
            novaReceita.setAnotacoes(editAnotacoes.getText().toString().trim());
            novaReceita.setFonte(editFonte.getText().toString().trim());
            novaReceita.setLink(editLink.getText().toString().trim());
            viewModel.insert(novaReceita);
            Toast.makeText(this, "Receita salva!", Toast.LENGTH_SHORT).show();
        }

        finish();  // Fecha a Activity após salvar
    }
}