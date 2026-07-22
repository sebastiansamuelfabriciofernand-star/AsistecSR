package com.example.asistecsr

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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

class RegisterProfesorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_profesor)

        val btnAtras = findViewById<ImageView>(R.id.btnRegProfesorVolver)
        val btnCrearCuenta = findViewById<AppCompatButton>(R.id.btnProfesorCrearCuenta)

        val edtNombre = findViewById<EditText>(R.id.edtProfesorNombre)
        val spinnerEspecialidad = findViewById<Spinner>(R.id.spinnerProfesorEspecialidad)
        val edtNumero = findViewById<EditText>(R.id.edtProfesorNumero)
        
        // Correo Personal
        val edtEmailPrefijo = findViewById<EditText>(R.id.edtProfesorEmailPrefijo)
        val spinnerEmailDominio = findViewById<Spinner>(R.id.spinnerProfesorEmailDominio)
        
        // Correo Institucional
        val edtEmailInstPrefijo = findViewById<EditText>(R.id.edtProfesorEmailInstPrefijo)
        val spinnerEmailInstDominio = findViewById<Spinner>(R.id.spinnerProfesorEmailInstDominio)
        
        val edtPassword = findViewById<EditText>(R.id.edtProfesorPassword)

        // Configurar Spinners
        configurarSpinners(spinnerEspecialidad, spinnerEmailDominio, spinnerEmailInstDominio)

        btnAtras.setupClickAnimation {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCrearCuenta.setupClickAnimation {
            val nombre = edtNombre.text.toString().trim()
            val especialidad = spinnerEspecialidad.selectedItem.toString()
            val numero = edtNumero.text.toString().trim()
            
            val emailPrefijo = edtEmailPrefijo.text.toString().trim()
            val emailDominio = spinnerEmailDominio.selectedItem.toString()
            val email = "$emailPrefijo$emailDominio"
            
            val emailInstPrefijo = edtEmailInstPrefijo.text.toString().trim()
            val emailInstDominio = spinnerEmailInstDominio.selectedItem.toString()
            val emailInst = "$emailInstPrefijo$emailInstDominio"
            
            val password = edtPassword.text.toString().trim()

            if (nombre.isEmpty() || numero.isEmpty() || emailPrefijo.isEmpty() || emailInstPrefijo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setupClickAnimation
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setupClickAnimation
            }

            btnCrearCuenta.isEnabled = false

            lifecycleScope.launch {
                try {
                    SupabaseManager.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

                    if (userId != null) {
                        val nuevoDocente = DocenteModel(
                            idDocente = userId,
                            nombresApellidos = nombre,
                            especialidad = especialidad,
                            numero = numero,
                            correo = email,
                            correoInst = emailInst
                        )

                        SupabaseManager.client.from("Docentes").insert(nuevoDocente)

                        Toast.makeText(this@RegisterProfesorActivity, "¡Cuenta de Docente creada!", Toast.LENGTH_SHORT).show()
                        
                        val destino = "com.example.asistecsr.PerfilDocenteActivity"
                        
                        // Lógica para guardar cuenta recién creada
                        val sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                        val accountsJson = sharedPref.getString("saved_accounts_json", "[]") ?: "[]"
                        val savedAccounts = try {
                            Json.decodeFromString<List<AccountRecord>>(accountsJson)
                        } catch (e: Exception) {
                            emptyList()
                        }

                        if (savedAccounts.none { it.email == email }) {
                            mostrarDialogoGuardarCuenta(email, password, savedAccounts, sharedPref, destino)
                        } else {
                            irAlPerfil(destino)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterProfesorActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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

        view.findViewById<android.widget.TextView>(R.id.btnCancelSave).setOnClickListener {
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

    private fun configurarSpinners(especialidad: Spinner, dominioPers: Spinner, dominioInst: Spinner) {
        val adapterItem = R.layout.spinner_item_registro

        val adapterEsp = ArrayAdapter.createFromResource(this, R.array.registro_carreras, adapterItem)
        adapterEsp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        especialidad.adapter = adapterEsp

        val adapterDom = ArrayAdapter.createFromResource(this, R.array.dominios_correo, adapterItem)
        adapterDom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dominioPers.adapter = adapterDom
        dominioInst.adapter = adapterDom
        
        // Opcionalmente pre-seleccionar el institucional
        // dominioInst.setSelection(...)
    }
}