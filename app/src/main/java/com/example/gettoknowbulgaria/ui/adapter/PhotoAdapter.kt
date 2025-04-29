package com.example.gettoknowbulgaria.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gettoknowbulgaria.R

class PhotoAdapter(private val photoList: MutableList<PhotoItem>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPhoto)
        val uploaderView: TextView = itemView.findViewById(R.id.textViewUploader)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photoList[position]
        Glide.with(holder.itemView.context)
            .load(photo.imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_background) // ще се покаже ако не успее
            .into(holder.imageView)

        holder.uploaderView.text = "Качено от: ${photo.uploader}"

        Log.d("PhotoAdapter", "URL: ${photo.imageUrl}")

    }

    override fun getItemCount() = photoList.size


    fun updateData(newPhotos: List<PhotoItem>) {
        photoList.clear()
        photoList.addAll(newPhotos)
        notifyDataSetChanged()
    }


}
