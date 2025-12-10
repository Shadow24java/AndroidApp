package com.example.buscaAnimeconexionspring

import AnimeAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
        binding.fabBack.setOnClickListener { finish() }

        // Barra superior con título y flecha atrás
        supportActionBar?.title = "Favoritos"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AnimeAdapter(
            animes = lista,
            onClick = { anime ->
                val intent = Intent(this, AnimeDetailActivity::class.java)
                intent.putExtra("anime", anime)
                startActivity(intent)
            },
            onToggleFavoritos = { anime -> anime.id?.let { id -> quitarDeFavoritos(id) } },
            favoritos = favoritosIds,
            forceRemoveLabel = true
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
                val data = resp.body()?.data.orEmpty()
                lista.clear(); lista.addAll(data)
                favoritosIds.clear(); favoritosIds.addAll(data.mapNotNull { it.id })
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<ApiResponse<List<Anime>>>, t: Throwable) {
                Toast.makeText(this@FavoritosActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun quitarDeFavoritos(id: Long) {
        api.removeFavorite(id).enqueue(object : Callback<ApiResponse<Boolean>> {
            override fun onResponse(
                call: Call<ApiResponse<Boolean>>,
                resp: Response<ApiResponse<Boolean>>
            ) {
                if (resp.isSuccessful && resp.body()?.data == true) {
                    favoritosIds.remove(id)
                    lista.removeAll { it.id == id }
                    adapter.updateFavoritos(favoritosIds)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Boolean>>, t: Throwable) {
                Toast.makeText(this@FavoritosActivity, "No se pudo quitar", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
