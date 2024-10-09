package com.example.desafiodsm3.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.desafiodsm3.R
import com.example.desafiodsm3.model.Recurso
import android.content.Intent
import android.net.Uri

class ResourceAdapter(
    private var recursos: List<Recurso>,
    private val onEditClick: (Recurso) -> Unit,
    private val onDeleteClick: (Recurso) -> Unit
) : RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder>() {

    class ResourceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val linkTextView: TextView = itemView.findViewById(R.id.linkTextView)
        val editButton: View = itemView.findViewById(R.id.btnEdit)
        val deleteButton: View = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recurso, parent, false)
        return ResourceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        val recurso = recursos[position]
        holder.titleTextView.text = recurso.titulo
        holder.descriptionTextView.text = recurso.descripcion

        Glide.with(holder.itemView.context)
            .load(recurso.imagen)
            .into(holder.imageView)

        holder.linkTextView.text = "Enlace al recurso"
        holder.linkTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recurso.enlace))
            holder.itemView.context.startActivity(intent)
        }

        holder.editButton.setOnClickListener {
            onEditClick(recurso)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(recurso)
        }
    }

    override fun getItemCount() = recursos.size

    fun updateRecursos(newRecursos: List<Recurso>) {
        recursos = newRecursos
        notifyDataSetChanged()
    }
}