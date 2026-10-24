package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.GoogleAuthManager
import com.example.data.model.DriveAccount
import com.example.data.model.DriveFile
import com.example.data.model.FileCategory
import com.example.data.model.SortOption
import com.example.data.model.ViewMode
import com.example.data.repository.GoogleDriveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

sealed interface DriveDialogType {
    object AddAccount : DriveDialogType
    object NewFolder : DriveDialogType
    data class FileDetails(val file: DriveFile) : DriveDialogType
    data class RenameFile(val file: DriveFile) : DriveDialogType
    data class ConfirmDelete(val file: DriveFile) : DriveDialogType
}

data class DriveUiState(
    val accounts: List<DriveAccount> = emptyList(),
    val activeAccount: DriveAccount? = null,
    val folderStack: List<DriveFile> = emptyList(),
    val rawFiles: List<DriveFile> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: FileCategory = FileCategory.ALL,
    val sortOption: SortOption = SortOption.NAME_ASC,
    val viewMode: ViewMode = ViewMode.LIST,
    val activeDialog: DriveDialogType? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null
) {
    val currentParentId: String?
        get() = folderStack.lastOrNull()?.id

    val currentFolderTitle: String
        get() = folderStack.lastOrNull()?.name ?: "My Drive"

    val filteredFiles: List<DriveFile>
        get() {
            var list = rawFiles

            // Filter category
            if (selectedCategory != FileCategory.ALL) {
                list = list.filter { it.category == selectedCategory }
            }

            // Search query
            if (searchQuery.isNotBlank()) {
                val query = searchQuery.trim().lowercase(Locale.ROOT)
                list = list.filter { it.name.lowercase(Locale.ROOT).contains(query) }
            }

            // Sort
            return when (sortOption) {
                SortOption.NAME_ASC -> list.sortedWith(
                    compareByDescending<DriveFile> { it.isFolder }.thenBy { it.name.lowercase(Locale.ROOT) }
                )
                SortOption.NAME_DESC -> list.sortedWith(
                    compareByDescending<DriveFile> { it.isFolder }.thenByDescending { it.name.lowercase(Locale.ROOT) }
                )
                SortOption.DATE_DESC -> list.sortedWith(
                    compareByDescending<DriveFile> { it.isFolder }.thenByDescending { it.modifiedTime ?: "" }
                )
                SortOption.DATE_ASC -> list.sortedWith(
                    compareByDescending<DriveFile> { it.isFolder }.thenBy { it.modifiedTime ?: "" }
                )
                SortOption.SIZE_DESC -> list.sortedWith(
                    compareByDescending<DriveFile> { it.isFolder }.thenByDescending { it.size ?: 0L }
                )
            }
        }
}

class DriveViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GoogleDriveRepository(application.applicationContext)
    private val authManager = GoogleAuthManager(application.applicationContext)

    private val _uiState = MutableStateFlow(DriveUiState())
    val uiState: StateFlow<DriveUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        val list = repository.getAccounts()
        _uiState.update { it.copy(accounts = list) }
    }

    /**
     * Account-as-Folder navigation:
     * User taps an account root folder on the home screen.
     */
    fun openAccountRoot(account: DriveAccount) {
        _uiState.update {
            it.copy(
                activeAccount = account,
                folderStack = emptyList(),
                searchQuery = "",
                selectedCategory = FileCategory.ALL
            )
        }
        loadCurrentFolderFiles()
    }

    /**
     * Navigates into a subfolder:
     * Query: q = "'<parent_id>' in parents and trashed = false"
     */
    fun openFolder(folder: DriveFile) {
        if (!folder.isFolder) return
        _uiState.update {
            it.copy(
                folderStack = it.folderStack + folder,
                searchQuery = "",
                selectedCategory = FileCategory.ALL
            )
        }
        loadCurrentFolderFiles()
    }

    /**
     * Moves back through folder levels, or returns to Account-as-Folder list when at root.
     */
    fun navigateBack(): Boolean {
        val state = _uiState.value
        return if (state.folderStack.isNotEmpty()) {
            _uiState.update { it.copy(folderStack = it.folderStack.dropLast(1)) }
            loadCurrentFolderFiles()
            true
        } else if (state.activeAccount != null) {
            _uiState.update {
                it.copy(
                    activeAccount = null,
                    folderStack = emptyList(),
                    rawFiles = emptyList()
                )
            }
            loadAccounts()
            true
        } else {
            false
        }
    }

    /**
     * Tapping a specific crumb in the breadcrumb trail.
     * index == -1 -> root ("My Drive")
     * index >= 0 -> folder at that position in folderStack
     */
    fun navigateToBreadcrumb(index: Int) {
        if (index < 0) {
            _uiState.update { it.copy(folderStack = emptyList()) }
        } else {
            val target = _uiState.value.folderStack.take(index + 1)
            _uiState.update { it.copy(folderStack = target) }
        }
        loadCurrentFolderFiles()
    }

    fun refresh() {
        if (_uiState.value.activeAccount != null) {
            loadCurrentFolderFiles()
        } else {
            loadAccounts()
        }
    }

    private fun loadCurrentFolderFiles() {
        val account = _uiState.value.activeAccount ?: return
        val parentId = _uiState.value.currentParentId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.listFiles(account, parentId)
            result.onSuccess { files ->
                _uiState.update {
                    it.copy(
                        rawFiles = files,
                        isLoading = false
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Failed to load files."
                    )
                }
            }
        }
    }

    fun createFolder(name: String) {
        val account = _uiState.value.activeAccount ?: return
        val parentId = _uiState.value.currentParentId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.createFolder(account, name, parentId)
            result.onSuccess { newFolder ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Folder \"${newFolder.name}\" created"
                    )
                }
                loadCurrentFolderFiles()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Failed to create folder"
                    )
                }
            }
        }
    }

    fun uploadFile(fileName: String, mimeType: String, bytes: ByteArray) {
        val account = _uiState.value.activeAccount ?: return
        val parentId = _uiState.value.currentParentId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.uploadFile(account, fileName, mimeType, bytes, parentId)
            result.onSuccess { uploaded ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Uploaded \"${uploaded.name}\" (${uploaded.formattedSize})"
                    )
                }
                loadCurrentFolderFiles()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Upload failed"
                    )
                }
            }
        }
    }

    fun downloadFile(file: DriveFile, destinationUri: Uri?) {
        val account = _uiState.value.activeAccount ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.downloadFile(account, file, destinationUri)
            result.onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = message
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Download failed"
                    )
                }
            }
        }
    }

    fun renameFile(file: DriveFile, newName: String) {
        val account = _uiState.value.activeAccount ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.renameFile(account, file, newName)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Renamed to \"$newName\""
                    )
                }
                loadCurrentFolderFiles()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Rename failed"
                    )
                }
            }
        }
    }

    fun deleteFile(file: DriveFile) {
        val account = _uiState.value.activeAccount ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteFile(account, file)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Deleted \"${file.name}\""
                    )
                }
                loadCurrentFolderFiles()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Delete failed"
                    )
                }
            }
        }
    }

    /**
     * Adds an account either via Credential Manager token or direct OAuth access token.
     */
    fun addAccount(
        email: String,
        displayName: String,
        accessToken: String,
        isDemo: Boolean
    ) {
        val newAccount = DriveAccount(
            id = "acc_${UUID.randomUUID().toString().take(8)}",
            email = email.trim(),
            displayName = displayName.ifBlank { email.substringBefore("@") }.trim(),
            accessToken = accessToken.trim(),
            isDemo = isDemo,
            storageUsedBytes = if (isDemo) 4_294_967_296L else 1_073_741_824L,
            storageTotalBytes = 15L * 1024 * 1024 * 1024
        )
        repository.saveAccount(newAccount)
        loadAccounts()
        _uiState.update {
            it.copy(
                infoMessage = "Account ${newAccount.email} added successfully",
                activeDialog = null
            )
        }
    }

    fun signInWithCredentialManager(serverClientId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authManager.signInWithCredentialManager(
                serverClientId ?: "YOUR_SERVER_CLIENT_ID.apps.googleusercontent.com"
            )
            result.onSuccess { account ->
                repository.saveAccount(account)
                loadAccounts()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeDialog = null,
                        infoMessage = "Connected Google Account: ${account.email}"
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Sign In: ${ex.message}"
                    )
                }
            }
        }
    }

    fun removeAccount(accountId: String) {
        repository.removeAccount(accountId)
        if (_uiState.value.activeAccount?.id == accountId) {
            _uiState.update { it.copy(activeAccount = null, folderStack = emptyList()) }
        }
        loadAccounts()
        _uiState.update { it.copy(infoMessage = "Account removed") }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: FileCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun showDialog(dialog: DriveDialogType) {
        _uiState.update { it.copy(activeDialog = dialog) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = null) }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
