package com.example.asistecsr

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class RegisterProfesorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_profesor)

        val btnRegProfesorVolver = findViewById<ImageView>(R.id.btnRegProfesorVolver)
        val btnProfesorCrearCuenta = findViewById<AppCompatButton>(R.id.btnProfesorCrearCuenta)

        val edtNombre = findViewById<EditText>(R.id.edtProfesorNombre)
        val edtEspecialidad = findViewById<EditText>(R.id.edtProfesorEspecialidad)
        val edtNumero = findViewById<EditText>(R.id.edtProfesorNumero)
        val edtEmail = findViewById<EditText>(R.id.edtProfesorEmail)
        val edtEmailInst = findViewById<EditText>(R.id.edtProfesorEmailInst)
        val edtPassword = findViewById<EditText>(R.id.edtProfesorPassword)

        btnRegProfesorVolver.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnProfesorCrearCuenta.setOnClickListener {
            val nombreText = edtNombre.text.toString().trim()
            val especialidadText = edtEspecialidad.text.toString().trim()
            val numeroText = edtNumero.text.toString().trim()
            val emailText = edtEmail.text.toString().trim()
            val emailInstText = edtEmailInst.text.toString().trim()
            val passwordText = edtPassword.text.toString().trim()

            // Validación exhaustiva de los campos fundamentales
            if (nombreText.isEmpty() || emailText.isEmpty() || emailInstText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los datos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnProfesorCrearCuenta.isEnabled = false

            lifecycleScope.launch {
                try {
                    // 1. Registro en la sección de seguridad (Auth) usando el correo personal
                    val userResult = SupabaseManager.client.auth.signUpWith(Email) {
                        email = emailText
                        password = passwordText
                    }

                    // 2. Extracción directa del identificador UID único
                    val userId = userResult?.id

                    if (userId != null) {
                        // Marcadores por defecto lógicos si el usuario los dejó opcionales
                        val finalEspecialidad = if (especialidadText.isEmpty()) "General" else especialidadText
                        val finalNumero = if (numeroText.isEmpty()) "000000000" else numeroText

                        val nuevoDocente = DocenteProfile(
                            idDocente = userId,
                            nombresApellidos = nombreText,
                            especialidad = finalEspecialidad,
                            numero = finalNumero,
                            correo = emailText,
                            correoInst = emailInstText,
                            carrera = "Por asignar",
                            estado = true
                        )
                        // 4. Inserción directa en la tabla relacional de Postgres
                        SupabaseManager.client.postgrest["Docentes"].insert(nuevoDocente)

                        Toast.makeText(this@RegisterProfesorActivity, "¡Cuenta de Docente creada con éxito!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegisterProfesorActivity, "Error al generar identificador único de Supabase.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterProfesorActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    btnProfesorCrearCuenta.isEnabled = true
                }
            }
        }
    }
}