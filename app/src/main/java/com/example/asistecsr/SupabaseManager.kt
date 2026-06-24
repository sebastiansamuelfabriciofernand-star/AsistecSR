package com.example.asistecsr

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseManager {
    // URL CORREGIDA: Se añadió la 'y' después de la 'f' para coincidir con el proyecto real
    val SUPABASE_URL = "https://srzzzcyfyhnslluduhab.supabase.co"

    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNyenp6Y3lmeWhuc2xsdWR1aGFiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODExNzA2NTQsImV4cCI6MjA5Njc0NjY1NH0.fUcOctEuARFi-aj8QSoxXfMDh9NArXMHIld5gE6x464"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Realtime)
    }
}
