package com.example.buscaAnimeconexionspring.api

import com.example.buscaAnimeconexionspring.AnimeCreateDto
import com.example.buscaAnimeconexionspring.model.Anime
import com.example.buscaAnimeconexionspring.model.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AnimeApiService {

    @GET("/api/animes")
    fun getAll(): Call<ApiResponse<List<Anime>>>

    @GET("/api/animes/{id}")
    fun getById(@Path("id") id: Long): Call<ApiResponse<Anime>>


    @GET("/api/animes/emision")
    fun getAiring(): Call<ApiResponse<List<Anime>>>


    @GET("/api/animes/categoria/{categoria}")
    fun getByCategory(@Path("categoria") categoria: String): Call<ApiResponse<List<Anime>>>


    @GET("/api/animes/anilist/{anilistId}")
    fun getByAnilist(@Path("anilistId") anilistId: Long): Call<ApiResponse<Anime>>

    @GET("/api/favoritos")
    fun getFavorites(): Call<ApiResponse<List<Anime>>>

    @POST("/api/favoritos/{id}")
    fun addFavorite(@Path("id") id: Long): Call<ApiResponse<Boolean>>

    @POST("/api/animes")
    fun createAnime(@Body body: AnimeCreateDto): Call<ApiResponse<Anime>>

    @DELETE("/api/favoritos/{id}")
    fun removeFavorite(@Path("id") id: Long): Call<ApiResponse<Boolean>>
}


