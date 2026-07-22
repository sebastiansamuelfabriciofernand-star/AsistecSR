package com.example.asistecsr

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class AlumnoAdapter(
    private val lista: List<EstudianteModel>,
    private val onEstadoChanged: (EstudianteModel, Boolean) -> Unit
) : RecyclerView.Adapter<AlumnoAdapter.AlumnoViewHolder>() {

    class AlumnoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNum: TextView = view.findViewById(R.id.txtNumeroOrden)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreAlumno)
        val txtId: TextView = view.findViewById(R.id.txtIdAlumno)
        val txtCarrera: TextView = view.findViewById(R.id.txtCarrera)
        val txtCiclo: TextView = view.findViewById(R.id.txtCiclo)
        val swEstado: SwitchCompat = view.findViewById(R.id.swEstadoAlumno)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_alumno_tabla, parent, false)
        return AlumnoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        val alumno = lista[position]
        val context = holder.itemView.context

        // Conversión limpia a String nativa de Kotlin sin recurrir a Java ni a concatenaciones crudas
        val numeroOrden = (position + 1).toString()
        holder.txtNum.text = numeroOrden

        // Interpolación nativa limpia que cumple las especificaciones del IDE
        val nombreCompleto = "${alumno.nombres} ${alumno.apellidos}"
        holder.txtNombre.text = nombreCompleto

        holder.txtId.text = context.getString(R.string.formato_id_alumno, alumno.dni)
        holder.txtCarrera.text = alumno.carrera?.uppercase() ?: ""

        val cicloTexto = alumno.ciclo.toString()
        holder.txtCiclo.text = context.getString(R.string.formato_ciclo_alumno, cicloTexto)

        // Configurar Switch
        holder.swEstado.setOnCheckedChangeListener(null) // Evitar disparos accidentales
        holder.swEstado.isChecked = alumno.estado
        
        if (alumno.estado) {
            holder.txtNombre.setTextColor(Color.WHITE)
        } else {
            holder.txtNombre.setTextColor(Color.GRAY)
        }

        holder.swEstado.setOnCheckedChangeListener { _, isChecked ->
            onEstadoChanged(alumno, isChecked)
            if (isChecked) {
                holder.txtNombre.setTextColor(Color.WHITE)
            } else {
                holder.txtNombre.setTextColor(Color.GRAY)
            }
        }
    }

    override fun getItemCount(): Int = lista.size
}