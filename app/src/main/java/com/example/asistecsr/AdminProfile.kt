package com.example.asistecsr

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.io.Serializable as JavaSerializable

@Serializable
data class AdminProfile(
    @SerialName("id_Administrador") val id_Administrador: String,
    @SerialName("Nombres") val nombres: String,     // ✓ Cambiado a minúscula
    @SerialName("Apellidos") val apellidos: String, // ✓ Cambiado a minúscula
    @SerialName("Dni") val dni: String,             // ✓ Cambiado a minúscula
    @SerialName("Numero") val numero: String,       // ✓ Cambiado a minúscula
    @SerialName("Correo") val correo: String
) : JavaSerializable