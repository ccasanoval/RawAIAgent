package com.cesoft.rawagent

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    MainScreen(uiState, vm::onGo)
}

@Composable
fun Q(onGo: (String) -> Unit) {
    val context = LocalContext.current
    var prompt by remember { mutableStateOf("") }

    // Launcher for speech recognition
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val spokenText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                prompt = spokenText  // Update prompt with recognized text
                android.util.Log.e("AAA", "-*---------- $spokenText")
                onGo(spokenText)
            } else {
                Toast.makeText(context, "Failed to recognize speech", Toast.LENGTH_SHORT).show()
            }
        }
    )

    fun listen() {
        val permission = Manifest.permission.RECORD_AUDIO
        val ok = PackageManager.PERMISSION_GRANTED
        if (ContextCompat.checkSelfPermission(context, permission) == ok) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            speechRecognizerLauncher.launch(intent)
        }
        else {
//            ActivityCompat.requestPermissions(
//                context as Activity,
//                arrayOf(Manifest.permission.RECORD_AUDIO),
//                100
//            )
        }
    }

    Column {
        Button(onClick = { listen() }) {
            Text(text = stringResource(R.string.talk))
        }
    }
}


@Composable
fun MainScreen(uiState: UiState, onGo: (String) -> Unit) {
    var prompt by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Q(onGo) }
        item { Spacer(Modifier.size(20.dp)) }
        item {
            TextField(
                value = prompt,
                label = { Text(stringResource(R.string.prompt)) },
                onValueChange = { prompt = it },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(onClick = { onGo(prompt) }) {
                Text(text = stringResource(R.string.go))
            }
        }
        item { Spacer(Modifier.size(20.dp)) }
        when(uiState) {
            is UiState.Loading -> {
                item { CircularProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            is UiState.Success -> {
                if(uiState.outputText.isNotBlank()) {

                }
                item {
                    Text(
                        text = uiState.outputText,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    )
                }
            }
            is UiState.Error -> {
                item {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer)
                    )
                }
            }
            else -> {}
        }
    }
}

//--------------------------------------------------------------------------------------------------
private class UiStateProvider: PreviewParameterProvider<UiState> {
    override val values = sequenceOf(
        UiState.Initial,
        UiState.Loading,
        UiState.Success(outputText = "LLM Output Text"),
        UiState.Error(errorMessage = "LLM Error Message")
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun MainScreen_Preview(@PreviewParameter(UiStateProvider ::class) value: UiState) {
    MainScreen(value) {}
}