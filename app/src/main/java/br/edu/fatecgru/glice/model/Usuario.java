package br.edu.fatecgru.glice.model;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String nome;
    private String email;
    private List<String> favoritos;


    public Usuario() {}

    public Usuario(String nome, String email) {
        this.nome = nome;
        this.email = email;
        this.favoritos = new ArrayList<>();
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }


    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getFavoritos() { return favoritos; }
    public void setFavoritos(List<String> favoritos) { this.favoritos = favoritos; }
}