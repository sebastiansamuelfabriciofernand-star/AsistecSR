package com.example.asistecsr

import android.content.Intent
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

class RegisterEstudianteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_estudiante)

        val btnAtras = findViewById<ImageView>(R.id.btnAtrasRegisterEstudiante)
        val btnCrearCuenta = findViewById<AppCompatButton>(R.id.btnCrearCuentaEstudiante)

        val idTxtNombre = findViewById<EditText>(R.id.txtRegisterNombre)
        val idTxtApellidos = findViewById<EditText>(R.id.txtRegisterApellidos)
        val idTxtDni = findViewById<EditText>(R.id.txtRegisterDni)
        val idTxtEdad = findViewById<EditText>(R.id.txtRegisterEdad)
        val idTxtCiclo = findViewById<EditText>(R.id.txtRegisterCiclo)
        val idTxtCarrera = findViewById<EditText>(R.id.txtRegisterCarrera)
        val idTxtTurno = findViewById<EditText>(R.id.txtRegisterTurno)
        val idTxtCorreo = findViewById<EditText>(R.id.txtRegisterCorreo)
        val idTxtContrasena = findViewById<EditText>(R.id.txtRegisterContrasena)

        btnAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCrearCuenta.setOnClickListener {
            val nombreTxt = idTxtNombre.text.toString().trim()
            val apellidoTxt = idTxtApellidos.text.toString().trim()
            val dniTxt = idTxtDni.text.toString().trim()
            val edadStr = idTxtEdad.text.toString().trim()
            val cicloStr = idTxtCiclo.text.toString().trim()
            val carreraTxt = idTxtCarrera.text.toString().trim()
            val turnoTxt = idTxtTurno.text.toString().trim()
            val correoTxt = idTxtCorreo.text.toString().trim()
            val contrasenaTxt = idTxtContrasena.text.toString().trim()

            // CORRECCIÓN: Contexto explícito para evitar la desorientación del compilador
            if (nombreTxt.isEmpty() || apellidoTxt.isEmpty() || dniTxt.isEmpty() ||
                edadStr.isEmpty() || cicloStr.isEmpty() || carreraTxt.isEmpty() ||
                turnoTxt.isEmpty() || correoTxt.isEmpty() || contrasenaTxt.isEmpty()) {
                Toast.makeText(this@RegisterEstudianteActivity, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasenaTxt.length < 6) {
                Toast.makeText(this@RegisterEstudianteActivity, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val edadInt = edadStr.toIntOrNull() ?: 0
            val cicloInt = cicloStr.toIntOrNull() ?: 1

            btnCrearCuenta.isEnabled = false

            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.signUpWith(Email) {
                        email = correoTxt
                        password = contrasenaTxt
                    }

                    val uid = SupabaseManager.client.auth.currentUserOrNull()?.id

                    if (uid != null) {
                        val nuevoEstudiante = EstudianteModel(
                            codigoQr = uid,
                            nombres = nombreTxt,
                            apellidos = apellidoTxt,
                            dni = dniTxt,
                            edad = edadInt,
                            email = correoTxt,
                            ciclo = cicloInt,
                            carrera = carreraTxt,
                            estado = true,
                            turno = turnoTxt
                        )

                        // CORRECCIÓN: Migración de .postgrest[...] a la API estándar .from(...)
                        SupabaseManager.client.from("Estudiantes").insert(nuevoEstudiante)

                        Toast.makeText(this@RegisterEstudianteActivity, "¡Cuenta de Alumno creada con éxito!", Toast.LENGTH_SHORT).show()

                        try {
                            val intent = Intent(this@RegisterEstudianteActivity, Class.forName("com.example.asistecsr.PerfilEstudianteActivity"))
                            startActivity(intent)
                            finish()
                        } catch (_: ClassNotFoundException) {
                            Toast.makeText(this@RegisterEstudianteActivity, "Registro exitoso en la base de datos.", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        Toast.makeText(this@RegisterEstudianteActivity, "Error: No se pudo obtener el ID único del servidor.", Toast.LENGTH_LONG).show()
                    }

                } catch (e: io.github.jan.supabase.exceptions.RestException) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterEstudianteActivity, "Error de base de datos: ${e.error}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterEstudianteActivity, "Error de Red/Código: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    btnCrearCuenta.isEnabled = true
                }
            }
        }
    }
}