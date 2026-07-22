package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SeleccionAsistenciaActivity : AppCompatActivity() {

    private lateinit var mapeoTarjetas: List<Pair<Int, Pair<Int, String>>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_asistencia)

        // Botón Atrás
        findViewById<View>(R.id.btnAtrasSeleccion).setupClickAnimation { finish() }

        // Configuración de los Spinners de Filtros superiores
        val spinnerTurno = findViewById<Spinner>(R.id.spinnerTurnoAsistencia)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCicloAsistencia)

        val turnos = arrayOf("TODOS LOS TURNOS", "DIURNO", "NOCTURNO")
        val ciclos = arrayOf("TODOS LOS CICLOS", "1", "2", "3", "4", "5", "6")

        spinnerTurno.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, turnos)
        spinnerCiclo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ciclos)

        // Botón superior: LISTA DE ALUMNOS
        findViewById<View>(R.id.btnIrListaAlumnos).setupClickAnimation {
            startActivity(Intent(this, ListaAlumnosActivity::class.java))
        }

        // Definir el mapeo de tarjetas
        mapeoTarjetas = listOf(
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

        // Configurar clics y filtros
        configurarTarjetas()
        
        val listenerFiltros = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                aplicarFiltros(spinnerTurno.selectedItem.toString(), spinnerCiclo.selectedItem.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerTurno.onItemSelectedListener = listenerFiltros
        spinnerCiclo.onItemSelectedListener = listenerFiltros

        // Botón inferior: ACTUALIZAR CARPETA
        findViewById<View>(R.id.btnActualizarCarpeta).setupClickAnimation {
            Toast.makeText(this, "Sincronizando registros con la base de datos...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarTarjetas() {
        mapeoTarjetas.forEach { (idCard, datos) ->
            findViewById<CardView>(idCard)?.setupClickAnimation {
                val intent = Intent(this, ListaAsistenciasActivity::class.java)
                intent.putExtra("EXTRA_CICLO", datos.first)
                intent.putExtra("EXTRA_TURNO", datos.second)
                startActivity(intent)
            }
        }
    }

    private fun aplicarFiltros(turno: String, ciclo: String) {
        mapeoTarjetas.forEach { (idCard, datos) ->
            val coincideTurno = turno == "TODOS LOS TURNOS" || datos.second == turno
            val coincideCiclo = ciclo == "TODOS LOS CICLOS" || datos.first.toString() == ciclo
            
            findViewById<CardView>(idCard)?.visibility = if (coincideTurno && coincideCiclo) View.VISIBLE else View.GONE
        }
    }
}
