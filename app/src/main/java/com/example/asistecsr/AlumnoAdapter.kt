package com.example.asistecsr

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlumnoAdapter(private val lista: List<UserProfile>) :
    RecyclerView.Adapter<AlumnoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNum: TextView = view.findViewById(R.id.txtNumeroOrden)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreAlumno)
        val txtId: TextView = view.findViewById(R.id.txtIdAlumno)
        val txtCarrera: TextView = view.findViewById(R.id.txtCarrera)
        val txtCiclo: TextView = view.findViewById(R.id.txtCiclo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alumno_tabla, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = lista[position]

        // 1. Asignamos los textos originales (sin añadir etiquetas extra que rompan el diseño)
        holder.txtNum.text = (position + 1).toString()
        holder.txtNombre.text = "${user.nombres} ${user.apellidos}"
        holder.txtId.text = "ID: ${user.dni}"
        holder.txtCarrera.text = (user.carrera ?: "").uppercase()
        holder.txtCiclo.text = "Ciclo: ${user.ciclo ?: "-"}"

        // 2. Aplicamos la lógica de colores SIN romper el diseño original
        if (user.estado) {
            // ALUMNO ACTIVO: Restauramos los colores exactos de tu XML
            holder.txtNum.setTextColor(Color.parseColor("#8A8A9E"))
            holder.txtNombre.setTextColor(Color.parseColor("#FFFFFF"))
            holder.txtId.setTextColor(Color.parseColor("#8A8A9E"))
            holder.txtCarrera.setTextColor(Color.parseColor("#E65100"))
            holder.txtCiclo.setTextColor(Color.parseColor("#8A8A9E"))
        } else {
            // ALUMNO INACTIVO: Usamos un tono de gris para que parezca "desactivado"
            // Mantenemos la estructura pero bajamos la intensidad del color
            val colorInactivo = Color.parseColor("#5A5A5A")
            holder.txtNum.setTextColor(colorInactivo)
            holder.txtNombre.setTextColor(colorInactivo)
            holder.txtId.setTextColor(colorInactivo)
            holder.txtCarrera.setTextColor(colorInactivo)
            holder.txtCiclo.setTextColor(colorInactivo)
        }
    }

    override fun getItemCount() = lista.size
}