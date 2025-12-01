package br.edu.fatecgru.glice.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.TextView;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;
import br.edu.fatecgru.glice.network.CloudinaryConfig;

public class UploadReceitaActivity extends AppCompatActivity {
    private ReceitaDAO receitaDAO = new ReceitaDAO();

    private ImageView imageViewFotoReceita;
    private EditText editTextNomeReceita;
    private EditText editTextModoPreparo;
    private Button buttonUploadReceita;
    private Button btnCancelar;

    private String imageFilePath = null;
    private ExecutorService executorService;
    private TextInputEditText editTextTempoPreparo;

    // Variáveis de ingredientes REMOVIDAS

    private Receita receitaParaEditar = null;
    public static final String EXTRA_RECEITA_PARA_EDITAR = "RECEITA_PARA_EDITAR";

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleImageUri);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_receita);

        imageViewFotoReceita = findViewById(R.id.imageView_fotoReceita);
        editTextNomeReceita = findViewById(R.id.editText_nomeReceita);
        editTextModoPreparo = findViewById(R.id.editText_modoPreparo);
        editTextTempoPreparo = findViewById(R.id.editText_tempoPreparo);
        buttonUploadReceita = findViewById(R.id.button_uploadReceita);
        btnCancelar = findViewById(R.id.btnCancelar);

        // Views de ingredientes REMOVIDAS da inicialização

        executorService = Executors.newSingleThreadExecutor();

        imageViewFotoReceita.setOnClickListener(v -> selectImage());
        buttonUploadReceita.setOnClickListener(v -> handlePublicarReceita());
        btnCancelar.setOnClickListener(v-> finish());
        // Listener de adicionar ingrediente REMOVIDO

        if (getIntent().hasExtra(EXTRA_RECEITA_PARA_EDITAR)) {
            receitaParaEditar = getIntent().getParcelableExtra(EXTRA_RECEITA_PARA_EDITAR);
            if (receitaParaEditar != null) {
                preencherCamposParaEdicao(receitaParaEditar);
            }
        }
        // Lógica de adicionar ingrediente inicial REMOVIDA
    }

    private void preencherCamposParaEdicao(Receita receita) {
        ((TextView)findViewById(R.id.textView_titulo)).setText("Editar Receita");
        buttonUploadReceita.setText("Atualizar Receita");

        editTextNomeReceita.setText(receita.getNome());
        editTextModoPreparo.setText(receita.getPreparo());
        editTextTempoPreparo.setText(String.valueOf(receita.getTempoPreparo()));

        // Lógica de preenchimento de ingredientes REMOVIDA
    }

    // MÉTODOS DE INGREDIENTES (adicionarNovoIngrediente, IngredienteParse, parseIngredienteDetalhe, coletar...) FORAM REMOVIDOS

    private void handlePublicarReceita() {
        String nome = editTextNomeReceita.getText().toString().trim();
        String preparo = editTextModoPreparo.getText().toString().trim();
        String tempoPreparo = editTextTempoPreparo.getText().toString().trim();

        // Coleta de ingredientes REMOVIDA

        // Usaremos listas vazias, mas mantemos o formato do método para evitar grandes refatorações
        List<String> ingredientesDetalhe = new ArrayList<>();
        List<String> nomesIngredientes = new ArrayList<>();


        if (nome.isEmpty() || preparo.isEmpty() || tempoPreparo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos da receita.", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonUploadReceita.setEnabled(false);
        String mensagem = receitaParaEditar == null ? "Iniciando upload e publicação..." : "Iniciando verificação e atualização...";
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();


        if (receitaParaEditar != null) {
            if (imageFilePath != null) {
                uploadImageAndSaveRecipe(imageFilePath, nome, preparo, tempoPreparo, ingredientesDetalhe, nomesIngredientes);
            } else {
                // Passa os dados de ingredientes EXISTENTES do objeto Receita para manter no DB
                atualizarDadosDaReceita(nome, preparo, receitaParaEditar.getUrlImagem(), tempoPreparo, receitaParaEditar.getIngredientesDetalhe(), receitaParaEditar.getNomesIngredientes());
            }
        } else {
            if (imageFilePath == null) {
                Toast.makeText(this, "Por favor, selecione uma foto para a receita.", Toast.LENGTH_SHORT).show();
                buttonUploadReceita.setEnabled(true);
                return;
            }
            uploadImageAndSaveRecipe(imageFilePath, nome, preparo, tempoPreparo, ingredientesDetalhe, nomesIngredientes);
        }
    }

    private void uploadImageAndSaveRecipe(String path, String nome, String preparo, String tempo,
                                          List<String> ingredientesDetalhe, List<String> nomesIngredientes) {
        File file = new File(path);
        Cloudinary cloudinary = CloudinaryConfig.getInstance();

        executorService.execute(() -> {
            try {
                Map uploadResult = cloudinary.uploader().upload(
                        file,
                        ObjectUtils.asMap("folder", "receitas_app", "secure", true)
                );
                String imageUrl = (String) uploadResult.get("secure_url");

                runOnUiThread(() -> {
                    if (imageUrl != null) {
                        if (receitaParaEditar != null) {
                            // Ao atualizar, precisamos garantir que os ingredientes antigos sejam preservados se a imagem mudar
                            atualizarDadosDaReceita(nome, preparo, imageUrl, tempo, receitaParaEditar.getIngredientesDetalhe(), receitaParaEditar.getNomesIngredientes());
                        } else {
                            salvarDadosDaReceita(nome, preparo, imageUrl, tempo, ingredientesDetalhe, nomesIngredientes);
                        }
                    } else {
                        Toast.makeText(this, "Erro de Upload: URL não retornada.", Toast.LENGTH_LONG).show();
                        buttonUploadReceita.setEnabled(true);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erro de Upload da Imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    buttonUploadReceita.setEnabled(true);
                });
            }
        });
    }

    private void salvarDadosDaReceita(String nome, String preparo, String imageUrl, String tempo,
                                      List<String> ingredientesDetalhe, List<String> nomesIngredientes) {

        int indiceGlicemico = 0;

        int tempoPreparoInt = 0;
        try {
            tempoPreparoInt = Integer.parseInt(tempo);
        } catch (NumberFormatException e) {
            Log.e("Upload", "Erro ao converter tempo de preparo para int", e);
        }

        Receita novaReceita = new Receita(
                nome,
                indiceGlicemico,
                imageUrl,
                "App Glice",
                preparo,
                tempoPreparoInt,
                new ArrayList<>(), // Inserindo listas vazias para novas receitas
                new ArrayList<>()
        );

        receitaDAO.salvarNovaReceita(novaReceita, new ReceitaDAO.SalvarReceitaCallback() {
            @Override
            public void onSuccess(Receita receitaSalva) {
                Toast.makeText(UploadReceitaActivity.this, "Receita publicada com sucesso!", Toast.LENGTH_LONG).show();
                buttonUploadReceita.setEnabled(true);
                finish();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(UploadReceitaActivity.this, "Falha ao salvar a receita: " + msg, Toast.LENGTH_LONG).show();
                buttonUploadReceita.setEnabled(true);
            }
        });
    }

    private void atualizarDadosDaReceita(String nome, String preparo, String imageUrl, String tempo,
                                         List<String> ingredientesDetalhe, List<String> nomesIngredientes) {

        int indiceGlicemico = receitaParaEditar.getIndiceGlicemico();

        int tempoPreparoInt = 0;
        try {
            tempoPreparoInt = Integer.parseInt(tempo);
        } catch (NumberFormatException e) {
            Log.e("Upload", "Erro ao converter tempo de preparo para int", e);
        }

        Receita receitaAtualizada = receitaParaEditar;
        receitaAtualizada.setNome(nome);
        receitaAtualizada.setFotoUrl(imageUrl);
        receitaAtualizada.setPreparo(preparo);
        receitaAtualizada.setTempoPreparo(tempoPreparoInt);

        // Preserva os ingredientes e nomes existentes (passados como argumento)
        receitaAtualizada.setIngredientesDetalhe(ingredientesDetalhe);
        receitaAtualizada.setNomesIngredientes(nomesIngredientes);

        receitaDAO.atualizarReceita(receitaAtualizada, new ReceitaDAO.AtualizarReceitaCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UploadReceitaActivity.this, "Receita atualizada com sucesso!", Toast.LENGTH_LONG).show();
                buttonUploadReceita.setEnabled(true);
                finish();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(UploadReceitaActivity.this, "Falha ao atualizar a receita: " + msg, Toast.LENGTH_LONG).show();
                buttonUploadReceita.setEnabled(true);
            }
        });
    }

    private void selectImage() {
        galleryLauncher.launch("image/*");
    }

    private void handleImageUri(Uri uri) {
        if (uri != null) {
            imageViewFotoReceita.setImageURI(uri);
            imageFilePath = getPathFromUri(uri);

            if (imageFilePath == null) {
                Toast.makeText(this, "Não foi possível obter o caminho do arquivo.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String path = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e("UploadReceita", "Erro ao buscar caminho do URI", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}