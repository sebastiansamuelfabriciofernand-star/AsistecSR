package com.example.asistecsr

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.io.Serializable as JavaSerializable

@Serializable
data class UserProfile(
    @SerialName("codigoQr") val codigoQr: String? = "",
    @SerialName("Nombres") val nombres: String,
    @SerialName("Apellidos") val apellidos: String,
    @SerialName("Dni") val dni: String,
    @SerialName("Edad") val edad: Int = 18,
    @SerialName("Email") val email: String,
    @SerialName("ciclo") val ciclo: Int? = 1,
    @SerialName("carrera") val carrera: String? = "",
    @SerialName("estado") val estado: Boolean = true
) : JavaSerializable
