package com.example.asistecsr

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ForgotActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)

        val btnEnviarInstrucciones = findViewById<AppCompatButton>(R.id.btnEnviarInstrucciones)
        val btnVolverInicio = findViewById<AppCompatButton>(R.id.btnVolverInicio)

        // Acción al pulsar "ENVIAR INSTRUCCIONES"
        btnEnviarInstrucciones.setupClickAnimation {
            Toast.makeText(this, "Instrucciones enviadas al correo", Toast.LENGTH_LONG).show()
        }

        // Acción al pulsar "VOLVER AL INICIO"
        btnVolverInicio.setupClickAnimation {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
