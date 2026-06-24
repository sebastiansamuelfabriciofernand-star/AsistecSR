package com.example.asistecsr

import kotlinx.serialization.Serializable

@Serializable
data class AlumnoAsistencia(
    val numeroOrden: Int = 0,
    val nombre: String = "",
    val idAlumno: String = "",
    val materia: String = "",
    var asistio: Boolean = true,
    val fecha: String = "",
    val ciclo: Int = 0,      // 🚨 Asegúrate de que existan estas dos líneas
    val turno: String = ""
)