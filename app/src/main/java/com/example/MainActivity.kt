package com.example

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DriveFile
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.FileDetailsBottomSheet
import com.example.ui.components.NewFolderDialog
import com.example.ui.components.RenameFileDialog
import com.example.ui.screens.AccountFolderScreen
import com.example.ui.screens.FileBrowserScreen
import com.example.ui.theme.MultiDriveTheme
import com.example.ui.viewmodel.DriveDialogType
import com.example.ui.viewmodel.DriveViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiDriveTheme {
                MultiDriveApp()
            }
        }
    }
}

@Composable
fun MultiDriveApp(
    viewModel: DriveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // State for pending download target file
    var pendingDownloadFile by remember { mutableStateOf<DriveFile?>(null) }

    // System File Picker for Uploads
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileInfo = getFileNameAndSize(context, uri)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            if (bytes != null) {
                viewModel.uploadFile(fileInfo.first, mimeType, bytes)
            }
        }
    }

    // System Document Creator for Downloads
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { destUri: Uri? ->
        val fileToDownload = pendingDownloadFile
        if (destUri != null && fileToDownload != null) {
            viewModel.downloadFile(fileToDownload, destUri)
        }
        pendingDownloadFile = null
    }

    // Handle Snackbars
    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearInfoMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearErrorMessage()
        }
    }

    // Handle system back button for folder hierarchy
    BackHandler(enabled = uiState.activeAccount != null) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val activeAccount = uiState.activeAccount

            if (activeAccount == null) {
                // Screen 1: Account-as-Folder Architecture Root Screen
                AccountFolderScreen(
                    accounts = uiState.accounts,
                    onAccountSelected = { account ->
                        viewModel.openAccountRoot(account)
                    },
                    onAddAccountClicked = {
                        viewModel.showDialog(DriveDialogType.AddAccount)
                    },
                    onRemoveAccountClicked = { accountId ->
                        viewModel.removeAccount(accountId)
                    },
                    onRefreshClicked = {
                        viewModel.refresh()
                    }
                )
            } else {
                // Screen 2: Google Drive File Browser with drill-down folders
                FileBrowserScreen(
                    account = activeAccount,
                    folderStack = uiState.folderStack,
                    files = uiState.filteredFiles,
                    isLoading = uiState.isLoading,
                    searchQuery = uiState.searchQuery,
                    selectedCategory = uiState.selectedCategory,
                    sortOption = uiState.sortOption,
                    viewMode = uiState.viewMode,
                    onBackClicked = {
                        viewModel.navigateBack()
                    },
                    onCrumbClicked = { index ->
                        viewModel.navigateToBreadcrumb(index)
                    },
                    onFolderClicked = { folder ->
                        viewModel.openFolder(folder)
                    },
                    onFileClicked = { file ->
                        viewModel.showDialog(DriveDialogType.FileDetails(file))
                    },
                    onDownloadFile = { file ->
                        pendingDownloadFile = file
                        // Prompt user where to save file
                        saveFileLauncher.launch(file.name)
                    },
                    onMoreOptionsClicked = { file ->
                        viewModel.showDialog(DriveDialogType.FileDetails(file))
                    },
                    onSearchQueryChanged = { query ->
                        viewModel.setSearchQuery(query)
                    },
                    onCategoryChanged = { category ->
                        viewModel.setSelectedCategory(category)
                    },
                    onSortOptionChanged = { option ->
                        viewModel.setSortOption(option)
                    },
                    onViewModeToggled = {
                        viewModel.setViewMode(
                            if (uiState.viewMode == com.example.data.model.ViewMode.LIST)
                                com.example.data.model.ViewMode.GRID
                            else
                                com.example.data.model.ViewMode.LIST
                        )
                    },
                    onCreateFolderClicked = {
                        viewModel.showDialog(DriveDialogType.NewFolder)
                    },
                    onUploadFileClicked = {
                        filePickerLauncher.launch("*/*")
                    },
                    onRefreshClicked = {
                        viewModel.refresh()
                    }
                )
            }

            // Dialogs & Sheets
            when (val dialog = uiState.activeDialog) {
                is DriveDialogType.AddAccount -> {
                    AddAccountDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onSignInCredentialManager = {
                            viewModel.signInWithCredentialManager()
                        },
                        onAddCustomAccount = { email, name, token, isDemo ->
                            viewModel.addAccount(email, name, token, isDemo)
                        }
                    )
                }

                is DriveDialogType.NewFolder -> {
                    NewFolderDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirm = { folderName ->
                            viewModel.dismissDialog()
                            viewModel.createFolder(folderName)
                        }
                    )
                }

                is DriveDialogType.RenameFile -> {
                    RenameFileDialog(
                        file = dialog.file,
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirm = { newName ->
                            viewModel.dismissDialog()
                            viewModel.renameFile(dialog.file, newName)
                        }
                    )
                }

                is DriveDialogType.ConfirmDelete -> {
                    ConfirmDeleteDialog(
                        file = dialog.file,
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirm = {
                            viewModel.dismissDialog()
                            viewModel.deleteFile(dialog.file)
                        }
                    )
                }

                is DriveDialogType.FileDetails -> {
                    FileDetailsBottomSheet(
                        file = dialog.file,
                        onDismiss = { viewModel.dismissDialog() },
                        onDownload = {
                            pendingDownloadFile = dialog.file
                            saveFileLauncher.launch(dialog.file.name)
                        },
                        onRename = {
                            viewModel.showDialog(DriveDialogType.RenameFile(dialog.file))
                        },
                        onDelete = {
                            viewModel.showDialog(DriveDialogType.ConfirmDelete(dialog.file))
                        }
                    )
                }

                null -> { /* No active modal */ }
            }
        }
    }
}

private fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, Long> {
    var name = "uploaded_file"
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
            }
        }
    } catch (_: Exception) { }
    return Pair(name, size)
}
