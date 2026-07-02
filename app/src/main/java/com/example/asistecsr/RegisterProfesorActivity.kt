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
import io.github.jan.supabase.postgrest.from
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

            // CORRECCIÓN: Contexto explícito en el Toast
            if (nombreText.isEmpty() || emailText.isEmpty() || emailInstText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this@RegisterProfesorActivity, "Por favor complete todos los datos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                Toast.makeText(this@RegisterProfesorActivity, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnProfesorCrearCuenta.isEnabled = false

            lifecycleScope.launch {
                try {
                    // En Supabase Auth, signUpWith retorna una estructura cuyo ID se accede a través del objeto user
                    val authResult = SupabaseManager.client.auth.signUpWith(Email) {
                        email = emailText
                        password = passwordText
                    }

                    val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

                    if (userId != null) {
                        val finalEspecialidad = especialidadText.ifEmpty { "General" }
                        val finalNumero = numeroText.ifEmpty { "000000000" }

                        // CORRECCIÓN DE PARÁMETROS: Se removieron 'carrera' y 'estado' para coincidir con tu DocenteModel real del Adapter
                        val nuevoDocente = DocenteModel(
                            idDocente = userId,
                            nombresApellidos = nombreText,
                            especialidad = finalEspecialidad,
                            numero = finalNumero,
                            correo = emailText,
                            correoInst = emailInstText
                        )

                        // CORRECCIÓN POSTGREST: Migración del obsoleto .postgrest[] a .from()
                        SupabaseManager.client.from("Docentes").insert(nuevoDocente)

                        Toast.makeText(this@RegisterProfesorActivity, "¡Cuenta de Docente creada con éxito!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegisterProfesorActivity, "Error al generar identificador único de la base de datos.", Toast.LENGTH_SHORT).show()
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