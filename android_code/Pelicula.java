package com.joange.cineapp.model;

import java.io.Serializable;

public class Pelicula implements Serializable {
    
    private Long idPelicula;
    private String titulo;
    private String genero;
    private int anio;
    private int duracion;
    private String director;
    
    public Pelicula() {
    }
    
    public Pelicula(String titulo, String genero, int anio, int duracion, String director) {
        this.titulo = titulo;
        this.genero = genero;
        this.anio = anio;
        this.duracion = duracion;
        this.director = director;
    }
    
    // Getters y Setters
    public Long getIdPelicula() {
        return idPelicula;
    }
    
    public void setIdPelicula(Long idPelicula) {
        this.idPelicula = idPelicula;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public int getAnio() {
        return anio;
    }
    
    public void setAnio(int anio) {
        this.anio = anio;
    }
    
    public int getDuracion() {
        return duracion;
    }
    
    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }
    
    public String getDirector() {
        return director;
    }
    
    public void setDirector(String director) {
        this.director = director;
    }
    
    @Override
    public String toString() {
        return "Pelicula{" +
                "idPelicula=" + idPelicula +
                ", titulo='" + titulo + '\'' +
                ", genero='" + genero + '\'' +
                ", anio=" + anio +
                ", duracion=" + duracion +
                ", director='" + director + '\'' +
                '}';
    }
}

