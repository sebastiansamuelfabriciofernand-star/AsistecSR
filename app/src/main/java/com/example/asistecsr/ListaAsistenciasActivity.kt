package com.example.asistecsr

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ListaAsistenciasActivity : AppCompatActivity() {

    private lateinit var rvListaAsistencias: RecyclerView
    private lateinit var txtFechaSeleccionada: TextView

    private var cicloFiltro: Int = -1
    private var turnoFiltro: String = ""
    private var fechaSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_asistencias)

        // 1. Recibir parámetros enviados desde la pantalla del Profesor
        cicloFiltro = intent.getIntExtra("EXTRA_CICLO", -1)
        turnoFiltro = (intent.getStringExtra("EXTRA_TURNO") ?: "").uppercase(Locale.getDefault()).trim()

        // 2. Inicializar vistas vinculadas exactamente a tu XML
        rvListaAsistencias = findViewById(R.id.rvListaAsistencias)
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada)

        // CORRECCIÓN: Contexto explícito de la Activity para el LayoutManager
        rvListaAsistencias.layoutManager = LinearLayoutManager(this@ListaAsistenciasActivity)

        findViewById<View>(R.id.btnAtrasAsistencias).setOnClickListener {
            finish()
        }

        // 3. Configurar fecha actual por defecto (Formato YYYY-MM-DD)
        val calendario = Calendar.getInstance()
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaSeleccionada = formatoFecha.format(calendario.time)

        txtFechaSeleccionada.text = getString(R.string.formato_fecha_mostrar, fechaSeleccionada)

        // 4. Selector de fecha integrado al hacer clic sobre el TextView
        txtFechaSeleccionada.setOnClickListener {
            DatePickerDialog(
                this@ListaAsistenciasActivity,
                { _, yearSelected, monthSelected, daySelected ->
                    calendario.set(Calendar.YEAR, yearSelected)
                    calendario.set(Calendar.MONTH, monthSelected)
                    calendario.set(Calendar.DAY_OF_MONTH, daySelected)

                    fechaSeleccionada = formatoFecha.format(calendario.time)
                    txtFechaSeleccionada.text = getString(R.string.formato_fecha_mostrar, fechaSeleccionada)

                    cargarHistorialAsistencias()
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Carga inicial automática de la lista
        cargarHistorialAsistencias()
    }

    private fun cargarHistorialAsistencias() {
        if (cicloFiltro == -1 || turnoFiltro.isEmpty()) {
            Toast.makeText(this@ListaAsistenciasActivity, getString(R.string.error_filtros_invalidos), Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val idDocenteLogueado = SupabaseManager.client.auth.currentUserOrNull()?.id

                if (idDocenteLogueado == null) {
                    Toast.makeText(this@ListaAsistenciasActivity, getString(R.string.error_sesion_docente), Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Consulta limpia apuntando a la tabla "Asistencia" en Supabase
                val listaAsistencias = SupabaseManager.client.from("Asistencia").select {
                    filter {
                        eq("ciclo", cicloFiltro)
                        eq("turno", turnoFiltro)
                        eq("fecha", fechaSeleccionada)
                        eq("id_docente", idDocenteLogueado)
                    }
                }.decodeList<AsistenciaModel>()

                // Vinculación con tu AsistenciaAdapter existente
                rvListaAsistencias.adapter = AsistenciaAdapter(listaAsistencias)

                if (listaAsistencias.isEmpty()) {
                    Toast.makeText(this@ListaAsistenciasActivity, getString(R.string.sin_asistencias_hoy), Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                val mensajeError = getString(R.string.error_traer_asistencias, e.localizedMessage ?: "")
                Toast.makeText(this@ListaAsistenciasActivity, mensajeError, Toast.LENGTH_LONG).show()
            }
        }
    }
}