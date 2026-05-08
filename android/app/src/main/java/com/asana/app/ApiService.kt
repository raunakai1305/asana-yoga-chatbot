package com.asana.app

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class ChatRequest(val session_id: String?, val message: String)
data class ChatResponse(val reply: String, val session_id: String, val image_url: String?)
data class ImageRequest(val message: String)
data class ImageResponse(val image_url: String?)
data class HistoryMessage(val role: String, val content: String)
data class HistoryResponse(val messages: List<HistoryMessage>)

interface ApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @POST("image")
    suspend fun getImage(@Body request: ImageRequest): ImageResponse

    @GET("history/{sessionId}")
    suspend fun getHistory(@Path("sessionId") sessionId: String): HistoryResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://asana-yoga-chatbot.fly.dev/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
