package com.example.buscaAnimeconexionspring

import AnimeAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buscaAnimeconexionspring.api.AnimeApiService
import com.example.buscaAnimeconexionspring.databinding.ActivityFavoritosBinding
import com.example.buscaAnimeconexionspring.model.Anime
import com.example.buscaAnimeconexionspring.model.ApiResponse
import com.example.buscaAnimeconexionspring.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var adapter: AnimeAdapter
    private val lista = mutableListOf<Anime>()
    private val favoritosIds = mutableListOf<Long>()
    private val api: AnimeApiService by lazy { ApiClient.getAnimeApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Flecha de back en la barra superior
        supportActionBar?.title = "Favoritos"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AnimeAdapter(
            animes = lista,
            onClick = { /* detalle si quieres */ },
            onToggleFavoritos = { /* toggle si quieres */ },
            favoritos = favoritosIds
        )
        binding.recyclerFavoritos.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavoritos.adapter = adapter

        cargarFavoritos()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun cargarFavoritos() {
        api.getFavorites().enqueue(object : Callback<ApiResponse<List<Anime>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Anime>>>,
                resp: Response<ApiResponse<List<Anime>>>
            ) {
                val data = resp.body()?.data ?: emptyList()
                lista.clear(); lista.addAll(data)
                favoritosIds.clear(); favoritosIds.addAll(data.mapNotNull { it.id })
                adapter.notifyDataSetChanged()
            }
            override fun onFailure(call: Call<ApiResponse<List<Anime>>>, t: Throwable) { /* mostrar error */ }
        })
    }
}
