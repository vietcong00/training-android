package com.example.android.marsphotos.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import java.io.Serializable

data class Response<T>(
    @SerializedName("code")
    @Expose
    val code: Int? = null,

    @SerializedName("data")
    @Expose
    val data: T,

    @SerializedName("message")
    @Expose
    val message: String? = null

) : Serializable {
    fun isSuccess(): Boolean = ApiCode.OK == code
}

data class BasicResponse(
    @SerializedName("code")
    @Expose
    val code: Int? = null,

    @SerializedName("message")
    @Expose
    val message: String? = null

) : Serializable {
    fun isSuccess(): Boolean = ApiCode.OK == code
}

object ApiCode {
    const val OK = 200
    const val INVALID = "400"
    const val UNEXPECTED = "500"
}
