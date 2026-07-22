package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class PerfilAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_admin)

        val btnAtras = findViewById<ImageView>(R.id.btnAtrasPerfilAdmin)
        val txtNombre = findViewById<TextView>(R.id.txtAdminNombre)
        val txtEmail = findViewById<TextView>(R.id.txtAdminEmail)

        val btnListaAlumnos = findViewById<LinearLayout>(R.id.btnListaAlumnos)
        val btnListaDocentes = findViewById<LinearLayout>(R.id.btnListaDocentes)
        val btnModoMantenimiento = findViewById<AppCompatButton>(R.id.btnModoMantenimiento)

        btnAtras.setupClickAnimation { finish() }

        btnListaDocentes.setupClickAnimation {
            val intent = Intent(this, SelectorCarrerasActivity::class.java)
            startActivity(intent)
        }

        btnListaAlumnos.setupClickAnimation {
            val intent = Intent(this, SeleccionAlumnosActivity::class.java)
            startActivity(intent)
        }

        btnModoMantenimiento.setupClickAnimation {
            Toast.makeText(this, "Modo Mantenimiento Activado", Toast.LENGTH_SHORT).show()
        }

        cargarDatosAdministrador(txtNombre, txtEmail)
    }

    private fun cargarDatosAdministrador(txtNom: TextView, txtCorreo: TextView) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val response = SupabaseManager.client.from("Administrador").select {
                        filter { eq("id_Administrador", userId) }
                    }
                    // CORRECCIÓN: Vinculación directa con el nuevo nombre AdminModel
                    val admin = response.decodeSingle<AdminModel>()

                    // CORRECCIÓN LINSER: Evitamos la concatenación directa en setText
                    val nombreCompleto = "${admin.nombres} ${admin.apellidos}"
                    txtNom.text = nombreCompleto
                    txtCorreo.text = admin.correo
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@PerfilAdminActivity, "Error al cargar datos del Administrador", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}