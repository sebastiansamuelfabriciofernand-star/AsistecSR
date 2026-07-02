package com.example.asistecsr

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnProfesor = findViewById<View>(R.id.btnProfesor)
        val btnEstudiante = findViewById<View>(R.id.btnEstudiante)
        val btnAdministrador = findViewById<View>(R.id.btnAdministrador)

        // CORRECCIÓN: Uso de contexto explícito de la Activity dentro de los bloques
        configurarAnimacionPresion(btnProfesor) {
            val intent = Intent(this@MenuActivity, LoginActivity::class.java)
            intent.putExtra("ROL_SELECCIONADO", "PROFESOR")
            startActivity(intent)
        }

        configurarAnimacionPresion(btnEstudiante) {
            val intent = Intent(this@MenuActivity, LoginActivity::class.java)
            intent.putExtra("ROL_SELECCIONADO", "ESTUDIANTE")
            startActivity(intent)
        }

        configurarAnimacionPresion(btnAdministrador) {
            val intent = Intent(this@MenuActivity, LoginActivity::class.java)
            intent.putExtra("ROL_SELECCIONADO", "ADMINISTRADOR")
            startActivity(intent)
        }
    }

    /**
     * Función que genera el efecto visual de "encogimiento" al mantener presionado.
     * Añade soporte de accesibilidad llamando de manera segura a performClick().
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun configurarAnimacionPresion(vista: View, accionClick: () -> Unit) {
        vista.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Se encoge un 5% al tocarlo
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP -> {
                    // Regresa a su tamaño original al soltarlo y ejecuta la acción
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    v.performClick() // SOLUCIÓN LINTER: Garantiza soporte para accesibilidad
                    accionClick()
                }
                MotionEvent.ACTION_CANCEL -> {
                    // Regresa a su tamaño original si se cancela el gesto
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            true
        }
    }
}