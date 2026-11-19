
package com.example.agricultural

import androidx.core.net.toUri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class FarmPhotoAdapter(
    private var photos: List<String>
) : RecyclerView.Adapter<FarmPhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgFarmGallery)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_farm_photo, parent, false)

        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PhotoViewHolder,
        position: Int
    ) {
        try {
            holder.image.setImageURI(
                photos[position].toUri()
            )

            if (holder.image.drawable == null) {
                holder.image.setImageResource(
                    R.drawable.farm_placeholder
                )
            }
        } catch (_: Exception) {
            holder.image.setImageResource(
                R.drawable.farm_placeholder
            )
        }
    }

    override fun getItemCount(): Int = photos.size

}
