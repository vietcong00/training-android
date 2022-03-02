package com.example.android.marsphotos.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CreateProductRequest(
    @SerializedName("name") @Expose val name: String,
    @SerializedName("descriptions") @Expose val descriptions: String,
    )

