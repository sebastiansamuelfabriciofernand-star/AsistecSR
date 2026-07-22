package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.core.content.edit
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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

        // Lógica interactiva para múltiples cuentas guardadas
        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        val accountsJson = sharedPref.getString("saved_accounts_json", "[]") ?: "[]"
        val savedAccounts = try {
            Json.decodeFromString<List<AccountRecord>>(accountsJson)
        } catch (e: Exception) {
            emptyList()
        }

        if (savedAccounts.isNotEmpty()) {
            mostrarRecomendacionAcceso(savedAccounts, edtUsuario, edtContrasena)
        }

        val rolSeleccionadoIntent = intent.getStringExtra("ROL_SELECCIONADO") ?: "ESTUDIANTE"
        txtRolSeleccionado.text = rolSeleccionadoIntent

        iluminarIconoRol(rolSeleccionadoIntent)

        btnRegisterEnviar.setupClickAnimation {
            val nombreClase = when (rolSeleccionadoIntent) {
                "ADMINISTRADOR" -> "com.example.asistecsr.RegisterAdminActivity"
                "PROFESOR" -> "com.example.asistecsr.RegisterProfesorActivity"
                else -> "com.example.asistecsr.RegisterEstudianteActivity"
            }
            try {
                startActivity(Intent(this, Class.forName(nombreClase)))
            } catch (_: Exception) {
                Toast.makeText(this, "Error al abrir registro.", Toast.LENGTH_SHORT).show()
            }
        }

        btnLoginEnviar.setupClickAnimation {
            val correo = edtUsuario.text.toString().trim()
            val password = edtContrasena.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setupClickAnimation
            }

            btnLoginEnviar.isEnabled = false

            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.signInWith(Email) {
                        email = correo
                        this.password = password
                    }

                    val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                    if (userId != null) {
                        val (tabla, columnaId) = when (rolSeleccionadoIntent) {
                            "ESTUDIANTE" -> "Estudiantes" to "codigoQr"
                            "PROFESOR" -> "Docentes" to "id_docente"
                            "ADMINISTRADOR" -> "Administrador" to "id_Administrador"
                            else -> "Estudiantes" to "codigoQr"
                        }

                        // Optimización: Solo verificamos 'estado' si es Estudiante
                        val columnsToSelect = if (rolSeleccionadoIntent == "ESTUDIANTE") {
                            Columns.list("estado")
                        } else {
                            Columns.list(columnaId) // Solo traemos el ID para confirmar existencia
                        }

                        val usuarioData = SupabaseManager.client.from(tabla).select(columns = columnsToSelect) {
                            filter { eq(columnaId, userId) }
                            limit(1)
                        }.decodeSingleOrNull<JsonObject>()

                        if (usuarioData != null) {
                            // Si es estudiante, verificamos si está activo
                            if (rolSeleccionadoIntent == "ESTUDIANTE") {
                                val estaActivo = usuarioData["estado"]?.jsonPrimitive?.boolean ?: true
                                if (!estaActivo) {
                                    Toast.makeText(this@LoginActivity, "Cuenta INACTIVA.", Toast.LENGTH_LONG).show()
                                    SupabaseManager.client.auth.signOut()
                                    btnLoginEnviar.isEnabled = true
                                    return@launch
                                }
                            }

                            // Redirección inmediata
                            val destino = when (rolSeleccionadoIntent) {
                                "ESTUDIANTE" -> "com.example.asistecsr.PerfilEstudianteActivity"
                                "PROFESOR" -> "com.example.asistecsr.PerfilDocenteActivity"
                                "ADMINISTRADOR" -> "com.example.asistecsr.PerfilAdminActivity"
                                else -> "com.example.asistecsr.PerfilEstudianteActivity"
                            }

                            // Preguntar si desea guardar la cuenta actual con un diseño interactivo
                            val estaGuardada = savedAccounts.any { it.email == correo }
                            if (!estaGuardada) {
                                mostrarDialogoGuardarCuenta(correo, password, savedAccounts, sharedPref, destino)
                            } else {
                                irAlPerfil(destino)
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "No tienes permisos de $rolSeleccionadoIntent.", Toast.LENGTH_LONG).show()
                            SupabaseManager.client.auth.signOut()
                        }
                    }
                } catch (e: AuthRestException) {
                    Toast.makeText(this@LoginActivity, "Correo o contraseña incorrectos.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    btnLoginEnviar.isEnabled = true
                }
            }
        }

        txtOlvidaste.setOnClickListener {
            try {
                startActivity(Intent(this, Class.forName("com.example.asistecsr.ForgotActivity")))
            } catch (_: Exception) {}
        }
    }

    private fun mostrarDialogoGuardarCuenta(
        correo: String,
        pass: String,
        listaCuentas: List<AccountRecord>,
        prefs: android.content.SharedPreferences,
        destino: String
    ) {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_save, null)

        view.findViewById<AppCompatButton>(R.id.btnConfirmSave).setOnClickListener {
            val nuevasCuentas = listaCuentas + AccountRecord(correo, pass)
            prefs.edit {
                putString("saved_accounts_json", Json.encodeToString(nuevasCuentas))
            }
            bottomSheet.dismiss()
            irAlPerfil(destino)
        }

        view.findViewById<TextView>(R.id.btnCancelSave).setOnClickListener {
            bottomSheet.dismiss()
            irAlPerfil(destino)
        }

        bottomSheet.setCancelable(false)
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun mostrarRecomendacionAcceso(cuentas: List<AccountRecord>, edtU: EditText, edtC: EditText) {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_login, null)
        val container = view.findViewById<LinearLayout>(R.id.llAccountsContainer)
        
        cuentas.forEach { cuenta ->
            val itemView = layoutInflater.inflate(R.layout.item_saved_account, container, false)
            itemView.findViewById<TextView>(R.id.tvItemEmail).text = cuenta.email
            
            itemView.setOnClickListener {
                edtU.setText(cuenta.email)
                edtC.setText(cuenta.pass)
                bottomSheet.dismiss()
                Toast.makeText(this, "Cuenta cargada", Toast.LENGTH_SHORT).show()
            }
            container.addView(itemView)
        }
        
        view.findViewById<TextView>(R.id.btnManualLogin).setOnClickListener {
            bottomSheet.dismiss()
        }
        
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun irAlPerfil(destino: String) {
        try {
            startActivity(Intent(this, Class.forName(destino)))
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir perfil.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iluminarIconoRol(rol: String) {
        val indProfesor = findViewById<android.widget.ImageView>(R.id.indProfesor)
        val indEstudiante = findViewById<android.widget.ImageView>(R.id.indEstudiante)
        val indAdministrador = findViewById<android.widget.ImageView>(R.id.indAdministrador)

        val colorApagado = android.graphics.Color.GRAY
        indProfesor.setColorFilter(colorApagado)
        indEstudiante.setColorFilter(colorApagado)
        indAdministrador.setColorFilter(colorApagado)

        when (rol) {
            "PROFESOR" -> {
                indProfesor.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.rojo_brillante))
                indProfesor.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3D1010"))
            }
            "ESTUDIANTE" -> {
                indEstudiante.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.azul_asiste))
                indEstudiante.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10203D"))
            }
            "ADMINISTRADOR" -> {
                indAdministrador.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.verde_neon))
                indAdministrador.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#103D10"))
            }
        }
    }
}