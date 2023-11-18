package com.apicta.stetoskop_digital.model.remote.api

import com.apicta.stetoskop_digital.model.remote.response.GetUserByIdResponse
import com.apicta.stetoskop_digital.model.remote.response.LoginResponse
import com.apicta.stetoskop_digital.model.remote.response.LogoutResponse
import com.apicta.stetoskop_digital.model.remote.response.PredictResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("user/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): GetUserByIdResponse

    @POST("logout")
    suspend fun logout(): LogoutResponse

    @Multipart
    @POST("ownCheck")
    suspend fun predict(
        @Part("user_id") id: RequestBody,
        @Part file: MultipartBody.Part
    ): PredictResponse

//    @POST("ownCheck")
//    suspend fun predict(
//        @Field()
//    )
}