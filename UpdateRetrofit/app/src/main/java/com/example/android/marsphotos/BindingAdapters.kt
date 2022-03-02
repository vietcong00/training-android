package com.example.android.marsphotos

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.marsphotos.network.Product
import com.example.android.marsphotos.overview.MarsApiStatus
import com.example.android.marsphotos.overview.ProductGridAdapter

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView,
                     data: List<Product>?) {
    val adapter = recyclerView.adapter as ProductGridAdapter
    adapter.submitList(data)
}

@BindingAdapter("marsApiStatus")
fun bindStatus(statusImageView: ImageView,
               status: MarsApiStatus?) {
    when (status) {
        MarsApiStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
        MarsApiStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        MarsApiStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
    }
}

@BindingAdapter("nameProduct")
fun bindText(textView: TextView, name: String?) {
    textView.text = name
}

@BindingAdapter("idProduct")
fun bindText(textView: TextView, id: Int?) {
    textView.text = id.toString()
}