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
import io.github.jan.supabase.postgrest.from // 👈 CORRECCIÓN: Importación agregada
import kotlinx.coroutines.launch

class PerfilDocenteActivity : AppCompatActivity() {

    // Variable global para almacenar el ID del docente logueado
    private var docenteLogueadoId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_docente)

        val btnAtras = findViewById<ImageView>(R.id.btnAtrasPerfilDocente)
        val txtNombreDocente = findViewById<TextView>(R.id.txtNombreDocente)
        val txtPerfilNumero = findViewById<TextView>(R.id.txtPerfilNumero)
        val txtPerfilCorreoInst = findViewById<TextView>(R.id.txtPerfilCorreoInst)
        val txtPerfilCorreoPersonal = findViewById<TextView>(R.id.txtPerfilCorreoPersonal)

        val btnIniciarEscaner = findViewById<AppCompatButton>(R.id.btnIniciarEscaner)
        val btnListaAlumnos = findViewById<LinearLayout>(R.id.btnContenedorListaAlumnos)
        val btnListaAsistencias = findViewById<LinearLayout>(R.id.btnContenedorListaAsistencias)

        btnAtras.setOnClickListener { finish() }

        val docenteExtra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("DOCENTE_OBJETO", DocenteModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("DOCENTE_OBJETO") as? DocenteModel
        }

        if (docenteExtra != null) {
            docenteLogueadoId = docenteExtra.idDocente
            actualizarUI(docenteExtra, txtNombreDocente, txtPerfilNumero, txtPerfilCorreoInst, txtPerfilCorreoPersonal)
        } else {
            cargarDatosDesdeSupabase(txtNombreDocente, txtPerfilNumero, txtPerfilCorreoInst, txtPerfilCorreoPersonal)
        }

        // CORRECCIÓN: Ahora sí le pasamos el ID del docente al escáner para que pueda registrar
        btnIniciarEscaner.setOnClickListener {
            val intent = Intent(this, EscanerQrActivity::class.java).apply {
                putExtra("EXTRA_CICLO", 1)
                putExtra("EXTRA_TURNO", "DIURNO")
                putExtra("EXTRA_ID_DOCENTE", docenteLogueadoId) // 👈 AQUÍ SE ENVÍA EL ID
            }
            startActivity(intent)
        }

        btnListaAlumnos.setOnClickListener {
            val intent = Intent(this, ListaAlumnosActivity::class.java)
            startActivity(intent)
        }

        btnListaAsistencias.setOnClickListener {
            val intent = Intent(this, SeleccionAsistenciaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarDatosDesdeSupabase(txtNom: TextView, txtNum: TextView, txtInst: TextView, txtPers: TextView) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

        if (userId != null) {
            docenteLogueadoId = userId
            lifecycleScope.launch {
                try {
                    val response = SupabaseManager.client.from("Docentes").select {
                        filter { eq("id_docente", userId) }
                    }
                    val docente = response.decodeSingle<DocenteModel>()
                    actualizarUI(docente, txtNom, txtNum, txtInst, txtPers)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@PerfilDocenteActivity, "Error al cargar datos del profesor", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Sesión de Supabase inválida o expirada", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun actualizarUI(docente: DocenteModel, txtNom: TextView, txtNum: TextView, txtInst: TextView, txtPers: TextView) {
        txtNom.text = docente.nombresApellidos
        txtNum.text = getString(R.string.perfil_docente_numero, docente.numero)
        txtInst.text = getString(R.string.perfil_docente_email_inst, docente.correoInst)
        txtPers.text = getString(R.string.perfil_docente_email_personal, docente.correo)
    }
}