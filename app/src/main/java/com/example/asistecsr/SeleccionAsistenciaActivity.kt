package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SeleccionAsistenciaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_asistencia)

        // Botón Atrás
        findViewById<View>(R.id.btnAtrasSeleccion).setOnClickListener { finish() }

        // Configuración de los Spinners de Filtros superiores
        val spinnerTurno = findViewById<Spinner>(R.id.spinnerTurnoAsistencia)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCicloAsistencia)

        val turnos = arrayOf("DIURNO", "NOCTURNO")
        val ciclos = arrayOf("1", "2", "3", "4", "5", "6")

        // CORRECCIÓN: Contexto explícito para evitar fallos de inicialización
        spinnerTurno.adapter = ArrayAdapter(this@SeleccionAsistenciaActivity, android.R.layout.simple_spinner_dropdown_item, turnos)
        spinnerCiclo.adapter = ArrayAdapter(this@SeleccionAsistenciaActivity, android.R.layout.simple_spinner_dropdown_item, ciclos)

        // Botón superior: LISTA DE ALUMNOS
        findViewById<View>(R.id.btnIrListaAlumnos).setOnClickListener {
            val intent = Intent(this@SeleccionAsistenciaActivity, ListaAlumnosActivity::class.java)
            startActivity(intent)
        }

        // Mapear y configurar las tarjetas del Grid de Ciclos
        mapearTarjetasGrid()

        // Botón inferior: ACTUALIZAR CARPETA
        findViewById<View>(R.id.btnActualizarCarpeta).setOnClickListener {
            Toast.makeText(this@SeleccionAsistenciaActivity, "Sincronizando registros con la base de datos...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mapea de forma segura las tarjetas del Grid y pasa los datos correctos sin perder el contexto.
     */
    private fun mapearTarjetasGrid() {
        val mapeoTarjetas = listOf(
            Pair(R.id.btn1Diurno, Pair(1, "DIURNO")),
            Pair(R.id.btn1Nocturno, Pair(1, "NOCTURNO")),
            Pair(R.id.btn2Diurno, Pair(2, "DIURNO")),
            Pair(R.id.btn2Nocturno, Pair(2, "NOCTURNO")),
            Pair(R.id.btn3Diurno, Pair(3, "DIURNO")),
            Pair(R.id.btn3Nocturno, Pair(3, "NOCTURNO")),
            Pair(R.id.btn4Diurno, Pair(4, "DIURNO")),
            Pair(R.id.btn4Nocturno, Pair(4, "NOCTURNO")),
            Pair(R.id.btn5Diurno, Pair(5, "DIURNO")),
            Pair(R.id.btn5Nocturno, Pair(5, "NOCTURNO")),
            Pair(R.id.btn6Diurno, Pair(6, "DIURNO")),
            Pair(R.id.btn6Nocturno, Pair(6, "NOCTURNO"))
        )

        mapeoTarjetas.forEach { (idCard, datos) ->
            findViewById<CardView>(idCard)?.setOnClickListener {
                try {
                    // CORRECCIÓN: Contexto explícito y uso seguro de la clase de destino
                    val intent = Intent(this@SeleccionAsistenciaActivity, Class.forName("com.example.asistecsr.ListaAsistenciasActivity"))
                    intent.putExtra("EXTRA_CICLO", datos.first)
                    intent.putExtra("EXTRA_TURNO", datos.second)
                    startActivity(intent)
                } catch (e: ClassNotFoundException) {
                    Toast.makeText(this@SeleccionAsistenciaActivity, "Pantalla de asistencias en desarrollo.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}