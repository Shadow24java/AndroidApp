package com.example.buscaAnimeconexionspring.model

import java.io.Serializable

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
) : Serializable
