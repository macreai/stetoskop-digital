package com.apicta.stetoskop_digital.model.remote.api

import com.apicta.stetoskop_digital.model.remote.response.LoginResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun loginPatient(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse
}