package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.edit88.ui.EditorScreen
import com.example.edit88.ui.HomeScreen
import com.example.edit88.viewmodel.VideoEditorViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    HOME,
    EDITOR
}

class MainActivity : ComponentActivity() {
    private val viewModel: VideoEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    Edit88App(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Edit88App(viewModel: VideoEditorViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                viewModel = viewModel,
                onOpenEditor = { currentScreen = AppScreen.EDITOR }
            )
        }
        AppScreen.EDITOR -> {
            BackHandler {
                currentScreen = AppScreen.HOME
            }
            EditorScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = AppScreen.HOME }
            )
        }
    }
}
