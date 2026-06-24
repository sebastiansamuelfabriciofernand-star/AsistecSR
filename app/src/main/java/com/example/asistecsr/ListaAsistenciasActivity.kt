package com.example.asistecsr

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class ListaAsistenciasActivity : AppCompatActivity() {

    private lateinit var txtFechaSeleccionada: TextView
    private lateinit var rvListaAsistencias: RecyclerView
    private lateinit var adapter: AsistenciaAdapter

    private var filtroCiclo: Int = -1
    private var filtroTurno: String? = null
    private var fechaActualNodo: String = ""

    // 🛠️ Guardamos el estado de la fecha seleccionada para el DatePicker
    private var anioSeleccionado: Int = 0
    private var mesSeleccionado: Int = 0
    private var diaSeleccionado: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_asistencias)

        filtroCiclo = intent.getIntExtra("EXTRA_CICLO", -1)
        filtroTurno = intent.getStringExtra("EXTRA_TURNO")

        val btnAtras = findViewById<ImageView>(R.id.btnAtrasAsistencias)
        txtFechaSeleccionada = findViewById<TextView>(R.id.txtFechaSeleccionada)
        rvListaAsistencias = findViewById<RecyclerView>(R.id.rvListaAsistencias)

        rvListaAsistencias.layoutManager = LinearLayoutManager(this)

        adapter = AsistenciaAdapter(arrayListOf())
        rvListaAsistencias.adapter = adapter

        btnAtras.setOnClickListener { finish() }

        // Inicializamos las variables con la fecha de hoy por defecto
        val calendario = Calendar.getInstance()
        anioSeleccionado = calendario.get(Calendar.YEAR)
        mesSeleccionado = calendario.get(Calendar.MONTH)
        diaSeleccionado = calendario.get(Calendar.DAY_OF_MONTH)

        // Carga inicial (Hoy)
        actualizarFechaYConsultar(diaSeleccionado, mesSeleccionado + 1, anioSeleccionado)

        // Escuchador de clics en la barra de fecha
        txtFechaSeleccionada.setOnClickListener {
            // 🛠️ Ahora abre el calendario mostrando la fecha que se está consultando actualmente
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // Guardamos el nuevo estado seleccionado
                anioSeleccionado = year
                mesSeleccionado = month // Nota: Se guarda el mes base 0 nativo
                diaSeleccionado = dayOfMonth

                actualizarFechaYConsultar(dayOfMonth, month + 1, year)
            }, anioSeleccionado, mesSeleccionado, diaSeleccionado).show()
        }
    }

    private fun actualizarFechaYConsultar(dia: Int, mes: Int, anio: Int) {
        val fechaFormateada = String.format("%02d/%02d/%d", dia, mes, anio)
        txtFechaSeleccionada.text = "📅 Día: $fechaFormateada\n($filtroTurno - ${filtroCiclo}° CICLO)"
        fechaActualNodo = String.format("%d-%02d-%02d", anio, mes, dia)

        cargarAlumnosPorFiltro()
    }

    private fun cargarAlumnosPorFiltro() {
        lifecycleScope.launch {
            try {
                // 1. Traer todos los estudiantes que pertenecen a esta carpeta/salón
                val resultadoEstudiantes = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["Estudiantes"].select {
                        filter {
                            if (filtroCiclo != -1) eq("ciclo", filtroCiclo)
                            if (filtroTurno != null) eq("turno", filtroTurno!!)
                        }
                    }.decodeList<JsonObject>()
                }

                // 2. Traer los códigos QR que YA fueron escaneados de la tabla "Asistencia" en el día seleccionado
                val asistenciasDeHoy = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest["Asistencia"].select {
                        filter {
                            eq("fecha", fechaActualNodo)
                        }
                    }.decodeList<JsonObject>()
                }

                // Creamos un Set con los códigos QR escaneados para buscar rápido
                val codigosEscaneadosSet = asistenciasDeHoy.mapNotNull {
                    it["codigoQr"]?.jsonPrimitive?.content
                }.toSet()

                // 3. Mapeamos cruzando la información
                val listaAlumnos = resultadoEstudiantes.mapIndexed { index, json ->
                    val nom = json["Nombres"]?.jsonPrimitive?.content ?: "Sin Nombre"
                    val ape = json["Apellidos"]?.jsonPrimitive?.content ?: ""
                    val nombreCompleto = "$nom $ape".trim()
                    val qrEstudiante = json["codigoQr"]?.jsonPrimitive?.content ?: "S/ID"

                    val yaSeEscaneo = codigosEscaneadosSet.contains(qrEstudiante)

                    AlumnoAsistencia(
                        numeroOrden = index + 1,
                        nombre = nombreCompleto,
                        idAlumno = qrEstudiante,
                        materia = json["carrera"]?.jsonPrimitive?.content ?: "Sistemas",
                        asistio = yaSeEscaneo,
                        fecha = fechaActualNodo,
                        ciclo = filtroCiclo,
                        turno = filtroTurno ?: ""
                    )
                }

                adapter.actualizarLista(listaAlumnos)

                if (listaAlumnos.isEmpty()) {
                    Toast.makeText(this@ListaAsistenciasActivity, "No se encontraron alumnos con estos filtros", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ListaAsistenciasActivity, "Error de carga: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}