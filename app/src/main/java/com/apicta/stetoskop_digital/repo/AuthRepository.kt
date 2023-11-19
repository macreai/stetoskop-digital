package com.apicta.stetoskop_digital.repo

import com.apicta.stetoskop_digital.model.local.UserPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Response

class AuthRepository(private val preference: UserPreference): Interceptor {

    private suspend fun getToken(): String{
        return withContext(Dispatchers.IO){
            val user = preference.getUserToken().first()
            user
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            getToken()
        }
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}