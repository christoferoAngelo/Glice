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
    public int indiceGlicemico;
    public String foto_url;
    public String fonte;
    public String preparo;
    public List<String> ingredientesDetalhe;
    public List<String> nomesIngredientes;
    private boolean isFavorita = false;
    @Exclude
    private String documentId;
    public String resumo;
    public String justificativaGlice;
    public String substituicoes;
    public String linkReceita;

    public int tempoPreparo;

    // Construtor vazio para Firebase/Retrofit
    public Receita() {
    }

    // Construtor completo para criação de novos objetos
    public Receita(String nome, int indiceGlicemico, String foto_url, String fonte, String preparo,
                   int tempoPreparo, List<String> ingredientesDetalhe, List<String> nomesIngredientes,
                   String resumo, String justificativaGlice, String substituicoes, String linkReceita) {
        this.nome = nome;
        this.indiceGlicemico = indiceGlicemico;
        this.foto_url = foto_url;
        this.fonte = fonte;
        this.preparo = preparo;
        this.tempoPreparo = tempoPreparo;
        this.ingredientesDetalhe = ingredientesDetalhe;
        this.nomesIngredientes = nomesIngredientes;

        this.resumo = resumo;
        this.justificativaGlice = justificativaGlice;
        this.substituicoes = substituicoes;
        this.linkReceita = linkReceita;
    }
    protected Receita(Parcel in) {
        this.id = in.readLong();
        this.created_at = in.readString();
        this.nome = in.readString();
        this.indiceGlicemico = in.readInt();
        this.foto_url = in.readString();
        this.fonte = in.readString();
        this.preparo = in.readString();
        this.ingredientesDetalhe = in.createStringArrayList();
        this.nomesIngredientes = in.createStringArrayList();
        this.resumo = in.readString();
        this.justificativaGlice = in.readString();
        this.substituicoes = in.readString();
        this.linkReceita = in.readString();

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
        dest.writeInt(this.indiceGlicemico);
        dest.writeString(this.foto_url);
        dest.writeString(this.fonte);
        dest.writeString(this.preparo);
        dest.writeStringList(this.ingredientesDetalhe);
        dest.writeStringList(this.nomesIngredientes);
        dest.writeString(this.resumo);
        dest.writeString(this.justificativaGlice);
        dest.writeString(this.substituicoes);
        dest.writeString(this.linkReceita);

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
        map.put("indiceGlicemico", indiceGlicemico);
        map.put("fonte", fonte);
        map.put("foto_url", foto_url);
        map.put("preparo", preparo);
        map.put("tempoPreparo", tempoPreparo);
        map.put("ingredientesDetalhe", ingredientesDetalhe);
        map.put("nomesIngredientes", nomesIngredientes);
        map.put("resumo", resumo);
        map.put("justificativaGlice", justificativaGlice);
        map.put("substituicoes", substituicoes);
        map.put("linkReceita", linkReceita);
        return map;
    }
    public String getNome() {
        return nome;
    }
    public int getIndiceGlicemico() {
        return indiceGlicemico;
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
    public void setIndice(int indice) {this.indiceGlicemico = indice;}
    public void setIngredientesDetalhe(List<String> ingredientesDetalhe) {this.ingredientesDetalhe = ingredientesDetalhe;}
    public String getResumo() {return resumo;}

    public void setResumo(String resumo) {this.resumo = resumo;}

    public String getJustificativaGlice() {return justificativaGlice;}

    public void setJustificativaGlice(String justificativaGlice) {this.justificativaGlice = justificativaGlice;}

    public String getSubstituicoes() {return substituicoes;}

    public void setSubstituicoes(String substituicoes) {this.substituicoes = substituicoes;}

    public String getLinkReceita() {return linkReceita;}

    public void setLinkReceita(String linkReceita) {this.linkReceita = linkReceita;}
    public void setFonte(String fonte) {this.fonte = fonte;}
    public void setNomesIngredientes(List<String> nomesIngredientes) {this.nomesIngredientes = nomesIngredientes;}
    public String getDocumentId() {return documentId;}
    public int getTempoPreparo() {return tempoPreparo;}
    public void setTempoPreparo(int tempoPreparo) {this.tempoPreparo = tempoPreparo;}

    public void setDocumentId(String documentId) {this.documentId = documentId;}

    public List<String> getIngredientesDetalhe() {return ingredientesDetalhe;}

    public List<String> getNomesIngredientes() {return nomesIngredientes;}
}