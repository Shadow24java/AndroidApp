package com.example.buscaAnimeconexionspring.adapter  // ⬅️ ESTA ES LA LÍNEA QUE FALTABA

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.buscaAnimeconexionspring.R
import com.example.buscaAnimeconexionspring.model.Anime

class AnimeAdapter(
    private var animes: MutableList<Anime>,
    private val onClick: (Anime) -> Unit,
    private val onToggleFavoritos: ((Anime) -> Unit)?,
    private val favoritos: MutableList<Long>,
    private val forceRemoveLabel: Boolean = false,
    private val onDelete: ((Anime) -> Unit)? = null
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    fun updateAnimes(newAnimes: List<Anime>) {
        animes.clear()
        animes.addAll(newAnimes)
        notifyDataSetChanged()
    }

    fun updateFavoritos(favIds: List<Long>) {
        favoritos.clear()
        favoritos.addAll(favIds)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        holder.bind(animes[position])
    }

    override fun getItemCount(): Int = animes.size

    inner class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val tvAutor: TextView = itemView.findViewById(R.id.tvAutor)
        private val tvValoracion: TextView = itemView.findViewById(R.id.tvValoracion)
        private val tvCategorias: TextView = itemView.findViewById(R.id.tvCategorias)
        private val btnFavorito: Button = itemView.findViewById(R.id.btnFavorito)
        private val btnEliminar: Button = itemView.findViewById(R.id.btnEliminar)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val imgCover: ImageView = itemView.findViewById(R.id.imgCover)

        fun bind(anime: Anime) {
            tvTitulo.text = anime.nombre ?: ""
            tvAutor.text = "Autor: ${anime.autor ?: "-"}"
            tvValoracion.text = "Valoración: ${anime.valoracion ?: "-"}"
            tvCategorias.text = "Categoría: ${anime.categoria ?: "-"}"

            // Determinar cómo cargar la imagen
            when {
                // Si es una imagen Base64
                !anime.coverUrl.isNullOrBlank() && anime.coverUrl.startsWith("data:image") -> {
                    try {
                        // Extraer el Base64 puro (después de la coma)
                        val base64String = anime.coverUrl.substring(anime.coverUrl.indexOf(",") + 1)
                        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imgCover.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        imgCover.setImageResource(R.drawable.placeholder_anime)
                    }
                }
                // Si hay coverUrl y parece una URL completa (empieza con http)
                !anime.coverUrl.isNullOrBlank() && anime.coverUrl.startsWith("http") -> {
                    Glide.with(itemView.context)
                        .load(anime.coverUrl)
                        .placeholder(R.drawable.placeholder_anime)
                        .error(R.drawable.placeholder_anime)
                        .into(imgCover)
                }
                // Si hay coverUrl pero no es URL completa, construir URL local
                !anime.coverUrl.isNullOrBlank() -> {
                    Glide.with(itemView.context)
                        .load("http://10.0.2.2:8090/images/${anime.coverUrl}")
                        .placeholder(R.drawable.placeholder_anime)
                        .error(R.drawable.placeholder_anime)
                        .into(imgCover)
                }
                // Si hay miniatura, construir URL local
                !anime.miniatura.isNullOrBlank() -> {
                    Glide.with(itemView.context)
                        .load("http://10.0.2.2:8090/images/${anime.miniatura}")
                        .placeholder(R.drawable.placeholder_anime)
                        .error(R.drawable.placeholder_anime)
                        .into(imgCover)
                }
                // Si no hay nada, mostrar placeholder
                else -> {
                    imgCover.setImageResource(R.drawable.placeholder_anime)
                }
            }

            // Mostrar/ocultar botón de favoritos según si hay callback
            if (onToggleFavoritos != null) {
                val isFav = forceRemoveLabel || (anime.id != null && favoritos.contains(anime.id!!))
                btnFavorito.text = if (isFav) "Quitar" else "Favoritos"
                btnFavorito.visibility = View.VISIBLE
                btnFavorito.setOnClickListener { onToggleFavoritos.invoke(anime) }
            } else {
                btnFavorito.visibility = View.GONE
            }

            // Mostrar/ocultar botón de eliminar según si hay callback
            if (onDelete != null) {
                btnEliminar.visibility = View.VISIBLE
                btnEliminar.setOnClickListener { onDelete.invoke(anime) }
            } else {
                btnEliminar.visibility = View.GONE
            }

            cardView.setOnClickListener { onClick(anime) }
        }
    }
}
