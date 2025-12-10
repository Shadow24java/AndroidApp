package com.example.buscaAnimeconexionspring

import com.example.buscaAnimeconexionspring.adapter.AnimeAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buscaAnimeconexionspring.api.AnimeApiService
import com.example.buscaAnimeconexionspring.databinding.ActivityEmisionBinding
import com.example.buscaAnimeconexionspring.model.Anime
import com.example.buscaAnimeconexionspring.model.ApiResponse
import com.example.buscaAnimeconexionspring.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmisionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmisionBinding
    private lateinit var adapter: AnimeAdapter
    private val lista = mutableListOf<Anime>()
    private val favoritosIds = mutableListOf<Long>()
    private val api: AnimeApiService by lazy { ApiClient.getAnimeApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmisionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fabBack.setOnClickListener { finish() }

        supportActionBar?.title = "En emisión"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AnimeAdapter(
            animes = lista,
            onClick = { anime ->
                val intent = Intent(this, AnimeDetailActivity::class.java)
                intent.putExtra("anime", anime)
                startActivity(intent)
            },
            onToggleFavoritos = { anime -> anime.id?.let { toggleFavorito(it) } },
            favoritos = favoritosIds,
            forceRemoveLabel = false
        )
        binding.recyclerEmision.layoutManager = LinearLayoutManager(this)
        binding.recyclerEmision.adapter = adapter

        // ====== BOTTOM NAVIGATION ======
        binding.bottomNavigation.selectedItemId = R.id.navigation_emision
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_favoritos -> {
                    startActivity(Intent(this, FavoritosActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_emision -> {
                    // Ya estamos aquí
                    true
                }
                R.id.navigation_mis_animes -> {
                    startActivity(Intent(this, MisAnimesActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_perfil -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        // =====================

        cargarFavoritosYEmision()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun cargarFavoritosYEmision() {
        binding.tvEstado.text = "Cargando..."
        api.getFavorites().enqueue(object : Callback<ApiResponse<List<Anime>>> {
            override fun onResponse(call: Call<ApiResponse<List<Anime>>>, resp: Response<ApiResponse<List<Anime>>>) {
                val favs = resp.body()?.data.orEmpty()
                favoritosIds.clear()
                favoritosIds.addAll(favs.mapNotNull { it.id })
                cargarEmision()
            }
            override fun onFailure(call: Call<ApiResponse<List<Anime>>>, t: Throwable) {
                cargarEmision() // seguimos aunque falle favoritos
            }
        })
    }

    private fun cargarEmision() {
        api.getAiring().enqueue(object : Callback<ApiResponse<List<Anime>>> {
            override fun onResponse(call: Call<ApiResponse<List<Anime>>>, resp: Response<ApiResponse<List<Anime>>>) {
                val data = resp.body()?.data
                if (resp.isSuccessful && data != null) {
                    lista.clear()
                    lista.addAll(data)
                    adapter.updateFavoritos(favoritosIds)
                    adapter.notifyDataSetChanged()
                    binding.tvEstado.text = "Animes en emisión: ${lista.size}"
                } else {
                    binding.tvEstado.text = "Error: ${resp.code()}"
                }
            }
            override fun onFailure(call: Call<ApiResponse<List<Anime>>>, t: Throwable) {
                binding.tvEstado.text = "Error de conexión"
                Toast.makeText(this@EmisionActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleFavorito(id: Long) {
        if (favoritosIds.contains(id)) {
            api.removeFavorite(id).enqueue(object : Callback<ApiResponse<Boolean>> {
                override fun onResponse(call: Call<ApiResponse<Boolean>>, resp: Response<ApiResponse<Boolean>>) {
                    if (resp.isSuccessful && resp.body()?.data == true) {
                        favoritosIds.remove(id)
                        adapter.updateFavoritos(favoritosIds)
                    }
                }
                override fun onFailure(call: Call<ApiResponse<Boolean>>, t: Throwable) {}
            })
        } else {
            api.addFavorite(id).enqueue(object : Callback<ApiResponse<Boolean>> {
                override fun onResponse(call: Call<ApiResponse<Boolean>>, resp: Response<ApiResponse<Boolean>>) {
                    if (resp.isSuccessful && resp.body()?.data == true) {
                        favoritosIds.add(id)
                        adapter.updateFavoritos(favoritosIds)
                    }
                }
                override fun onFailure(call: Call<ApiResponse<Boolean>>, t: Throwable) {}
            })
        }
    }
}
