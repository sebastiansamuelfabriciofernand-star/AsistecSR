package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val txtRolSeleccionado = findViewById<TextView>(R.id.txtRolSeleccionado)
        val txtOlvidaste = findViewById<TextView>(R.id.txtOlvidaste)
        val btnRegisterEnviar = findViewById<AppCompatButton>(R.id.btnRegisterEnviar)
        val btnLoginEnviar = findViewById<AppCompatButton>(R.id.btnLoginEnviar)
        val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
        val edtContrasena = findViewById<EditText>(R.id.edtContrasena)

        val rolSeleccionadoIntent = intent.getStringExtra("ROL_SELECCIONADO") ?: "ESTUDIANTE"
        txtRolSeleccionado.text = rolSeleccionadoIntent

        // Redirección dinámica al registro correspondiente
        btnRegisterEnviar.setOnClickListener {
            val destino = when (rolSeleccionadoIntent) {
                "ADMINISTRADOR" -> RegisterAdminActivity::class.java
                "PROFESOR" -> RegisterProfesorActivity::class.java
                else -> RegisterEstudianteActivity::class.java
            }
            startActivity(Intent(this, destino))
        }

        // Lógica de Inicio de Sesión
        btnLoginEnviar.setOnClickListener {
            val correo = edtUsuario.text.toString().trim()
            val password = edtContrasena.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLoginEnviar.isEnabled = false

            lifecycleScope.launch {
                try {
                    // 1. Intentar autenticación en Supabase Auth
                    SupabaseManager.client.auth.signInWith(Email) {
                        email = correo
                        this.password = password
                    }

                    val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

                    if (userId != null) {
                        // 2. Determinar tabla y columna según el rol
                        val (tabla, columnaId) = when (rolSeleccionadoIntent) {
                            "ESTUDIANTE" -> "Estudiantes" to "codigoQr"
                            "PROFESOR" -> "Docentes" to "id_docente"
                            "ADMINISTRADOR" -> "Administrador" to "id_Administrador"
                            else -> "Estudiantes" to "codigoQr"
                        }

                        // 3. Consultar a Postgrest de forma segura
                        val result = SupabaseManager.client.postgrest[tabla]
                            .select {
                                filter {
                                    eq(columnaId, userId)
                                }
                            }

                        val list = result.decodeList<JsonObject>()

                        if (list.isNotEmpty()) {
                            val usuarioJson = list[0]

                            // Comprobación de estado si existe el campo
                            if (usuarioJson.containsKey("estado")) {
                                val estaActivo = usuarioJson["estado"]?.jsonPrimitive?.boolean ?: true
                                if (!estaActivo) {
                                    Toast.makeText(this@LoginActivity, "Cuenta INACTIVA. Contacte al administrador.", Toast.LENGTH_LONG).show()
                                    SupabaseManager.client.auth.signOut()
                                    return@launch
                                }
                            }

                            Toast.makeText(this@LoginActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

                            val activityDestino = when (rolSeleccionadoIntent) {
                                "ESTUDIANTE" -> PerfilEstudianteActivity::class.java
                                "PROFESOR" -> PerfilDocenteActivity::class.java
                                "ADMINISTRADOR" -> PerfilAdminActivity::class.java
                                else -> PerfilEstudianteActivity::class.java
                            }

                            startActivity(Intent(this@LoginActivity, activityDestino))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "No tienes registros en la tabla $tabla.", Toast.LENGTH_LONG).show()
                            SupabaseManager.client.auth.signOut()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Error de acceso: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    btnLoginEnviar.isEnabled = true
                }
            }
        }

        txtOlvidaste.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }
    }
}
