package br.edu.fatecgru.glice.model;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.Exclude; // Adicionado para documentId

public class Usuario {
    private String nome;
    private String email;
    private List<String> favoritos;
    private String fotoPerfilLocalPath;
    @Exclude
    private String documentId;

    public Usuario() {}

    public Usuario(String nome, String email) {
        this.nome = nome;
        this.email = email;
        this.favoritos = new ArrayList<>();
        this.fotoPerfilLocalPath = null;
    }

    // Getters
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public List<String> getFavoritos() { return favoritos; }
    public String getFotoPerfilLocalPath() { return fotoPerfilLocalPath; }
    public String getDocumentId() { return documentId; }

    // Setters
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setFavoritos(List<String> favoritos) { this.favoritos = favoritos; }
    public void setFotoPerfilLocalPath(String fotoPerfilLocalPath) { this.fotoPerfilLocalPath = fotoPerfilLocalPath; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}