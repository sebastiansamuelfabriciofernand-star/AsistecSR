package com.example.asistecsr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ListaDocentesActivity : AppCompatActivity() {
    private lateinit var adapter: DocenteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Vincula el diseño que pusimos arriba
        setContentView(R.layout.activity_lista_docentes)

        // 2. Busca el RecyclerView usando el ID exacto del XML
        val recyclerView = findViewById<RecyclerView>(R.id.rvDocentes)

        adapter = DocenteAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 3. Recibe la carrera que mandó el SelectorCarrerasActivity
        val carrera = intent.getStringExtra("CARRERA_SELECCIONADA") ?: ""

        if (carrera.isNotEmpty()) {
            cargarDocentes(carrera)
        }
    }

    private fun cargarDocentes(carrera: String) {
        lifecycleScope.launch {
            try {
                // Filtra en Supabase por la columna 'carrera'
                val lista = SupabaseManager.client.postgrest["Docentes"]
                    .select { filter { eq("carrera", carrera) } }
                    .decodeList<DocenteProfile>()

                adapter.update(lista)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}