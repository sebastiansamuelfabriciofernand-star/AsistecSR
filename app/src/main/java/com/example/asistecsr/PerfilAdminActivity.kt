package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class PerfilAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_admin)

        // Vinculación de los elementos vigentes en la interfaz
        val btnAtras = findViewById<ImageView>(R.id.btnAtrasPerfilAdmin)
        val txtNombre = findViewById<TextView>(R.id.txtAdminNombre)
        val txtEmail = findViewById<TextView>(R.id.txtAdminEmail)

        // Vinculación exacta con los botones naranjas de tipo LinearLayout
        val btnListaAlumnos = findViewById<LinearLayout>(R.id.btnListaAlumnos)
        val btnListaDocentes = findViewById<LinearLayout>(R.id.btnListaDocentes)
        val btnModoMantenimiento = findViewById<AppCompatButton>(R.id.btnModoMantenimiento)

        // Acción para cerrar ventana actual
        btnAtras.setOnClickListener {
            finish()
        }

        // Abrir la Lista de Docentes (Selector de Carreras)
        btnListaDocentes.setOnClickListener {
            val intent = Intent(this, SelectorCarrerasActivity::class.java)
            startActivity(intent)
        }

        // Abrir la Lista de Alumnos
        btnListaAlumnos.setOnClickListener {
            val intent = Intent(this, ListaAlumnosActivity::class.java)
            startActivity(intent)
        }

        // Modo Mantenimiento
        btnModoMantenimiento.setOnClickListener {
            Toast.makeText(this, "Modo Mantenimiento Activado", Toast.LENGTH_SHORT).show()
        }
    }
}