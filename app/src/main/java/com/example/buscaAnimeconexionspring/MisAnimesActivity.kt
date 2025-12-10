package com.example.buscaAnimeconexionspring

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.buscaAnimeconexionspring.adapter.AnimeAdapter
import com.example.buscaAnimeconexionspring.api.AnimeApiService
import com.example.buscaAnimeconexionspring.databinding.ActivityMisAnimesBinding
import com.example.buscaAnimeconexionspring.model.Anime
import com.example.buscaAnimeconexionspring.model.ApiResponse
import com.example.buscaAnimeconexionspring.service.ApiClient
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisAnimesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisAnimesBinding
    private lateinit var adapter: AnimeAdapter
    private val lista = mutableListOf<Anime>()
    private val api: AnimeApiService by lazy { ApiClient.getAnimeApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisAnimesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Configurar RecyclerView con adapter que incluye opción de eliminar
        adapter = AnimeAdapter(
            animes = lista,
            onClick = { anime ->
                val intent = Intent(this, AnimeDetailActivity::class.java)
                intent.putExtra("anime", anime)
                startActivity(intent)
            },
            onToggleFavoritos = null, // No necesitamos favoritos aquí
            favoritos = mutableListOf(),
            forceRemoveLabel = false,
            onDelete = { anime ->
                // Mostrar diálogo de confirmación antes de eliminar
                mostrarDialogoEliminar(anime)
            }
        )
        
        binding.recyclerMisAnimes.layoutManager = LinearLayoutManager(this)
        binding.recyclerMisAnimes.adapter = adapter

        // FAB para crear nuevo anime
        binding.fabAddAnime.setOnClickListener {
            startActivity(Intent(this, CreateAnimeActivity::class.java))
        }

        // ====== BOTTOM NAVIGATION ======
        binding.bottomNavigation.selectedItemId = R.id.navigation_mis_animes
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
                    startActivity(Intent(this, EmisionActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_mis_animes -> {
                    // Ya estamos aquí
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

        // Cargar mis animes
        cargarMisAnimes()
    }

    override fun onResume() {
        super.onResume()
        // Recargar cuando volvemos a esta pantalla (por si creamos un anime nuevo)
        cargarMisAnimes()
    }

    private fun cargarMisAnimes() {
        binding.tvEstado.text = "Cargando..."
        
        // Cargar animes guardados en SharedPreferences
        val sharedPref = getSharedPreferences("MisAnimes", Context.MODE_PRIVATE)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val misAnimesJson = sharedPref.getString("user_$userId", "[]")
        
        // Convertir JSON a lista de animes
        try {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<Anime>>() {}.type
            val animesList: List<Anime> = gson.fromJson(misAnimesJson, type) ?: emptyList()
            
            lista.clear()
            lista.addAll(animesList)
            adapter.notifyDataSetChanged()
            
            binding.tvEstado.text = if (lista.isEmpty()) {
                "No has creado ningún anime aún.\nToca el botón + para crear uno."
            } else {
                "Mis animes: ${lista.size}"
            }
        } catch (e: Exception) {
            binding.tvEstado.text = "Error al cargar animes"
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoEliminar(anime: Anime) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar anime")
            .setMessage("¿Estás seguro de que quieres eliminar '${anime.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarAnime(anime)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarAnime(anime: Anime) {
        // Eliminar de SharedPreferences
        val sharedPref = getSharedPreferences("MisAnimes", Context.MODE_PRIVATE)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        val misAnimesJson = sharedPref.getString("user_$userId", "[]")
        
        try {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<MutableList<Anime>>() {}.type
            val animesList: MutableList<Anime> = gson.fromJson(misAnimesJson, type) ?: mutableListOf()
            
            // Eliminar el anime de la lista
            animesList.removeIf { it.id == anime.id }
            
            // Guardar de nuevo
            val editor = sharedPref.edit()
            editor.putString("user_$userId", gson.toJson(animesList))
            editor.apply()
            
            Toast.makeText(this, "Anime eliminado correctamente", Toast.LENGTH_SHORT).show()
            
            // Recargar la lista
            cargarMisAnimes()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al eliminar el anime: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
