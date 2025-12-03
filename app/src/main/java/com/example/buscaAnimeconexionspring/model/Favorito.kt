package com.example.buscaAnimeconexionspring.model

import java.io.Serializable
data class Favorito (
    val id: Long? = null,
    val uid: String,
    val animeId: Long
) : Serializable