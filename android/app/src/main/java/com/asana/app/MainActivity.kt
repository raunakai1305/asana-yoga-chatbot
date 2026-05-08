package com.asana.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.asana.app.ui.*

sealed class Screen { object Splash : Screen(); object Chat : Screen(); object Voice : Screen() }

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var speechManager: SpeechManager

    private val micPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled inline */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechManager = SpeechManager(this)

        setContent {
            AsanaTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
                val messages by viewModel.messages.collectAsState()

                // Auto-speak last AI message
                LaunchedEffect(messages) {
                    val last = messages.lastOrNull()
                    if (last != null && !last.isUser) {
                        speechManager.speak(last.content)
                    }
                }

                when (currentScreen) {
                    Screen.Splash -> SplashScreen(
                        onNavigateToChat = { currentScreen = Screen.Chat }
                    )
                    Screen.Chat -> ChatScreen(
                        viewModel = viewModel,
                        onMicPressed = {
                            requestMicAndListen(
                                onNavigateToVoice = { currentScreen = Screen.Voice },
                                onResult = { text ->
                                    currentScreen = Screen.Chat
                                    viewModel.sendMessage(text)
                                },
                                onError = { currentScreen = Screen.Chat }
                            )
                        },
                        onMicReleased = {
                            speechManager.stopListening()
                            currentScreen = Screen.Chat
                        }
                    )
                    Screen.Voice -> VoiceRecordingScreen(isListening = true)
                }
            }
        }
    }

    private fun requestMicAndListen(
        onNavigateToVoice: () -> Unit,
        onResult: (String) -> Unit,
        onError: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        onNavigateToVoice()
        speechManager.startListening(onResult = onResult, onError = onError)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.destroy()
    }
}
