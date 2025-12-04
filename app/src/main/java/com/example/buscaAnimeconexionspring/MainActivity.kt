package com.example.buscaAnimeconexionspring

import AnimeAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.buscaAnimeconexionspring.api.AnimeApiService
import com.example.buscaAnimeconexionspring.model.Anime
import com.example.buscaAnimeconexionspring.model.ApiResponse
import com.example.buscaAnimeconexionspring.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnimeAdapter
    private val animesList = mutableListOf<Anime>()
    private lateinit var btnCargar: Button
    private lateinit var tvEstado: TextView
    private lateinit var apiService: AnimeApiService

    companion object { private const val TAG = "MainActivity" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        btnCargar = findViewById(R.id.btnCargar)
        tvEstado = findViewById(R.id.tvEstado)

        apiService = ApiClient.getAnimeApiService()

        adapter = AnimeAdapter(
            animes = animesList,
            onClick = { /* abre detalle si quieres */ },
            onToggleFavoritos = { /* llama a /api/favoritos si lo usas */ },
            favoritos = mutableListOf()
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnCargar.setOnClickListener { cargarAnimes() }
        cargarAnimes()
    }

    private fun cargarAnimes() {
        tvEstado.text = "Cargando..."
        btnCargar.isEnabled = false

        val call: Call<ApiResponse<List<Anime>>> = apiService.getAll()

        call.enqueue(object : Callback<ApiResponse<List<Anime>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Anime>>>,
                response: Response<ApiResponse<List<Anime>>>
            ) {
                btnCargar.isEnabled = true
                val data = response.body()?.data
                if (response.isSuccessful && data != null) {
                    animesList.clear()
                    animesList.addAll(data)
                    adapter.notifyDataSetChanged()
                    tvEstado.text = "Animes cargados: ${animesList.size}"
                    Toast.makeText(this@MainActivity, "Se cargaron ${animesList.size} animes", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Animes cargados: ${animesList.size}")
                } else {
                    tvEstado.text = "Error: ${response.code()}"
                    Toast.makeText(this@MainActivity, "Error al cargar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error en respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Anime>>>, t: Throwable) {
                btnCargar.isEnabled = true
                tvEstado.text = "Error de conexión"
                Toast.makeText(this@MainActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error en petición", t)
            }
        })
    }
}
