package com.example.asistecsr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EstudianteModel(
    @SerialName("codigoQr") val codigoQr: String,
    @SerialName("Nombres") val nombres: String,   // Asegúrate de que aquí diga 'nombres' en minúscula
    @SerialName("Apellidos") val apellidos: String, // Asegúrate de que aquí diga 'apellidos' en minúscula
    @SerialName("Dni") val dni: String,             // Asegúrate de que aquí diga 'dni' en minúscula
    @SerialName("Edad") val edad: Int,
    @SerialName("Email") val email: String,
    @SerialName("ciclo") val ciclo: Int,
    @SerialName("carrera") val carrera: String,
    @SerialName("estado") val estado: Boolean,
    @SerialName("turno") val turno: String
)