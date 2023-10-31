package com.apicta.stetoskop_digital.viewmodel

import androidx.lifecycle.ViewModel
import com.apicta.stetoskop_digital.model.remote.api.ApiConfig
import com.apicta.stetoskop_digital.model.remote.response.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.lang.Exception

class AuthViewModel: ViewModel() {

    suspend fun patientLogin(
        email: String,
        password: String
    ): Flow<Result<LoginResponse>> = flow {
        try {
            val response = ApiConfig.getApiService().loginPatient(email, password)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}