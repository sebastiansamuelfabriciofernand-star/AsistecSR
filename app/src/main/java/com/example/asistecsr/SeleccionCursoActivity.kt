package com.example.asistecsr

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SeleccionCursoActivity : AppCompatActivity() {

    private lateinit var recyclerCursos: RecyclerView
    private lateinit var adapter: CursoAdapter

    private val listaCursos = mutableListOf<CursoModel>()

    private var idDocente: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_curso)
        
        Log.d("SeleccionCurso", "Iniciando SeleccionCursoActivity con docente: ${intent.getStringExtra("EXTRA_ID_DOCENTE")}")

        recyclerCursos = findViewById(R.id.rvCursos)
        recyclerCursos.layoutManager = LinearLayoutManager(this)

        idDocente = intent.getStringExtra("EXTRA_ID_DOCENTE") ?: ""

        findViewById<ImageView>(R.id.btnAtrasCurso).setupClickAnimation {
            finish()
        }

        cargarCursos()
    }

    private fun cargarCursos() {
        lifecycleScope.launch {
            try {
                Log.d("SeleccionCurso", "Iniciando descarga...")
                
                val response = SupabaseManager.client
                    .from("Cursos")
                    .select()

                val cursos = response.decodeList<CursoModel>()
                
                withContext(Dispatchers.Main) {
                    listaCursos.clear()
                    listaCursos.addAll(cursos)

                    if (listaCursos.isEmpty()) {
                        Toast.makeText(this@SeleccionCursoActivity, "No se encontraron cursos.", Toast.LENGTH_SHORT).show()
                    }

                    adapter = CursoAdapter(listaCursos, idDocente, this@SeleccionCursoActivity)
                    recyclerCursos.adapter = adapter
                    Log.d("SeleccionCurso", "Lista vinculada con éxito")
                }

            } catch (e: Exception) {
                Log.e("SeleccionCurso", "Error detectado: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SeleccionCursoActivity, "Error al cargar cursos. Verifica tu conexión.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}