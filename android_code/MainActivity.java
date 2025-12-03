package com.joange.cineapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.joange.cineapp.R;
import com.joange.cineapp.adapter.PeliculaAdapter;
import com.joange.cineapp.api.PeliculaApiService;
import com.joange.cineapp.model.Pelicula;
import com.joange.cineapp.service.ApiClient;

public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    
    private RecyclerView recyclerView;
    private PeliculaAdapter adapter;
    private List<Pelicula> peliculasList;
    private Button btnCargar;
    private TextView tvEstado;
    
    private PeliculaApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Inicializar componentes
        recyclerView = findViewById(R.id.recyclerView);
        btnCargar = findViewById(R.id.btnCargar);
        tvEstado = findViewById(R.id.tvEstado);
        
        // Inicializar lista
        peliculasList = new ArrayList<>();
        
        // Configurar RecyclerView
        adapter = new PeliculaAdapter(peliculasList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Inicializar API Service
        apiService = ApiClient.getApiService();
        
        // Configurar botón
        btnCargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarPeliculas();
            }
        });
        
        // Cargar películas al iniciar
        cargarPeliculas();
    }
    
    private void cargarPeliculas() {
        tvEstado.setText("Cargando...");
        btnCargar.setEnabled(false);
        
        Call<List<Pelicula>> call = apiService.getAllPeliculas();
        
        call.enqueue(new Callback<List<Pelicula>>() {
            @Override
            public void onResponse(Call<List<Pelicula>> call, Response<List<Pelicula>> response) {
                btnCargar.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    peliculasList.clear();
                    peliculasList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    tvEstado.setText("Películas cargadas: " + peliculasList.size());
                    Toast.makeText(MainActivity.this, 
                        "Se cargaron " + peliculasList.size() + " películas", 
                        Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "Películas cargadas: " + peliculasList.size());
                } else {
                    tvEstado.setText("Error: " + response.code());
                    Toast.makeText(MainActivity.this, 
                        "Error al cargar: " + response.code(), 
                        Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error en respuesta: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<List<Pelicula>> call, Throwable t) {
                btnCargar.setEnabled(true);
                tvEstado.setText("Error de conexión");
                Toast.makeText(MainActivity.this, 
                    "Error de conexión: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error en petición", t);
            }
        });
    }
}

