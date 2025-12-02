package br.edu.fatecgru.glice.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidade Room que representa uma receita salva localmente pelo usuário (Livro de Receitas).
 */
@Entity(tableName = "receitas_pessoais")
public class ReceitaPessoal implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String titulo;

    @Nullable
    private String ingredientes;

    private String preparo;

    @Nullable
    private String anotacoes;

    @Nullable
    private String imageUrl;

    @Nullable
    private String fonte;

    @Nullable
    private String link;

    @Nullable
    private Long dataCriacao;

    // Construtor vazio obrigatório para o Room
    public ReceitaPessoal() {
    }

    // Construtor para criação de novos objetos
    public ReceitaPessoal(String titulo, String preparo) {
        this.titulo = titulo;
        this.preparo = preparo;
        this.dataCriacao = System.currentTimeMillis();
    }

    // ---------- PARCELABLE ---------- //

    protected ReceitaPessoal(Parcel in) {
        id = in.readInt();
        titulo = in.readString();
        ingredientes = in.readString();
        preparo = in.readString();
        anotacoes = in.readString();
        imageUrl = in.readString();
        fonte = in.readString();
        link = in.readString();

        if (in.readByte() == 0) {
            dataCriacao = null;
        } else {
            dataCriacao = in.readLong();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(titulo);
        dest.writeString(ingredientes);
        dest.writeString(preparo);
        dest.writeString(anotacoes);
        dest.writeString(imageUrl);
        dest.writeString(fonte);
        dest.writeString(link);

        if (dataCriacao == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(dataCriacao);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReceitaPessoal> CREATOR = new Creator<ReceitaPessoal>() {
        @Override
        public ReceitaPessoal createFromParcel(Parcel in) {
            return new ReceitaPessoal(in);
        }

        @Override
        public ReceitaPessoal[] newArray(int size) {
            return new ReceitaPessoal[size];
        }
    };

    // ---------- GETTERS e SETTERS ---------- //

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    @Nullable
    public String getIngredientes() { return ingredientes; }
    public void setIngredientes(@Nullable String ingredientes) { this.ingredientes = ingredientes; }

    public String getPreparo() { return preparo; }
    public void setPreparo(String preparo) { this.preparo = preparo; }

    @Nullable
    public String getAnotacoes() { return anotacoes; }
    public void setAnotacoes(@Nullable String anotacoes) { this.anotacoes = anotacoes; }

    @Nullable
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(@Nullable String imageUrl) { this.imageUrl = imageUrl; }

    @Nullable
    public String getFonte() { return fonte; }
    public void setFonte(@Nullable String fonte) { this.fonte = fonte; }

    @Nullable
    public String getLink() { return link; }
    public void setLink(@Nullable String link) { this.link = link; }

    @Nullable
    public Long getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(@Nullable Long dataCriacao) { this.dataCriacao = dataCriacao; }
}
