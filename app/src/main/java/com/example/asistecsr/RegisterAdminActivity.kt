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

class RegisterAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_admin)

        // Vincular componentes con los IDs reales de activity_register_admin.xml
        val btnAtras = findViewById<ImageView>(R.id.btnAtrasRegisterAdmin)
        val btnCrearCuenta = findViewById<AppCompatButton>(R.id.btnCrearCuentaAdmin)

        val edtNombre = findViewById<EditText>(R.id.txtRegisterAdminNombre)
        val edtApellidos = findViewById<EditText>(R.id.txtRegisterAdminApellidos)
        val edtDni = findViewById<EditText>(R.id.txtRegisterAdminDni)
        val edtNumero = findViewById<EditText>(R.id.txtRegisterAdminNumero)
        val edtCorreo = findViewById<EditText>(R.id.txtRegisterAdminCorreo)
        val edtPassword = findViewById<EditText>(R.id.txtRegisterAdminContrasena)

        btnAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCrearCuenta.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val apellidos = edtApellidos.text.toString().trim()
            val dni = edtDni.text.toString().trim()
            val numero = edtNumero.text.toString().trim()
            val correo = edtCorreo.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || numero.isEmpty() || correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCrearCuenta.isEnabled = false

            lifecycleScope.launch {
                try {
                    // 1. Crear cuenta en la sección Auth de Supabase
                    SupabaseManager.client.auth.signUpWith(Email) {
                        email = correo
                        this.password = password
                    }

                    // Obtener el ID del usuario recién creado
                    val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

                    if (userId != null) {
                        // 2. Crear el objeto para la base de datos (Tabla Administrador)
                        val nuevoAdmin = AdminProfile(
                            id_Administrador = userId,
                            Nombres = nombre,
                            Apellidos = apellidos,
                            Dni = dni,
                            Numero = numero,
                            Correo = correo
                        )

                        // 3. Insertar en la tabla real de la Database
                        SupabaseManager.client.postgrest["Administrador"].insert(nuevoAdmin)

                        Toast.makeText(this@RegisterAdminActivity, "¡Administrador registrado exitosamente!", Toast.LENGTH_LONG).show()
                        finish() // Volver al login
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterAdminActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    btnCrearCuenta.isEnabled = true
                }
            }
        }
    }
}