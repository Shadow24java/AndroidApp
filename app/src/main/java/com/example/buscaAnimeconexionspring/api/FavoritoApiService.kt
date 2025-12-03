package com.example.buscaAnimeconexionspring.api

import retrofit2.Call
import com.example.buscaAnimeconexionspring.model.Favorito
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoritoApiService {
    @GET("/favorito/{uid}")
    fun getFavoritos(@Path("uid") uid: String): Call<List<Favorito>>

    @POST("/favorito/add")
    fun addFavorito(@Body fav: Favorito): Call<Favorito>

    @DELETE("/favorito/delete/{id}")
    fun deleteFavorito(@Path("id") id: Long): Call<Void>
}