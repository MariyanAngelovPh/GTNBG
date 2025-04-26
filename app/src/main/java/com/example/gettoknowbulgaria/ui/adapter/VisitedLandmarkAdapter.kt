package com.example.gettoknowbulgaria.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gettoknowbulgaria.R
import com.example.gettoknowbulgaria.data.VisitedLandmark

class VisitedLandmarkAdapter(
    private var landmarks: List<VisitedLandmark>  // ✅ mutable
) : RecyclerView.Adapter<VisitedLandmarkAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageViewLandmark)
        val nameTextView: TextView = view.findViewById(R.id.textViewLandmarkName)
        val dateTextView: TextView = view.findViewById(R.id.textViewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_visited_landmark, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val landmark = landmarks[position]
        holder.nameTextView.text = landmark.landmark_name
        holder.dateTextView.text = landmark.dateVisited

        Glide.with(holder.itemView.context)
            .load(landmark.imagePath)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = landmarks.size

    fun updateList(newLandmarks: List<VisitedLandmark>) {  // ✅ добавяме updateList
        landmarks = newLandmarks
        notifyDataSetChanged()
    }
}