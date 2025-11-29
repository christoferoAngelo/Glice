package br.edu.fatecgru.glice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;

import java.security.AlgorithmParameterGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.edu.fatecgru.glice.R;
import br.edu.fatecgru.glice.adapter.ReceitaAdapter;
import br.edu.fatecgru.glice.dao.ReceitaDAO;
import br.edu.fatecgru.glice.model.Receita;

public class ReceitasActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ReceitaAdapter adapter;
    private List<Receita> lista = new ArrayList<>();
    private ReceitaDAO receitaDao;

    private ImageView imgPerfil;

    private static Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receitas);

        //coisos
        imgPerfil = findViewById(R.id.imgPerfil);



        Map config = new HashMap();
        config.put("cloud_name", "de4j4ibb6");
        config.put("secure", true);
        MediaManager.init(this, config);

        MediaManager.get().upload("\"C:\\Users\\angel\\AndroidStudioProjects\\Glice2311\\app\\src\\main\\res\\drawable\\welcome.png\"").dispatch();

        recycler = findViewById(R.id.recyclerReceitas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReceitaAdapter(this, lista);
        recycler.setAdapter(adapter);

        receitaDao = new ReceitaDAO();

        carregarReceitas();
    }

    public static Cloudinary getInstance() {
        if (cloudinary == null) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "de4j4ibb6"); // Substitua pelo seu Cloud Name
            config.put("api_key", "792215385188622");       // Substitua pela sua API Key
            config.put("api_secret", "LB10k0h9NhImokGxXyp9GLwxJpk"); // Substitua pelo seu API Secret
            cloudinary = new Cloudinary(config);
        }
        return cloudinary;
    }
    private void carregarReceitas() {
        receitaDao.getReceitas(new ReceitaDAO.BuscarReceitasCallback() {
            @Override
            public void onSuccess(List<Receita> receitas) {
                lista.clear();
                lista.addAll(receitas);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(ReceitasActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirPerfil(View view){
        Intent it = new Intent(this, PerfilUsuario.class);
        startActivity(it);
    }

    private void abrirFiltro(View view){
      //TODO
    }
}
