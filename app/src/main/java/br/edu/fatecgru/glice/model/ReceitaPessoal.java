package br.edu.fatecgru.glice.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidade Room que representa uma receita salva localmente pelo usuário (Livro de Receitas).
 * Usada para armazenamento offline e privado. Este modelo substitui o ReceitaLocal.
 */
@Entity(tableName = "receitas_pessoais")
public class ReceitaPessoal {

    // Chave primária autoincrementável
    @PrimaryKey(autoGenerate = true)
    private int id;

    // Título da receita
    private String titulo;

    // Conteúdo completo da receita (ingredientes + preparo)
    private String conteudo;

    // URL opcional da imagem (ou URI local)
    private String imageUrl;

    // Construtor vazio obrigatório para o Room
    public ReceitaPessoal() {
    }

    // Construtor para criação de novos objetos
    public ReceitaPessoal(String titulo, String conteudo, String imageUrl) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.imageUrl = imageUrl;
    }

    // Getters e Setters (necessários para o Room funcionar)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}