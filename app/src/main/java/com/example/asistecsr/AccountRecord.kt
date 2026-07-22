package com.example.asistecsr

import kotlinx.serialization.Serializable

@Serializable
data class AccountRecord(val email: String, val pass: String)
