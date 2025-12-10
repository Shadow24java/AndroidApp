package com.example.buscaAnimeconexionspring.service

import com.example.buscaAnimeconexionspring.api.AnimeApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // 👇 IMPORTANTE: puerto numérico + barra final
    // si tu backend va en 8091, cambia 8090 por 8091
    private const val BASE_URL = "http://10.0.2.2:8091/api/"

    private var retrofit: Retrofit? = null

    fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)                         
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getAnimeApiService(): AnimeApiService {
        return getRetrofit().create(AnimeApiService::class.java)
    }
}
