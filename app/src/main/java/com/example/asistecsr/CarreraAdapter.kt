package com.example.asistecsr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarreraAdapter(
    private val listaCarreras: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CarreraAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.txtNombreCarrera)
        val imgIcono: ImageView = view.findViewById(R.id.imgIconoCarrera)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrera, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val carreraNombre = listaCarreras[position]
        holder.txtNombre.text = carreraNombre.uppercase()
        
        // Lógica para asignar iconos llamativos según la carrera
        val nombreLower = carreraNombre.lowercase()
        val iconoRes = when {
            nombreLower.contains("sistemas") -> android.R.drawable.ic_menu_preferences
            nombreLower.contains("medicina") -> android.R.drawable.ic_menu_myplaces
            nombreLower.contains("contabilidad") -> android.R.drawable.ic_menu_agenda
            nombreLower.contains("mecatronica") -> android.R.drawable.ic_menu_directions
            nombreLower.contains("electricidad") -> android.R.drawable.ic_menu_compass
            else -> R.drawable.ic_usuarios // Icono por defecto
        }
        
        holder.imgIcono.setImageResource(iconoRes)
        
        holder.itemView.setOnClickListener {
            onItemClick(carreraNombre)
        }
    }

    override fun getItemCount() = listaCarreras.size
}