package com.example.asistecsr

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegresarLogin = findViewById<ImageView>(R.id.btnRegresarLogin)
        val btnCrearCuenta = findViewById<AppCompatButton>(R.id.btnCrearCuenta)

        // Acción para regresar a la pantalla anterior
        btnRegresarLogin.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Acción al pulsar Crear Cuenta
        btnCrearCuenta.setOnClickListener {
            Toast.makeText(this, "Procesando registro de usuario...", Toast.LENGTH_SHORT).show()
        }
    }
}