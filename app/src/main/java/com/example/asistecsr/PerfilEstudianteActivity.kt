package com.example.asistecsr

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.util.Locale

class PerfilEstudianteActivity : AppCompatActivity() {

    private var dniEstudiante: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_estudiante)

        val btnAtras = findViewById<ImageView>(R.id.btnAtrasPerfilEstudiante)
        val btnMostrarQR = findViewById<AppCompatButton>(R.id.btnMostrarQR)
        val btnRealizarConsulta = findViewById<AppCompatButton>(R.id.btnRealizarConsulta)
        val ivCodigoQR = findViewById<ImageView>(R.id.ivCodigoQR)

        val txtPerfilNombre = findViewById<TextView>(R.id.txtPerfilNombre)
        val txtPerfilApellidos = findViewById<TextView>(R.id.txtPerfilApellidos)
        val txtPerfilDni = findViewById<TextView>(R.id.txtPerfilDni)
        val txtPerfilEmail = findViewById<TextView>(R.id.txtPerfilEmail)

        btnAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cargarDatosDelAlumno(txtPerfilNombre, txtPerfilApellidos, txtPerfilDni, txtPerfilEmail, ivCodigoQR)

        btnMostrarQR.setOnClickListener {
            val dni = dniEstudiante
            if (!dni.isNullOrEmpty()) {
                val qrBitmapGrande = generarCodigoQR(dni, 500)

                if (qrBitmapGrande != null) {
                    val dialog = Dialog(this@PerfilEstudianteActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                    dialog.setContentView(R.layout.dialog_qr_maximizada)

                    val ivQrGigante = dialog.findViewById<ImageView>(R.id.ivQrGigante)
                    val btnCerrarQrGigante = dialog.findViewById<AppCompatButton>(R.id.btnCerrarQrGigante)

                    ivQrGigante.setImageBitmap(qrBitmapGrande)

                    btnCerrarQrGigante.setOnClickListener {
                        btnCerrarQrGigante.animate()
                            .scaleX(0.92f)
                            .scaleY(0.92f)
                            .setDuration(80)
                            .withEndAction {
                                btnCerrarQrGigante.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .withEndAction {
                                        dialog.dismiss()
                                    }
                            }
                    }

                    dialog.show()
                } else {
                    Toast.makeText(this@PerfilEstudianteActivity, "Error al expandir el código QR.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@PerfilEstudianteActivity, "La información aún no se ha descargado", Toast.LENGTH_SHORT).show()
            }
        }

        btnRealizarConsulta.setOnClickListener {
            Toast.makeText(this@PerfilEstudianteActivity, "Abriendo solicitudes de justificación...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosDelAlumno(
        txtNombre: TextView,
        txtApellidos: TextView,
        txtDni: TextView,
        txtEmail: TextView,
        ivQr: ImageView
    ) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val response = SupabaseManager.client.from("Estudiantes")
                        .select { filter { eq("codigoQr", userId) } }

                    val perfilAlumno = response.decodeSingle<EstudianteModel>()

                    dniEstudiante = perfilAlumno.codigoQr

                    txtNombre.text = getString(R.string.estudiante_nombres, perfilAlumno.nombres.uppercase(Locale.getDefault()))
                    txtApellidos.text = getString(R.string.estudiante_apellidos, perfilAlumno.apellidos.uppercase(Locale.getDefault()))
                    txtDni.text = getString(R.string.estudiante_dni, perfilAlumno.dni)
                    txtEmail.text = getString(R.string.estudiante_email, perfilAlumno.email)

                    if (perfilAlumno.codigoQr.isNotEmpty()) {
                        val qrBitmap = generarCodigoQR(perfilAlumno.codigoQr, 350)
                        if (qrBitmap != null) {
                            ivQr.setImageBitmap(qrBitmap)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@PerfilEstudianteActivity, "Error de enlace: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this@PerfilEstudianteActivity, "Sesión no válida.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun generarCodigoQR(contenido: String, dimensiones: Int): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                contenido,
                BarcodeFormat.QR_CODE,
                dimensiones,
                dimensiones
            )
            val ancho = bitMatrix.width
            val alto = bitMatrix.height

            val bitmap = createBitmap(ancho, alto, Bitmap.Config.RGB_565)

            for (x in 0 until ancho) {
                for (y in 0 until alto) {
                    val pixelNegro = bitMatrix.get(x, y)
                    // CORRECCIÓN: Uso nativo de setPixel para evitar errores por falta de imports KTX de operador
                    bitmap.setPixel(x, y, if (pixelNegro) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}