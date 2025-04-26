package com.example.gettoknowbulgaria.ui

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
import com.example.gettoknowbulgaria.data.Landmark

class LandmarkSmallAdapter(
    private val context: Context,
    private var items: List<Landmark>
) : RecyclerView.Adapter<LandmarkSmallAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageViewSmall)
        val name: TextView = view.findViewById(R.id.textViewSmallName)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val landmark = items[position]
                    val intent = Intent(context, LandmarkDetailsActivity::class.java).apply {
                        putExtra("name", landmark.name)
                        putExtra("description", landmark.description)
                        putExtra("image_path", landmark.image_path)
                        putExtra("type", landmark.type)
                        putExtra("city", landmark.city)
                        putExtra("manicipality", landmark.manicipality)
                        putExtra("location", landmark.location)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_landmark_small, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val landmark = items[position]
        holder.name.text = landmark.name
        Glide.with(context)
            .load(landmark.image_path)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.image)
    }

    fun setData(newItems: List<Landmark>) {
        items = newItems
        notifyDataSetChanged()
    }
}
