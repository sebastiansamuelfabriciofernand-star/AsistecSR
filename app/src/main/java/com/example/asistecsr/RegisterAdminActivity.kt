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

class RegisterAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_admin)

        val btnAtras = findViewById<ImageView>(R.id.btnAtrasRegisterAdmin)
        val btnCrearCuenta = findViewById<AppCompatButton>(R.id.btnCrearCuentaAdmin)

        val idTxtNombre = findViewById<EditText>(R.id.txtRegisterAdminNombre)
        val idTxtApellidos = findViewById<EditText>(R.id.txtRegisterAdminApellidos)
        val idTxtDni = findViewById<EditText>(R.id.txtRegisterAdminDni)
        val idTxtNumero = findViewById<EditText>(R.id.txtRegisterAdminNumero)
        val idTxtCorreo = findViewById<EditText>(R.id.txtRegisterAdminCorreo)
        val idTxtContrasena = findViewById<EditText>(R.id.txtRegisterAdminContrasena)

        btnAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCrearCuenta.setOnClickListener {
            val nombreTxt = idTxtNombre.text.toString().trim()
            val apellidoTxt = idTxtApellidos.text.toString().trim()
            val dniTxt = idTxtDni.text.toString().trim()
            val numeroTxt = idTxtNumero.text.toString().trim()
            val correoTxt = idTxtCorreo.text.toString().trim()
            val contrasenaTxt = idTxtContrasena.text.toString().trim()

            if (nombreTxt.isEmpty() || apellidoTxt.isEmpty() || dniTxt.isEmpty() ||
                numeroTxt.isEmpty() || correoTxt.isEmpty() || contrasenaTxt.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasenaTxt.length < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCrearCuenta.isEnabled = false

            lifecycleScope.launch {
                try {
                    // 1. Crear usuario en Supabase Auth
                    SupabaseManager.client.auth.signUpWith(Email) {
                        email = correoTxt
                        password = contrasenaTxt
                    }

                    val uid = SupabaseManager.client.auth.currentUserOrNull()?.id

                    if (uid != null) {
                        // CORRECCIÓN: Estructura corregida y limpia del constructor de AdminModel
                        val nuevoAdmin = AdminModel(
                            id_Administrador = uid,
                            nombres = nombreTxt,
                            apellidos = apellidoTxt,
                            dni = dniTxt,
                            numero = numeroTxt,
                            correo = correoTxt
                        )

                        // 2. Inserción con la sintaxis .from()
                        SupabaseManager.client.from("Administrador").insert(nuevoAdmin)

                        Toast.makeText(this@RegisterAdminActivity, "¡Cuenta de Administrador creada!", Toast.LENGTH_SHORT).show()

                        try {
                            val intent = Intent(this@RegisterAdminActivity, Class.forName("com.example.asistecsr.PerfilAdminActivity"))
                            startActivity(intent)
                            finish()
                        } catch (_: ClassNotFoundException) {
                            Toast.makeText(this@RegisterAdminActivity, "Registro exitoso en Supabase.", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        Toast.makeText(this@RegisterAdminActivity, "Error: No se pudo obtener el ID único.", Toast.LENGTH_LONG).show()
                    }

                } catch (e: io.github.jan.supabase.exceptions.RestException) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterAdminActivity, "Error de Supabase: ${e.error}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterAdminActivity, "Error de Red/Código: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    btnCrearCuenta.isEnabled = true
                }
            }
        }
    }
}