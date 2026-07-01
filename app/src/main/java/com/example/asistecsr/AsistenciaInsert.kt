package com.example.asistecsr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsistenciaInsert(
    @SerialName("id_docente") val idDocente: String,
    @SerialName("id_alumno") val idAlumno: String,
    @SerialName("fecha") val fecha: String,
    @SerialName("estado") val estado: String, // "ASISTIO" o "TARDANZA"
    @SerialName("ciclo") val ciclo: Int,
    @SerialName("turno") val turno: String
)