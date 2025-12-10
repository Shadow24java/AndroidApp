package com.example.buscaAnimeconexionspring

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.buscaAnimeconexionspring.databinding.ActivityAnimeDetailBinding
import com.example.buscaAnimeconexionspring.model.Anime

class AnimeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón flotante para volver atrás
        binding.fabBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Obtenemos el anime del intent; si no viene, cerramos la pantalla
        val anime = intent.getSerializableExtra("anime") as? Anime
            ?: run {
                finish()
                return
            }

        // Fecha de emisión (inicio / próxima / fin)
        val fechaTxt = anime.fechaInicio ?: anime.proximoEpFecha ?: anime.fechaFin
        binding.tvFecha.text = "Fecha emisión: ${fechaTxt ?: "-"}"

        // Próximo episodio
        val proxTxt = when {
            anime.proximoEpNum != null && anime.proximoEpFecha != null ->
                "Próximo ep. ${anime.proximoEpNum}:"
            anime.proximoEpNum != null ->
                "Próximo ep. ${anime.proximoEpNum}"
            else -> "Próximo ep.: -"
        }
        binding.tvProximo.text = proxTxt

        // Resto de datos
        binding.tvTitulo.text = anime.nombre ?: ""
        binding.tvCategoria.text = "Categoría: ${anime.categoria ?: "-"}"
        binding.tvValoracion.text = "Valoración: ${anime.valoracion ?: "-"}"
        binding.tvAutor.text = "Autor: ${anime.autor ?: "-"}"
        binding.tvDescripcion.text = anime.descripcion ?: "Sin descripción"

        // Imagen (usa coverUrl o la miniatura del backend)
        val urlImg = anime.coverUrl ?: anime.miniatura?.let { "http://10.0.2.2:8091/images/$it" }
        Glide.with(this)
            .load(urlImg)
            .placeholder(R.drawable.placeholder_anime)
            .error(R.drawable.placeholder_anime)
            .into(binding.imgCover)

        // Botón "Ver"
        binding.btnVer.setOnClickListener {
            anime.enlaceVer
                ?.takeIf { it.isNotBlank() }
                ?.let { openLink(it) }
        }

        // Botón "Trailer"
        binding.btnTrailer.setOnClickListener {
            anime.enlaceTrailer
                ?.takeIf { it.isNotBlank() }
                ?.let { openLink(it) }
        }
    }

    private fun openLink(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
