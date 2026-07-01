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

        btnAsistenciasTop.setOnClickListener {
            startActivity(Intent(this, ListaAlumnosActivity::class.java))
        }

        btnAtras.setOnClickListener { finish() }

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
                // Usamos el cliente real que tienes en tu proyecto
                // Cambia la línea en tu función cargarDocentesDesdeBaseDatos:
                val listaDocentes = SupabaseManager.client.from("Docentes") // Cambiado a "Docentes" con D mayúscula
                    .select {
                        filter {
                            eq("carrera", carrera)
                        }
                    }
                    .decodeList<DocenteProfile>()

                txtTotal.text = listaDocentes.size.toString()
                txtAct.text = listaDocentes.size.toString()
                txtInactivosVista.text = "0"

                docenteAdapter = DocenteAdapter(listaDocentes)
                rvDocentes.adapter = docenteAdapter

            } catch (e: Exception) {
                e.printStackTrace()
                // Aquí verás el error real en pantalla si algo falla
                Toast.makeText(this@ListaDocentesActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}