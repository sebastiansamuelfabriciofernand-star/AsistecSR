package com.example.asistecsr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CursoModel(

    @SerialName("id_curso")
    val idCurso: String? = null,

    @SerialName("nombre_curso")
    val nombreCurso: String? = null,

    @SerialName("horas_sem")
    val horasSem: Int? = null,

    @SerialName("ciclo")
    val ciclo: Int? = null,

    @SerialName("turno")
    val turno: String? = null

)