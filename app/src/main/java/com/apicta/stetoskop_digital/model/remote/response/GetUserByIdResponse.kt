package com.apicta.stetoskop_digital.model.remote.response

import com.google.gson.annotations.SerializedName

data class GetUserByIdResponse(

	@field:SerializedName("data")
	val user: UserProfile? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class UserProfile(

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("ktp")
	val ktp: String? = null,

	@field:SerializedName("nama_lengkap")
	val namaLengkap: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("email_verified_at")
	val emailVerifiedAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("sip")
	val sip: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("alamat")
	val alamat: String? = null
)
