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

public class UploadReceitaActivity extends AppCompatActivity {
    private ReceitaDAO receitaDAO = new ReceitaDAO(); // Instancie sua DAO

    private ImageView imageViewFotoReceita;
    private EditText editTextNomeReceita;
    private EditText editTextIngredientes;
    private EditText editTextModoPreparo;
    private Button buttonUploadReceita;

    private String imageFilePath = null; // Caminho do arquivo da imagem selecionada
    private ExecutorService executorService;

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
        editTextIngredientes = findViewById(R.id.editText_ingredientes);
        editTextModoPreparo = findViewById(R.id.editText_modoPreparo);
        buttonUploadReceita = findViewById(R.id.button_uploadReceita);

        // Inicializar o Executor
        executorService = Executors.newSingleThreadExecutor();


        // 2. Configurar Listeners
        imageViewFotoReceita.setOnClickListener(v -> selectImage());
        buttonUploadReceita.setOnClickListener(v -> handlePublicarReceita());
    }

    // --- Métodos de Seleção de Imagem ---

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
        String ingredientes = editTextIngredientes.getText().toString().trim();
        String preparo = editTextModoPreparo.getText().toString().trim();

        if (nome.isEmpty() || ingredientes.isEmpty() || preparo.isEmpty()) {
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
        uploadImageAndSaveRecipe(imageFilePath, nome, ingredientes, preparo);
    }

    // --- Lógica do Cloudinary (Da sua pergunta anterior) ---

    private void uploadImageAndSaveRecipe(String path, String nome, String ingredientes, String preparo) {
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
                        salvarDadosDaReceita(nome, ingredientes, preparo, imageUrl);
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

    private void salvarDadosDaReceita(String nome, String ingredientes, String preparo, String imageUrl) {
        // 1. Cria o objeto Receita
        Receita novaReceita = new Receita(
                nome,
                // Indice: Usando 0 como padrão.
                0,
                imageUrl,
                // Fonte: Usando "App Glice" como padrão.
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