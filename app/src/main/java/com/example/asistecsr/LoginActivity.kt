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
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from // 👈 CORRECCIÓN: Importación moderna correcta
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
            val nombreClase = when (rolSeleccionadoIntent) {
                "ADMINISTRADOR" -> "com.example.asistecsr.RegisterAdminActivity"
                "PROFESOR" -> "com.example.asistecsr.RegisterProfesorActivity"
                else -> "com.example.asistecsr.RegisterEstudianteActivity"
            }
            try {
                val destino = Class.forName(nombreClase)
                startActivity(Intent(this, destino))
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "La pantalla de registro aún no está creada física o correctamente.", Toast.LENGTH_SHORT).show()
            }
        }

        // Lógica de Inicio de Sesión conectada a Supabase
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
                        // 2. Determinar tabla y columna según el rol seleccionado
                        val (tabla, columnaId) = when (rolSeleccionadoIntent) {
                            "ESTUDIANTE" -> "Estudiantes" to "codigoQr"
                            "PROFESOR" -> "Docentes" to "id_docente"
                            "ADMINISTRADOR" -> "Administrador" to "id_Administrador"
                            else -> "Estudiantes" to "codigoQr"
                        }

                        // 3. CORRECCIÓN: Se cambia el viejo .postgrest[tabla] por .from(tabla)
                        val response = SupabaseManager.client.from(tabla).select {
                            filter {
                                eq(columnaId, userId)
                            }
                        }

                        val list = response.decodeList<JsonObject>()

                        if (list.isNotEmpty()) {
                            val usuarioJson = list[0]

                            if (usuarioJson.containsKey("estado")) {
                                val estaActivo = usuarioJson["estado"]?.jsonPrimitive?.boolean ?: true
                                if (!estaActivo) {
                                    Toast.makeText(this@LoginActivity, "Cuenta INACTIVA. Contacte al administrador.", Toast.LENGTH_LONG).show()
                                    SupabaseManager.client.auth.signOut()
                                    return@launch
                                }
                            }

                            Toast.makeText(this@LoginActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

                            // Redireccionar al perfil correspondiente
                            val nombrePerfilClase = when (rolSeleccionadoIntent) {
                                "ESTUDIANTE" -> "com.example.asistecsr.PerfilEstudianteActivity"
                                "PROFESOR" -> "com.example.asistecsr.PerfilDocenteActivity"
                                "ADMINISTRADOR" -> "com.example.asistecsr.PerfilAdminActivity"
                                else -> "com.example.asistecsr.PerfilEstudianteActivity"
                            }

                            try {
                                val activityDestino = Class.forName(nombrePerfilClase)
                                startActivity(Intent(this@LoginActivity, activityDestino))
                                finish()
                            } catch (e: ClassNotFoundException) {
                                Toast.makeText(this@LoginActivity, "Login exitoso, pero la pantalla de Perfil no existe todavía.", Toast.LENGTH_LONG).show()
                            }

                        } else {
                            Toast.makeText(this@LoginActivity, "No tienes permisos como $rolSeleccionadoIntent.", Toast.LENGTH_LONG).show()
                            SupabaseManager.client.auth.signOut()
                        }
                    }
                } catch (e: AuthRestException) {
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Credenciales incorrectas: Verifique su correo o contraseña", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    btnLoginEnviar.isEnabled = true
                }
            }
        }

        txtOlvidaste.setOnClickListener {
            try {
                val destinoForgot = Class.forName("com.example.asistecsr.ForgotActivity")
                startActivity(Intent(this, destinoForgot))
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "La pantalla de recuperación no existe todavía.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}