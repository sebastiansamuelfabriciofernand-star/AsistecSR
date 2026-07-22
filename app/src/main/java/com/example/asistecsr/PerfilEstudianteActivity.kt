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
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.util.Locale

class PerfilEstudianteActivity : AppCompatActivity() {

    // Variable global para almacenar el UUID del QR único y pasarlo al maximizado
    private var codigoQrEstudiante: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_estudiante) //[cite: 2]

        val btnAtras = findViewById<ImageView>(R.id.btnAtrasPerfilEstudiante) //[cite: 2]
        val btnMostrarQR = findViewById<AppCompatButton>(R.id.btnMostrarQR) //[cite: 2]
        val btnRealizarConsulta = findViewById<AppCompatButton>(R.id.btnRealizarConsulta) //[cite: 2]
        val ivCodigoQR = findViewById<ImageView>(R.id.ivCodigoQR) //[cite: 2]

        val txtPerfilNombre = findViewById<TextView>(R.id.txtPerfilNombre) //[cite: 2]
        val txtPerfilApellidos = findViewById<TextView>(R.id.txtPerfilApellidos) //[cite: 2]
        val txtPerfilDni = findViewById<TextView>(R.id.txtPerfilDni) //[cite: 2]
        val txtPerfilEmail = findViewById<TextView>(R.id.txtPerfilEmail) //[cite: 2]

        btnAtras.setupClickAnimation {
            onBackPressedDispatcher.onBackPressed()
        }

        // 1. CARGA DE DATOS: Llamamos a tu método asíncrono original conectado a tu Supabase Manager
        cargarDatosDelAlumno(txtPerfilNombre, txtPerfilApellidos, txtPerfilDni, txtPerfilEmail, ivCodigoQR)

        // 2. DIÁLOGO MAXIMIZADO: Al presionar, abre tu dialog_qr_maximizada.xml con el UUID
        btnMostrarQR.setupClickAnimation {
            val qrUnico = codigoQrEstudiante // Usa la variable global con el UUID de la base de datos
            if (!qrUnico.isNullOrEmpty()) {
                val qrBitmapGrande = generarCodigoQR(qrUnico, 500)

                if (qrBitmapGrande != null) {
                    val dialog = Dialog(this@PerfilEstudianteActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                    dialog.setContentView(R.layout.dialog_qr_maximizada)

                    val ivQrGigante = dialog.findViewById<ImageView>(R.id.ivQrGigante)
                    val btnCerrarQrGigante = dialog.findViewById<AppCompatButton>(R.id.btnCerrarQrGigante)

                    ivQrGigante.setImageBitmap(qrBitmapGrande)

                    btnCerrarQrGigante.setupClickAnimation {
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

        btnRealizarConsulta.setupClickAnimation {
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
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id //[cite: 2]

        if (userId != null) { //[cite: 2]
            lifecycleScope.launch { //[cite: 2]
                try {
                    val response = SupabaseManager.client.from("Estudiantes") //[cite: 2]
                        .select { filter { eq("codigoQr", userId) } } //[cite: 2]

                    val perfilAlumno = response.decodeSingle<EstudianteModel>() //[cite: 2]

                    // Guardamos el UUID largo maestro en la variable global para usarlo en el QR grande[cite: 2]
                    codigoQrEstudiante = perfilAlumno.codigoQr

                    // Pintamos los datos en tu interfaz gráfica[cite: 2]
                    txtNombre.text = getString(R.string.estudiante_nombres, perfilAlumno.nombres?.uppercase(Locale.getDefault()) ?: "")
                    txtApellidos.text = getString(R.string.estudiante_apellidos, perfilAlumno.apellidos?.uppercase(Locale.getDefault()) ?: "")
                    txtDni.text = getString(R.string.estudiante_dni, perfilAlumno.dni ?: "")
                    txtEmail.text = getString(R.string.estudiante_email, perfilAlumno.email ?: "")

                    // Generamos el QR mediano para el perfil usando el UUID único
                    if (!perfilAlumno.codigoQr.isNullOrEmpty()) {
                        val qrBitmap = generarCodigoQR(perfilAlumno.codigoQr, 350)
                        if (qrBitmap != null) { //[cite: 2]
                            ivQr.setImageBitmap(qrBitmap) //[cite: 2]
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace() //[cite: 2]
                    Toast.makeText(this@PerfilEstudianteActivity, "Error de enlace: ${e.localizedMessage}", Toast.LENGTH_LONG).show() //[cite: 2]
                }
            }
        } else {
            Toast.makeText(this@PerfilEstudianteActivity, "Sesión no válida.", Toast.LENGTH_LONG).show() //[cite: 2]
            finish() //[cite: 2]
        }
    }

    // CORRECCIÓN DE COMPILACIÓN: Método universal usando setPixel estándar sin errores de matrices
    private fun generarCodigoQR(contenido: String, dimensiones: Int): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                contenido,
                BarcodeFormat.QR_CODE,
                dimensiones,
                dimensiones
            ) //[cite: 2]
            val ancho = bitMatrix.width //[cite: 2]
            val alto = bitMatrix.height //[cite: 2]

            val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565)

            for (x in 0 until ancho) { //[cite: 2]
                for (y in 0 until alto) { //[cite: 2]
                    val pixelNegro = bitMatrix.get(x, y) //[cite: 2]
                    // Solución definitiva al error de operadores usando el método setPixel estándar de Android
                    bitmap.setPixel(x, y, if (pixelNegro) Color.BLACK else Color.WHITE)
                }
            }
            bitmap //[cite: 2]
        } catch (e: Exception) {
            e.printStackTrace() //[cite: 2]
            null //[cite: 2]
        }
    }
}