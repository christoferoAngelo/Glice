package br.edu.fatecgru.glice.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;
import br.edu.fatecgru.glice.network.CloudinaryConfig;

public class UploadReceitaActivity extends AppCompatActivity {

    private ReceitaDAO receitaDAO = new ReceitaDAO();
    private ExecutorService executorService;

    // UI Views existentes
    private ImageView imageViewFotoReceita;
    private TextInputEditText editTextNomeReceita;
    private TextInputEditText editTextModoPreparo;
    private TextInputEditText editTextTempoPreparo;
    private Button buttonUploadReceita;
    private Button btnCancelar;
    private Button buttonExcluirReceita;

    // UI Views (Metadados)
    private TextInputEditText editTextResumoReceita;
    private RadioGroup radioGroupIndiceGlicemico;
    private TextInputEditText editTextJustificativaGlice;
    private TextInputEditText editTextSubstituicoes;
    private TextInputEditText editTextFonte;
    private TextInputEditText editTextLinkReceita;

    // UI Views para Ingredientes
    private LinearLayout containerIngredientes;
    private Button buttonAdicionarIngrediente;

    private String imageFilePath = null;
    private Receita receitaParaEditar = null;
    public static final String EXTRA_RECEITA_PARA_EDITAR = "RECEITA_PARA_EDITAR";

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::handleImageUri);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_receita);

        // 1. Inicialização das Views
        imageViewFotoReceita = findViewById(R.id.imageView_fotoReceita);
        editTextNomeReceita = findViewById(R.id.editText_nomeReceita);
        editTextModoPreparo = findViewById(R.id.editText_modoPreparo);
        editTextTempoPreparo = findViewById(R.id.editText_tempoPreparo);
        buttonUploadReceita = findViewById(R.id.button_uploadReceita);
        btnCancelar = findViewById(R.id.btnCancelar);
        buttonExcluirReceita = findViewById(R.id.button_excluirReceita);

        // Inicialização das NOVAS Views (Metadados)
        editTextResumoReceita = findViewById(R.id.editText_resumoReceita);
        radioGroupIndiceGlicemico = findViewById(R.id.radioGroup_indiceGlicemico);
        editTextJustificativaGlice = findViewById(R.id.editText_justificativaGlice);
        editTextSubstituicoes = findViewById(R.id.editText_substituicoes);
        editTextFonte = findViewById(R.id.editText_fonte);
        editTextLinkReceita = findViewById(R.id.editText_linkReceita);

        // Inicialização das Views de Ingredientes
        containerIngredientes = findViewById(R.id.container_ingredientes);
        buttonAdicionarIngrediente = findViewById(R.id.button_adicionarIngrediente);

        executorService = Executors.newSingleThreadExecutor();

        // 2. Listeners
        imageViewFotoReceita.setOnClickListener(v -> selectImage());
        buttonUploadReceita.setOnClickListener(v -> handlePublicarReceita());
        btnCancelar.setOnClickListener(v-> finish());
        buttonAdicionarIngrediente.setOnClickListener(v -> adicionarNovoIngrediente(null));

        // 3. Verifica Modo de Edição
        if (getIntent().hasExtra(EXTRA_RECEITA_PARA_EDITAR)) {
            receitaParaEditar = getIntent().getParcelableExtra(EXTRA_RECEITA_PARA_EDITAR);
            if (receitaParaEditar != null) {
                preencherCamposParaEdicao(receitaParaEditar);
            }
        } else {
            // Adiciona um campo de ingrediente inicial se for nova receita
            adicionarNovoIngrediente(null);
        }

        // 4. Listener de Exclusão (Apenas em Edição)
        buttonExcluirReceita.setOnClickListener(v -> excluirReceita());
    }

    // --- LÓGICA DE INGREDIENTES DINÂMICOS ---

    private void adicionarNovoIngrediente(String valorInicial) {
        // Infla o layout para a linha de ingrediente
        final View ingredienteView = LayoutInflater.from(this).inflate(
                R.layout.item_ingrediente_input,
                containerIngredientes,
                false
        );

        final TextInputEditText input = ingredienteView.findViewById(R.id.editText_ingrediente);
        final ImageButton btnRemover = ingredienteView.findViewById(R.id.button_removerIngrediente);

        if (valorInicial != null) {
            input.setText(valorInicial);
        }

        btnRemover.setOnClickListener(v -> containerIngredientes.removeView(ingredienteView));

        containerIngredientes.addView(ingredienteView);
    }

    private List<String> coletarIngredientesDetalhe() {
        List<String> lista = new ArrayList<>();
        int count = containerIngredientes.getChildCount();

        for (int i = 0; i < count; i++) {
            View view = containerIngredientes.getChildAt(i);
            TextInputEditText input = view.findViewById(R.id.editText_ingrediente);

            if (input != null) {
                String detalhe = input.getText().toString().trim();
                if (!detalhe.isEmpty()) {
                    lista.add(detalhe);
                }
            }
        }
        return lista;
    }
    private List<String> extrairNomesLimpos(List<String> detalhe) {
        // Definição das palavras de ruído (medidas e preposições comuns)
        List<String> stopWords = java.util.Arrays.asList(
                "unidade", "unidades", "un", "unid", "colher", "colheres", "chá", "sopa", "rasa",
                "xícara", "xicaras", "copo", "copos", "grama", "gramas", "g", "ml", "litro", "l",
                "kilo", "quilo", "kg", "mg", "pitada", "pingo", "fatia", "fatias", "a", "o", "de",
                "da", "do", "das", "dos", "em", "um", "uma", "e", "ou", "q.b", "quanto", "bas",
                "baste", "meia", "inteira", "grande", "pequena", "pequeno", "médio", "médios"
        );

        List<String> nomesLimpos = new ArrayList<>();

        for (String item : detalhe) {
            String itemLower = item.toLowerCase();

            // Pré-processamento: remove números e pontuações/caracteres especiais
            // Mantém apenas letras e espaços
            String processedItem = itemLower.replaceAll("[^a-záéíóúãõç\\s]", " ").trim();

            // Quebrar em palavras e filtrar stop words
            String[] palavras = processedItem.split("\\s+");
            StringBuilder nomePuro = new StringBuilder();

            for (String palavra : palavras) {
                if (!palavra.isEmpty() && palavra.length() > 2 && !stopWords.contains(palavra)) {
                    nomePuro.append(palavra).append(" ");
                }
            }

            String nomeFinal = nomePuro.toString().trim();

            if (!nomeFinal.isEmpty()) {
                // Adiciona o nome puro. Pode ser uma palavra ("ovo") ou uma frase ("farinha de aveia")
                nomesLimpos.add(nomeFinal);
            }
        }

        // Garante que não haja nomes duplicados (útil para o Firestore)
        return new ArrayList<>(new java.util.HashSet<>(nomesLimpos));
    }

    // --- LÓGICA DE EDIÇÃO E PREENCHIMENTO ---
    private void preencherCamposParaEdicao(Receita receita) {
        TextView titulo = findViewById(R.id.textView_titulo);
        titulo.setText("Editar Receita");
        buttonUploadReceita.setText("Atualizar Receita");
        buttonExcluirReceita.setVisibility(View.VISIBLE);

        editTextNomeReceita.setText(receita.getNome());
        editTextResumoReceita.setText(receita.getResumo());
        editTextModoPreparo.setText(receita.getPreparo());
        editTextTempoPreparo.setText(String.valueOf(receita.getTempoPreparo()));
        editTextJustificativaGlice.setText(receita.getJustificativaGlice());
        editTextSubstituicoes.setText(receita.getSubstituicoes());
        editTextFonte.setText(receita.getFonte());
        editTextLinkReceita.setText(receita.getLinkReceita());

        // Preencher Índice Glicêmico
        int radioId = 0;
        if (receita.getIndiceGlicemico() == 1) {
            radioId = R.id.radio_glicemico1;
        } else if (receita.getIndiceGlicemico() == 2) {
            radioId = R.id.radio_glicemico2;
        } else if (receita.getIndiceGlicemico() == 3) {
            radioId = R.id.radio_glicemico3;
        }
        if (radioId != 0) {
            radioGroupIndiceGlicemico.check(radioId);
        }

        // Preencher Ingredientes
        containerIngredientes.removeAllViews(); // Limpa o campo inicial
        if (receita.getIngredientesDetalhe() != null) {
            for (String ingrediente : receita.getIngredientesDetalhe()) {
                adicionarNovoIngrediente(ingrediente);
            }
        }

        // Preencher Imagem (se existir)
        if (receita.getUrlImagem() != null && !receita.getUrlImagem().isEmpty()) {
            Glide.with(this).load(receita.getUrlImagem()).into(imageViewFotoReceita);
        }
    }

    // --- LÓGICA DE EXCLUSÃO ---

    private void excluirReceita() {
        if (receitaParaEditar == null || receitaParaEditar.getDocumentId() == null) {
            Toast.makeText(this, "Erro: Não há receita selecionada para excluir.", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonExcluirReceita.setEnabled(false);
        Toast.makeText(this, "Excluindo receita...", Toast.LENGTH_SHORT).show();

        receitaDAO.excluirReceita(receitaParaEditar.getDocumentId(), new ReceitaDAO.ExcluirReceitaCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UploadReceitaActivity.this, "Receita excluída com sucesso!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(UploadReceitaActivity.this, "Falha ao excluir a receita: " + msg, Toast.LENGTH_LONG).show();
                buttonExcluirReceita.setEnabled(true);
            }
        });
    }

    // --- LÓGICA PRINCIPAL DE SALVAMENTO/ATUALIZAÇÃO ---

    private void handlePublicarReceita() {

        // Coletar todos os dados da UI
        String nome = editTextNomeReceita.getText().toString().trim();
        String resumo = editTextResumoReceita.getText().toString().trim();
        String preparo = editTextModoPreparo.getText().toString().trim();
        String tempoStr = editTextTempoPreparo.getText().toString().trim();
        String justificativa = editTextJustificativaGlice.getText().toString().trim();
        String fonte = editTextFonte.getText().toString().trim();
        // Opcionais
        String substituicoes = editTextSubstituicoes.getText().toString().trim();
        String linkReceita = editTextLinkReceita.getText().toString().trim();

        List<String> ingredientesDetalhe = coletarIngredientesDetalhe();
        int indiceGlicemico = getSelectedIndiceGlicemico();

        // 1. VALIDAÇÃO DOS CAMPOS OBRIGATÓRIOS
        if (nome.isEmpty() || resumo.isEmpty() || preparo.isEmpty() || tempoStr.isEmpty() ||
                justificativa.isEmpty() || fonte.isEmpty() || ingredientesDetalhe.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios (Nome, Resumo, Preparo, Tempo, Justificativa, Fonte e Ingredientes).", Toast.LENGTH_LONG).show();
            return;
        }

        if (indiceGlicemico == 0) {
            Toast.makeText(this, "Selecione a classificação do Índice Glicêmico.", Toast.LENGTH_LONG).show();
            return;
        }

        if (receitaParaEditar == null && imageFilePath == null) {
            Toast.makeText(this, "Por favor, selecione uma foto para a receita.", Toast.LENGTH_LONG).show();
            return;
        }

        buttonUploadReceita.setEnabled(false);
        String mensagem = receitaParaEditar == null ? "Iniciando upload e publicação..." : "Iniciando verificação e atualização...";
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();

        // 2. EXTRAIR NOMES LIMPOS
        List<String> nomesIngredientes = extrairNomesLimpos(ingredientesDetalhe);

        // 3. DECISÃO DE UPLOAD E SALVAMENTO
        if (receitaParaEditar != null) {
            if (imageFilePath != null) {
                // Modo Edição: Nova imagem selecionada
                uploadImageAndSaveRecipe(imageFilePath, nome, resumo, preparo, tempoStr, fonte, justificativa, substituicoes, linkReceita, ingredientesDetalhe, nomesIngredientes);
            } else {
                // Modo Edição: Imagem não mudou
                atualizarDadosDaReceita(nome, resumo, preparo, receitaParaEditar.getUrlImagem(), tempoStr, indiceGlicemico, fonte, justificativa, substituicoes, linkReceita, ingredientesDetalhe, nomesIngredientes);
            }
        } else {
            // Nova Receita
            uploadImageAndSaveRecipe(imageFilePath, nome, resumo, preparo, tempoStr, fonte, justificativa, substituicoes, linkReceita, ingredientesDetalhe, nomesIngredientes);
        }
    }

    private int getSelectedIndiceGlicemico() {
        int selectedId = radioGroupIndiceGlicemico.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_glicemico1) return 1;
        if (selectedId == R.id.radio_glicemico2) return 2;
        if (selectedId == R.id.radio_glicemico3) return 3;
        return 0; // Não selecionado
    }

    private void uploadImageAndSaveRecipe(String path, String nome, String resumo, String preparo, String tempo, String fonte, String justificativa, String substituicoes, String linkReceita,
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
                            atualizarDadosDaReceita(nome, resumo, preparo, imageUrl, tempo, getSelectedIndiceGlicemico(), fonte, justificativa, substituicoes, linkReceita, ingredientesDetalhe, nomesIngredientes);
                        } else {
                            salvarDadosDaReceita(nome, resumo, preparo, imageUrl, tempo, getSelectedIndiceGlicemico(), fonte, justificativa, substituicoes, linkReceita, ingredientesDetalhe, nomesIngredientes);
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

    private void salvarDadosDaReceita(String nome, String resumo, String preparo, String imageUrl, String tempo, int indiceGlicemico, String fonte, String justificativa, String substituicoes, String linkReceita,
                                      List<String> ingredientesDetalhe, List<String> nomesIngredientes) {

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
                fonte,
                preparo,
                tempoPreparoInt,
                ingredientesDetalhe,
                nomesIngredientes,
                resumo, // Novo
                justificativa, // Novo
                substituicoes, // Novo
                linkReceita // Novo
        );

        // O restante do método permanece o mesmo...

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

    private void atualizarDadosDaReceita(String nome, String resumo, String preparo, String imageUrl, String tempo, int indiceGlicemico, String fonte, String justificativa, String substituicoes, String linkReceita,
                                         List<String> ingredientesDetalhe, List<String> nomesIngredientes) {

        int tempoPreparoInt = 0;
        try {
            tempoPreparoInt = Integer.parseInt(tempo);
        } catch (NumberFormatException e) {
            Log.e("Upload", "Erro ao converter tempo de preparo para int", e);
        }

        Receita receitaAtualizada = receitaParaEditar;

        // Atualiza todos os campos
        receitaAtualizada.setNome(nome);
        receitaAtualizada.setFotoUrl(imageUrl);
        receitaAtualizada.setPreparo(preparo);
        receitaAtualizada.setTempoPreparo(tempoPreparoInt);
        receitaAtualizada.setIndice(indiceGlicemico); // Usa o setIndice que atualiza indiceGlicemico

        // Novos campos
        receitaAtualizada.setResumo(resumo);
        receitaAtualizada.setJustificativaGlice(justificativa);
        receitaAtualizada.setSubstituicoes(substituicoes);
        receitaAtualizada.setLinkReceita(linkReceita);
        receitaAtualizada.setFonte(fonte); // Novo setFonte, se não existir, adicione no Model!

        // Listas
        receitaAtualizada.setIngredientesDetalhe(ingredientesDetalhe);
        receitaAtualizada.setNomesIngredientes(nomesIngredientes);

        // O restante do método permanece o mesmo...

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

    // --- MÉTODOS AUXILIARES DE IMAGEM/URI ---

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
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}