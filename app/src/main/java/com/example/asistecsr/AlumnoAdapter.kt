package com.example.asistecsr

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlumnoAdapter(private val lista: List<EstudianteModel>) :
    RecyclerView.Adapter<AlumnoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNum: TextView = view.findViewById(R.id.txtNumeroOrden)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreAlumno)
        val txtId: TextView = view.findViewById(R.id.txtIdAlumno)
        val txtCarrera: TextView = view.findViewById(R.id.txtCarrera)
        val txtCiclo: TextView = view.findViewById(R.id.txtCiclo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alumno_tabla, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alumno = lista[position]
        val context = holder.itemView.context

        holder.txtNum.text = (position + 1).toString()
        holder.txtNombre.text = "${alumno.nombres} ${alumno.apellidos}"
        holder.txtId.text = context.getString(R.string.formato_id_alumno, alumno.dni)
        holder.txtCarrera.text = alumno.carrera.uppercase()
        holder.txtCiclo.text = context.getString(R.string.formato_ciclo_alumno, alumno.ciclo.toString())

        if (alumno.estado) {
            holder.txtNombre.setTextColor(Color.WHITE)
        } else {
            holder.txtNombre.setTextColor(Color.GRAY)
        }
    }

    override fun getItemCount() = lista.size
}