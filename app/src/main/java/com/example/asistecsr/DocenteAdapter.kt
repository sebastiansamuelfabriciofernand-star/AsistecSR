package com.example.asistecsr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DocenteAdapter(private var lista: List<DocenteProfile>) : RecyclerView.Adapter<DocenteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Enlazamos con los nuevos IDs definidos en tu item_docente_table.xml
        val txtIndex: TextView = view.findViewById(R.id.txtIndex)
        val imgAvatarDocente: ImageView = view.findViewById(R.id.imgAvatarDocente)
        val txtNombre: TextView = view.findViewById(R.id.txtNombre)
        val txtIdDocente: TextView = view.findViewById(R.id.txtIdDocente)
        val txtClase: TextView = view.findViewById(R.id.txtClase)
        val txtCorreo: TextView = view.findViewById(R.id.txtCorreo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_docente_table, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val d = lista[position]

        // 1. Número correlativo (#) en la tabla (empieza en 1)
        holder.txtIndex.text = (position + 1).toString()

        // 2. Nombre completo (Usa nombresApellidos en camelCase)
        holder.txtNombre.text = d.nombresApellidos

        // 3. ID único debajo del nombre (Usa idDocente en camelCase)
        holder.txtIdDocente.text = "ID: ${d.idDocente}"

        // 4. Especialidad / Clase (Muestra la especialidad del profesor)
        holder.txtClase.text = d.especialidad

        // 5. Correo institucional (Usa correoInst en camelCase)
        holder.txtCorreo.text = d.correoInst
    }

    override fun getItemCount() = lista.size

    fun update(nueva: List<DocenteProfile>) {
        lista = nueva
        notifyDataSetChanged()
    }
}