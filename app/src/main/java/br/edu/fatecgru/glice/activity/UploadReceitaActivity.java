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

// Imports do Cloudinary e Utilitários (como CloudinaryConfig da resposta anterior)
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;
import br.edu.fatecgru.glice.network.CloudinaryConfig;
// ... (Você pode colocar a classe CloudinaryConfig aqui ou em um arquivo separado)

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView; // Usamos AutoCompleteTextView como Spinner em Material Design
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputEditText;

public class UploadReceitaActivity extends AppCompatActivity {
    private ReceitaDAO receitaDAO = new ReceitaDAO(); // Instancie sua DAO

    private ImageView imageViewFotoReceita;
    private EditText editTextNomeReceita;

    private EditText editTextModoPreparo;
    private Button buttonUploadReceita;
    private Button btnCancelar;

    private String imageFilePath = null; // Caminho do arquivo da imagem selecionada
    private ExecutorService executorService;
    private TextInputEditText editTextTempoPreparo; // Incluindo o tempo de preparo

    private Button buttonAdicionarIngrediente; // Novo botão
    private LinearLayout containerIngredientes; // Novo container para as linhas


    // Contrato para lidar com a seleção de imagem
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleImageUri);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_receita);

        // 1. Inicializar Views
        imageViewFotoReceita = findViewById(R.id.imageView_fotoReceita);
        editTextNomeReceita = findViewById(R.id.editText_nomeReceita);
        // REMOVIDO: findViewById(R.id.editText_ingredientes);
        editTextModoPreparo = findViewById(R.id.editText_modoPreparo);
        editTextTempoPreparo = findViewById(R.id.editText_tempoPreparo); // Inicializar este também!
        buttonUploadReceita = findViewById(R.id.button_uploadReceita);
        btnCancelar = findViewById(R.id.btnCancelar);

        // NOVAS VIEWS
        containerIngredientes = findViewById(R.id.container_ingredientes);
        buttonAdicionarIngrediente = findViewById(R.id.button_adicionarIngrediente);

        executorService = Executors.newSingleThreadExecutor();


        // 2. Configurar Listeners
        imageViewFotoReceita.setOnClickListener(v -> selectImage());
        buttonUploadReceita.setOnClickListener(v -> handlePublicarReceita());
        btnCancelar.setOnClickListener(v-> finish());
        // NOVO LISTENER
        buttonAdicionarIngrediente.setOnClickListener(v -> adicionarNovoIngrediente());

        // Adiciona um ingrediente inicial para não deixar o campo vazio
        adicionarNovoIngrediente();
    }

    // --- Métodos de Seleção de Imagem ---

    // --- NOVO MÉTODO: Adicionar Linha de Ingrediente ---
    private void adicionarNovoIngrediente() {
        // Infla o layout da linha de ingrediente
        final View ingredienteView = LayoutInflater.from(this)
                .inflate(R.layout.item_ingrediente, containerIngredientes, false);

        // Componentes da nova View
        AutoCompleteTextView spinnerMedida = ingredienteView.findViewById(R.id.spinner_medida);
        ImageButton buttonRemover = ingredienteView.findViewById(R.id.button_remover_ingrediente);

        // 1. Configurar o Spinner/AutoCompleteTextView para as medidas
        String[] medidas = {"g", "ml", "xícara", "colher de sopa", "pitada", "unidade", "a gosto"};
        // Importante: use 'android.R.layout.simple_spinner_dropdown_item'
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, medidas);
        spinnerMedida.setAdapter(adapter);

        // Adiciona um listener para o botão de remover
        buttonRemover.setOnClickListener(v -> {
            containerIngredientes.removeView(ingredienteView);
            // Opcional: Garantir que sempre haja pelo menos um campo
            if (containerIngredientes.getChildCount() == 0) {
                adicionarNovoIngrediente();
            }
        });

        // Adiciona a View ao container principal
        containerIngredientes.addView(ingredienteView);
    }

    // --- NOVO MÉTODO: Coletar os Ingredientes ---
    private String coletarIngredientesFormatados() {
        StringBuilder ingredientesBuilder = new StringBuilder();
        int count = containerIngredientes.getChildCount();

        for (int i = 0; i < count; i++) {
            View ingredienteView = containerIngredientes.getChildAt(i);

            // Views da linha
            TextInputEditText editQuantidade = ingredienteView.findViewById(R.id.edit_quantidade);
            AutoCompleteTextView spinnerMedida = ingredienteView.findViewById(R.id.spinner_medida);
            TextInputEditText editNomeIngrediente = ingredienteView.findViewById(R.id.edit_nome_ingrediente);

            // Coleta os valores
            String quantidade = editQuantidade.getText().toString().trim();
            String medida = spinnerMedida.getText().toString().trim();
            String nome = editNomeIngrediente.getText().toString().trim();

            // Se o nome do ingrediente não estiver vazio, adiciona à lista
            if (!nome.isEmpty()) {
                // Formato exigido: "[ingrediente] [medida] [quantidade]"
                // Para exibir no texto: "Quantidade Medida de Nome"
                ingredientesBuilder.append(quantidade).append(" ");
                ingredientesBuilder.append(medida).append(" de ");
                ingredientesBuilder.append(nome);

                // Adiciona quebra de linha, exceto para o último
                if (i < count - 1) {
                    ingredientesBuilder.append("\n");
                }
            }
        }
        return ingredientesBuilder.toString().trim();
    }

    private void selectImage() {
        // Solicita ao sistema que abra a galeria para selecionar uma imagem (MIME type "image/*")
        galleryLauncher.launch("image/*");
    }

    private void handleImageUri(Uri uri) {
        if (uri != null) {
            // Exibir a imagem na ImageView
            imageViewFotoReceita.setImageURI(uri);

            // ATENÇÃO: É NECESSÁRIO CONVERTER O URI PARA UM CAMINHO DE ARQUIVO (FILE PATH)
            // Esta conversão pode ser complexa. Para simplicidade, assuma que esta função existe:
            imageFilePath = getPathFromUri(uri);

            if (imageFilePath == null) {
                Toast.makeText(this, "Não foi possível obter o caminho do arquivo.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // >>> Importante: Implementar o método getPathFromUri <<<
    // Esta função requer manipulação de ContentResolver e Cursor, sendo específica para cada versão do Android.
    // Você deve procurar uma função utilitária confiável para fazer esta conversão, pois é um ponto de falha comum.
    private String getPathFromUri(Uri uri) {
        // *** Implementação Real do ContentResolver e Cursor ***
        // Para a maioria dos casos simples:
        String path = null;
        try {
            // Exemplo Simplificado (Pode não funcionar em todos os dispositivos/versões)
            String[] projection = { android.provider.MediaStore.Images.Media.DATA };
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Tentar outras abordagens, como o Storage Access Framework (SAF)
        }
        return path;
    }


    // --- Método Principal de Publicação ---

    private void handlePublicarReceita() {
        String nome = editTextNomeReceita.getText().toString().trim();
        String preparo = editTextModoPreparo.getText().toString().trim();
        String ingredientesFormatados = coletarIngredientesFormatados();
        String tempoPreparo = editTextTempoPreparo.getText().toString().trim();
        if (nome.isEmpty() || ingredientesFormatados.isEmpty() || preparo.isEmpty() || nome.isEmpty() || tempoPreparo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos da receita.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageFilePath == null) {
            Toast.makeText(this, "Por favor, selecione uma foto para a receita.", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonUploadReceita.setEnabled(false); // Desabilita para evitar cliques múltiplos
        Toast.makeText(this, "Iniciando upload e publicação...", Toast.LENGTH_LONG).show();

        // Inicia o processo de upload da imagem em uma Thread Separada
        uploadImageAndSaveRecipe(imageFilePath, nome, ingredientesFormatados, preparo, tempoPreparo);
    }

    // --- Lógica do Cloudinary (Da sua pergunta anterior) ---

    private void uploadImageAndSaveRecipe(String path, String nome, String ingredientes, String preparo, String tempo) {
        File file = new File(path);
        Cloudinary cloudinary = CloudinaryConfig.getInstance();

        executorService.execute(() -> {
            try {
                // 1. Upload para o Cloudinary
                Map uploadResult = cloudinary.uploader().upload(
                        file,
                        ObjectUtils.asMap("folder", "receitas_app", "secure", true) // Opcional: organiza suas imagens
                );


                String imageUrl = (String) uploadResult.get("secure_url");

                // 2. Volta para a Main Thread para salvar os dados no banco
                runOnUiThread(() -> {
                    if (imageUrl != null) {
                        // SUCESSO: Salva a receita com a URL
                        salvarDadosDaReceita(nome, ingredientes, preparo, imageUrl, tempo);
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


    // ... (dentro da classe UploadReceitaActivity) ...

    private void salvarDadosDaReceita(String nome, String ingredientes, String preparo, String imageUrl, String tempo) {

        // Converte o tempo (agora uma String) para Integer para o modelo
        int tempoEmMinutos = 0;
        try {
            tempoEmMinutos = Integer.parseInt(tempo);
        } catch (NumberFormatException e) {
            Log.e("UploadReceita", "Tempo de preparo inválido: " + tempo);
            // Opcional: Exibir Toast de erro ou usar um valor padrão.
        }

        // 1. Cria o objeto Receita
        Receita novaReceita = new Receita(
                nome,
                tempoEmMinutos, // AGORA USAMOS tempoEmMinutos
                imageUrl,
                "App Glice",
                ingredientes,
                preparo
        );

        // 2. Chama a DAO para salvar no Firebase Firestore
        receitaDAO.salvarNovaReceita(novaReceita, new ReceitaDAO.SalvarReceitaCallback() {
            @Override
            public void onSuccess(Receita receitaSalva) {
                // Sucesso no salvamento do Firebase
                Toast.makeText(UploadReceitaActivity.this, "Receita publicada com sucesso!", Toast.LENGTH_LONG).show();
                buttonUploadReceita.setEnabled(true);
                finish(); // Fecha a Activity
            }

            @Override
            public void onError(String msg) {
                // Falha no Firebase
                Toast.makeText(UploadReceitaActivity.this, "Falha ao salvar a receita: " + msg, Toast.LENGTH_LONG).show();
                buttonUploadReceita.setEnabled(true); // Reabilita o botão para que o usuário tente novamente
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Garante que o executor seja desligado
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}