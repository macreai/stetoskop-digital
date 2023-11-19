package com.apicta.stetoskop_digital.viewmodel

import androidx.lifecycle.ViewModel
import com.apicta.stetoskop_digital.model.local.UserPreference
import com.apicta.stetoskop_digital.model.remote.api.ApiConfig
import com.apicta.stetoskop_digital.model.remote.response.WavPredictionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.lang.Exception

class AudioViewModel(private val preference: UserPreference): ViewModel() {

    suspend fun postWav(id: RequestBody, file: MultipartBody.Part): Flow<Result<WavPredictionResponse>> = flow {
        try {
            val response = ApiConfig.getApiService(preference).predict(id, file)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}