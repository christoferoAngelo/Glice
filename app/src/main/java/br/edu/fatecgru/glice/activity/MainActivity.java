package br.edu.fatecgru.glice.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import br.edu.fatecgru.glice.R;

public class MainActivity extends AppCompatActivity {

    Button inicial;
    Button upload;
    Button login;
    Button tutorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        inicial = findViewById(R.id.inicial);
        inicial.setOnClickListener(v -> {
            Intent it = new Intent(this, ReceitasActivity.class);
            startActivity(it);
        });

        upload = findViewById(R.id.upload);
        upload.setOnClickListener(v -> {
            Intent it = new Intent(this, UploadReceitaActivity.class);
            startActivity(it);

        });

        login = findViewById(R.id.login);
        login.setOnClickListener(v -> {
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);

        });

        tutorial = findViewById(R.id.tutorial);
        tutorial.setOnClickListener(v -> {
            Intent it = new Intent(this, TutorialActivity.class);
            startActivity(it);
        });

    }
}