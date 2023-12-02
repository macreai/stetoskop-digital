package com.apicta.stetoskop_digital.model.remote.api

import com.apicta.stetoskop_digital.model.remote.response.GetAllPredictionResponse
import com.apicta.stetoskop_digital.model.remote.response.GetUserByIdResponse
import com.apicta.stetoskop_digital.model.remote.response.LoginResponse
import com.apicta.stetoskop_digital.model.remote.response.LogoutResponse
import com.apicta.stetoskop_digital.model.remote.response.PatientByIdResponse
import com.apicta.stetoskop_digital.model.remote.response.PredictionByIdResponse
import com.apicta.stetoskop_digital.model.remote.response.RegisterResponse
import com.apicta.stetoskop_digital.model.remote.response.WavPredictionResponse
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

    @GET("pasien/{id}")
    suspend fun getPatientById(
        @Path("id") id: Int
    ): PatientByIdResponse

    @POST("logout")
    suspend fun logout(): LogoutResponse

    @Multipart
    @POST("ownCheck")
    suspend fun predict(
        @Part("user_id") id: RequestBody,
        @Part file: MultipartBody.Part
    ): WavPredictionResponse

    @GET("prediksi/user/{id}")
    suspend fun getAllRecord(
        @Path("id") id: Int
    ): GetAllPredictionResponse

    @GET("prediksi/{id}")
    suspend fun getRecordById(
        @Path("id") id: Int
    ): PredictionByIdResponse

    @Multipart
    @POST("register_pasien")
    suspend fun patientRegister(
        @Part("email") email: String,
        @Part("nama_lengkap") name: String,
        @Part("alamat") address: String,
        @Part("gender") gender: String,
        @Part("password") password: String
    ): RegisterResponse
}