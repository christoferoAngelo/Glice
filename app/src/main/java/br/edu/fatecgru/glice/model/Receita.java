package br.edu.fatecgru.glice.model;

import java.util.HashMap;
import java.util.Map;

public class Receita {
    public long id;
    public String created_at;
    public String nome;
    public int indice;
    public String foto_url;
    public String fonte;
    public String ingredientes;
    public String preparo;
    private boolean isFavorita = false;
    // Construtor vazio para Firebase/Retrofit
    public Receita() {
    }

    // Construtor completo para criação de novos objetos
    public Receita(String nome, int indice, String foto_url, String fonte, String ingredientes, String preparo) {
        this.nome = nome;
        this.indice = indice;
        this.foto_url = foto_url;
        this.fonte = fonte;
        this.ingredientes = ingredientes;
        this.preparo = preparo;
        // created_at e id serão preenchidos pelo banco de dados
    }

    // Método para facilitar a conversão para Map (útil para Supabase e Firebase)
    // Dentro de Receita.java

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        // CAMPOS OBRIGATÓRIOS (NOT NULL no Supabase):
        map.put("nome", nome);
        map.put("indice", indice);
        map.put("fonte", fonte); // <--- ESTE CAMPO PODE TER ESTADO AUSENTE NO SEU MAP ANTERIOR!

        // CAMPOS QUE VOCÊ ESTÁ ADICIONANDO NA MODEL/UI:
        map.put("foto_url", foto_url);
        map.put("ingredientes", ingredientes); // Incluir ingrediente
        map.put("preparo", preparo);           // Incluir preparo

        return map;
    }

    public String getNome() {
        return nome;
    }

    public int getIndiceGlicemico() {
        return indice;
    }

    public String getFonte() {
        return fonte;
    }

    public String getUrlImagem() {
        return foto_url;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public String getPreparo() {
        return preparo;
    }

    public boolean isFavorita() {return isFavorita;}

    public void setFavorita(boolean favorita) {isFavorita = favorita;}
}