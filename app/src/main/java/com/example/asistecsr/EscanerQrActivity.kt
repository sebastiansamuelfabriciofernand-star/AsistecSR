package com.example.asistecsr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EscanerQrActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView

    private var cicloFiltro: Int = -1
    private var turnoFiltro: String = ""
    private var isScanning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escaner_qr)

        // 1. Recibir parámetros del docente
        cicloFiltro = intent.getIntExtra("EXTRA_CICLO", 1)
        turnoFiltro = intent.getStringExtra("EXTRA_TURNO") ?: "DIURNO"

        previewView = findViewById(R.id.previewViewCamara)
        findViewById<ImageView>(R.id.btnAtrasEscaner).setOnClickListener { finish() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 2. Solicitar permisos de cámara
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e("EscanerQrActivity", "Error al iniciar cámara", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: androidx.camera.core.ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && isScanning) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            isScanning = false // Pausar escaneo para procesar
                            registrarAsistencia(rawValue)
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("EscanerQrActivity", "Error al escanear QR", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun registrarAsistencia(valorQrEscaneado: String) {
        lifecycleScope.launch {
            try {
                val idDocente = SupabaseManager.client.auth.currentUserOrNull()?.id
                if (idDocente == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EscanerQrActivity, "Sesión no válida", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return@launch
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaHoy = sdf.format(Calendar.getInstance().time)

                // IMPORTANTE: Asegúrate de que "C001" esté registrado en la tabla "Cursos" en tu Supabase
                val idCursoExistente = "C001"

                // CORRECCIÓN STRUCTURAL: Enviamos únicamente las columnas físicas que existen en la tabla Asistencia
                val datosAsistencia = buildJsonObject {
                    put("fecha", fechaHoy)
                    put("codigoQr", valorQrEscaneado) // El UID largo de Supabase Auth del estudiante
                    put("id_docente", idDocente)
                    put("id_curso", idCursoExistente)
                }

                // Inserción limpia en la tabla física en singular "Asistencia"
                SupabaseManager.client.from("Asistencia").insert(datosAsistencia)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EscanerQrActivity, "¡Asistencia registrada exitosamente!", Toast.LENGTH_LONG).show()
                    // Esperar 2 segundos antes de permitir el próximo escaneo para evitar duplicados rápidos
                    delay(2000)
                    isScanning = true
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EscanerQrActivity, "Error BD: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    isScanning = true
                }
            }
        }
    }

    private suspend fun delay(timeMillis: Long) {
        kotlinx.coroutines.delay(timeMillis)
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}