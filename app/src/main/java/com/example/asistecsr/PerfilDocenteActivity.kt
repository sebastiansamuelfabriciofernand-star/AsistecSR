package com.example.asistecsr

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class PerfilDocenteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_docente)

        // 1. Vincular vistas con los IDs del XML
        val btnAtras = findViewById<ImageView>(R.id.btnAtrasPerfilDocente)
        val txtNombreDocente = findViewById<TextView>(R.id.txtNombreDocente)
        val txtPerfilNumero = findViewById<TextView>(R.id.txtPerfilNumero)
        val txtPerfilCorreoInst = findViewById<TextView>(R.id.txtPerfilCorreoInst)
        val txtPerfilCorreoPersonal = findViewById<TextView>(R.id.txtPerfilCorreoPersonal)

        val btnIniciarEscaner = findViewById<AppCompatButton>(R.id.btnIniciarEscaner)
        val btnListaAlumnos = findViewById<LinearLayout>(R.id.btnContenedorListaAlumnos)
        val btnListaAsistencias = findViewById<LinearLayout>(R.id.btnContenedorListaAsistencias)

        // Botón Atrás
        btnAtras.setOnClickListener { finish() }

        // 2. Intentar cargar el objeto DocenteProfile de forma segura según la versión de Android
        val docenteExtra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("DOCENTE_OBJETO", DocenteProfile::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("DOCENTE_OBJETO") as? DocenteProfile
        }

        // 3. Evaluar el origen de los datos
        if (docenteExtra != null) {
            actualizarUI(docenteExtra, txtNombreDocente, txtPerfilNumero, txtPerfilCorreoInst, txtPerfilCorreoPersonal)
        } else {
            cargarDatosDesdeSupabase(txtNombreDocente, txtPerfilNumero, txtPerfilCorreoInst, txtPerfilCorreoPersonal)
        }

        // 4. Configurar listeners de botones e Intents de navegación
        btnIniciarEscaner.setOnClickListener {
            Toast.makeText(this, "Abriendo escáner...", Toast.LENGTH_SHORT).show()
        }

        btnListaAlumnos.setOnClickListener {
            startActivity(Intent(this, ListaAlumnosActivity::class.java))
        }

        btnListaAsistencias.setOnClickListener {
            startActivity(Intent(this, SeleccionAsistenciaActivity::class.java))
        }
    }

    private fun cargarDatosDesdeSupabase(txtNom: TextView, txtNum: TextView, txtInst: TextView, txtPers: TextView) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    // Consulta a la tabla Docentes filtrando por id_docente
                    val docente = SupabaseManager.client.postgrest["Docentes"]
                        .select { filter { eq("id_docente", userId) } }
                        .decodeSingle<DocenteProfile>()

                    actualizarUI(docente, txtNom, txtNum, txtInst, txtPers)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@PerfilDocenteActivity, "Error al cargar datos del profesor", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Manejo de error si no hay sesión activa
            Toast.makeText(this, "Sesión inválida o expirada", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun actualizarUI(docente: DocenteProfile, txtNom: TextView, txtNum: TextView, txtInst: TextView, txtPers: TextView) {
        // Mapeo idéntico con las propiedades definidas en tu DocenteProfile.kt
        txtNom.text = docente.nombresApellidos
        txtNum.text = "NUMERO: ${docente.numero}"
        txtInst.text = "CORREO INST: ${docente.correoInst}"
        txtPers.text = "CORREO PERS: ${docente.correo}"
    }
}