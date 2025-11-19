package br.edu.fatecgru.glice.dao;

import android.util.Log;

import br.edu.fatecgru.glice.model.Receita;
import br.edu.fatecgru.glice.network.SupabaseApi;
import br.edu.fatecgru.glice.network.SupabaseClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceitaDAO {

    private final SupabaseApi api;

    public ReceitaDAO() {
        this.api = SupabaseClient.getApi();
    }

    public ReceitaDAO(SupabaseApi api) {
        this.api = api;
    }

    // 🔹 Buscar todas receitas
    public void getReceitas(ReceitasCallback callback) {
        Call<List<Receita>> call = api.getReceitas("return=representation", "*");

        call.enqueue(new Callback<List<Receita>>() {
            @Override
            public void onResponse(Call<List<Receita>> call, Response<List<Receita>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Erro: " + response.errorBody());
                    Log.e("Errinho", String.valueOf(response));
                }
            }

            @Override
            public void onFailure(Call<List<Receita>> call, Throwable t) {
                callback.onError("Falha: " + t.getMessage());
                Log.e("Falhinha", String.valueOf(t.getMessage()));
            }
        });
    }

    // 🔹 Inserir receita
    public void insertReceita(String nome, int indice, String fotoUrl, String fonte, InserirCallback callback) {

        Map<String, Object> body = new HashMap<>();
        body.put("nome", nome);
        body.put("indice", indice);
        body.put("foto_url", fotoUrl);
        body.put("fonte", fonte);

        Call<List<Receita>> call = api.insertReceita("return=representation", body);

        call.enqueue(new Callback<List<Receita>>() {
            @Override
            public void onResponse(Call<List<Receita>> call, Response<List<Receita>> response) {
                if (response.isSuccessful()) {
                    callback.onInserted(response.body().get(0));
                } else {
                    callback.onError("Erro: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Receita>> call, Throwable t) {
                callback.onError("Falha: " + t.getMessage());
            }
        });
    }

    // 🔹 Interfaces de callback (DAO estilo)
    public interface ReceitasCallback {
        void onSuccess(List<Receita> receitas);
        void onError(String msg);
    }

    public interface InserirCallback {
        void onInserted(Receita receita);
        void onError(String msg);
    }
}

