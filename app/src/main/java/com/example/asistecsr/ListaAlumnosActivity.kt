package com.example.asistecsr

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from // 👈 Sintaxis moderna de Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import java.util.Locale

class ListaAlumnosActivity : AppCompatActivity() {

    private lateinit var recyclerAlumnos: RecyclerView
    private lateinit var txtTotal: TextView
    private lateinit var txtActivos: TextView
    private lateinit var txtInactivos: TextView
    private lateinit var edtBuscarAlumno: EditText

    // Lista en memoria para almacenar absolutamente todos los alumnos descargados
    private var listaCompletaAlumnos: List<EstudianteModel> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos)

        // Inicializar componentes de la interfaz
        recyclerAlumnos = findViewById(R.id.recyclerAlumnos)
        txtTotal = findViewById(R.id.txtTotalAlumnosContador)
        txtActivos = findViewById(R.id.txtAlumnosActivosContador)
        txtInactivos = findViewById(R.id.txtAlumnosInactivosContador)
        edtBuscarAlumno = findViewById(R.id.edtBuscarAlumno)

        val spinnerCarrera = findViewById<Spinner>(R.id.spinnerCarrera)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCiclo)
        findViewById<ImageView>(R.id.btnAtrasListaAlumnos).setOnClickListener { finish() }

        recyclerAlumnos.layoutManager = LinearLayoutManager(this)

        // Configurar las opciones de los Spinners
        val carreras = listOf("Todas", "MEDICINA TECNICA", "MECATRONICA AUTOMOTRIZ", "DESARROLLO DE SISTEMAS", "CONTABILIDAD", "ELECTRICIDAD INDUSTRIAL", "QUIMICA")
        val ciclos = listOf("Todos", "1", "2", "3", "4", "5", "6")

        spinnerCarrera.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, carreras)
        spinnerCiclo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ciclos)

        // Listener dinámico para cuando uses los filtros superiores voluntariamente
        val listenerFiltros = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                aplicarFiltrosLocales()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerCarrera.onItemSelectedListener = listenerFiltros
        spinnerCiclo.onItemSelectedListener = listenerFiltros

        // Buscador por caja de texto (Nombre o Apellido)
        edtBuscarAlumno.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltrosLocales()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // LLAMADA INICIAL: Carga de golpe TODOS los alumnos sin filtros trabados
        cargarTodosLosAlumnosDeBaseDeDatos()
    }

    // Descarga la base de datos entera de alumnos una sola vez para máxima velocidad
    private fun cargarTodosLosAlumnosDeBaseDeDatos() {
        lifecycleScope.launch {
            try {
                // CORRECCIÓN: Consulta limpia usando .from()
                val lista = SupabaseManager.client.from("Estudiantes").select {
                    order("Apellidos", Order.ASCENDING)
                }.decodeList<EstudianteModel>()

                listaCompletaAlumnos = lista
                // Muestra la totalidad de los datos de inmediato
                actualizarContadoresYLista(listaCompletaAlumnos)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ListaAlumnosActivity, "Error al conectar base de datos: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Filtra localmente en tiempo real sobre la lista completa guardada sin saturar la red
    private fun aplicarFiltrosLocales() {
        val spinnerCarrera = findViewById<Spinner>(R.id.spinnerCarrera)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCiclo)

        val carreraSeleccionada = spinnerCarrera.selectedItem.toString()
        val cicloSeleccionado = spinnerCiclo.selectedItem.toString()
        val textoBusqueda = edtBuscarAlumno.text.toString().lowercase(Locale.getDefault()).trim()

        var listaFiltrada = listaCompletaAlumnos

        // 1. Filtrar por Carrera si no es "Todas"
        if (carreraSeleccionada != "Todas") {
            listaFiltrada = listaFiltrada.filter { it.carrera.equals(carreraSeleccionada, ignoreCase = true) }
        }

        // 2. CORRECCIÓN: Se cambió "cycleSeleccionado" por "cicloSeleccionado"
        if (cicloSeleccionado != "Todos") {
            val cicloInt = cicloSeleccionado.toIntOrNull()
            if (cicloInt != null) {
                listaFiltrada = listaFiltrada.filter { it.ciclo == cicloInt }
            }
        }

        // 3. Filtrar por cuadro de búsqueda de texto
        if (textoBusqueda.isNotEmpty()) {
            listaFiltrada = listaFiltrada.filter { alumno ->
                alumno.nombres.lowercase(Locale.getDefault()).contains(textoBusqueda) ||
                        alumno.apellidos.lowercase(Locale.getDefault()).contains(textoBusqueda)
            }
        }

        actualizarContadoresYLista(listaFiltrada)
    }

    private fun actualizarContadoresYLista(lista: List<EstudianteModel>) {
        val total = lista.size
        val activos = lista.count { it.estado }
        val inactivos = total - activos

        txtTotal.text = getString(R.string.formato_total_alumnos, total)
        txtActivos.text = getString(R.string.formato_activos, activos)
        txtInactivos.text = getString(R.string.formato_inactivos, inactivos)

        // Refrescar el RecyclerView
        recyclerAlumnos.adapter = AlumnoAdapter(lista)
    }
}