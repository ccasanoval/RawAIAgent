package com.cesoft.rawagent.remote

import com.cesoft.rawagent.remote.entity.GroqDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/v1/responses")
    suspend fun openAI(@Body body: RequestBody): Result<String>

    @POST("/v1")
    suspend fun deepSeek(@Body body: RequestBody): Result<String>

    @POST("/openai/v1/chat/completions")
    suspend fun groq(@Body body: RequestBody): Result<GroqDto>
}