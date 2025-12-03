package com.joange.cineapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.joange.cineapp.R;
import com.joange.cineapp.model.Pelicula;

public class PeliculaAdapter extends RecyclerView.Adapter<PeliculaAdapter.PeliculaViewHolder> {
    
    private List<Pelicula> peliculas;
    
    public PeliculaAdapter(List<Pelicula> peliculas) {
        this.peliculas = peliculas;
    }
    
    @NonNull
    @Override
    public PeliculaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pelicula, parent, false);
        return new PeliculaViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PeliculaViewHolder holder, int position) {
        Pelicula pelicula = peliculas.get(position);
        holder.bind(pelicula);
    }
    
    @Override
    public int getItemCount() {
        return peliculas != null ? peliculas.size() : 0;
    }
    
    static class PeliculaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitulo;
        private TextView tvGenero;
        private TextView tvAnio;
        private TextView tvDuracion;
        private TextView tvDirector;
        private CardView cardView;
        
        public PeliculaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvGenero = itemView.findViewById(R.id.tvGenero);
            tvAnio = itemView.findViewById(R.id.tvAnio);
            tvDuracion = itemView.findViewById(R.id.tvDuracion);
            tvDirector = itemView.findViewById(R.id.tvDirector);
            cardView = itemView.findViewById(R.id.cardView);
        }
        
        public void bind(Pelicula pelicula) {
            tvTitulo.setText(pelicula.getTitulo());
            tvGenero.setText("Género: " + pelicula.getGenero());
            tvAnio.setText("Año: " + pelicula.getAnio());
            tvDuracion.setText("Duración: " + pelicula.getDuracion() + " min");
            tvDirector.setText("Director: " + pelicula.getDirector());
        }
    }
}

