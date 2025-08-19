package com.cesoft.rawagent.remote.result

import android.os.Message
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.lang.reflect.Parameter
import java.net.HttpURLConnection

fun <T : Any> handleApi(
    execute: () -> Response<T>
): Result<T> {
    return try {
        val response = execute()
        val body = response.body()
        val error = response.errorBody()?.string()

        if(response.isSuccessful && body != null) {
            Result.success(body)
        }
        else {
            if(error?.contains("User has no clocked sessions") == false) {
                android.util.Log.e("ApiExecutor", "handleApi---FAIL---------- ${response.code()} : $error")
            }
            if(response.code() in 400..499 && error != null) {
                data class OpenAiSubError(
                    val message: String = "",
                    val type: String = "",
                    val parameter: String = "",
                    val code: String = ""
                )
                data class OpenAiError(
                    val error: OpenAiSubError
                )
                val failure = Gson().fromJson(error, OpenAiError::class.java)
                try {
                    val jsonObj = JSONObject(error)
                    android.util.Log.e("ApiExecutor","handleApi------------json- $jsonObj")
                    val error = JSONObject(jsonObj.get("error").toString())
                    android.util.Log.e("ApiExecutor","handleApi------------error- $error")
                    val message = error.get("message").toString()
                    android.util.Log.e("ApiExecutor","handleApi------------message- $message")
                    val type = error.get("type").toString()
                    android.util.Log.e("ApiExecutor","handleApi------------typpe- $type")
                }
                catch(ee: Exception) {
                    android.util.Log.e("ApiExecutor","handleApi-----------ee-- $ee")
                }
                Result.failure(Throwable(failure.error.message))
            }
            else if(response.code() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                android.util.Log.e("ApiExecutor","handleApi---HTTP_INTERNAL_ERROR---------- ${response.code()} : $error")
                //Result.failure(InternalException(response.code(), if(BuildConfig.DEBUG) error ?: "" else ""))
                Result.failure(Throwable(response.code().toString()))
            }
            else if(response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                Result.failure(Throwable(""))
            }
            else {
                android.util.Log.e("ApiExecutor","handleApi---${response.code()}---------- ${response.code()} : $error")
                Result.failure(Throwable(response.message()))
            }
        }
    } catch(e: HttpException) {
        android.util.Log.e("ApiExecutor","handleApi---HttpException---------- $e")
        Result.failure(e)
    } catch(t: Throwable) {
        android.util.Log.e("ApiExecutor","handleApi---Throwable---------- $t")
        Result.failure(t)
    }
}