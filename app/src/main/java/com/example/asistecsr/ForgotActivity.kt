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
        configurarEfectoBoton(btnEnviarInstrucciones) {
            Toast.makeText(this, "Instrucciones enviadas al correo", Toast.LENGTH_LONG).show()
        }

        // Acción al pulsar "VOLVER AL INICIO"
        configurarEfectoBoton(btnVolverInicio) {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // Animación de escala táctil suave con soporte de accesibilidad
    @SuppressLint("ClickableViewAccessibility")
    private fun configurarEfectoBoton(boton: View, accion: () -> Unit) {
        boton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).start()
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                    // Corrección de accesibilidad: ejecuta el click nativo
                    v.performClick()
                    accion()
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                }
            }
            true
        }
    }
}