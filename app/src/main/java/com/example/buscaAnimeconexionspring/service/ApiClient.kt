package com.example.buscaAnimeconexionspring.service

import com.example.buscaAnimeconexionspring.api.AnimeApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    

    private const val BASE_URL = "http://10.0.2.2:8090/api/"

    
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
    
    fun getAnimeApiService(): AnimeApiService{
        return getRetrofit().create(AnimeApiService::class.java)
    }
}

