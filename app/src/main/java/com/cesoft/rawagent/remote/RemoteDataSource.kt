package com.cesoft.rawagent.remote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.cesoft.rawagent.BuildConfig
import com.cesoft.rawagent.location.LocationProvider
import com.cesoft.rawagent.remote.entity.GroqDto
import com.cesoft.rawagent.remote.result.NetworkResultCallAdapterFactory
import com.google.gson.GsonBuilder
import io.github.vyfor.groqkt.GroqClient
import io.github.vyfor.groqkt.GroqModel
import io.github.vyfor.groqkt.api.GroqResponse
import io.github.vyfor.groqkt.api.chat.ChatCompletion
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration


class RemoteDataSource(
    private val context: Context,
    private val httpInterceptor: HttpInterceptor
) {
    private val apiService: ApiServiceLLM = getRetrofit(API).create(ApiServiceLLM::class.java)
    private val apiServiceGeo: ApiServiceGeo = getRetrofit(API_GEO).create(ApiServiceGeo::class.java)

    init {
        android.util.Log.e("RemoteDS", "LLM API:--------------------------------------- $API")
    }

    private fun getHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(httpInterceptor)
        //if (auth) { httpClient.authenticator(authAuthenticator) }
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            httpClient.addNetworkInterceptor(logging)
        }
        httpClient.cache(Cache(context.cacheDir, 1024 * 1024 * 2L))
        val timeout = 130L
        httpClient.connectTimeout(Duration.ofSeconds(timeout))
        httpClient.readTimeout(Duration.ofSeconds(timeout))
        httpClient.writeTimeout(Duration.ofSeconds(timeout))
        return httpClient.build()
    }

    private fun getRetrofit(url: String): Retrofit {
        val gson = GsonBuilder().create()
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
            .client(getHttpClient())
            .build()
    }

    /// REVERSE GEO
    //----------------------------------------------------------------------------------------------

    suspend fun reverseGeo(lat: Double, lon: Double): Result<String> {
        val ret = apiServiceGeo.reverseGeocode(BuildConfig.API_KEY_GEO, lat, lon)
        val res = ret.getOrNull()
        res?.features?.firstOrNull()?.properties?.formatted?.let { return Result.success(it) }
        return Result.failure(ret.exceptionOrNull() ?: Exception())
    }

    /// LLM
    //----------------------------------------------------------------------------------------------
    suspend fun getAddress(): String? {
        var address = ""
        val pFine = Manifest.permission.ACCESS_FINE_LOCATION
        val pCoarse = Manifest.permission.ACCESS_COARSE_LOCATION
        val ok = PackageManager.PERMISSION_GRANTED
        if (ActivityCompat.checkSelfPermission(context, pFine) == ok
            && ActivityCompat.checkSelfPermission(context, pCoarse) == ok) {
            val location = LocationProvider.getLocation(context)
            android.util.Log.e("RemoteDS", "LOCATION------------------------ $location")
            location?.let {
                android.util.Log.e("RemoteDS", "ADDRESS---------------------- $address")
                address = reverseGeo(location.latitude, location.longitude).getOrNull() ?: ""
                return address
            }
        }
        return null
    }

    suspend fun promptKt(userPrompt: String): Result<String> {
        val address = getAddress()
        val assistantPrompt = """
        You are a helpful assistant for finding cheap gas stations.
        Respond briefly with the name and address of the cheaper gas station.
        Respond also with the price for the product the user is asking for, or gasoline 95 if he does not indicates one.
        If the user do not talk about a concrete location, or talks about the current location he is at,
        use the address $address as the location for the search.
        """.trimIndent().replace("\n", "")
        val client = GroqClient(BuildConfig.API_KEY) {
            defaults {
                chatCompletion {
                    model = GroqModel.LLAMA_3_8B_8192
                }
            }
        }
        val response: Result<GroqResponse<ChatCompletion>> = client.chat {
            model = GroqModel.LLAMA_3_8B_8192
            messages {
                system(assistantPrompt)
                text(userPrompt)
            }
        }
        android.util.Log.e("AAA", "--------------0- $response")
        return if(response.isSuccess) {
            val res = response.getOrNull()?.data?.choices?.firstOrNull()?.message?.content ?: ""
            if(res.isNotBlank()) {
                android.util.Log.e("AAA", "--------------A- $res")
                Result.success(res)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown"))
            }
        }
        else {
            Result.failure(response.exceptionOrNull() ?: Exception("Unknown"))
        }
    }

    suspend fun promptRaw(userPrompt: String): Result<String> {
        // Current address by device geoposition
        val address = getAddress()

        //val model = "openai/gpt-oss-20b"
        //val model = "llama-3.3-70b-versatile"
        //val model = "openai/gpt-oss-120b"
        //val model = "compound-beta"
        val model = "compound-beta"

        val searchSettings = """
        "search_settings": {
            "include_domains": ["geoportalgasolineras.es"]
        },
        """.trimIndent()

        val assistantPrompt = """
        You are a helpful assistant for finding cheap gas stations.
        Respond briefly with the name and address of the cheaper gas station.
        Respond also with the price for the product the user is asking for, or gasoline 95 if he does not indicates one.
        If the user do not talk about a concrete location, or talks about the current location he is at,
        use the address $address as the location for the search.
        """.trimIndent().replace("\n", "")

        val tools = """
        "tools": [{
            "type": "function",
            "function": {
                "name": "get_current_weather",
                "description": "Get the current weather in a given location",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "location": {
                            "type": "string",
                            "description": "The city and state, e.g. San Francisco, CA"
                        },
                        "unit": {
                        "type": "string",
                        "enum": ["celsius", "fahrenheit"]
                    }
                },
                "required": ["location"]
            }
        }],
        "tool_choice": "auto"
        """.trimIndent()

        val prompt =
            """
            {
                "model": "$model",
                "messages": [{
                    "role": "assistant",
                    "content": "$assistantPrompt"
                },{
                    "role": "user",
                    "content": "$userPrompt"
                }]
            }
            """.trimIndent()
        val body = prompt.toRequestBody(contentType)
        val res = apiService.groq(body)
        return if(res.isSuccess) {
            val data: GroqDto? = res.getOrNull()
            data?.choices?.firstOrNull()?.message?.content?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response"))
        }
        else {
            return res.exceptionOrNull()?.let { Result.failure(it) }
                ?: Result.failure(Exception("Empty response"))
        }
    }

    companion object {
        const val API = BuildConfig.API_URL
        const val API_GEO = BuildConfig.API_URL_GEO
        val contentType = "application/json; charset=utf-8".toMediaTypeOrNull()
    }
}
