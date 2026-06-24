package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class SelectorCarrerasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector_carreras)

        findViewById<ImageView>(R.id.btnAtrasSelector).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.btnActualizarCarpeta).setOnClickListener {
            // Forzar recarga si se desea
        }

        // Conexión de clics para tus 7 áreas reales de la imagen
        configurarClic(R.id.cardMedicina, "MEDICINA TECNICA")
        configurarClic(R.id.cardMecatronica, "MECATRONICA AUTOMOTRIZ")
        configurarClic(R.id.cardSistemas, "DESARROLLO DE SISTEMAS E INFORMACION")
        configurarClic(R.id.cardContabilidad, "CONTABILIDAD")
        configurarClic(R.id.cardElectricidad, "ELECTRICIDAD INDUSTRIAL")
        configurarClic(R.id.cardQuimica, "QUIMICA")
        configurarClic(R.id.cardCursos, "CURSOS IMPLEMENTARIOS")
    }

    private fun configurarClic(id: Int, nombreCarrera: String) {
        findViewById<LinearLayout>(id).setOnClickListener {
            val intent = Intent(this, ListaDocentesActivity::class.java)
            intent.putExtra("CARRERA_SELECCIONADA", nombreCarrera)
            startActivity(intent)
        }
    }
}