package com.example.android.marsphotos.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.marsphotos.databinding.GridViewItemBinding
import com.example.android.marsphotos.network.Product

class ProductGridAdapter(
    private val overviewFragment: OverviewFragment
) : ListAdapter<Product,
        ProductGridAdapter.MarsPhotoViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            TODO("Not yet implemented")
            return (oldItem.name == newItem.name && oldItem.descriptions == newItem.descriptions)
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            TODO("Not yet implemented")
            return (oldItem.name == newItem.name && oldItem.descriptions == newItem.descriptions)
        }
    }

    class MarsPhotoViewHolder(private var binding: GridViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.product = product
            binding.executePendingBindings()
        }
        val view = binding.itemLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarsPhotoViewHolder {
        return MarsPhotoViewHolder(
            GridViewItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: MarsPhotoViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
        holder.view. setOnClickListener {
            overviewFragment.selectProduct(product)
        }
    }
}