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
import io.github.jan.supabase.postgrest.postgrest
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
        val idTxtCorreo = findViewById<EditText>(R.id.txtRegisterCorreo)
        val idTxtContrasena = findViewById<EditText>(R.id.txtRegisterContrasena)

        btnAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCrearCuenta.setOnClickListener {
            val nombreTxt = idTxtNombre.text.toString().trim()
            val apellidoTxt = idTxtApellidos.text.toString().trim()
            val dniTxt = idTxtDni.text.toString().trim()
            val correoTxt = idTxtCorreo.text.toString().trim()
            val contrasenaTxt = idTxtContrasena.text.toString().trim()

            // Validaciones básicas de entrada
            if (nombreTxt.isEmpty() || apellidoTxt.isEmpty() || dniTxt.isEmpty() || correoTxt.isEmpty() || contrasenaTxt.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasenaTxt.length < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCrearCuenta.isEnabled = false

            lifecycleScope.launch {
                try {
                    // 1. Registrar al usuario en Supabase Auth y guardar la respuesta de inmediato
                    val response = SupabaseManager.client.auth.signUpWith(Email) {
                        email = correoTxt
                        password = contrasenaTxt
                    }

                    // 2. Extraer el UID directamente de la respuesta del servidor
                    val uid = response?.id

                    if (uid != null) {
                        // Construimos el objeto UserProfile con los datos ingresados
                        val nuevoEstudiante = UserProfile(
                            codigoQr = uid,
                            nombres = nombreTxt,
                            apellidos = apellidoTxt,
                            dni = dniTxt, // <-- Aquí aseguramos el envío del DNI a Supabase
                            email = correoTxt,
                            edad = 18,
                        )

                        // 3. Insertar el perfil en la tabla de la Base de Datos 'Estudiantes'
                        SupabaseManager.client.postgrest["Estudiantes"].insert(nuevoEstudiante)

                        Toast.makeText(this@RegisterEstudianteActivity, "¡Cuenta de Alumno creada!", Toast.LENGTH_SHORT).show()

                        // CAMBIO CLAVE: Pasar directo a la pantalla del perfil de forma limpia
                        val intent = Intent(this@RegisterEstudianteActivity, PerfilEstudianteActivity::class.java)
                        startActivity(intent)
                        finish() // Cerramos el registro para no volver atrás

                    } else {
                        Toast.makeText(this@RegisterEstudianteActivity, "Error: No se pudo obtener el ID único del servidor.", Toast.LENGTH_LONG).show()
                    }

                } catch (e: io.github.jan.supabase.exceptions.RestException) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterEstudianteActivity, "Error de Supabase: ${e.error}", Toast.LENGTH_LONG).show()
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