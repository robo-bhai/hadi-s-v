package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriveAccount
import com.example.data.model.DriveFile
import com.example.data.model.FileCategory
import com.example.data.model.SortOption
import com.example.data.model.ViewMode
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.CategoryFilterChips
import com.example.ui.components.EmptyFilesState
import com.example.ui.components.FileItemGrid
import com.example.ui.components.FileItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    account: DriveAccount,
    folderStack: List<DriveFile>,
    files: List<DriveFile>,
    isLoading: Boolean,
    searchQuery: String,
    selectedCategory: FileCategory,
    sortOption: SortOption,
    viewMode: ViewMode,
    onBackClicked: () -> Unit,
    onCrumbClicked: (Int) -> Unit,
    onFolderClicked: (DriveFile) -> Unit,
    onFileClicked: (DriveFile) -> Unit,
    onDownloadFile: (DriveFile) -> Unit,
    onMoreOptionsClicked: (DriveFile) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCategoryChanged: (FileCategory) -> Unit,
    onSortOptionChanged: (SortOption) -> Unit,
    onViewModeToggled: () -> Unit,
    onCreateFolderClicked: () -> Unit,
    onUploadFileClicked: () -> Unit,
    onRefreshClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    val currentFolderTitle = folderStack.lastOrNull()?.name ?: "📁 ${account.email}"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentFolderTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (folderStack.isEmpty()) "Root Folder • ${account.displayName}" else "In ${account.email}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClicked,
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Toggle search
                        IconButton(
                            onClick = { isSearchActive = !isSearchActive },
                            modifier = Modifier.testTag("toggle_search_button")
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = if (isSearchActive) "Close search" else "Search files"
                            )
                        }

                        // Toggle View Mode (List / Grid)
                        IconButton(
                            onClick = onViewModeToggled,
                            modifier = Modifier.testTag("toggle_view_mode_button")
                        ) {
                            Icon(
                                imageVector = if (viewMode == ViewMode.LIST) Icons.Filled.GridView else Icons.Filled.ViewList,
                                contentDescription = "Toggle view mode"
                            )
                        }

                        // Sort Menu
                        Box {
                            IconButton(
                                onClick = { sortMenuExpanded = true },
                                modifier = Modifier.testTag("sort_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort files"
                                )
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                SortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.title,
                                                fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                                color = if (sortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            sortMenuExpanded = false
                                            onSortOptionChanged(option)
                                        }
                                    )
                                }
                            }
                        }

                        // Refresh
                        IconButton(
                            onClick = onRefreshClicked,
                            modifier = Modifier.testTag("refresh_files_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Search field bar when active
                if (isSearchActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Search in this folder...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_field"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChanged("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Interactive Breadcrumb Trail
                BreadcrumbBar(
                    account = account,
                    folderStack = folderStack,
                    onCrumbClick = onCrumbClicked
                )

                // Filter Category Chips
                CategoryFilterChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategoryChanged
                )
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (fabMenuExpanded) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .testTag("fab_menu_surface"),
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        fabMenuExpanded = false
                                        onCreateFolderClicked()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .testTag("fab_action_create_folder"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CreateNewFolder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("New Folder", fontWeight = FontWeight.Medium)
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        fabMenuExpanded = false
                                        onUploadFileClicked()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .testTag("fab_action_upload_file"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Upload File", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                FloatingActionButton(
                    onClick = { fabMenuExpanded = !fabMenuExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("main_fab")
                ) {
                    Icon(
                        imageVector = if (fabMenuExpanded) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = "Add or Upload"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && files.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading Google Drive files...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                files.isEmpty() -> {
                    EmptyFilesState(
                        isSearch = searchQuery.isNotEmpty() || selectedCategory != FileCategory.ALL,
                        onClearSearch = {
                            onSearchQueryChanged("")
                            onCategoryChanged(FileCategory.ALL)
                        }
                    )
                }

                viewMode == ViewMode.LIST -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                    ) {
                        items(files, key = { it.id }) { file ->
                            FileItemRow(
                                file = file,
                                onClick = {
                                    if (file.isFolder) {
                                        onFolderClicked(file)
                                    } else {
                                        onFileClicked(file)
                                    }
                                },
                                onMoreClick = { onMoreOptionsClicked(file) },
                                onDownloadClick = { onDownloadFile(file) }
                            )
                        }
                    }
                }

                viewMode == ViewMode.GRID -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(files, key = { it.id }) { file ->
                            FileItemGrid(
                                file = file,
                                onClick = {
                                    if (file.isFolder) {
                                        onFolderClicked(file)
                                    } else {
                                        onFileClicked(file)
                                    }
                                },
                                onMoreClick = { onMoreOptionsClicked(file) },
                                onDownloadClick = { onDownloadFile(file) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}
