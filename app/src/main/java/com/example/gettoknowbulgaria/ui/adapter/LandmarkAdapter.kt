package com.example.gettoknowbulgaria.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gettoknowbulgaria.R
import com.example.gettoknowbulgaria.ui.LandmarkDetailsActivity
import com.example.gettoknowbulgaria.data.Landmark

class LandmarkAdapter(
    private val context: Context,
    private var landmarkList: List<Landmark>
) : RecyclerView.Adapter<LandmarkAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewLandmark: ImageView = itemView.findViewById(R.id.imageViewLandmark)
        val textViewName: TextView = itemView.findViewById(R.id.textViewLandmarkName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_landmark, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = landmarkList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val landmark = landmarkList[position]
        holder.textViewName.text = landmark.name

        // Зареждане на изображение с Glide от imagePath
        Glide.with(context)
            .load(landmark.image_path) // <-- Увери се, че това е името в Landmark.kt
            .placeholder(R.drawable.ic_launcher_foreground) // по избор
            .error(R.drawable.ic_launcher_background) // ако няма снимка
            .into(holder.imageViewLandmark)

        // Преминаване към екран с детайли
        holder.itemView.setOnClickListener {
            val intent = Intent(context, LandmarkDetailsActivity::class.java)
            intent.putExtra("landmark_id", landmark.id)
            context.startActivity(intent)
        }
    }

    fun updateList(newList: List<Landmark>) {
        landmarkList = newList
        notifyDataSetChanged()
    }
}
