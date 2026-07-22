package com.example.asistecsr

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.io.Serializable as JavaSerializable

@Serializable
data class AdminModel(
    @SerialName("id_Administrador") val id_Administrador: String? = "",
    @SerialName("Nombres") val nombres: String? = "",
    @SerialName("Apellidos") val apellidos: String? = "",
    @SerialName("Dni") val dni: String? = "",
    @SerialName("Numero") val numero: String? = "",
    @SerialName("Correo") val correo: String? = ""
) : JavaSerializable