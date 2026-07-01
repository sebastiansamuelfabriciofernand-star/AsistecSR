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
import io.github.jan.supabase.postgrest.from // 👈 IMPORTANTE: Para usar la sintaxis moderna .from()
import kotlinx.coroutines.launch

class PerfilDocenteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_docente)

        // 1. Vincular vistas con los IDs de tu XML original
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

        // 2. Cargar el objeto Serializable de forma segura según la versión de Android
        val docenteExtra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("DOCENTE_OBJETO", DocenteModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("DOCENTE_OBJETO") as? DocenteModel
        }

        // 3. Evaluar el origen de los datos
        if (docenteExtra != null) {
            actualizarUI(docenteExtra, txtNombreDocente, txtPerfilNumero, txtPerfilCorreoInst, txtPerfilCorreoPersonal)
        } else {
            cargarDatosDesdeSupabase(txtNombreDocente, txtPerfilNumero, txtPerfilCorreoInst, txtPerfilCorreoPersonal)
        }

        // 4. Configurar listeners de botones integrados formalmente a tu flujo

        // Botón Cámara (Abre directamente el escáner QR)
        btnIniciarEscaner.setOnClickListener {
            val intent = Intent(this, EscanerQrActivity::class.java).apply {
                putExtra("EXTRA_CICLO", 1)
                putExtra("EXTRA_TURNO", "DIURNO")
            }
            startActivity(intent)
        }

        // Botón Lista de Alumnos
        btnListaAlumnos.setOnClickListener {
            val intent = Intent(this, ListaAlumnosActivity::class.java)
            startActivity(intent)
        }

        // Botón Historial/Lista de Asistencias
        btnListaAsistencias.setOnClickListener {
            val intent = Intent(this, SeleccionAsistenciaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarDatosDesdeSupabase(txtNom: TextView, txtNum: TextView, txtInst: TextView, txtPers: TextView) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    // CORRECCIÓN: Uso de la sintaxis limpia .from() compatible con tu BOM actual
                    val response = SupabaseManager.client.from("Docentes").select {
                        filter {
                            eq("id_docente", userId)
                        }
                    }

                    val docente = response.decodeSingle<DocenteModel>()
                    actualizarUI(docente, txtNom, txtNum, txtInst, txtPers)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@PerfilDocenteActivity, "Error al cargar datos del profesor", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            @Suppress("SpellCheckingInspection")
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