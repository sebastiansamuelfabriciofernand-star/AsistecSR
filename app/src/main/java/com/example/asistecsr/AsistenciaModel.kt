package com.example.asistecsr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsistenciaModel(
    @SerialName("fecha") val fecha: String, // Formato "YYYY-MM-DD"
    @SerialName("codigoQr") val codigoQr: String,
    @SerialName("id_docente") val idDocente: String,
    @SerialName("id_curso") val idCurso: String,
    @SerialName("ciclo") val ciclo: Int,
    @SerialName("turno") val turno: String
)