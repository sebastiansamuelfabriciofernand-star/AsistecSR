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
        // Vinculamos de manera correcta al diseño de ciclos y turnos
        setContentView(R.layout.activity_seleccion_asistencia)

        // Botón Atrás
        findViewById<View>(R.id.btnAtrasSeleccion).setOnClickListener { finish() }

        // Configuración de los Spinners de Filtros superiores
        val spinnerTurno = findViewById<Spinner>(R.id.spinnerTurnoAsistencia)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCicloAsistencia)

        // Opciones quemadas o traídas de strings.xml si los tienes creados
        val turnos = arrayOf("DIURNO", "NOCTURNO")
        val ciclos = arrayOf("1", "2", "3", "4", "5", "6")

        spinnerTurno.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, turnos)
        spinnerCiclo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ciclos)

        // Botón superior: LISTA DE ALUMNOS
        findViewById<View>(R.id.btnIrListaAlumnos).setOnClickListener {
            val intent = Intent(this, ListaAlumnosActivity::class.java)
            startActivity(intent)
        }

        // Mapear y configurar las tarjetas del Grid de Ciclos
        configurarTarjetasGrid()

        // Botón inferior: ACTUALIZAR CARPETA
        findViewById<View>(R.id.btnActualizarCarpeta).setOnClickListener {
            Toast.makeText(this, "Sincronizando registros con Supabase...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarTarjetasGrid() {
        // Lista ordenada de IDs y sus respectivos (Ciclo, Turno)
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
                // Al presionar la tarjeta, abrimos el historial pasándole el ciclo y turno clicleado
                val intent = Intent(this, ListaAsistenciasActivity::class.java).apply {
                    putExtra("EXTRA_CICLO", datos.first)
                    putExtra("EXTRA_TURNO", datos.second)
                }
                startActivity(intent)
            }
        }
    }
}