package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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
        
        val idTxtCorreoPrefijo = findViewById<EditText>(R.id.txtRegisterAdminCorreoPrefijo)
        val spinnerCorreoDominio = findViewById<Spinner>(R.id.spinnerRegisterAdminCorreoDominio)
        
        val idTxtContrasena = findViewById<EditText>(R.id.txtRegisterAdminContrasena)

        // Configurar Spinner de Dominio
        val adapterDominio = ArrayAdapter.createFromResource(
            this,
            R.array.dominios_correo,
            R.layout.spinner_item_registro
        )
        adapterDominio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCorreoDominio.adapter = adapterDominio

        btnAtras.setupClickAnimation {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCrearCuenta.setupClickAnimation {
            val nombreTxt = idTxtNombre.text.toString().trim()
            val apellidoTxt = idTxtApellidos.text.toString().trim()
            val dniTxt = idTxtDni.text.toString().trim()
            val numeroTxt = idTxtNumero.text.toString().trim()
            
            val correoPrefijo = idTxtCorreoPrefijo.text.toString().trim()
            val correoDominio = spinnerCorreoDominio.selectedItem.toString()
            val correoTxt = "$correoPrefijo$correoDominio"
            
            val contrasenaTxt = idTxtContrasena.text.toString().trim()

            if (nombreTxt.isEmpty() || apellidoTxt.isEmpty() || dniTxt.isEmpty() ||
                numeroTxt.isEmpty() || correoPrefijo.isEmpty() || contrasenaTxt.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setupClickAnimation
            }

            if (contrasenaTxt.length < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setupClickAnimation
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

                        // Inserción con la sintaxis .from()
                        SupabaseManager.client.from("Administrador").insert(nuevoAdmin)

                        Toast.makeText(this@RegisterAdminActivity, "¡Cuenta de Administrador creada!", Toast.LENGTH_SHORT).show()

                        val destino = "com.example.asistecsr.PerfilAdminActivity"
                        
                        // Lógica para guardar cuenta recién creada
                        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                        val accountsJson = sharedPref.getString("saved_accounts_json", "[]") ?: "[]"
                        val savedAccounts = try {
                            Json.decodeFromString<List<AccountRecord>>(accountsJson)
                        } catch (e: Exception) {
                            emptyList()
                        }

                        if (savedAccounts.none { it.email == correoTxt }) {
                            mostrarDialogoGuardarCuenta(correoTxt, contrasenaTxt, savedAccounts, sharedPref, destino)
                        } else {
                            irAlPerfil(destino)
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

    private fun irAlPerfil(destino: String) {
        try {
            startActivity(Intent(this, Class.forName(destino)))
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir perfil.", Toast.LENGTH_SHORT).show()
        }
    }
}