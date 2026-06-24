package com.example.asistecsr

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.io.Serializable as JavaSerializable

@Serializable
data class AdminProfile(
    @SerialName("id_Administrador") val id_Administrador: String,
    @SerialName("Nombres") val Nombres: String,
    @SerialName("Apellidos") val Apellidos: String,
    @SerialName("Dni") val Dni: String,
    @SerialName("Numero") val Numero: String,
    @SerialName("Correo") val Correo: String
) : JavaSerializable
