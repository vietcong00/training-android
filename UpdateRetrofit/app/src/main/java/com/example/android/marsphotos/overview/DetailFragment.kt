/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.marsphotos.overview

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.marsphotos.R
import com.example.android.marsphotos.databinding.FragmentDetailBinding
import com.example.android.marsphotos.databinding.FragmentOverviewBinding
import com.example.android.marsphotos.databinding.GridViewItemBinding
import com.example.android.marsphotos.network.Product
import com.example.android.marsphotos.pojo.CreateProductRequest
import com.example.android.marsphotos.pojo.UpdateProductRequest

/**
 * This fragment shows the the status of the Mars photos web services transaction.
 */
class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private val viewModel: OverviewViewModel by viewModels()
    private var idProduct = 0
    private val VALID = "valid"

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the OverviewFragment
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater)
//        val binding = GridViewItemBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the OverviewViewModel
        binding.viewModel = viewModel

        binding.apply {
            updateBtn.setOnClickListener {
                var name = inputName.text.toString()
                var valid = checkNull(name)
                if (valid === VALID) {
                    outlinedName.error = null
                } else {
                    outlinedName.error = valid
                }
                var descriptions = inputDescriptions.text.toString()
                valid = checkNull(descriptions)
                if (valid === VALID) {
                    outlinedName.error = null
                } else {
                    outlinedName.error = valid
                }
                var request = UpdateProductRequest(name = name, descriptions = descriptions)
                viewModel?.updateProduct(idProduct, request)
            }

            createBtn.setOnClickListener {
                var name = inputName.text.toString()
                var valid = checkNull(name)
                if (valid === VALID) {
                    outlinedName.error = null
                } else {
                    outlinedName.error = valid
                }
                var descriptions = inputDescriptions.text.toString()
                valid = checkNull(descriptions)
                if (valid === VALID) {
                    outlinedName.error = null
                } else {
                    outlinedName.error = valid
                }
                var request = CreateProductRequest(name = name, descriptions = descriptions)
                viewModel?.createProduct(request)
            }
            deleteBtn.setOnClickListener {
                viewModel?.deleteProduct(idProduct)
            }
        }
        return binding.root
    }

    fun setProduct(product: Product) {
        binding.apply {
            inputId.setText(product.id.toString(), TextView.BufferType.EDITABLE);
            inputName.setText(product.name, TextView.BufferType.EDITABLE);
            inputDescriptions.setText(product.descriptions, TextView.BufferType.EDITABLE);
            idProduct = product.id
        }

    }

    private fun checkNull(text: String): String {
        if (text!!.isEmpty()) {
            return getString(R.string.null_error)
        }
        return VALID
    }
}
