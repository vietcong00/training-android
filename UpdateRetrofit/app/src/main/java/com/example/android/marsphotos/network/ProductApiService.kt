package com.example.android.marsphotos.network

import com.example.android.marsphotos.pojo.CreateProductRequest
import com.example.android.marsphotos.pojo.UpdateProductRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.google.gson.Gson
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

private val gson = Gson()

//Add the following constant for the base URL for the web service.
private const val BASE_URL =
    "https://878c-222-252-24-243.ngrok.io/api/"

val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create(gson))
    .baseUrl(BASE_URL)
    .build()

interface ProductApiService {
    @GET("product")
    suspend fun getProduct(): Response<ArrayList<Product>>

    @PATCH("product/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body updateProductRequest: UpdateProductRequest
    ): BasicResponse

    @POST("product")
    suspend fun createProduct(@Body createProductRequest: CreateProductRequest): BasicResponse

    @DELETE("product/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int,
    ): BasicResponse
}