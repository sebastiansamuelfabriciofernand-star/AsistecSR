package com.example.asistecsr

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CursoAdapter(
    private val listaCursos: List<CursoModel>,
    private val idDocente: String,
    private val activity: SeleccionCursoActivity
) : RecyclerView.Adapter<CursoAdapter.CursoViewHolder>() {

    // Colores para los bordes (estilo neón sutil)
    private val coloresBorde = listOf(
        "#FF4D4D", // Rojo
        "#00ADEF", // Azul
        "#39FF14", // Verde
        "#FFD700", // Amarillo
        "#FF00FF", // Magenta
        "#00FFFF"  // Cyan
    )

    inner class CursoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNombreCurso: TextView = itemView.findViewById(R.id.txtNombreCurso)
        val txtCiclo: TextView = itemView.findViewById(R.id.txtCiclo)
        val txtTurno: TextView = itemView.findViewById(R.id.txtTurno)
        val layoutRaiz: View = itemView.findViewById(R.id.rootItemCurso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curso, parent, false)
        return CursoViewHolder(view)
    }

    override fun getItemCount(): Int = listaCursos.size

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        val curso = listaCursos[position]

        holder.txtNombreCurso.text = curso.nombreCurso ?: "Sin nombre"
        holder.txtCiclo.text = "CICLO: ${curso.ciclo ?: 0}"
        holder.txtTurno.text = "TURNO: ${curso.turno ?: "DIURNO"}"

        // Aplicar color de borde dinámico
        val colorHex = coloresBorde[position % coloresBorde.size]
        val background = holder.layoutRaiz.background as? GradientDrawable
        background?.setStroke(2, Color.parseColor(colorHex))

        // Aplicar animación de presión moderna
        holder.layoutRaiz.setupClickAnimation {
            val intent = Intent(activity, EscanerQrActivity::class.java)
            intent.putExtra("EXTRA_ID_DOCENTE", idDocente)
            intent.putExtra("EXTRA_ID_CURSO", curso.idCurso ?: "")
            intent.putExtra("EXTRA_CICLO", curso.ciclo ?: 0)
            intent.putExtra("EXTRA_TURNO", curso.turno ?: "DIURNO")
            activity.startActivity(intent)
            activity.finish()
        }
    }
}