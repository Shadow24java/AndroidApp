package com.example.buscaAnimeconexionspring.model

import java.io.Serializable

data class Anime(
    val id: Long,
    val title: String,
    val author: String,
    val releaseDate: String,
    val thumbnailUrl: String,
    val bannerUrl: String,
    val trailerUrl: String,
    val rating: Double,
    val airing: Boolean,
    val categories: String,
    val platformLinks: String
) : Serializable


