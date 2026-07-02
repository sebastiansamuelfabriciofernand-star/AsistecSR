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
import io.github.jan.supabase.postgrest.from // 👈 IMPORTACIÓN CORRECTA PARA EL BOM ACTUAL
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import java.util.Locale

class ListaAlumnosActivity : AppCompatActivity() {

    private lateinit var recyclerAlumnos: RecyclerView
    private lateinit var txtTotal: TextView
    private lateinit var txtActivos: TextView
    private lateinit var txtInactivos: TextView
    private lateinit var edtBuscarAlumno: EditText

    private var listaCompletaAlumnos: List<EstudianteModel> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos)

        recyclerAlumnos = findViewById(R.id.recyclerAlumnos)
        txtTotal = findViewById(R.id.txtTotalAlumnosContador)
        txtActivos = findViewById(R.id.txtAlumnosActivosContador)
        txtInactivos = findViewById(R.id.txtAlumnosInactivosContador)
        edtBuscarAlumno = findViewById(R.id.edtBuscarAlumno)

        val spinnerCarrera = findViewById<Spinner>(R.id.spinnerCarrera)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCiclo)
        findViewById<ImageView>(R.id.btnAtrasListaAlumnos).setOnClickListener { finish() }

        recyclerAlumnos.layoutManager = LinearLayoutManager(this)

        val carreras = listOf("Todas", "MEDICINA TECNICA", "MECATRONICA AUTOMOTRIZ", "DESARROLLO DE SISTEMAS", "CONTABILIDAD", "ELECTRICIDAD INDUSTRIAL", "QUIMICA")
        val ciclos = listOf("Todos", "1", "2", "3", "4", "5", "6")

        spinnerCarrera.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, carreras)
        spinnerCiclo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ciclos)

        val listenerFiltros = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                aplicarFiltrosLocales()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerCarrera.onItemSelectedListener = listenerFiltros
        spinnerCiclo.onItemSelectedListener = listenerFiltros

        edtBuscarAlumno.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltrosLocales()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarTodosLosAlumnosDeBaseDeDatos()
    }

    private fun cargarTodosLosAlumnosDeBaseDeDatos() {
        lifecycleScope.launch {
            try {
                // CORRECCIÓN SINTAXIS .from()
                val lista = SupabaseManager.client.from("Estudiantes").select {
                    order("Apellidos", Order.ASCENDING)
                }.decodeList<EstudianteModel>()

                listaCompletaAlumnos = lista
                actualizarContadoresYLista(listaCompletaAlumnos)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ListaAlumnosActivity, "Error BD: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun aplicarFiltrosLocales() {
        val spinnerCarrera = findViewById<Spinner>(R.id.spinnerCarrera)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCiclo)

        val carreraSeleccionada = spinnerCarrera.selectedItem.toString()
        val cicloSeleccionado = spinnerCiclo.selectedItem.toString()
        val textoBusqueda = edtBuscarAlumno.text.toString().lowercase(Locale.getDefault()).trim()

        var listaFiltrada = listaCompletaAlumnos

        if (carreraSeleccionada != "Todas") {
            listaFiltrada = listaFiltrada.filter { it.carrera.equals(carreraSeleccionada, ignoreCase = true) }
        }

        if (cicloSeleccionado != "Todos") {
            val cicloInt = cicloSeleccionado.toIntOrNull()
            if (cicloInt != null) {
                listaFiltrada = listaFiltrada.filter { it.ciclo == cicloInt }
            }
        }

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

        recyclerAlumnos.adapter = AlumnoAdapter(lista)
    }
}