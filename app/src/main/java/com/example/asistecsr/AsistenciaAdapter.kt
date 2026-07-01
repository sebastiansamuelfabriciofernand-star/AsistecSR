package com.example.asistecsr

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AsistenciaAdapter(private val lista: List<AsistenciaModel>) :
    RecyclerView.Adapter<AsistenciaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNum: TextView = view.findViewById(R.id.txtNumeroOrden)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreAlumno)
        val txtId: TextView = view.findViewById(R.id.txtIdAlumno)
        val txtMateria: TextView = view.findViewById(R.id.txtMateria)
        val btnEstado: TextView = view.findViewById(R.id.btnEstadoAsistencia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alumno_asistencia, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registro = lista[position]
        val context = holder.itemView.context

        holder.txtNum.text = (position + 1).toString()
        holder.txtNombre.text = "${registro.nombres ?: "Alumno"} ${registro.apellidos ?: "Desconocido"}"
        holder.txtId.text = context.getString(R.string.formato_id_alumno, registro.dniAlumno ?: "---")
        holder.txtMateria.text = registro.clase ?: "Sin Clase"

        // Configuración dinámica del texto y color según el estado del registro de asistencia
        val estado = registro.estadoAsistencia?.uppercase() ?: "ASISTIO"
        when (estado) {
            "ASISTIO" -> {
                holder.btnEstado.text = "✓ Asistió"
                holder.btnEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")) // Verde
            }
            "TARDANZA" -> {
                holder.btnEstado.text = "🕒 Tardanza"
                holder.btnEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EF6C00")) // Naranja
            }
            else -> { // FALTO
                holder.btnEstado.text = "✕ Faltó"
                holder.btnEstado.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#C62828")) // Rojo
            }
        }
    }

    override fun getItemCount() = lista.size
}