package com.mcs.productphotography

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProductListAdapter : ListAdapter<Product, ProductListAdapter.ProductViewHolder>(ProductsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.barcode,current.description,current.length,current.width,current.height,current.weight)
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descTV: TextView = itemView.findViewById(R.id.textDescription)
        private val barcodeTV: TextView = itemView.findViewById(R.id.textBarcode)
        private val dimensTV: TextView = itemView.findViewById(R.id.textDimensions)
        private val weightTV: TextView = itemView.findViewById(R.id.textWeight)

        fun bind(barcode: String?, desc: String, length: Double,width: Double, height: Double, weight:Double) {
            descTV.text = desc
            barcodeTV.text = barcode
            var dimens = "$length x $width x $height"
            dimensTV.text = dimens
            weightTV.text = weight.toString()
        }

        companion object {
            fun create(parent: ViewGroup): ProductViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return ProductViewHolder(view)
            }
        }
    }
    class ProductsComparator : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.barcode == newItem.barcode
        }
    }
}