package com.example.asistecsr

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DocenteModel(
    @SerialName("id_docente") val idDocente: String,
    @SerialName("nombres_apellidos") val nombresApellidos: String,
    @SerialName("especialidad") val especialidad: String,
    @SerialName("numero") val numero: String,
    @SerialName("correo") val correo: String,
    @SerialName("correo_inst") val correoInst: String
) : java.io.Serializable