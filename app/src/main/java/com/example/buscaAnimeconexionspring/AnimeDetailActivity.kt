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

        supportActionBar?.title = "Detalle"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val anime = intent.getSerializableExtra("anime") as? Anime
            ?: run { finish(); return }

        binding.tvTitulo.text = anime.nombre ?: ""
        binding.tvCategoria.text = "Categoría: ${anime.categoria ?: "-"}"
        binding.tvValoracion.text = "Valoración: ${anime.valoracion ?: "-"}"
        binding.tvAutor.text = "Autor: ${anime.autor ?: "-"}"
        binding.tvDescripcion.text = anime.descripcion ?: "Sin descripción"

        val urlImg = anime.coverUrl ?: anime.miniatura?.let { "http://10.0.2.2:8090/images/$it" }
        Glide.with(this)
            .load(urlImg)
            .placeholder(R.drawable.placeholder_anime)
            .error(R.drawable.placeholder_anime)
            .into(binding.imgCover)

        binding.btnVer.setOnClickListener {
            anime.enlaceVer?.takeIf { it.isNotBlank() }?.let { openLink(it) }
        }
        binding.btnTrailer.setOnClickListener {
            anime.enlaceTrailer?.takeIf { it.isNotBlank() }?.let { openLink(it) }
        }
    }

    private fun openLink(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
