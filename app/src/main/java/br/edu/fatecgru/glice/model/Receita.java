package br.edu.fatecgru.glice.model;

import com.google.firebase.firestore.Exclude;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Receita implements Parcelable {
    public long id;
    public String created_at;
    public String nome;
    public int indice;
    public String foto_url;
    public String fonte;
    public String preparo;

    // LINHA REMOVIDA: public String documentId;

    // Lista detalhada para exibição (substitui 'ingredientes' String)
    public List<String> ingredientesDetalhe;

    // Lista limpa de nomes de ingredientes para pesquisa
    public List<String> nomesIngredientes;
    private boolean isFavorita = false;

    @Exclude // O Firebase não deve tentar salvar este campo no banco
    private String documentId; // <--- APENAS ESTA DECLARAÇÃO FOI MANTIDA

    public int tempoPreparo;

    // Construtor vazio para Firebase/Retrofit
    public Receita() {
    }

    // Construtor completo para criação de novos objetos
    public Receita(String nome, int indice, String foto_url, String fonte, String preparo,
                   int tempoPreparo, List<String> ingredientesDetalhe, List<String> nomesIngredientes) {
        this.nome = nome;
        this.indice = indice;
        this.foto_url = foto_url;
        this.fonte = fonte;
        this.preparo = preparo;
        this.tempoPreparo = tempoPreparo;
        this.ingredientesDetalhe = ingredientesDetalhe;
        this.nomesIngredientes = nomesIngredientes;
    }

    // EM Receita.java (Construtor protegido)
    protected Receita(Parcel in) {
        this.id = in.readLong();
        this.created_at = in.readString();
        this.nome = in.readString();
        this.indice = in.readInt();
        this.foto_url = in.readString();
        this.fonte = in.readString();
        this.preparo = in.readString();
        this.ingredientesDetalhe = in.createStringArrayList();
        this.nomesIngredientes = in.createStringArrayList();

        this.tempoPreparo = in.readInt();
        this.isFavorita = in.readByte() != 0;
        this.documentId = in.readString();
    }
    // 3. O CREATOR: Gera instâncias da classe a partir do Parcel
    public static final Creator<Receita> CREATOR = new Creator<Receita>() {
        @Override
        public Receita createFromParcel(Parcel in) {
            return new Receita(in);
        }

        @Override
        public Receita[] newArray(int size) {
            return new Receita[size];
        }
    };

    // 4. writeToParcel: Escreve os dados na ordem exata do construtor Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.created_at);
        dest.writeString(this.nome);
        dest.writeInt(this.indice);
        dest.writeString(this.foto_url);
        dest.writeString(this.fonte);
        dest.writeString(this.preparo);
        dest.writeStringList(this.ingredientesDetalhe);
        dest.writeStringList(this.nomesIngredientes);

        dest.writeInt(this.tempoPreparo);
        dest.writeByte((byte) (this.isFavorita ? 1 : 0));
        dest.writeString(this.documentId);
    }

    // 5. describeContents: Quase sempre retorna 0
    @Override
    public int describeContents() {
        return 0;
    }

    // Método para facilitar a conversão para Map (útil para Supabase e Firebase)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("nome", nome);
        map.put("indice", indice);
        map.put("fonte", fonte);
        map.put("foto_url", foto_url);
        map.put("preparo", preparo);
        map.put("tempoPreparo", tempoPreparo);
        map.put("ingredientesDetalhe", ingredientesDetalhe);
        map.put("nomesIngredientes", nomesIngredientes);

        return map;
    }
    public String getNome() {
        return nome;
    }

    public int getIndiceGlicemico() {
        return indice;
    }

    public String getFonte() {return fonte;}

    public String getUrlImagem() {
        return foto_url;
    }

    public String getPreparo() {
        return preparo;
    }

    public boolean isFavorita() {return isFavorita;}

    public void setFavorita(boolean favorita) {isFavorita = favorita;}
    public void setNome(String nome) {this.nome = nome;}

    public void setFotoUrl(String foto_url) {this.foto_url = foto_url;}

    public void setPreparo(String preparo) {this.preparo = preparo;}

    public void setIndice(int indice) {this.indice = indice;}

    public void setIngredientesDetalhe(List<String> ingredientesDetalhe) {this.ingredientesDetalhe = ingredientesDetalhe;}

    public void setNomesIngredientes(List<String> nomesIngredientes) {this.nomesIngredientes = nomesIngredientes;}
    public String getDocumentId() {return documentId;}
    public int getTempoPreparo() {return tempoPreparo;}
    public void setTempoPreparo(int tempoPreparo) {this.tempoPreparo = tempoPreparo;}

    public void setDocumentId(String documentId) {this.documentId = documentId;}

    public List<String> getIngredientesDetalhe() {return ingredientesDetalhe;}

    public List<String> getNomesIngredientes() {return nomesIngredientes;}
}