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
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class PerfilDocenteActivity : AppCompatActivity() {

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

        btnAtras.setupClickAnimation {
            finish()
        }

        val docenteExtra =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("DOCENTE_OBJETO", DocenteModel::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra("DOCENTE_OBJETO") as? DocenteModel
            }

        if (docenteExtra != null) {

            docenteLogueadoId = docenteExtra.idDocente ?: ""

            actualizarUI(
                docenteExtra,
                txtNombreDocente,
                txtPerfilNumero,
                txtPerfilCorreoInst,
                txtPerfilCorreoPersonal
            )

        } else {

            cargarDatosDesdeSupabase(
                txtNombreDocente,
                txtPerfilNumero,
                txtPerfilCorreoInst,
                txtPerfilCorreoPersonal
            )

        }

        // Ahora abre la pantalla para seleccionar el curso
        btnIniciarEscaner.setupClickAnimation {

            Toast.makeText(
                this,
                "Abriendo selección de cursos...",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, SeleccionCursoActivity::class.java)
            intent.putExtra("EXTRA_ID_DOCENTE", docenteLogueadoId)

            startActivity(intent)
        }

        btnListaAlumnos.setupClickAnimation {

            startActivity(
                Intent(this, SeleccionAlumnosActivity::class.java)
            )

        }

        btnListaAsistencias.setupClickAnimation {

            startActivity(
                Intent(this, SeleccionAsistenciaActivity::class.java)
            )

        }

    }

    private fun cargarDatosDesdeSupabase(
        txtNom: TextView,
        txtNum: TextView,
        txtInst: TextView,
        txtPers: TextView
    ) {

        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

        if (userId != null) {

            docenteLogueadoId = userId

            lifecycleScope.launch {

                try {

                    val response =
                        SupabaseManager.client
                            .from("Docentes")
                            .select {

                                filter {
                                    eq("id_docente", userId)
                                }

                            }

                    val docente =
                        response.decodeSingle<DocenteModel>()

                    actualizarUI(
                        docente,
                        txtNom,
                        txtNum,
                        txtInst,
                        txtPers
                    )

                } catch (e: Exception) {

                    e.printStackTrace()

                    Toast.makeText(
                        this@PerfilDocenteActivity,
                        "Error al cargar datos del profesor",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        } else {

            Toast.makeText(
                this,
                "Sesión expirada",
                Toast.LENGTH_LONG
            ).show()

            finish()

        }

    }

    private fun actualizarUI(
        docente: DocenteModel,
        txtNom: TextView,
        txtNum: TextView,
        txtInst: TextView,
        txtPers: TextView
    ) {

        txtNom.text = docente.nombresApellidos

        txtNum.text =
            getString(
                R.string.perfil_docente_numero,
                docente.numero
            )

        txtInst.text =
            getString(
                R.string.perfil_docente_email_inst,
                docente.correoInst
            )

        txtPers.text =
            getString(
                R.string.perfil_docente_email_personal,
                docente.correo
            )

    }

}