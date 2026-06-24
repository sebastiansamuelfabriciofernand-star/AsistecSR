package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class SeleccionAsistenciaActivity : AppCompatActivity() {

    // Cambiamos el Triple para almacenar el LinearLayout de la tarjeta y el nombre de la carrera
    private lateinit var listaTarjetasCarreras: List<Pair<LinearLayout, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Vinculamos con tu archivo real de carreras
        setContentView(R.layout.activity_selector_carreras)

        // Botón Atrás (ID corregido según tu XML: btnAtrasSelector)
        findViewById<View>(R.id.btnAtrasSelector).setOnClickListener {
            finish()
        }

        // Mapeamos los LinearLayout de tus tarjetas reales definidos en activity_selector_carreras.xml
        listaTarjetasCarreras = listOf(
            Pair(findViewById(R.id.cardMedicina), "MEDICINA TECNICA"),
            Pair(findViewById(R.id.cardMecatronica), "MECATRONICA AUTOMOTRIZ"),
            Pair(findViewById(R.id.cardSistemas), "DESARROLLO DE SISTEMAS E INFORMACION"),
            Pair(findViewById(R.id.cardContabilidad), "CONTABILIDAD"),
            Pair(findViewById(R.id.cardElectricidad), "ELECTRICIDAD INDUSTRIAL"),
            Pair(findViewById(R.id.cardQuimica), "QUIMICA"),
            Pair(findViewById(R.id.cardCursos), "CURSOS IMPLEMENTARIOS")
        )

        // Al hacer clic en una carrera, abrimos la actividad que muestra la lista de docentes
        listaTarjetasCarreras.forEach { (card, nombreCarrera) ->
            card.setOnClickListener {
                // Pasamos la carrera seleccionada para que la lista de docentes sepa qué filtrar de Supabase
                val intent = Intent(this, ListaDocentesActivity::class.java).apply {
                    putExtra("EXTRA_CARRERA", nombreCarrera)
                }
                startActivity(intent)
            }
        }

        // Configuración de los Spinners con los IDs reales de tu XML
        val spinnerTurno = findViewById<Spinner>(R.id.spinnerTurno)
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCiclo)
        val etBuscarArea = findViewById<EditText>(R.id.etBuscarArea)

        val adapterTurno = ArrayAdapter.createFromResource(this, R.array.opciones_turno, android.R.layout.simple_spinner_item)
        adapterTurno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTurno.adapter = adapterTurno

        val adapterCiclo = ArrayAdapter.createFromResource(this, R.array.opciones_ciclo, android.R.layout.simple_spinner_item)
        adapterCiclo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCiclo.adapter = adapterCiclo

        // Listener para filtrar las tarjetas por Turno / Ciclo / Texto de búsqueda
        val filtroListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val turnoSel = spinnerTurno.selectedItem.toString()
                val cicloSel = spinnerCiclo.selectedItem.toString()
                aplicarFiltros(turnoSel, cicloSel, etBuscarArea.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerTurno.onItemSelectedListener = filtroListener
        spinnerCiclo.onItemSelectedListener = filtroListener

        // Botón superior azul: "LISTA DE DOCENTE"
        findViewById<AppCompatButton>(R.id.btnListaDocenteTop).setOnClickListener {
            val intent = Intent(this, ListaDocentesActivity::class.java)
            startActivity(intent)
        }

        // Botón rojo inferior: "ACTUALIZAR CARPETA"
        findViewById<AppCompatButton>(R.id.btnActualizarCarpeta).setOnClickListener {
            // Aquí puedes colocar la lógica para refrescar datos desde Supabase
        }
    }

    private fun aplicarFiltros(turnoSel: String, cicloSel: String, textoBusqueda: String) {
        listaTarjetasCarreras.forEach { (card, nombreCarrera) ->
            // Filtro por texto en el EditText
            val coincideTexto = textoBusqueda.isEmpty() || nombreCarrera.contains(textoBusqueda.uppercase(), ignoreCase = true)

            // Aquí puedes vincular lógicas de turno/ciclo adicionales si tu base de datos segmenta las carreras por ellos.
            // Por ahora, si coincide con el texto de búsqueda, se muestra
            if (coincideTexto) {
                card.visibility = View.VISIBLE
            } else {
                card.visibility = View.GONE
            }
        }
    }
}