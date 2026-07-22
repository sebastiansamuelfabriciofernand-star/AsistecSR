package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class ListaDocentesActivity : AppCompatActivity() {

    private lateinit var rvDocentes: RecyclerView
    private lateinit var txtTituloCarreraDinamico: TextView
    private lateinit var docenteAdapter: DocenteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_docentes)

        val btnAtras = findViewById<ImageView>(R.id.btnAtrasLista)
        val btnAsistenciasTop = findViewById<AppCompatButton>(R.id.btnAsistenciasTop)
        val txtTotalDocentes = findViewById<TextView>(R.id.txtTotalDocentesCount)
        val txtActivos = findViewById<TextView>(R.id.txtActivosCount)
        val txtInactivosVista = findViewById<TextView>(R.id.txtInactivosCount)

        rvDocentes = findViewById(R.id.rvDocentes)
        txtTituloCarreraDinamico = findViewById(R.id.txtTituloCarreraDinamico)

        rvDocentes.layoutManager = LinearLayoutManager(this)

        val carreraSeleccionada = intent.getStringExtra("CARRERA_SELECCIONADA") ?: "DOCENTES"
        txtTituloCarreraDinamico.text = carreraSeleccionada.uppercase()

        btnAsistenciasTop.setupClickAnimation {
            // Intenta abrir ListaAlumnosActivity de forma segura
            try {
                startActivity(Intent(this, ListaAlumnosActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "La pantalla de alumnos no está disponible.", Toast.LENGTH_SHORT).show()
            }
        }

        btnAtras.setupClickAnimation { finish() }

        cargarDocentesDesdeBaseDatos(carreraSeleccionada, txtTotalDocentes, txtActivos, txtInactivosVista)
    }

    private fun cargarDocentesDesdeBaseDatos(
        carrera: String,
        txtTotal: TextView,
        txtAct: TextView,
        txtInactivosVista: TextView
    ) {
        lifecycleScope.launch {
            try {
                // CONSULTA IMPRECCINDIBLE: Sintaxis .from() limpia y mapeo directo a DocenteModel
                val response = SupabaseManager.client.from("Docentes").select {
                    filter {
                        eq("carrera", carrera)
                    }
                }

                val listaDocentes = response.decodeList<DocenteModel>()

                // Modificamos los contadores de la interfaz con los tamaños reales de la lista
                txtTotal.text = listaDocentes.size.toString()
                txtAct.text = listaDocentes.size.toString()
                txtInactivosVista.text = "0"

                // Vinculación al adaptador corregido que usa DocenteModel
                docenteAdapter = DocenteAdapter(listaDocentes)
                rvDocentes.adapter = docenteAdapter

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ListaDocentesActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}