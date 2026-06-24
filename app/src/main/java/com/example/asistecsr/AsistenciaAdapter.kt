package com.example.asistecsr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AsistenciaAdapter(private var lista: ArrayList<AlumnoAsistencia>) :
    RecyclerView.Adapter<AsistenciaAdapter.AsistenciaViewHolder>() {

    class AsistenciaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNumero: TextView = itemView.findViewById(R.id.txtNumeroOrden)
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombreAlumno)
        val txtId: TextView = itemView.findViewById(R.id.txtIdAlumno)
        val txtMateria: TextView = itemView.findViewById(R.id.txtMateria)
        val btnEstado: TextView = itemView.findViewById(R.id.btnEstadoAsistencia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsistenciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alumno_asistencia, parent, false)
        return AsistenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AsistenciaViewHolder, position: Int) {
        val alumno = lista[position]

        holder.txtNumero.text = alumno.numeroOrden.toString()
        holder.txtNombre.text = alumno.nombre
        holder.txtId.text = "ID: ${alumno.idAlumno}"
        holder.txtMateria.text = alumno.materia

        // Forzar a que el elemento sea interactivo ignorando las restricciones del XML
        holder.btnEstado.isClickable = true
        holder.btnEstado.isFocusable = true

        actualizarEstiloBoton(holder.btnEstado, alumno.asistio)

        // Evento de clic interactivo
        holder.btnEstado.setOnClickListener {
            alumno.asistio = !alumno.asistio
            actualizarEstiloBoton(holder.btnEstado, alumno.asistio)
        }
    }

    private fun actualizarEstiloBoton(btnEstado: TextView, asistio: Boolean) {
        val context = btnEstado.context
        if (asistio) {
            btnEstado.text = "✓ Asistió"
            btnEstado.setBackgroundResource(R.drawable.barra_progreso_clip)
            btnEstado.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_green_dark)
        } else {
            btnEstado.text = "✕ No asistió"
            btnEstado.setBackgroundResource(R.drawable.barra_progreso_clip)
            btnEstado.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.holo_red_dark)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<AlumnoAsistencia>) {
        this.lista.clear()
        this.lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}