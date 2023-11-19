package com.apicta.stetoskop_digital.viewmodel

import androidx.lifecycle.ViewModel
import com.apicta.stetoskop_digital.model.local.UserPreference
import com.apicta.stetoskop_digital.model.remote.api.ApiConfig
import com.apicta.stetoskop_digital.model.remote.response.GetAllPredictionResponse
import com.apicta.stetoskop_digital.model.remote.response.PredictionByIdResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.lang.Exception

class RecordViewModel(private val preference: UserPreference): ViewModel() {

    suspend fun getAllRecord(id: Int): Flow<Result<GetAllPredictionResponse>> = flow {
        try {
            val response = ApiConfig.getApiService(preference).getAllRecord(id)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getRecordById(id: Int): Flow<Result<PredictionByIdResponse>> = flow<Result<PredictionByIdResponse>> {
        try {
            val response = ApiConfig.getApiService(preference).getRecordById(id)
            emit(Result.success(response))
        } catch (e: Exception){
            e.printStackTrace()
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}