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
        val dto = AnimeCreateDto(
            nombre = binding.etNombre.text.toString().trim(),
            categoria = binding.etCategoria.text.toString().trim(),
            valoracion = binding.etValoracion.text.toString().toDoubleOrNull(),
            autor = binding.etAutor.text.toString().trim().nullIfBlank(),
            enlaceTrailer = binding.etEnlaceTrailer.text.toString().trim().nullIfBlank(),
            enlaceVer = binding.etEnlaceVer.text.toString().trim().nullIfBlank(),
            miniatura = binding.etMiniatura.text.toString().trim().nullIfBlank(),
            coverUrl = binding.etCoverUrl.text.toString().trim().nullIfBlank(),
            estado = null, // pon "RELEASING" si quieres valor por defecto
            fechaInicio = binding.etFechaInicio.text.toString().trim().nullIfBlank(),
            fechaFin = binding.etFechaFin.text.toString().trim().nullIfBlank(),
            proximoEpNum = binding.etProximoEpNum.text.toString().toIntOrNull(),
            proximoEpFecha = binding.etProximoEpFecha.text.toString().trim().nullIfBlank(),
            descripcion = binding.etDescripcion.text.toString().trim().nullIfBlank()
        )

        if (dto.nombre.isBlank() || dto.categoria.isBlank()) {
            Toast.makeText(this, "Nombre y categoría son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnGuardar.isEnabled = false

        api.createAnime(dto).enqueue(object : Callback<ApiResponse<Anime>> {
            override fun onResponse(call: Call<ApiResponse<Anime>>, resp: Response<ApiResponse<Anime>>) {
                binding.btnGuardar.isEnabled = true
                if (resp.isSuccessful) {
                    Toast.makeText(this@CreateAnimeActivity, "Anime creado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateAnimeActivity, "Error: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse<Anime>>, t: Throwable) {
                binding.btnGuardar.isEnabled = true
                Toast.makeText(this@CreateAnimeActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun String.nullIfBlank(): String? = if (isBlank()) null else this
}
