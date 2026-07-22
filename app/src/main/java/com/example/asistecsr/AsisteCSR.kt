package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AsisteCSR : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asiste_c_s_r)

        val txtPorcentaje = findViewById<TextView>(R.id.txtPorcentaje)
        val barra1 = findViewById<View>(R.id.barra1)
        val barra2 = findViewById<View>(R.id.barra2)
        val barra3 = findViewById<View>(R.id.barra3)

        // Iniciamos con las barras totalmente vacías
        barra1.foreground?.level = 0
        barra2.foreground?.level = 0
        barra3.foreground?.level = 0

        lifecycleScope.launch {
            for (progreso in 1..100) {
                // Actualizamos el porcentaje usando el recurso de strings
                txtPorcentaje.text = getString(R.string.formato_porcentaje, progreso)

                when {
                    progreso <= 33 -> {
                        val nivel = (progreso * 10000) / 33
                        barra1.foreground?.level = nivel
                    }
                    progreso in 34..66 -> {
                        barra1.foreground?.level = 10000
                        val nivel = ((progreso - 33) * 10000) / 33
                        barra2.foreground?.level = nivel
                    }
                    else -> {
                        barra1.foreground?.level = 10000
                        barra2.foreground?.level = 10000
                        val nivel = ((progreso - 66) * 10000) / 34
                        barra3.foreground?.level = nivel
                    }
                }
                // Velocidad del contador (mantenida de tu lógica original)
                delay(40)
            }

            // Aseguramos el llenado total al finalizar
            barra1.foreground?.level = 10000
            barra2.foreground?.level = 10000
            barra3.foreground?.level = 10000

            delay(300)

            // Pasamos al Menú Principal
            val intent = Intent(this@AsisteCSR, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
