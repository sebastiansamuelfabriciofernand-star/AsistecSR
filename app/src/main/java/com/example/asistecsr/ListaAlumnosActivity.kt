package com.example.asistecsr

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.jan.supabase.postgrest.from // 👈 CORRECCIÓN: Importación correcta
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ListaAlumnosActivity : AppCompatActivity() {

    private lateinit var recyclerAlumnos: RecyclerView
    private lateinit var txtTotal: TextView
    private lateinit var txtActivos: TextView
    private lateinit var txtInactivos: TextView
    private lateinit var edtBuscarAlumno: EditText

    private var listaCompletaAlumnos: List<EstudianteModel> = listOf()
    private var listaFiltradaGlobal: List<EstudianteModel> = listOf()
    private var carreraFiltrada: String? = null
    private var turnoFiltrado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alumnos)

        recyclerAlumnos = findViewById(R.id.recyclerAlumnos)
        txtTotal = findViewById(R.id.txtTotalAlumnosContador)
        txtActivos = findViewById(R.id.txtAlumnosActivosContador)
        txtInactivos = findViewById(R.id.txtAlumnosInactivosContador)
        edtBuscarAlumno = findViewById(R.id.edtBuscarAlumno)
        val txtTitulo = findViewById<TextView>(R.id.txtTituloListaAlumnos)

        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCiclo)
        findViewById<ImageView>(R.id.btnAtrasListaAlumnos).setupClickAnimation { finish() }

        recyclerAlumnos.layoutManager = LinearLayoutManager(this)

        val ciclos = listOf("Todos", "1", "2", "3", "4", "5", "6")

        spinnerCiclo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ciclos)

        val listenerFiltros = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                aplicarFiltrosLocales()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerCiclo.onItemSelectedListener = listenerFiltros

        // Manejar filtros iniciales si vienen de la pantalla de selección
        carreraFiltrada = intent.getStringExtra("EXTRA_CARRERA")
        turnoFiltrado = intent.getStringExtra("EXTRA_TURNO")
        
        if (carreraFiltrada != null) {
            val subtitulo = if (turnoFiltrado != null) "$carreraFiltrada ($turnoFiltrado)" else carreraFiltrada
            txtTitulo.text = subtitulo
        }

        edtBuscarAlumno.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltrosLocales()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarTodosLosAlumnosDeBaseDeDatos()

        findViewById<View>(R.id.btnExportarListaAlumnos).setupClickAnimation {
            if (listaFiltradaGlobal.isEmpty()) {
                Toast.makeText(this, "No hay alumnos para exportar", Toast.LENGTH_SHORT).show()
            } else {
                generarReporteAlumnosWord()
            }
        }
    }

    private fun generarReporteAlumnosWord() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Calendar.getInstance().time)
                val nombreArchivo = "Lista_Alumnos_${carreraFiltrada?.replace(" ", "_") ?: "General"}_$timestamp.doc"
                
                val contenidoHtml = buildString {
                    append("<html><head><meta charset='UTF-8'></head><body>")
                    append("<h2 style='text-align: center; color: #1A237E;'>LISTA DE ESTUDIANTES</h2>")
                    append("<p><b>Carrera:</b> ${carreraFiltrada ?: "Todas"}</p>")
                    append("<p><b>Turno:</b> ${turnoFiltrado ?: "Todos"}</p>")
                    append("<hr/>")
                    append("<table border='1' style='width: 100%; border-collapse: collapse;'>")
                    append("<tr style='background-color: #1A237E; color: white;'>")
                    append("<th style='padding: 8px;'>N°</th>")
                    append("<th style='padding: 8px;'>DNI</th>")
                    append("<th style='padding: 8px;'>Apellidos y Nombres</th>")
                    append("<th style='padding: 8px;'>Ciclo</th>")
                    append("<th style='padding: 8px;'>Estado</th>")
                    append("</tr>")

                    listaFiltradaGlobal.forEachIndexed { index, item ->
                        append("<tr>")
                        append("<td style='padding: 8px; text-align: center;'>${index + 1}</td>")
                        append("<td style='padding: 8px;'>${item.dni}</td>")
                        append("<td style='padding: 8px;'>${item.apellidos} ${item.nombres}</td>")
                        append("<td style='padding: 8px; text-align: center;'>${item.ciclo}</td>")
                        val textoEstado = if (item.estado) "ACTIVO" else "INACTIVO"
                        append("<td style='padding: 8px; text-align: center;'>$textoEstado</td>")
                        append("</tr>")
                    }

                    append("</table>")
                    append("<br/>")
                    append("<p style='text-align: right;'><b>Total de Alumnos:</b> ${listaFiltradaGlobal.size}</p>")
                    append("<p style='margin-top: 30px; font-size: 10px; color: gray;'>Generado por AsisteCSR - ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)}</p>")
                    append("</body></html>")
                }

                val uriResult = saveFileToDownloads(nombreArchivo, contenidoHtml)

                withContext(Dispatchers.Main) {
                    if (uriResult != null) {
                        mostrarOpcionesDeArchivo(uriResult, nombreArchivo)
                    } else {
                        Toast.makeText(this@ListaAlumnosActivity, "Error al guardar el archivo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ListaAlumnosActivity, "❌ Error al generar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarOpcionesDeArchivo(uri: Uri, nombre: String) {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_reporte, null)

        view.findViewById<TextView>(R.id.txtTituloSheet).text = "Lista Exportada"
        view.findViewById<TextView>(R.id.txtSubtituloSheet).text = nombre

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

        view.findViewById<View>(R.id.btnCompartirReporte).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/msword"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Compartir lista vía..."))
            bottomSheet.dismiss()
        }

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
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeText(content, Charsets.UTF_8)
            android.media.MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null, null)
            uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        }
        return uri
    }

    private fun cargarTodosLosAlumnosDeBaseDeDatos() {
        lifecycleScope.launch {
            try {
                // CORRECCIÓN: Uso de la sintaxis moderna .from()
                val lista = SupabaseManager.client.from("Estudiantes").select {
                    order("Apellidos", Order.ASCENDING)
                }.decodeList<EstudianteModel>()

                listaCompletaAlumnos = lista
                // Importante: Llamar a aplicarFiltrosLocales() después de cargar para que el
                // filtro inicial (si existe) se aplique a los datos recién descargados.
                aplicarFiltrosLocales()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ListaAlumnosActivity, "Error BD: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun aplicarFiltrosLocales() {
        val spinnerCiclo = findViewById<Spinner>(R.id.spinnerCiclo)

        val cicloSeleccionado = spinnerCiclo.selectedItem.toString()
        val textoBusqueda = edtBuscarAlumno.text.toString().lowercase(Locale.getDefault()).trim()

        var listaFiltrada = listaCompletaAlumnos

        if (carreraFiltrada != null) {
            listaFiltrada = listaFiltrada.filter { it.carrera.equals(carreraFiltrada, ignoreCase = true) }
        }

        if (turnoFiltrado != null) {
            listaFiltrada = listaFiltrada.filter { it.turno.equals(turnoFiltrado, ignoreCase = true) }
        }

        // CORRECCIÓN: Se cambió "cycleSeleccionado" por "cicloSeleccionado"
        if (cicloSeleccionado != "Todos") {
            val cicloInt = cicloSeleccionado.toIntOrNull()
            if (cicloInt != null) {
                listaFiltrada = listaFiltrada.filter { it.ciclo == cicloInt }
            }
        }

        if (textoBusqueda.isNotEmpty()) {
            listaFiltrada = listaFiltrada.filter { alumno ->
                (alumno.nombres?.lowercase(Locale.getDefault())?.contains(textoBusqueda) ?: false) ||
                        (alumno.apellidos?.lowercase(Locale.getDefault())?.contains(textoBusqueda) ?: false)
            }
        }

        actualizarContadoresYLista(listaFiltrada)
    }

    private fun actualizarContadoresYLista(lista: List<EstudianteModel>) {
        listaFiltradaGlobal = lista
        val total = lista.size
        val activos = lista.count { it.estado }
        val inactivos = total - activos

        txtTotal.text = getString(R.string.formato_total_alumnos, total)
        txtActivos.text = getString(R.string.formato_activos, activos)
        txtInactivos.text = getString(R.string.formato_inactivos, inactivos)

        recyclerAlumnos.adapter = AlumnoAdapter(lista) { alumno, nuevoEstado ->
            actualizarEstadoAlumno(alumno, nuevoEstado)
        }
    }

    private fun actualizarEstadoAlumno(alumno: EstudianteModel, nuevoEstado: Boolean) {
        lifecycleScope.launch {
            try {
                // 1. Actualizar en Supabase
                alumno.codigoQr?.let { id ->
                    SupabaseManager.client.from("Estudiantes").update(
                        mapOf("estado" to nuevoEstado)
                    ) {
                        filter { eq("codigoQr", id) }
                    }
                }

                // 2. Actualizar localmente en la lista completa para mantener sincronía
                listaCompletaAlumnos = listaCompletaAlumnos.map {
                    if (it.codigoQr == alumno.codigoQr) it.copy(estado = nuevoEstado) else it
                }
                
                // 3. Re-aplicar filtros para refrescar contadores y vista
                aplicarFiltrosLocales()
                
                Toast.makeText(this@ListaAlumnosActivity, "Estado actualizado", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ListaAlumnosActivity, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                // Si falla, podríamos re-notificar al adapter para que regrese el switch a su posición
                aplicarFiltrosLocales()
            }
        }
    }
}