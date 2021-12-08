package com.mcs.productphotography

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mcs.takesaveloadpic.ExternalStoragePhoto

class ListPhotosAdapter:
    ListAdapter<ExternalStoragePhoto, ListPhotosAdapter.PicViewHolder>(ListPhotosAdapter.PhotosComparator()) {
    class PicViewHolder(item:View) :RecyclerView.ViewHolder(item){
        //private val tv: TextView = item.findViewById(R.id.textDescription)
        private  val  photo:ImageView = item.findViewById(R.id.ivPhoto)

        fun bind(pic: Bitmap) {
            photo.setImageBitmap(pic)
            //tv.text="Ndabvuma-202002020202020202020202020200 0202020"
        }

        companion object {
            fun create(parent: ViewGroup): ListPhotosAdapter.PicViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_photo, parent, false)
                return ListPhotosAdapter.PicViewHolder(view)
            }
        }
    }

    class PhotosComparator : DiffUtil.ItemCallback<ExternalStoragePhoto>(){
        override fun areItemsTheSame(
            oldItem: ExternalStoragePhoto,
            newItem: ExternalStoragePhoto
        ): Boolean {
           return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ExternalStoragePhoto,
            newItem: ExternalStoragePhoto
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListPhotosAdapter.PicViewHolder {
       return PicViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ListPhotosAdapter.PicViewHolder, position: Int) {
        val sharedStoragePhoto = getItem(position)
        holder.bind(sharedStoragePhoto.img)
    }
}