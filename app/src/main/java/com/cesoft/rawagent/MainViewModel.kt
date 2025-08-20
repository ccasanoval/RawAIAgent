package com.cesoft.rawagent

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cesoft.rawagent.remote.HttpInterceptor
import com.cesoft.rawagent.remote.RemoteDataSource
import io.github.vyfor.groqkt.GroqClient
import io.github.vyfor.groqkt.GroqModel
import io.github.vyfor.groqkt.api.GroqResponse
import io.github.vyfor.groqkt.api.audio.AudioResponseFormat
import io.github.vyfor.groqkt.api.chat.ChatCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface UiState {
    object Initial : UiState

    object Loading : UiState

    data class Success(
        val outputText: String
    ) : UiState

    data class Error(
        val errorMessage: String
    ) : UiState
}

class MainViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    val ds = RemoteDataSource(App.ThisApp.applicationContext, HttpInterceptor())

    val tts = TextToSpeech(App.ThisApp) {
        android.util.Log.e("AAA", "------- TTS: $it")
    }

    fun onGo(prompt: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                /// Groq Library
                val res = ds.promptKt(prompt)

                /// Groq Raw http call
                //val res = ds.promptRaw(prompt)

                val resValue = res.getOrNull()
                if(resValue != null) {
                    Log.e("MainVM", "onGo------------ok $resValue")
                    tts.language = Locale.getDefault()
                    tts.speak(
                        resValue,
                        TextToSpeech.QUEUE_ADD,
                        null, null
                    )
                    _uiState.value = UiState.Success(resValue)
                }
                else {
                    Log.e("MainVM", "onGo------------ko ${res.exceptionOrNull()}")
                    _uiState.value = UiState.Error(res.exceptionOrNull()?.message ?: "Unknown error")
                }
            }
            catch (e: Exception) {
                Log.e("MainVM", "onGo------------kk $e")
                Log.e("MainVM", "onGo------------kk ${e.localizedMessage}")
                Log.e("MainVM", "onGo------------kk ${e.message}")
                Log.e("MainVM", "onGo------------kk ${e.cause}")
                Log.e("MainVM", "onGo------------kk ${e.stackTrace.joinToString()}")
                _uiState.value = UiState.Error("Error: $e")
            }
        }
    }
}
