package com.winx.app.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.winx.app.data.remote.MediaItemDto
import com.winx.app.data.remote.MediaRepository
import com.winx.app.data.remote.TravelEntryRepository
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxLightPink
import com.winx.app.ui.theme.WinxOrange
import com.winx.app.ui.theme.WinxWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private enum class LibrarySort(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    A_TO_Z("A - Z")
}

private data class LibraryMedia(
    val media: MediaItemDto,
    val entry: TravelEntry
)

@Composable
fun LibraryScreen(
    onDashboardClick: () -> Unit = {},
    onEntriesClick: () -> Unit = {},
    onEntryDetailsClick: (TravelEntry) -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(LibrarySort.NEWEST) }
    var filterExpanded by remember { mutableStateOf(false) }
    var entries by remember { mutableStateOf<List<TravelEntry>>(emptyList()) }
    var mediaItems by remember { mutableStateOf<List<MediaItemDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    val entryRepository = remember { TravelEntryRepository() }
    val mediaRepository = remember { MediaRepository() }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null

        entryRepository.getEntries()
            .onSuccess { loadedEntries ->
                entries = loadedEntries

                mediaRepository.getMediaForEntries(loadedEntries.map { it.id })
                    .onSuccess { loadedMedia ->
                        mediaItems = loadedMedia
                        isLoading = false
                    }
                    .onFailure { error ->
                        mediaItems = emptyList()
                        errorMessage = error.message ?: "Unable to load Library media."
                        isLoading = false
                    }
            }
            .onFailure { error ->
                entries = emptyList()
                mediaItems = emptyList()
                errorMessage = error.message ?: "Unable to connect to the Winx REST API."
                isLoading = false
            }
    }

    val entryById = remember(entries) { entries.associateBy { it.id } }
    val libraryMedia = remember(mediaItems, entryById) {
        mediaItems.mapNotNull { media ->
            entryById[media.travelEntryId]?.let { entry -> LibraryMedia(media, entry) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WinxLightPink)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            LibraryHeader()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = WinxWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    LibraryTabs(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                            searchText = ""
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            placeholder = {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "Search photos..."
                                        1 -> "Search videos..."
                                        else -> "Search entries..."
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = LibrarySearchGrey,
                                unfocusedContainerColor = LibrarySearchGrey
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        Box {
                            Card(
                                modifier = Modifier
                                    .height(42.dp)
                                    .clickable { filterExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = LibrarySearchGrey
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(42.dp)
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FilterList,
                                        contentDescription = "Filter",
                                        modifier = Modifier.size(17.dp),
                                        tint = LibraryGrey
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Filter", fontSize = 11.sp, color = LibraryGrey)
                                }
                            }

                            DropdownMenu(
                                expanded = filterExpanded,
                                onDismissRequest = { filterExpanded = false }
                            ) {
                                LibrarySort.entries.forEach { sort ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (sort == selectedSort) {
                                                    "✓ ${sort.label}"
                                                } else {
                                                    sort.label
                                                }
                                            )
                                        },
                                        onClick = {
                                            selectedSort = sort
                                            filterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> LibraryLoadingState()
                    errorMessage != null -> LibraryErrorState(
                        message = errorMessage!!,
                        onRetry = { reloadKey++ }
                    )
                    selectedTab == 0 -> MediaLibraryContent(
                        media = filterMedia(
                            source = libraryMedia.filter {
                                it.media.mediaType.equals("Photo", ignoreCase = true)
                            },
                            searchText = searchText,
                            sort = selectedSort
                        ),
                        typeName = "photos",
                        isVideo = false,
                        onEntryClick = onEntryDetailsClick
                    )
                    selectedTab == 1 -> MediaLibraryContent(
                        media = filterMedia(
                            source = libraryMedia.filter {
                                it.media.mediaType.equals("Video", ignoreCase = true)
                            },
                            searchText = searchText,
                            sort = selectedSort
                        ),
                        typeName = "videos",
                        isVideo = true,
                        onEntryClick = onEntryDetailsClick
                    )
                    else -> EntryLibraryContent(
                        entries = filterEntries(entries, searchText, selectedSort),
                        onEntryClick = onEntryDetailsClick
                    )
                }
            }
        }

        BottomNavigationBar(
            selectedItem = "Library",
            onDashboardClick = onDashboardClick,
            onEntryClick = onEntriesClick,
            onCalendarClick = onCalendarClick,
            onLibraryClick = {},
            onSettingsClick = onSettingsClick
        )
    }
}

@Composable
private fun LibraryHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Library",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = WinxDarkBlue
        )
        Text(
            text = "Your saved memories",
            fontSize = 11.sp,
            color = LibraryGrey
        )
    }
}

@Composable
private fun LibraryTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("Photos", "Videos", "Entries").forEachIndexed { index, title ->
            val selected = selectedTab == index
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) WinxDarkBlue else LibraryGrey
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(3.dp)
                        .background(if (selected) WinxBlue else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun LibraryLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = WinxBlue)
            Spacer(Modifier.height(12.dp))
            Text("Loading your Library...", color = WinxDarkBlue)
        }
    }
}

@Composable
private fun LibraryErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = WinxWhite)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "We couldn't load your Library",
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )
                Spacer(Modifier.height(8.dp))
                Text(text = message, color = LibraryGrey, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
private fun LibraryEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = WinxWhite)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(24.dp),
                color = LibraryGrey
            )
        }
    }
}

@Composable
private fun MediaLibraryContent(
    media: List<LibraryMedia>,
    typeName: String,
    isVideo: Boolean,
    onEntryClick: (TravelEntry) -> Unit
) {
    if (media.isEmpty()) {
        LibraryEmptyState("No $typeName found.")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(media, key = { it.media.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEntryClick(item.entry) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WinxOrange),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 82.dp, height = 68.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(WinxWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isVideo) {
                            AsyncImage(
                                model = Uri.parse(item.media.filePath),
                                contentDescription = item.media.fileName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Video",
                                tint = WinxBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.entry.title.ifBlank { item.media.fileName },
                            fontWeight = FontWeight.Bold,
                            color = WinxDarkBlue
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "${item.entry.location}, ${item.entry.country}",
                            color = LibraryGrey,
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatDate(item.entry.date),
                            color = LibraryGrey,
                            fontSize = 11.sp
                        )
                        if (isVideo) {
                            Text(
                                text = item.media.fileName,
                                color = LibraryGrey,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Open entry",
                        tint = WinxDarkBlue
                    )
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun EntryLibraryContent(
    entries: List<TravelEntry>,
    onEntryClick: (TravelEntry) -> Unit
) {
    if (entries.isEmpty()) {
        LibraryEmptyState("No entries found.")
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries, key = { it.id }) { entry ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEntryClick(entry) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WinxOrange),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.title.ifBlank { "Untitled Entry" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WinxDarkBlue
                        )
                        Spacer(Modifier.height(5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = WinxDarkBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${entry.location}, ${entry.country}",
                                fontSize = 12.sp,
                                color = LibraryGrey
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = formatDate(entry.date),
                            fontSize = 11.sp,
                            color = LibraryGrey
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Star,
                                contentDescription = null,
                                tint = WinxDarkBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${entry.rating}/5",
                                fontSize = 11.sp,
                                color = LibraryGrey
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Open entry",
                        tint = WinxDarkBlue
                    )
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

private fun filterEntries(
    entries: List<TravelEntry>,
    searchText: String,
    sort: LibrarySort
): List<TravelEntry> {
    val query = searchText.trim()
    val filtered = entries.filter { entry ->
        query.isBlank() ||
                entry.title.contains(query, ignoreCase = true) ||
                entry.location.contains(query, ignoreCase = true) ||
                entry.country.contains(query, ignoreCase = true) ||
                entry.notes.contains(query, ignoreCase = true) ||
                entry.date.contains(query, ignoreCase = true)
    }
    return sortEntries(filtered, sort)
}

private fun filterMedia(
    source: List<LibraryMedia>,
    searchText: String,
    sort: LibrarySort
): List<LibraryMedia> {
    val query = searchText.trim()
    val filtered = source.filter { item ->
        query.isBlank() ||
                item.media.fileName.contains(query, ignoreCase = true) ||
                item.entry.title.contains(query, ignoreCase = true) ||
                item.entry.location.contains(query, ignoreCase = true) ||
                item.entry.country.contains(query, ignoreCase = true) ||
                item.entry.notes.contains(query, ignoreCase = true) ||
                item.entry.date.contains(query, ignoreCase = true)
    }

    return when (sort) {
        LibrarySort.NEWEST -> filtered.sortedByDescending { parseDate(it.entry.date) }
        LibrarySort.OLDEST -> filtered.sortedBy { parseDate(it.entry.date) }
        LibrarySort.A_TO_Z -> filtered.sortedBy { it.entry.title.lowercase(Locale.getDefault()) }
    }
}

private fun sortEntries(entries: List<TravelEntry>, sort: LibrarySort): List<TravelEntry> {
    return when (sort) {
        LibrarySort.NEWEST -> entries.sortedByDescending { parseDate(it.date) }
        LibrarySort.OLDEST -> entries.sortedBy { parseDate(it.date) }
        LibrarySort.A_TO_Z -> entries.sortedBy { it.title.lowercase(Locale.getDefault()) }
    }
}

private fun parseDate(value: String): LocalDate {
    return try {
        LocalDate.parse(value.take(10))
    } catch (_: Exception) {
        LocalDate.MIN
    }
}

private fun formatDate(value: String): String {
    return try {
        LocalDate.parse(value.take(10)).format(
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
        )
    } catch (_: DateTimeParseException) {
        value.ifBlank { "Date not specified" }
    }
}

private val LibraryGrey = Color(0xFF777777)
private val LibrarySearchGrey = Color(0xFFF1F1F1)
