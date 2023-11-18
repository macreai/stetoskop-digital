package com.apicta.stetoskop_digital.model.remote.response

import com.google.gson.annotations.SerializedName

data class PredictResponse(

    @field:SerializedName("result")
    val result: String? = null,

    @field:SerializedName("file_path")
    val filePath: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("user_id")
    val userId: Int? = null,

    @field:SerializedName("suara")
    val suara: String? = null,

    @field:SerializedName("jenis")
    val jenis: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

)