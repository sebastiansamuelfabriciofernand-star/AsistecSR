package com.example.asistecsr

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.view.View
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
                txtPorcentaje.text = "$progreso%"

                when {
                    // Tramo 1: De 1% a 33% se va llenando la primera barra progresivamente
                    progreso <= 33 -> {
                        val nivel = (progreso * 10000) / 33
                        barra1.foreground?.level = nivel
                    }
                    // Tramo 2: De 34% a 66% la primera se queda llena y la segunda avanza
                    progreso in 34..66 -> {
                        barra1.foreground?.level = 10000
                        val nivel = ((progreso - 33) * 10000) / 33
                        barra2.foreground?.level = nivel
                    }
                    // Tramo 3: De 67% a 100% las dos primeras llenas y la tercera avanza
                    progreso > 66 -> {
                        barra1.foreground?.level = 10000
                        barra2.foreground?.level = 10000
                        val nivel = ((progreso - 66) * 10000) / 34
                        barra3.foreground?.level = nivel
                    }
                }
                // Velocidad del contador (40 milisegundos por número)
                delay(40)
            }

            // Aseguramos el llenado total al finalizar
            barra1.foreground?.level = 10000
            barra2.foreground?.level = 10000
            barra3.foreground?.level = 10000

            // NUEVO: Esperamos un instante corto y saltamos al menú de perfiles
            delay(300)
            val intent = android.content.Intent(this@AsisteCSR, MenuActivity::class.java)
            startActivity(intent)
            finish() // Cerramos la pantalla de carga para que no regrese al pulsar atrás
        }
    }
}