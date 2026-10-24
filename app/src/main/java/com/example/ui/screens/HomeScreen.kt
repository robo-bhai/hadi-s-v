package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AccountMediaFile
import com.example.data.model.DriveFile
import com.example.data.model.HomeMediaFilter
import com.example.ui.components.FileIcon
import com.example.ui.components.FileItemRow
import com.example.ui.theme.DriveBluePrimary
import com.example.ui.theme.DriveGreen
import com.example.ui.theme.DriveRed
import com.example.ui.theme.DriveYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mediaFiles: List<AccountMediaFile>,
    selectedFilter: HomeMediaFilter,
    cachedPhotosCount: Int,
    cachedSizeFormatted: String,
    searchQuery: String,
    onFilterSelected: (HomeMediaFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPhotoClicked: (AccountMediaFile) -> Unit,
    onFileClicked: (DriveFile) -> Unit,
    onDownloadFile: (DriveFile) -> Unit,
    onToggleCache: (String) -> Unit,
    onClearCache: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Cloud,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MultiDrive Home",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "All Connected Google Accounts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { isSearchActive = !isSearchActive },
                            modifier = Modifier.testTag("home_search_toggle")
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = "Search media")
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("home_refresh_button")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh media")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                if (isSearchActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Search videos or photos across accounts...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_search_input"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )
                    }
                }

                // Media Filter Category Buttons (Videos, Photos, All)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Videos Button
                    FilterChip(
                        selected = selectedFilter == HomeMediaFilter.VIDEOS,
                        onClick = { onFilterSelected(HomeMediaFilter.VIDEOS) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Videos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DriveRed.copy(alpha = 0.15f),
                            selectedLabelColor = DriveRed,
                            selectedLeadingIconColor = DriveRed
                        ),
                        modifier = Modifier.testTag("home_filter_videos")
                    )

                    // Photos Button
                    FilterChip(
                        selected = selectedFilter == HomeMediaFilter.PHOTOS,
                        onClick = { onFilterSelected(HomeMediaFilter.PHOTOS) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Photos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("home_filter_photos")
                    )

                    // All Files Button
                    FilterChip(
                        selected = selectedFilter == HomeMediaFilter.ALL,
                        onClick = { onFilterSelected(HomeMediaFilter.ALL) },
                        label = { Text("All Files") },
                        modifier = Modifier.testTag("home_filter_all")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedFilter) {
                HomeMediaFilter.PHOTOS -> {
                    // Responsive Photo Gallery with Cache features
                    PhotoGalleryView(
                        photos = mediaFiles,
                        cachedCount = cachedPhotosCount,
                        cachedSizeFormatted = cachedSizeFormatted,
                        onPhotoClicked = onPhotoClicked,
                        onToggleCache = onToggleCache,
                        onClearCache = onClearCache
                    )
                }

                HomeMediaFilter.VIDEOS -> {
                    // Videos across all accounts
                    VideosListView(
                        videos = mediaFiles,
                        onVideoClicked = { onFileClicked(it.file) },
                        onDownloadVideo = { onDownloadFile(it.file) },
                        onToggleCache = onToggleCache
                    )
                }

                HomeMediaFilter.ALL -> {
                    // All media & files across all accounts
                    AllFilesListView(
                        items = mediaFiles,
                        onItemClicked = { onFileClicked(it.file) },
                        onDownloadItem = { onDownloadFile(it.file) }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoGalleryView(
    photos: List<AccountMediaFile>,
    cachedCount: Int,
    cachedSizeFormatted: String,
    onPhotoClicked: (AccountMediaFile) -> Unit,
    onToggleCache: (String) -> Unit,
    onClearCache: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Cache Feature Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Cached,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cached: $cachedCount photos ($cachedSizeFormatted)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (cachedCount > 0) {
                    TextButton(
                        onClick = onClearCache,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("clear_cache_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear Cache",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        if (photos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No photos found across connected accounts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Responsive Adaptive Grid (automatically scales columns from 2 to 4+ based on screen width)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos, key = { it.file.id }) { item ->
                    PhotoGridCard(
                        media = item,
                        onClick = { onPhotoClicked(item) },
                        onToggleCache = { onToggleCache(item.file.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoGridCard(
    media: AccountMediaFile,
    onClick: () -> Unit,
    onToggleCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("photo_item_${media.file.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.previewUrl ?: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800")
                    .crossfade(true)
                    .build(),
                contentDescription = media.file.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient scrim at bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            // Account origin badge at top right
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = media.account.initials,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            // Cache status badge at top left
            if (media.isCachedLocally) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    color = DriveGreen.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Cached",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(3.dp)
                            .size(12.dp)
                    )
                }
            }

            // File title at bottom
            Text(
                text = media.file.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun VideosListView(
    videos: List<AccountMediaFile>,
    onVideoClicked: (AccountMediaFile) -> Unit,
    onDownloadVideo: (AccountMediaFile) -> Unit,
    onToggleCache: (String) -> Unit
) {
    if (videos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No videos found across connected accounts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos, key = { it.file.id }) { item ->
                VideoCardItem(
                    media = item,
                    onClick = { onVideoClicked(item) },
                    onDownload = { onDownloadVideo(item) },
                    onToggleCache = { onToggleCache(item.file.id) }
                )
            }
        }
    }
}

@Composable
fun VideoCardItem(
    media: AccountMediaFile,
    onClick: () -> Unit,
    onDownload: () -> Unit,
    onToggleCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("video_card_${media.file.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Video Preview thumbnail with play overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(media.previewUrl ?: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800")
                        .crossfade(true)
                        .build(),
                    contentDescription = media.file.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )

                // Play Icon Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play Video",
                        tint = DriveRed,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Account badge at top right
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📁 ${media.account.email}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Size tag at bottom right
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = media.file.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Info & action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.file.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Modified ${media.file.formattedModifiedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.testTag("download_video_${media.file.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Download video",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllFilesListView(
    items: List<AccountMediaFile>,
    onItemClicked: (AccountMediaFile) -> Unit,
    onDownloadItem: (AccountMediaFile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.file.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onItemClicked(item) }
                    .testTag("all_file_row_${item.file.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FileIcon(category = item.file.category, isFolder = item.file.isFolder)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.file.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📁 ${item.account.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " • ${item.file.formattedSize}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDownloadItem(item) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
