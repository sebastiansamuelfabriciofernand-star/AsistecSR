package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class SeleccionAlumnosActivity : AppCompatActivity() {

    private lateinit var rvCarreras: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_alumnos)

        findViewById<View>(R.id.btnAtrasSeleccionAlumnos).setupClickAnimation { finish() }

        rvCarreras = findViewById(R.id.rvCarrerasDinamicas)
        rvCarreras.layoutManager = GridLayoutManager(this, 2)
        rvCarreras.setHasFixedSize(true)

        findViewById<View>(R.id.btnVerTodosAlumnos).setupClickAnimation {
            val intent = Intent(this, ListaAlumnosActivity::class.java)
            startActivity(intent)
        }

        obtenerCarrerasDinamicas()
    }

    private fun obtenerCarrerasDinamicas() {
        lifecycleScope.launch {
            try {
                val response = SupabaseManager.client.from("Estudiantes").select()
                val listaEstudiantes = response.decodeList<EstudianteModel>()

                // Extraemos nombres únicos de carrera, asegurando mayúsculas y sin espacios
                // para evitar repetidos por culpa de digitación.
                val carrerasUnicas = listaEstudiantes
                    .map { it.carrera?.trim()?.uppercase() ?: "" }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()

                if (carrerasUnicas.isEmpty()) {
                    Toast.makeText(this@SeleccionAlumnosActivity, "No se encontraron carreras.", Toast.LENGTH_LONG).show()
                } else {
                    val adapter = CarreraAdapter(carrerasUnicas) { carreraSeleccionada ->
                        val intent = Intent(this@SeleccionAlumnosActivity, ListaAlumnosActivity::class.java)
                        intent.putExtra("EXTRA_CARRERA", carreraSeleccionada)
                        startActivity(intent)
                    }
                    rvCarreras.adapter = adapter
                }

            } catch (e: Exception) {
                Log.e("SeleccionAlumnos", "Error: ${e.message}")
                Toast.makeText(this@SeleccionAlumnosActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
}