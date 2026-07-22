package com.example.asistecsr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsistenciaModel(
    @SerialName("fecha") val fecha: String? = "",
    @SerialName("codigoQr") val codigoQr: String? = "",
    @SerialName("id_docente") val idDocente: String? = "",
    @SerialName("id_curso") val idCurso: String? = "",
    @SerialName("ciclo") val ciclo: Int? = 0,
    @SerialName("turno") val turno: String? = "",

    // CAMPOS ADICIONALES REQUERIDOS POR TU ADAPTER:
    @SerialName("nombres") val nombres: String? = null,
    @SerialName("apellidos") val apellidos: String? = null,
    @SerialName("dniAlumno") val dniAlumno: String? = null,
    @SerialName("clase") val clase: String? = null,
    @SerialName("estadoAsistencia") val estadoAsistencia: String? = null
)