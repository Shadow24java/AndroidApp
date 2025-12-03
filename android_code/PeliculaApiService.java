package com.joange.cineapp.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import com.joange.cineapp.model.Pelicula;

public interface PeliculaApiService {
    
    // Obtener todas las películas
    @GET("peliculas")
    Call<List<Pelicula>> getAllPeliculas();
    
    // Obtener película por ID
    @GET("peliculas/{id}")
    Call<Pelicula> getPeliculaById(@Path("id") Long id);
    
    // Crear nueva película
    @POST("peliculas")
    Call<Pelicula> createPelicula(@Body Pelicula pelicula);
    
    // Actualizar película
    @PUT("peliculas")
    Call<Pelicula> updatePelicula(@Body Pelicula pelicula);
    
    // Eliminar película
    @DELETE("peliculas/{id}")
    Call<String> deletePelicula(@Path("id") Long id);
}

