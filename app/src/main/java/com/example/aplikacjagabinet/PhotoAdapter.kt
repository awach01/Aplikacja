package com.example.aplikacjagabinet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class Photo(
    val userId: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp? = null
)

class PhotoAdapter(
    private val context: Context,
    private val photos: List<Photo>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]


        Glide.with(context)
            .load(photo.imageUrl)
            .into(holder.imageView)


        holder.dateTextView.text = formatTimestampToPolishTime(photo.timestamp)
    }

    override fun getItemCount(): Int {
        return photos.size
    }


    private fun formatTimestampToPolishTime(timestamp: Timestamp?): String {
        if (timestamp == null) return context.getString(R.string.Nodata)

        val date = timestamp.toDate()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("pl", "PL"))
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")
        return dateFormat.format(date)
    }
}
