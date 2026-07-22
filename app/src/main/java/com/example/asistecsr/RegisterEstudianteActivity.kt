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
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerRegisterCiclo)
        val spinnerCarrera = findViewById<Spinner>(R.id.spinnerRegisterCarrera)
        val spinnerTurno = findViewById<Spinner>(R.id.spinnerRegisterTurno)
        val idTxtCorreoPrefijo = findViewById<EditText>(R.id.txtRegisterCorreoPrefijo)
        val spinnerCorreoDominio = findViewById<Spinner>(R.id.spinnerRegisterCorreoDominio)
        val idTxtContrasena = findViewById<EditText>(R.id.txtRegisterContrasena)

        // Configuración de los Spinners con las opciones dinámicas
        configurarSpinners(spinnerCiclo, spinnerCarrera, spinnerTurno, spinnerCorreoDominio)

        btnAtras.setupClickAnimation {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCrearCuenta.setupClickAnimation {
            val nombreTxt = idTxtNombre.text.toString().trim()
            val apellidoTxt = idTxtApellidos.text.toString().trim()
            val dniTxt = idTxtDni.text.toString().trim()
            val edadStr = idTxtEdad.text.toString().trim()
            
            // Obtenemos valores de los Spinners
            val cicloStr = spinnerCiclo.selectedItem.toString()
            val carreraTxt = spinnerCarrera.selectedItem.toString()
            val turnoTxt = spinnerTurno.selectedItem.toString()
            
            val correoPrefijo = idTxtCorreoPrefijo.text.toString().trim()
            val correoDominio = spinnerCorreoDominio.selectedItem.toString()
            val correoTxt = "$correoPrefijo$correoDominio"
            
            val contrasenaTxt = idTxtContrasena.text.toString().trim()

            if (nombreTxt.isEmpty() || apellidoTxt.isEmpty() || dniTxt.isEmpty() ||
                edadStr.isEmpty() || correoPrefijo.isEmpty() || contrasenaTxt.isEmpty()) {
                Toast.makeText(this@RegisterEstudianteActivity, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setupClickAnimation
            }

            if (contrasenaTxt.length < 6) {
                Toast.makeText(this@RegisterEstudianteActivity, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setupClickAnimation
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

                        SupabaseManager.client.from("Estudiantes").insert(nuevoEstudiante)

                        Toast.makeText(this@RegisterEstudianteActivity, "¡Cuenta de Alumno creada con éxito!", Toast.LENGTH_SHORT).show()

                        val destino = "com.example.asistecsr.PerfilEstudianteActivity"
                        
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

    private fun configurarSpinners(spinnerCiclo: Spinner, spinnerCarrera: Spinner, spinnerTurno: Spinner, spinnerCorreoDominio: Spinner) {
        // Adaptador para Ciclo
        val adapterCiclo = ArrayAdapter.createFromResource(
            this,
            R.array.registro_ciclos,
            R.layout.spinner_item_registro
        )
        adapterCiclo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCiclo.adapter = adapterCiclo

        // Adaptador para Carrera
        val adapterCarrera = ArrayAdapter.createFromResource(
            this,
            R.array.registro_carreras,
            R.layout.spinner_item_registro
        )
        adapterCarrera.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCarrera.adapter = adapterCarrera

        // Adaptador para Turno
        val adapterTurno = ArrayAdapter.createFromResource(
            this,
            R.array.registro_turnos,
            R.layout.spinner_item_registro
        )
        adapterTurno.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTurno.adapter = adapterTurno

        // Adaptador para Dominio de Correo
        val adapterDominio = ArrayAdapter.createFromResource(
            this,
            R.array.dominios_correo,
            R.layout.spinner_item_registro
        )
        adapterDominio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCorreoDominio.adapter = adapterDominio
    }
}