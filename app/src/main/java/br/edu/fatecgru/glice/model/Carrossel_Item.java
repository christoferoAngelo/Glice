package br.edu.fatecgru.glice.model;

public class Carrossel_Item {
    private String titulo;
    private String descricao;
    private int imagem;

    public Carrossel_Item(String titulo, String descricao, int imagem) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.imagem = imagem;
    }

    // Métodos que SEU adapter espera

    public String getTitle() {
        return titulo;
    }

    public String getDescription() {
        return descricao;
    }

    public int getImageRes() {
        return imagem;
    }
}