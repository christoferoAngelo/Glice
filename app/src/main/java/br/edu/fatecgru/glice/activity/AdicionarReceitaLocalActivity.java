package br.edu.fatecgru.glice.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.model.ReceitaPessoal;
import br.edu.fatecgru.glice.viewmodel.ReceitaLocalViewModel;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdicionarReceitaLocalActivity extends AppCompatActivity {

    private static final String TAG = "AdicionarReceitaLocal";

    private TextInputEditText editTitulo, editIngredientes, editPreparo, editAnotacoes, editFonte, editLink;
    private TextView tvNomeReceita;
    private MaterialButton btnSalvar;
    private ImageView imagePreview;
    private ReceitaLocalViewModel viewModel;
    private ReceitaPessoal receitaParaEditar;
    private Button btnCancelar;

    private Uri uriImagemSelecionada;
    private ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_receita_local);

        // Inicializa Views
        tvNomeReceita = findViewById(R.id.tvNomeReceita);
        editTitulo = findViewById(R.id.editTitulo);
        editIngredientes = findViewById(R.id.editIngredientes);
        editPreparo = findViewById(R.id.editPreparo);
        editAnotacoes = findViewById(R.id.editAnotacoes);
        editFonte = findViewById(R.id.editFonte);
        editLink = findViewById(R.id.editLink);
        btnSalvar = findViewById(R.id.btnSalvar);
        imagePreview = findViewById(R.id.imagePreview);

        btnCancelar = findViewById(R.id.btnCancelarEnvio);

        viewModel = new ViewModelProvider(this).get(ReceitaLocalViewModel.class);



        configurarSelecaoImagemLauncher();


        imagePreview.setOnClickListener(this::selecionarImagemReceita);

        // Atualiza título em tempo real
        editTitulo.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvNomeReceita.setText(s.length() > 0 ? s.toString() : "Nome da Receita");
            }
            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });






        // Verifica edição
        Intent intent = getIntent();
        if (intent.hasExtra("receita_para_editar")) {
            receitaParaEditar = intent.getParcelableExtra("receita_para_editar");
            preencherCamposParaEdicao();
            setTitle("Editar Receita");
            btnSalvar.setText("Salvar Alterações");
        } else {
            setTitle("Adicionar Receita");
            btnSalvar.setText("Salvar Receita");
            imagePreview.setImageResource(R.drawable.outline_menu_book_2_24);
        }

        btnSalvar.setOnClickListener(v -> salvarReceita());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void configurarSelecaoImagemLauncher() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String caminhoInterno = copiarImagemParaArmazenamentoInterno(uri);
                if (caminhoInterno != null) {
                    uriImagemSelecionada = Uri.fromFile(new File(caminhoInterno));
                    Glide.with(this)
                            .load(new File(caminhoInterno))
                            .placeholder(R.drawable.outline_menu_book_2_24)
                            .error(R.drawable.outline_menu_book_2_24)
                            .into(imagePreview);
                    Toast.makeText(this, "Imagem selecionada!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String copiarImagemParaArmazenamentoInterno(Uri uriOrigem) {
        try {
            File dir = new File(getFilesDir(), "Pictures");
            if (!dir.exists()) dir.mkdirs();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File destino = new File(dir, "IMG_" + timeStamp + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(uriOrigem);
                 FileOutputStream out = new FileOutputStream(destino)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            Log.d(TAG, "Imagem copiada para: " + destino.getAbsolutePath());
            return destino.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Erro ao copiar imagem", e);
            return null;
        }
    }

    public void selecionarImagemReceita(View view) {
        mGetContent.launch("image/*");
    }

    private void preencherCamposParaEdicao() {
        if (receitaParaEditar != null) {
            editTitulo.setText(receitaParaEditar.getTitulo());
            tvNomeReceita.setText(receitaParaEditar.getTitulo());
            editIngredientes.setText(receitaParaEditar.getIngredientes());
            editPreparo.setText(receitaParaEditar.getPreparo());
            editAnotacoes.setText(receitaParaEditar.getAnotacoes());
            editFonte.setText(receitaParaEditar.getFonte());
            editLink.setText(receitaParaEditar.getLink());

            if (receitaParaEditar.getImageUrl() != null && !receitaParaEditar.getImageUrl().isEmpty()) {
                File arquivo = new File(receitaParaEditar.getImageUrl());
                if (arquivo.exists()) {
                    uriImagemSelecionada = Uri.fromFile(arquivo);
                    Glide.with(this)
                            .load(arquivo)
                            .placeholder(R.drawable.outline_menu_book_2_24)
                            .error(R.drawable.outline_menu_book_2_24)
                            .into(imagePreview);
                }
            }
        }
    }

    private void salvarReceita() {
        String titulo = editTitulo.getText().toString().trim();
        String ingredientes = editIngredientes.getText().toString().trim();
        String preparo = editPreparo.getText().toString().trim();

        if (titulo.isEmpty() || ingredientes.isEmpty() || preparo.isEmpty()) {
            Toast.makeText(this, "Título, ingredientes e preparo são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUriString = uriImagemSelecionada != null ? uriImagemSelecionada.getPath() : null;

        if (receitaParaEditar != null) {
            receitaParaEditar.setTitulo(titulo);
            receitaParaEditar.setIngredientes(ingredientes);
            receitaParaEditar.setPreparo(preparo);
            receitaParaEditar.setAnotacoes(editAnotacoes.getText().toString().trim());
            receitaParaEditar.setFonte(editFonte.getText().toString().trim());
            receitaParaEditar.setLink(editLink.getText().toString().trim());
            receitaParaEditar.setImageUrl(imageUriString);

            viewModel.update(receitaParaEditar);
            Toast.makeText(this, "Receita atualizada!", Toast.LENGTH_SHORT).show();
        } else {
            ReceitaPessoal novaReceita = new ReceitaPessoal(titulo, preparo);
            novaReceita.setIngredientes(ingredientes);
            novaReceita.setAnotacoes(editAnotacoes.getText().toString().trim());
            novaReceita.setFonte(editFonte.getText().toString().trim());
            novaReceita.setLink(editLink.getText().toString().trim());
            novaReceita.setImageUrl(imageUriString);

            viewModel.insert(novaReceita);
            Toast.makeText(this, "Receita salva!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
