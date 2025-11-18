package br.edu.fatecgru.glice.network;

import br.edu.fatecgru.glice.model.Receita;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface SupabaseApi {

    @Headers({
            "apikey: YOUR_ANON_KEY",
            "Authorization: Bearer YOUR_ANON_KEY",
            "Content-Type: application/json"
    })
    @GET("rest/v1/receita")
    Call<List<Receita>> getReceitas(
            @Header("Prefer") String prefer,
            @Query("select") String select
    );

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVzandua2Fpc2Fid254aHZ0aGtsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM0NzEyNTksImV4cCI6MjA3OTA0NzI1OX0.rs93APWLWqE5l7LiJVW1J0ebC9OqZZ_w29ZQcnKhzXY",
            "Authorization: Bearer YOUR_ANON_KEY",
            "Content-Type: application/json"
    })
    @POST("rest/v1/receita")
    Call<List<Receita>> insertReceita(
            @Header("Prefer") String prefer,
            @Body Map<String, Object> body
    );
}
