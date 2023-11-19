package com.apicta.stetoskop_digital.model.remote.response

import com.google.gson.annotations.SerializedName

data class WavPredictionResponse(

	@field:SerializedName("data")
	val data: DataInfo? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataInfo(

	@field:SerializedName("result")
	val result: String? = null,

	@field:SerializedName("file_path")
	val filePath: String? = null,

	@field:SerializedName("note")
	val note: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("suara")
	val suara: String? = null,

	@field:SerializedName("jenis")
	val jenis: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
