package com.example.asistecsr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
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
    private lateinit var txtContador: TextView

    private var cicloFiltro: Int = -1
    private var turnoFiltro: String = ""
    private var idDocenteActual: String = ""
    private var idCursoActual: String = ""
    private var nombreCursoActual: String = ""
    private var isScanning = true
    private var contadorSesion: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escaner_qr)

        // 1. Recibir parámetros enviados desde el Perfil del Docente
        cicloFiltro = intent.getIntExtra("EXTRA_CICLO",1)
        turnoFiltro = intent.getStringExtra("EXTRA_TURNO") ?: "DIURNO"

        idDocenteActual = intent.getStringExtra("EXTRA_ID_DOCENTE") ?: ""
        idCursoActual = intent.getStringExtra("EXTRA_ID_CURSO") ?: ""

        previewView = findViewById(R.id.previewViewCamara)
        txtContador = findViewById(R.id.txtContadorAsistencias)
        findViewById<ImageView>(R.id.btnAtrasEscaner).setupClickAnimation { finish() }

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
                // Validación para asegurar de que el ID del docente e ID del curso no viajen vacíos
                if (idDocenteActual.isEmpty() || idCursoActual.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EscanerQrActivity, "Error: No se obtuvo el ID del docente o curso", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    return@launch
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val estudianteResponse = SupabaseManager.client
                    .from("Estudiantes")
                    .select {
                        filter {
                            eq("codigoQr", valorQrEscaneado)
                        }
                    }

                val listaEstudiantes = estudianteResponse.decodeList<EstudianteModel>()

                if (listaEstudiantes.isEmpty()) {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EscanerQrActivity,
                            "❌ QR no pertenece a ningún estudiante",
                            Toast.LENGTH_LONG
                        ).show()

                        isScanning = true
                    }

                    return@launch
                }
                val estudiante = listaEstudiantes.first()
                val fechaHoy = sdf.format(Calendar.getInstance().time)
                val asistenciaExistente = SupabaseManager.client
                    .from("Asistencia")
                    .select {
                        filter {
                            eq("codigoQr", valorQrEscaneado)
                            eq("fecha", fechaHoy)
                            eq("id_curso", idCursoActual)
                        }
                    }

                val listaAsistencia = asistenciaExistente.decodeList<AsistenciaModel>()

                if (listaAsistencia.isNotEmpty()) {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EscanerQrActivity,
                            "⚠ Este estudiante ya registró asistencia hoy.",
                            Toast.LENGTH_LONG
                        ).show()

                        isScanning = true
                    }

                    return@launch
                }


                // CORRECCIÓN DEFINITIVA basada en image_85d368.png (Mapeo completo de columnas)
                val datosAsistencia = buildJsonObject {
                    put("fecha", fechaHoy)
                    put("codigoQr", valorQrEscaneado)
                    put("id_docente", idDocenteActual)
                    put("id_curso", idCursoActual)
                    put("ciclo", cicloFiltro)    // 👈 Agregado según tu esquema físico
                    put("turno", turnoFiltro)    // 👈 Agregado según tu esquema físico
                }

                // Inserción limpia en la tabla "Asistencia"
                SupabaseManager.client.from("Asistencia").insert(datosAsistencia)

                contadorSesion++

                withContext(Dispatchers.Main) {
                    txtContador.text = "Asistencias: $contadorSesion"

                    Toast.makeText(
                        this@EscanerQrActivity,
                        "✅ ${estudiante.nombres} ${estudiante.apellidos}\nAsistencia registrada",
                        Toast.LENGTH_LONG
                    ).show()
                    // Esperar 2 segundos antes de volver a activar la cámara
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