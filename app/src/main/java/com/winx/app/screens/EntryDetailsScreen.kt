package com.winx.app.screens

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winx.app.database.PendingMediaUploadEntity
import com.winx.app.data.remote.MediaItemDto
import com.winx.app.data.remote.MediaRepository
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxLightPink
import com.winx.app.ui.theme.WinxOrange
import com.winx.app.ui.theme.WinxPurple
import com.winx.app.ui.theme.WinxWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun EntryDetailsScreen(
    entry: TravelEntry,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val mediaRepository = remember {
        MediaRepository()
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSavingMedia by remember { mutableStateOf(false) }
    var removingQueuedId by remember { mutableStateOf<Long?>(null) }

    var mediaItems by remember { mutableStateOf<List<MediaItemDto>>(emptyList()) }
    var queuedMedia by remember { mutableStateOf<List<PendingMediaUploadEntity>>(emptyList()) }
    var mediaError by remember { mutableStateOf<String?>(null) }
    var mediaMessage by remember { mutableStateOf<String?>(null) }

    // Reload when an offline entry receives its server ID. The client UUID
    // remains stable across that transition.
    LaunchedEffect(entry.id, entry.clientId) {
        mediaItems = emptyList()
        queuedMedia = emptyList()
        mediaError = null
        mediaRepository.getQueuedMediaForEntry(
            context, entry.clientId, entry.id.takeIf { it > 0 }
        ).onSuccess { queuedMedia = it }
            .onFailure { mediaError = it.message ?: "Unable to load queued media." }

        if (entry.id > 0) {
            mediaRepository.getMediaForEntry(entry.id)
                .onSuccess { mediaItems = it }
                .onFailure {
                    // Keep locally queued media visible if the server is offline.
                    mediaError = it.message ?: "Unable to load uploaded media."
                }
        }
    }

    // Always save the picked file to durable storage first. The app's network
    // synchronization pass uploads it later using its stable upload UUID.
    fun uploadSelectedMedia(uri: Uri) {
        if (isSavingMedia) return
        coroutineScope.launch {
            isSavingMedia = true
            mediaError = null
            mediaMessage = null
            try {
                mediaRepository.queueMediaUpload(
                    context = context,
                    uri = uri,
                    entryClientId = entry.clientId,
                    entryServerId = entry.id.takeIf { it > 0 }
                ).onSuccess {
                    mediaMessage = "Media saved on this device. It will upload when connected."
                    mediaRepository.getQueuedMediaForEntry(
                        context, entry.clientId, entry.id.takeIf { it > 0 }
                    ).onSuccess { pending -> queuedMedia = pending }
                    // Try immediately if the server is reachable; the durable
                    // queue remains intact if the attempt fails.
                    coroutineScope.launch {
                        mediaRepository.syncPendingMediaUploads(context)
                        mediaRepository.getQueuedMediaForEntry(
                            context, entry.clientId, entry.id.takeIf { it > 0 }
                        ).onSuccess { pending -> queuedMedia = pending }
                        if (entry.id > 0) {
                            mediaRepository.getMediaForEntry(entry.id)
                                .onSuccess { uploaded -> mediaItems = uploaded }
                        }
                    }
                }.onFailure {
                    mediaError = it.message ?: "Unable to save selected media."
                }
            } finally {
                isSavingMedia = false
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) uploadSelectedMedia(uri)
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) uploadSelectedMedia(uri)
    }

    val formattedDate = try {

        LocalDate.parse(entry.date)
            .format(
                DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy",
                    Locale.ENGLISH
                )
            )

    } catch (exception: DateTimeParseException) {

        entry.date.ifBlank {
            "Date not specified"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WinxWhite)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WinxDarkBlue)
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBackClick
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = WinxWhite
                )
            }

            Text(
                text = "Entry Details",
                color = WinxWhite,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {

                Text(
                    text = entry.title.ifBlank {
                        "Untitled Entry"
                    },
                    color = WinxDarkBlue,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = WinxBlue,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = listOfNotNull(
                            entry.location.takeIf { it.isNotBlank() },
                            entry.country.takeIf { it.isNotBlank() }
                        ).joinToString(", ").ifBlank {
                            "Location not specified"
                        },
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WinxLightPink
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Country",
                            tint = WinxOrange,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column {

                            Text(
                                text = "Country",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text = entry.country.ifBlank {
                                    "Not specified"
                                },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WinxDarkBlue
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WinxLightPink
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Date",
                            tint = WinxBlue,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column {

                            Text(
                                text = "Date",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(
                                modifier = Modifier.height(3.dp)
                            )

                            Text(
                                text = formattedDate,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WinxDarkBlue
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Your Rating",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    for (star in 1..5) {

                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = "Rating $star",
                            tint = if (star <= entry.rating) {
                                WinxOrange
                            } else {
                                Color.LightGray
                            },
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Notes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F8F8)
                    )
                ) {

                    Text(
                        text = entry.notes.ifBlank {
                            "No notes were added for this entry."
                        },
                        modifier = Modifier.padding(16.dp),
                        fontSize = 15.sp,
                        color = WinxDarkBlue
                    )
                }
            }

            item {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Photo & Video Memories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Button(
                        onClick = {
                            photoPickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSavingMedia && (entry.id > 0 || !entry.clientId.isNullOrBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WinxBlue
                        )
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text("Photo")
                    }

                    Button(
                        onClick = {
                            videoPickerLauncher.launch("video/*")
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSavingMedia && (entry.id > 0 || !entry.clientId.isNullOrBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WinxPurple
                        )
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text("Video")
                    }
                }

                if (isSavingMedia) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving media on this device…", color = WinxDarkBlue, fontSize = 13.sp)
                    }
                }

                if (entry.id <= 0) {
                    Text(
                        text = "Photos and videos are saved offline and uploaded after this entry syncs.",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                mediaMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = message, color = WinxDarkBlue, fontSize = 13.sp)
                }

                mediaError?.let { error ->

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }

            if (queuedMedia.isNotEmpty()) {
                items(items = queuedMedia, key = { "queued_${it.localId}" }) { pending ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (pending.mediaType == "Photo") Icons.Outlined.Image
                                    else Icons.Outlined.VideoLibrary,
                                contentDescription = pending.mediaType,
                                tint = WinxPurple,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pending.originalFileName,
                                    fontWeight = FontWeight.Bold,
                                    color = WinxDarkBlue
                                )
                                Text(
                                    text = when (pending.status) {
                                        "UPLOADING" -> "Uploading…"
                                        "FAILED" -> "Waiting to retry"
                                        else -> "Saved offline · Waiting to upload"
                                    },
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                            TextButton(
                                enabled = pending.status != "UPLOADING" && removingQueuedId != pending.localId,
                                onClick = {
                                    coroutineScope.launch {
                                        removingQueuedId = pending.localId
                                        mediaRepository.removeQueuedMedia(context, pending.localId)
                                            .onSuccess {
                                                queuedMedia = queuedMedia.filterNot { it.localId == pending.localId }
                                                mediaError = null
                                            }
                                            .onFailure {
                                                mediaError = it.message ?: "Unable to remove queued media."
                                            }
                                        removingQueuedId = null
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Remove queued media",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            if (mediaItems.isEmpty() && queuedMedia.isEmpty()) {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8)
                        )
                    ) {

                        Text(
                            text = "No photos or videos have been added yet.",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                }

            } else {

                items(
                    items = mediaItems,
                    key = { it.id }
                ) { media ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F8F8)
                        )
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = if (media.mediaType == "Photo") {
                                    Icons.Outlined.Image
                                } else {
                                    Icons.Outlined.VideoLibrary
                                },
                                contentDescription = media.mediaType,
                                tint = WinxBlue,
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(12.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = media.mediaType,
                                    fontWeight = FontWeight.Bold,
                                    color = WinxDarkBlue
                                )

                                Text(
                                    text = media.fileName,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }

                            TextButton(
                                onClick = {

                                    coroutineScope.launch {

                                        mediaRepository.deleteMedia(
                                            media.id
                                        ).onSuccess {

                                            mediaItems =
                                                mediaItems.filterNot {
                                                    it.id == media.id
                                                }

                                            mediaError = null

                                        }.onFailure { exception ->

                                            mediaError =
                                                exception.message
                                                    ?: "Failed to delete media."
                                        }
                                    }
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete media",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }

            item {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WinxPurple
                    )
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = WinxDarkBlue
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Edit Entry",
                        color = WinxDarkBlue,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = Color.Red
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text(
                        text = "Delete Entry",
                        color = Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
