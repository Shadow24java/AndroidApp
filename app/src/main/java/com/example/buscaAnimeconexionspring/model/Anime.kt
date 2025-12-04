package com.example.buscaAnimeconexionspring.model

import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

data class Anime(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val valoracion: Double?,
    val autor: String?,
    val enlaceTrailer: String?,
    val enlaceVer: String?,
    val miniaturas: String?,
    val coverUrl: String?,
    val estado: String?,
    val fechaInicio: LocalDate?,
    val fechaFin: LocalDate?,
    val proximoEpNum: Int?,
    val proximoEpFecha: LocalDateTime?,
    val descripcion: String?
    ) : Serializable


