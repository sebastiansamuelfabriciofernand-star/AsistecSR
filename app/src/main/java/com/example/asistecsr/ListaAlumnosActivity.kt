package com.example.asistecsr

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class ListaAlumnosActivity : AppCompatActivity() {

    private lateinit var recyclerAlumnos: RecyclerView

    // Vistas de contadores
    private lateinit var txtTotal: TextView
    private lateinit var txtActivos: TextView
    private lateinit var txtInactivos: TextView

    // Filtros activos actuales para la consulta
    private var cicloFiltro: Int? = null
    private var turnoFiltro: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos)

        // 1. CAPTURAR LOS DATOS DE LA VENTANA ANTERIOR
        val cicloInicial = intent.getIntExtra("EXTRA_CICLO", -1)
        val turnoInicial = intent.getStringExtra("EXTRA_TURNO")

        // Asignamos a nuestros filtros iniciales globales
        cicloFiltro = if (cicloInicial != -1) cicloInicial else null
        turnoFiltro = if (!turnoInicial.isNullOrEmpty() && turnoInicial != "TODOS") turnoInicial else null

        // Inicialización de vistas
        recyclerAlumnos = findViewById(R.id.recyclerAlumnos)
        txtTotal = findViewById(R.id.txtTotalAlumnosContador)
        txtActivos = findViewById(R.id.txtAlumnosActivosContador)
        txtInactivos = findViewById(R.id.txtAlumnosInactivosContador)

        val spinnerCarrera = findViewById<Spinner>(R.id.spinnerCarrera)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCiclo)
        findViewById<ImageView>(R.id.btnAtrasListaAlumnos).setOnClickListener { finish() }

        recyclerAlumnos.layoutManager = LinearLayoutManager(this)

        val carreras = listOf("Todas", "MEDICINA TECNICA", "MECATRONICA AUTOMOTRIZ", "DESARROLLO DE SISTEMAS", "CONTABILIDAD", "ELECTRICIDAD INDUSTRIAL", "QUIMICA")
        val ciclos = listOf("Ciclo", "1", "2", "3", "4", "5", "6")

        spinnerCarrera.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, carreras)
        spinnerCiclo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ciclos)

        // 2. CONFIGURAR PRE-SELECCIÓN AUTOMÁTICA DESDE CARPETAS
        if (cicloInicial in 1..6) {
            spinnerCiclo.setSelection(cicloInicial)
        }

        // 3. LISTENER ÚNICO OPTIMIZADO PARA AMBOS SPINNERS
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val carreraSeleccionada = if (spinnerCarrera.selectedItem.toString() == "Todas") null else spinnerCarrera.selectedItem.toString()

                // Leemos directamente lo que marque el Spinner en tiempo real
                cicloFiltro = spinnerCiclo.selectedItem.toString().toIntOrNull()

                // Ejecutamos la consulta pasándole la combinación exacta de filtros seleccionados
                cargarAlumnos(carreraSeleccionada, cicloFiltro, turnoFiltro)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerCarrera.onItemSelectedListener = listener
        spinnerCiclo.onItemSelectedListener = listener
    }

    /**
     * Consulta la tabla "Estudiantes" en Supabase aplicando filtros dinámicos no nulos
     */
    private fun cargarAlumnos(carrera: String?, ciclo: Int?, turno: String?) {
        lifecycleScope.launch {
            try {
                val lista = SupabaseManager.client.postgrest["Estudiantes"].select {
                    filter {
                        // Solo aplica el filtro a Supabase si el parámetro tiene un valor real
                        if (!carrera.isNullOrEmpty()) eq("carrera", carrera)
                        if (ciclo != null) eq("ciclo", ciclo)
                        if (!turno.isNullOrEmpty()) eq("turno", turno)
                    }
                    order("Apellidos", Order.ASCENDING)
                }.decodeList<UserProfile>()

                // 1. Calcular contadores basándonos en la propiedad "estado" de UserProfile.kt
                val total = lista.size
                val activos = lista.count { it.estado }
                val inactivos = total - activos

                // 2. Actualizar las vistas de la interfaz con formato limpio
                txtTotal.text = "TOTAL DE ALUMNOS\n$total"
                txtActivos.text = "ACTIVOS\n$activos"
                txtInactivos.text = "INACTIVOS\n$inactivos"

                // 3. Inyectar la lista filtrada al RecyclerView
                recyclerAlumnos.adapter = AlumnoAdapter(lista)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ListaAlumnosActivity, "Error al conectar base de datos: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}