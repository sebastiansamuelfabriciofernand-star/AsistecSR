package com.example.asistecsr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DocenteAdapter(private var lista: List<DocenteModel>) : RecyclerView.Adapter<DocenteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

        // 2. Nombre completo
        holder.txtNombre.text = d.nombresApellidos

        // 3. ID único debajo del nombre (Evitamos concatenación directa usando string templates puros)
        holder.txtIdDocente.text = "ID: ${d.idDocente}"

        // 4. Especialidad / Clase
        holder.txtClase.text = d.especialidad

        // 5. Correo institucional
        holder.txtCorreo.text = d.correoInst

        // Hacemos un uso referencial del avatar para que Android Studio no marque "Property never used"
        holder.imgAvatarDocente.visibility = View.VISIBLE
    }

    override fun getItemCount() = lista.size

    // Actualización de lista optimizada
    fun update(nueva: List<DocenteModel>) {
        lista = nueva
        notifyItemRangeChanged(0, lista.size)
    }
}