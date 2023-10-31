package com.apicta.stetoskop_digital.model.remote.response

data class LoginResponse(
    val authorisation: Authorisation? = null,
    val user: User? = null,
    val status: String? = null
)

data class User(
	val role: String? = null,
	val gender: String? = null,
	val updatedAt: String? = null,
	val ktp: Any? = null,
	val namaLengkap: String? = null,
	val createdAt: String? = null,
	val emailVerifiedAt: Any? = null,
	val id: Int? = null,
	val sip: Any? = null,
	val email: String? = null,
	val alamat: String? = null
)

data class Authorisation(
	val type: String? = null,
	val token: String? = null
)

