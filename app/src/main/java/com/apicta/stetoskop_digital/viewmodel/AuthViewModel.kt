package com.apicta.stetoskop_digital.viewmodel

import android.media.tv.TvContract.Channels.Logo
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.apicta.stetoskop_digital.model.local.UserPreference
import com.apicta.stetoskop_digital.model.remote.api.ApiConfig
import com.apicta.stetoskop_digital.model.remote.response.GetUserByIdResponse
import com.apicta.stetoskop_digital.model.remote.response.LoginResponse
import com.apicta.stetoskop_digital.model.remote.response.LogoutResponse
import com.apicta.stetoskop_digital.model.remote.response.PatientByIdResponse
import com.apicta.stetoskop_digital.model.remote.response.RegisterResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class AuthViewModel(private val preference: UserPreference): ViewModel() {

    fun saveId(id: Int){
        viewModelScope.launch {
            preference.saveUserId(id)
        }
    }

    fun saveToken(token: String){
        viewModelScope.launch {
            preference.saveUserToken(token)
        }
    }

    fun deleteId(){
        viewModelScope.launch {
            preference.deleteId()
        }
    }

    fun deleteToken(){
        viewModelScope.launch{
            preference.deleteToken()
        }
    }

    fun getId(): LiveData<Int> = preference.getUserId().asLiveData()

    suspend fun login(
        email: String,
        password: String
    ): Flow<Result<LoginResponse>> = flow {
        try {
            val response = ApiConfig.getApiService(preference).login(email, password)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getUserById(id: Int): Flow<Result<PatientByIdResponse>> = flow {
        try {
            val response = ApiConfig.getApiService(preference).getPatientById(id)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun logout(): Flow<Result<LogoutResponse>> = flow {
        try {
            val response = ApiConfig.getApiService(preference).logout()
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

    suspend fun register(
        email: String, name: String, address: String, gender: String, password: String
    ): Flow<Result<RegisterResponse>> = flow {
        try {
            val response = ApiConfig.getApiService(preference).patientRegister(email, name, address, gender, password)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }

//    fun logout(){
//            ApiConfig.getApiService(preference).logout()
//                .enqueue(object: Callback<LogoutResponse> {
//                    override fun onResponse(
//                        call: Call<LogoutResponse>,
//                        response: Response<LogoutResponse>
//                    ) {
//                        val responseBody = response.body()
//                        viewModelScope.launch {
//                            if (response.isSuccessful) {
//                                preference.deleteToken()
//                                preference.deleteId()
//                                _message.value = responseBody?.message
//                                _user.value = null
//                            } else {
//                                Log.d(TAG, "onResponse: ${response.body()?.message}")
//                                _message.value = responseBody?.message
//                            }
//                        }
//
//                    }
//
//                    override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
//                        Log.e(TAG, "onFailure: ${t.message}", )
//                        _message.value = "${t.message}"
//                    }
//
//                })
//
//    }
    companion object {
        private const val TAG = "AuthViewModel"
    }
}