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

        // Botón Atrás
        findViewById<ImageView>(R.id.btnAtrasSelector).setupClickAnimation {
            finish()
        }

        // Botón Actualizar
        findViewById<AppCompatButton>(R.id.btnActualizarCarpeta).setupClickAnimation {
            // Forzar recarga si se desea
        }

        // Conexión de clics mapeados explícitamente para evitar pérdida de contexto
        mapearCarrera(R.id.cardMedicina, "MEDICINA TECNICA")
        mapearCarrera(R.id.cardMecatronica, "MECATRONICA AUTOMOTRIZ")
        mapearCarrera(R.id.cardSistemas, "DESARROLLO DE SISTEMAS E INFORMACION")
        mapearCarrera(R.id.cardContabilidad, "CONTABILIDAD")
        mapearCarrera(R.id.cardElectricidad, "ELECTRICIDAD INDUSTRIAL")
        mapearCarrera(R.id.cardQuimica, "QUIMICA")

        // CORRECCIÓN TYPO: Se cambió IMPLEMENTARIOS por COMPLEMENTARIOS si aplica, o se mantiene corregido
        mapearCarrera(R.id.cardCursos, "CURSOS COMPLEMENTARIOS")
    }

    /**
     * Helper para mapear las carreras utilizando de forma segura el contexto de 'this@SelectorCarrerasActivity'
     */
    private fun mapearCarrera(idCard: Int, nombreCarrera: String) {
        findViewById<LinearLayout>(idCard)?.setupClickAnimation {
            val intent = Intent(this@SelectorCarrerasActivity, ListaDocentesActivity::class.java)
            intent.putExtra("CARRERA_SELECCIONADA", nombreCarrera)
            startActivity(intent)
        }
    }
}