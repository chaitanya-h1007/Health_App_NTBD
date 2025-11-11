package com.example.healtcareapp.UI

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healtcareapp.R
import com.example.healtcareapp.model.DoctorAdd

class DoctorAdapter(private val items: List<DoctorAdd>) :
    RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDoctorName)
        val tvAddress: TextView = view.findViewById(R.id.tvDoctorAddress)
        val tvRating: TextView = view.findViewById(R.id.tvDoctorRating)
        val tvSpec: TextView = view.findViewById(R.id.tvDoctorSpecialization)
        val tvDistance: TextView = view.findViewById(R.id.tvDoctorDistance)
        val btnMap: Button = view.findViewById(R.id.btnViewMap)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = items[position]
        holder.tvName.text = doctor.name
        holder.tvAddress.text = doctor.address
        holder.tvRating.text = "⭐ ${doctor.rating}"
        holder.tvSpec.text = "Specialization: ${doctor.specialization}"
        holder.tvDistance.text = "📍 ${doctor.distance}"

        holder.btnMap.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(doctor.mapsUrl))
            mapIntent.setPackage("com.google.android.apps.maps")
            holder.itemView.context.startActivity(mapIntent)
        }
    }

    override fun getItemCount(): Int = items.size
}
