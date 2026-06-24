package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class PerfilAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_admin)

        // Mapeo completo de IDs basados en tu interfaz real
        val btnAtras = findViewById<ImageView>(R.id.btnAtrasPerfilAdmin)
        val txtNombre = findViewById<TextView>(R.id.txtAdminNombre)
        val txtApellidos = findViewById<TextView>(R.id.txtAdminApellidos)
        val txtEdad = findViewById<TextView>(R.id.txtAdminEdad)
        val txtNumero = findViewById<TextView>(R.id.txtAdminNumero)
        val txtEmail = findViewById<TextView>(R.id.txtAdminEmail)

        val btnIniciarEscaner = findViewById<AppCompatButton>(R.id.btnIniciarEscaner)
        val btnListaAlumnos = findViewById<AppCompatButton>(R.id.btnListaAlumnos)
        val btnListaDocentes = findViewById<AppCompatButton>(R.id.btnListaDocentes)
        val btnModoMantenimiento = findViewById<AppCompatButton>(R.id.btnModoMantenimiento)

        // Acción para cerrar ventana actual
        btnAtras.setOnClickListener {
            finish()
        }

        // Abrir el selector de carreras corregido al pulsar LISTA DEDOCENTES
        btnListaDocentes.setOnClickListener {
            val intent = Intent(this, SelectorCarrerasActivity::class.java)
            startActivity(intent)
        }

        btnListaAlumnos.setOnClickListener {
            val intent = Intent(this, ListaAlumnosActivity::class.java)
            startActivity(intent)
        }

        btnIniciarEscaner.setOnClickListener {
            // Aquí puedes llamar a tu lógica de inicialización de la cámara QR
            Toast.makeText(this, "Abriendo Cámara QR...", Toast.LENGTH_SHORT).show()
        }

        btnModoMantenimiento.setOnClickListener {
            Toast.makeText(this, "Modo Mantenimiento Activado", Toast.LENGTH_SHORT).show()
        }
    }
}