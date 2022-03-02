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

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.marsphotos.network.ProductApi
import com.example.android.marsphotos.network.Product
import com.example.android.marsphotos.pojo.CreateProductRequest
import com.example.android.marsphotos.pojo.UpdateProductRequest
import kotlinx.coroutines.launch

enum class MarsApiStatus { LOADING, ERROR, DONE }

/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<MarsApiStatus>()
    private val _product = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _product

    // The external immutable LiveData for the request status
    val status: LiveData<MarsApiStatus> = _status

    /**
     * Call getMarsPhotos() on init so we can display status immediately.
     */
    init {
        getProduct()
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     */
    private fun getProduct() {
        viewModelScope.launch {
            try {
                _product.value = ProductApi.retrofitService.getProduct().data
                Log.i("tesss","product: " + products.value!![0].name)
            } catch (e: Exception){
                _product.value = listOf()
            }
        }
    }

    fun updateProduct(idProduct: Int, updateProductRequest: UpdateProductRequest) {
        viewModelScope.launch {
            try {
                var response = ProductApi.retrofitService.updateProduct(idProduct,updateProductRequest)
                if(response.isSuccess()){
                    getProduct()
                }
            } catch (e: Exception){
                _product.value = listOf()
            }
        }
    }

    fun createProduct(createProductRequest: CreateProductRequest) {
        viewModelScope.launch {
            try {
                var response = ProductApi.retrofitService.createProduct(createProductRequest)
                if(response.isSuccess()){
                    getProduct()
                }
            } catch (e: Exception){
                _product.value = listOf()

            }
        }
    }

    fun deleteProduct(idProduct: Int) {
        viewModelScope.launch {
            try {
                var response = ProductApi.retrofitService.deleteProduct(idProduct)
                if(response.isSuccess()){
                    getProduct()
                }
            } catch (e: Exception){
                _product.value = listOf()
            }
        }
    }
}
