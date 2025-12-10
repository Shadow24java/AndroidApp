package com.example.buscaAnimeconexionspring

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buscaAnimeconexionspring.api.AnimeApiService
import com.example.buscaAnimeconexionspring.databinding.ActivityCreateAnimeBinding
import com.example.buscaAnimeconexionspring.model.Anime
import com.example.buscaAnimeconexionspring.model.ApiResponse
import com.example.buscaAnimeconexionspring.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class AnimeCreateDto(
    val nombre: String,
    val categoria: String,
    val valoracion: Double?,
    val autor: String?,
    val enlaceTrailer: String?,
    val enlaceVer: String?,
    val miniatura: String?,
    val coverUrl: String?,
    val estado: String? = null,
    val fechaInicio: String?,
    val fechaFin: String?,
    val proximoEpNum: Int?,
    val proximoEpFecha: String?,
    val descripcion: String?
)

class CreateAnimeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAnimeBinding
    private val api: AnimeApiService by lazy { ApiClient.getAnimeApiService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAnimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Nuevo anime"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun guardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val categoria = binding.etCategoria.text.toString().trim()
        
        if (nombre.isBlank() || categoria.isBlank()) {
            Toast.makeText(this, "Nombre y categoría son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnGuardar.isEnabled = false

        try {
            // Crear el anime con los datos del formulario
            val nuevoAnime = Anime(
                id = System.currentTimeMillis(), // ID único basado en timestamp
                nombre = nombre,
                categoria = categoria,
                valoracion = binding.etValoracion.text.toString().toDoubleOrNull(),
                autor = binding.etAutor.text.toString().trim().nullIfBlank(),
                enlaceTrailer = binding.etEnlaceTrailer.text.toString().trim().nullIfBlank(),
                enlaceVer = binding.etEnlaceVer.text.toString().trim().nullIfBlank(),
                miniatura = binding.etMiniatura.text.toString().trim().nullIfBlank(),
                coverUrl = binding.etCoverUrl.text.toString().trim().nullIfBlank(),
                estado = "PERSONAL", // Marcador para animes personales
                fechaInicio = binding.etFechaInicio.text.toString().trim().nullIfBlank(),
                fechaFin = binding.etFechaFin.text.toString().trim().nullIfBlank(),
                proximoEpNum = binding.etProximoEpNum.text.toString().toIntOrNull(),
                proximoEpFecha = binding.etProximoEpFecha.text.toString().trim().nullIfBlank(),
                descripcion = binding.etDescripcion.text.toString().trim().nullIfBlank()
            )

            // Guardar en SharedPreferences
            val sharedPref = getSharedPreferences("MisAnimes", android.content.Context.MODE_PRIVATE)
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            val misAnimesJson = sharedPref.getString("user_$userId", "[]")

            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<MutableList<Anime>>() {}.type
            val animesList: MutableList<Anime> = gson.fromJson(misAnimesJson, type) ?: mutableListOf()

            // Agregar el nuevo anime
            animesList.add(nuevoAnime)

            // Guardar de vuelta
            val editor = sharedPref.edit()
            editor.putString("user_$userId", gson.toJson(animesList))
            editor.apply()

            Toast.makeText(this, "Anime creado correctamente", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: Exception) {
            binding.btnGuardar.isEnabled = true
            Toast.makeText(this, "Error al crear anime: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun String.nullIfBlank(): String? = if (isBlank()) null else this
}
