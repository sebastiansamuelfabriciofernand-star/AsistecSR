package com.example.asistecsr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EstudianteModel(
    @SerialName("codigoQr") val codigoQr: String,
    @SerialName("Nombres") val nombres: String,
    @SerialName("Apellidos") val apellidos: String,
    @SerialName("Dni") val dni: String,
    @SerialName("Edad") val edad: Int,
    @SerialName("Email") val email: String,
    @SerialName("ciclo") val ciclo: Int,
    @SerialName("carrera") val carrera: String,
    @SerialName("estado") val estado: Boolean,
    @SerialName("turno") val turno: String
) : java.io.Serializable