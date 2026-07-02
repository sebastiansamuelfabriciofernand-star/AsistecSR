package com.example.asistecsr

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {
    const val SUPABASE_URL = "https://srzzzcyfyhnslluduhab.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNyenp6Y3lmeWhuc2xsdWR1aGFiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODExNzA2NTQsImV4cCI6MjA5Njc0NjY1NH0.fUcOctEuARFi-aj8QSoxXfMDh9NArXMHIld5gE6x464"

    // CORRECCIÓN: Parámetros nombrados obligatorios para activar la base de datos correctamente
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}