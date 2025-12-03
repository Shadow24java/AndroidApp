package com.example.buscaAnimeconexionspring.api

import com.example.buscaAnimeconexionspring.model.Anime
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface AnimeApiService {

    @GET("/anime")
    fun getAll(): Call<List<Anime>>

    @GET("/anime/{id}")
    fun getById(@Path("id") id: Long): Call<Anime>

    @GET("/anime/emision")
    fun getAiring(): Call<List<Anime>>

    @GET("/anime/category/{category}")
    fun getByCategory(@Path("category") category: String): Call<List<Anime>>
}


