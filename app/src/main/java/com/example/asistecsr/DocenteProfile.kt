package com.example.asistecsr

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.io.Serializable as JavaSerializable

@Serializable
data class DocenteProfile(
    @SerialName("id_docente") val idDocente: String,
    @SerialName("nombres_apellidos") val nombresApellidos: String,
    @SerialName("especialidad") val especialidad: String,
    @SerialName("numero") val numero: String,             // <-- Asegúrate de tenerla así
    @SerialName("correo") val correo: String,             // <-- Y esta también
    @SerialName("correo_inst") val correoInst: String,
    @SerialName("estado") val estado: Boolean,
    @SerialName("carrera") val carrera: String
) : JavaSerializable