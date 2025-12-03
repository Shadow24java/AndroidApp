package com.joange.cineapp.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.joange.cineapp.api.PeliculaApiService;

public class ApiClient {
    
    // IMPORTANTE: Cambia esta URL por la IP de tu máquina
    // Para emulador Android: usa "http://10.0.2.2:8090/api/"
    // Para dispositivo físico: usa "http://TU_IP:8090/api/" (ejemplo: "http://192.168.1.100:8090/api/")
    private static final String BASE_URL = "http://10.0.2.2:8090/api/";
    
    private static Retrofit retrofit = null;
    
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    public static PeliculaApiService getApiService() {
        return getRetrofit().create(PeliculaApiService.class);
    }
}

