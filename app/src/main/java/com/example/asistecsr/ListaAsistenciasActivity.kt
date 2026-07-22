package com.example.asistecsr

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ListaAsistenciasActivity : AppCompatActivity() {

    private lateinit var rvListaAsistencias: RecyclerView
    private lateinit var txtFechaSeleccionada: TextView

    private var cicloFiltro: Int = -1
    private var turnoFiltro: String = ""
    private var fechaSeleccionada: String = ""
    private var listaGlobalAsistencias: List<AsistenciaModel> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_asistencias)

        // 1. Recibir parámetros enviados desde la pantalla del Profesor
        cicloFiltro = intent.getIntExtra("EXTRA_CICLO", -1)
        turnoFiltro = (intent.getStringExtra("EXTRA_TURNO") ?: "").uppercase(Locale.getDefault()).trim()

        // 2. Inicializar vistas vinculadas exactamente a tu XML
        rvListaAsistencias = findViewById(R.id.rvListaAsistencias)
        txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada)

        // CORRECCIÓN: Contexto explícito de la Activity para el LayoutManager
        rvListaAsistencias.layoutManager = LinearLayoutManager(this@ListaAsistenciasActivity)

        findViewById<View>(R.id.btnAtrasAsistencias).setupClickAnimation {
            finish()
        }

        // 3. Configurar fecha actual por defecto (Formato YYYY-MM-DD)
        val calendario = Calendar.getInstance()
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaSeleccionada = formatoFecha.format(calendario.time)

        txtFechaSeleccionada.text = getString(R.string.formato_fecha_mostrar, fechaSeleccionada)

        // 4. Selector de fecha integrado al hacer clic sobre el TextView
        txtFechaSeleccionada.setOnClickListener {
            DatePickerDialog(
                this@ListaAsistenciasActivity,
                { _, yearSelected, monthSelected, daySelected ->
                    calendario.set(Calendar.YEAR, yearSelected)
                    calendario.set(Calendar.MONTH, monthSelected)
                    calendario.set(Calendar.DAY_OF_MONTH, daySelected)

                    fechaSeleccionada = formatoFecha.format(calendario.time)
                    txtFechaSeleccionada.text = getString(R.string.formato_fecha_mostrar, fechaSeleccionada)

                    cargarHistorialAsistencias()
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Carga inicial automática de la lista
        cargarHistorialAsistencias()

        findViewById<View>(R.id.btnDescargarAsistencias).setupClickAnimation {
            if (listaGlobalAsistencias.isEmpty()) {
                Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
            } else {
                generarReporteWord()
            }
        }
    }

    private fun generarReporteWord() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val nombreArchivo = "Reporte_Asistencia_${fechaSeleccionada.replace("-", "_")}.doc"
                val contenidoHtml = buildString {
                    append("<html><head><meta charset='UTF-8'></head><body>")
                    append("<h2 style='text-align: center; color: #1A237E;'>REPORTE DE ASISTENCIA</h2>")
                    append("<p><b>Fecha del Reporte:</b> $fechaSeleccionada</p>")
                    append("<p><b>Ciclo:</b> $cicloFiltro | <b>Turno:</b> $turnoFiltro</p>")
                    append("<hr/>")
                    append("<table border='1' style='width: 100%; border-collapse: collapse;'>")
                    append("<tr style='background-color: #1A237E; color: white;'>")
                    append("<th style='padding: 8px;'>N°</th>")
                    append("<th style='padding: 8px;'>DNI</th>")
                    append("<th style='padding: 8px;'>Estudiante</th>")
                    append("<th style='padding: 8px;'>Clase</th>")
                    append("<th style='padding: 8px;'>Estado</th>")
                    append("</tr>")

                    listaGlobalAsistencias.forEachIndexed { index, item ->
                        append("<tr>")
                        append("<td style='padding: 8px; text-align: center;'>${index + 1}</td>")
                        append("<td style='padding: 8px;'>${item.dniAlumno ?: "---"}</td>")
                        append("<td style='padding: 8px;'>${item.apellidos ?: ""} ${item.nombres ?: ""}</td>")
                        append("<td style='padding: 8px;'>${item.clase ?: "---"}</td>")
                        append("<td style='padding: 8px; text-align: center;'>${item.estadoAsistencia ?: "ASISTIÓ"}</td>")
                        append("</tr>")
                    }

                    append("</table>")
                    append("<br/>")
                    append("<p style='text-align: right;'><b>Total de Alumnos:</b> ${listaGlobalAsistencias.size}</p>")
                    append("<p style='margin-top: 50px; border-top: 1px solid black; width: 200px; text-align: center;'>Firma del Docente</p>")
                    append("<p style='margin-top: 30px; font-size: 10px; color: gray;'>Generado por AsisteCSR - ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)}</p>")
                    append("</body></html>")
                }

                val uriResult = saveFileToDownloads(nombreArchivo, contenidoHtml)

                withContext(Dispatchers.Main) {
                    if (uriResult != null) {
                        mostrarOpcionesDeArchivo(uriResult, nombreArchivo)
                    } else {
                        Toast.makeText(this@ListaAsistenciasActivity, "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ListaAsistenciasActivity, "❌ Error al generar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarOpcionesDeArchivo(uri: Uri, nombre: String) {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_reporte, null)

        view.findViewById<TextView>(R.id.txtSubtituloSheet).text = nombre

        // Acción Abrir
        view.findViewById<View>(R.id.btnAbrirReporte).setOnClickListener {
            val openIntent = Intent(Intent.ACTION_VIEW)
            openIntent.setDataAndType(uri, "application/msword")
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(openIntent)
            } catch (ex: Exception) {
                Toast.makeText(this, "No hay una aplicación para abrir este archivo", Toast.LENGTH_SHORT).show()
            }
            bottomSheet.dismiss()
        }

        // Acción Compartir
        view.findViewById<View>(R.id.btnCompartirReporte).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/msword"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Compartir reporte vía..."))
            bottomSheet.dismiss()
        }

        // Acción Cerrar
        view.findViewById<View>(R.id.btnCerrarSheet).setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun saveFileToDownloads(fileName: String, content: String): Uri? {
        val resolver = contentResolver
        var uri: Uri? = null
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/msword")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                }
            }
        } else {
            // Para versiones antiguas (API < 29)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeText(content, Charsets.UTF_8)
            
            // Registrar en MediaScanner
            android.media.MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null, null)
            
            // Usar FileProvider para compartir en versiones antiguas
            uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        }
        return uri
    }

    private fun cargarHistorialAsistencias() {
        if (cicloFiltro == -1 || turnoFiltro.isEmpty()) {
            Toast.makeText(this@ListaAsistenciasActivity, getString(R.string.error_filtros_invalidos), Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val idDocenteLogueado = SupabaseManager.client.auth.currentUserOrNull()?.id

                if (idDocenteLogueado == null) {
                    Toast.makeText(this@ListaAsistenciasActivity, getString(R.string.error_sesion_docente), Toast.LENGTH_LONG).show()
                    return@launch
                }

                // 1. Consulta limpia apuntando a la tabla "Asistencia" en Supabase
                val asistenciasBase = SupabaseManager.client.from("Asistencia").select {
                    filter {
                        eq("ciclo", cicloFiltro)
                        eq("turno", turnoFiltro)
                        eq("fecha", fechaSeleccionada)
                        eq("id_docente", idDocenteLogueado)
                    }
                }.decodeList<AsistenciaModel>()

                if (asistenciasBase.isEmpty()) {
                    rvListaAsistencias.adapter = AsistenciaAdapter(emptyList())
                    Toast.makeText(this@ListaAsistenciasActivity, getString(R.string.sin_asistencias_hoy), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. Obtener datos de estudiantes para cruzar información (Join Manual)
                val listaEstudiantes = SupabaseManager.client.from("Estudiantes").select().decodeList<EstudianteModel>()

                // 3. Obtener datos de cursos para mostrar el nombre de la materia (Se mantiene lógica por si se requiere después)
                val listaCursos = SupabaseManager.client.from("Cursos").select().decodeList<CursoModel>()

                // 4. Cruzar información para llenar los campos "nombres", "apellidos", "dniAlumno" y "clase"
                val listaCompleta = asistenciasBase.map { asistencia ->
                    val estudiante = listaEstudiantes.find { it.codigoQr == asistencia.codigoQr }
                    val curso = listaCursos.find { it.idCurso == asistencia.idCurso }

                    asistencia.copy(
                        nombres = estudiante?.nombres,
                        apellidos = estudiante?.apellidos,
                        dniAlumno = estudiante?.dni,
                        clase = curso?.nombreCurso
                    )
                }

                // Vinculación con tu AsistenciaAdapter existente usando la lista enriquecida
                listaGlobalAsistencias = listaCompleta
                rvListaAsistencias.adapter = AsistenciaAdapter(listaGlobalAsistencias)

            } catch (e: Exception) {
                e.printStackTrace()
                val mensajeError = getString(R.string.error_traer_asistencias, e.localizedMessage ?: "")
                Toast.makeText(this@ListaAsistenciasActivity, mensajeError, Toast.LENGTH_LONG).show()
            }
        }
    }
}