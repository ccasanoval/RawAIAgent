package com.cesoft.rawagent.remote

import com.cesoft.rawagent.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class HttpInterceptor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val token = BuildConfig.API_KEY
        request.addHeader("Authorization", "Bearer $token")
        request.addHeader("Accept", "application/json")
        return chain.proceed(request.build())
    }
}
