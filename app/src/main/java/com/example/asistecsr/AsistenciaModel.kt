package com.example.asistecsr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsistenciaModel(
    val id: Int? = null,
    @SerialName("codigoQr") val dniAlumno: String? = "", // Cambiado a codigoQr que es lo que guardamos
    val nombres: String? = "Alumno",
    val apellidos: String? = "Desconocido",
    @SerialName("id_curso") val clase: String? = "",    // Coincide con id_curso del insert
    @SerialName("estado") val estadoAsistencia: String? = "ASISTIO",
    val fecha: String,
    val ciclo: Int,
    val turno: String,
    @SerialName("id_docente") val id_docente: String
)