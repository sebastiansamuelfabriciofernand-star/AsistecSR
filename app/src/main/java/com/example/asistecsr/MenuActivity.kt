package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnProfesor = findViewById<View>(R.id.btnProfesor)
        val btnEstudiante = findViewById<View>(R.id.btnEstudiante)
        val btnAdministrador = findViewById<View>(R.id.btnAdministrador)

        // Aplicamos la animación especial que "ilumina" el botón SOLO al tocarlo
        btnProfesor.setupRoleSelectionAnimation(R.color.rojo_brillante) {
            val intent = Intent(this@MenuActivity, LoginActivity::class.java)
            intent.putExtra("ROL_SELECCIONADO", "PROFESOR")
            startActivity(intent)
        }

        btnEstudiante.setupRoleSelectionAnimation(R.color.azul_asiste) {
            val intent = Intent(this@MenuActivity, LoginActivity::class.java)
            intent.putExtra("ROL_SELECCIONADO", "ESTUDIANTE")
            startActivity(intent)
        }

        btnAdministrador.setupRoleSelectionAnimation(R.color.verde_neon) {
            val intent = Intent(this@MenuActivity, LoginActivity::class.java)
            intent.putExtra("ROL_SELECCIONADO", "ADMINISTRADOR")
            startActivity(intent)
        }
    }
}