package com.example.android.marsphotos.network

object ProductApi {
    val retrofitService : ProductApiService by lazy {
        retrofit.create(ProductApiService::class.java) }
}