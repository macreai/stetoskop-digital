package com.apicta.stetoskop_digital.model.remote.response

import com.google.gson.annotations.SerializedName

data class PatientByIdResponse(

	@field:SerializedName("data")
	val data: PatientData? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class PatientData(

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("ktp")
	val ktp: Any? = null,

	@field:SerializedName("nama_lengkap")
	val namaLengkap: String? = null,

	@field:SerializedName("nama_dokter")
	val namaDokter: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("email_verified_at")
	val emailVerifiedAt: Any? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("sip")
	val sip: Any? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("alamat")
	val alamat: String? = null
)
