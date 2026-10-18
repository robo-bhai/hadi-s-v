package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.db.AppDatabase
import com.example.data.repository.CVRepository
import com.example.ui.screens.FormScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.SavedCVsScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CVViewModel
import com.example.ui.viewmodel.CVViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                QuickCVApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCVApp() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = CVRepository(database.cvDao())
    val cvViewModel: CVViewModel = viewModel(factory = CVViewModelFactory(repository))

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val savedCVs by cvViewModel.savedCVs.collectAsStateWithLifecycle()
    val currentCV by cvViewModel.currentCV.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentRoute == "home") {
                TopAppBar(
                    title = {
                        Text(
                            text = "Quick CV Maker",
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else if (currentRoute == "form") {
                TopAppBar(
                    title = {
                        Text(
                            text = "Build Your Resume",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (currentRoute == "home" || currentRoute == "saved") {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "home",
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "saved",
                        onClick = {
                            if (currentRoute != "saved") {
                                navController.navigate("saved")
                            }
                        },
                        icon = { Icon(Icons.Default.Description, contentDescription = "Saved CVs") },
                        label = { Text("Saved Resumes (${savedCVs.size})") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    savedCount = savedCVs.size,
                    onCreateNewClick = {
                        cvViewModel.resetForm()
                        navController.navigate("form")
                    },
                    onSavedCVsClick = {
                        navController.navigate("saved")
                    },
                    onLoadSampleClick = {
                        cvViewModel.loadSampleData()
                        navController.navigate("preview")
                    }
                )
            }

            composable("form") {
                FormScreen(
                    viewModel = cvViewModel,
                    cvData = currentCV,
                    onNavigateToPreview = {
                        navController.navigate("preview")
                    }
                )
            }

            composable("preview") {
                PreviewScreen(
                    viewModel = cvViewModel,
                    onBackToForm = {
                        navController.navigate("form") {
                            popUpTo("form") { inclusive = true }
                        }
                    }
                )
            }

            composable("saved") {
                SavedCVsScreen(
                    viewModel = cvViewModel,
                    onBack = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onEditCV = { cv ->
                        cvViewModel.loadCV(cv)
                        navController.navigate("form")
                    },
                    onPreviewCV = { cv ->
                        cvViewModel.loadCV(cv)
                        navController.navigate("preview")
                    },
                    onCreateNew = {
                        cvViewModel.resetForm()
                        navController.navigate("form")
                    }
                )
            }
        }
    }
}
