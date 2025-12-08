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
    private val onToggleFavoritos: (Anime) -> Unit,
    private val favoritos: MutableList<Long>
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
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val imgCover: ImageView = itemView.findViewById(R.id.imgCover)

        fun bind(anime: Anime) {
            tvTitulo.text = anime.nombre ?: ""
            tvAutor.text = "Autor: ${anime.autor ?: "-"}"
            tvValoracion.text = "Valoración: ${anime.valoracion ?: "-"}"
            tvCategorias.text = "Categoría: ${anime.categoria ?: "-"}"

            val url = anime.coverUrl ?: anime.miniaturas?.let { "http://10.0.2.2:8090/images/$it" }

            Glide.with(itemView.context)
                .load(url)
                .placeholder(R.drawable.placeholder_anime)
                .error(R.drawable.placeholder_anime)
                .into(imgCover)



            btnFavorito.text = if (favoritos.contains(anime.id)) "Quitar" else "Favoritos"

            cardView.setOnClickListener { onClick(anime) }
            btnFavorito.setOnClickListener { onToggleFavoritos(anime) }
        }
    }
}
