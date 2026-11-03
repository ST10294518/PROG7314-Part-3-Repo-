package com.winx.app.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Videocam

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxLightPink
import com.winx.app.ui.theme.WinxOrange
import com.winx.app.ui.theme.WinxPurple
import com.winx.app.ui.theme.WinxWhite


@Composable
fun EntryScreen(
    onCountriesClick: () -> Unit = {},
    onSaveClick: (TravelEntry) -> Unit = {},
    onCancelClick: () -> Unit = {},

    // Selected country can be supplied by the Countries screen/navigation.
    selectedCountry: String = ""
) {

    // -------------------------------------------------------------
    // CONTEXT
    // -------------------------------------------------------------

    val context = LocalContext.current

    val displayDate = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(Date())
    }

    val entryDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    }


    // -------------------------------------------------------------
    // SELECTED PICTURES
    // -------------------------------------------------------------

    var selectedPictures by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }


    // -------------------------------------------------------------
    // SELECTED VIDEOS
    // -------------------------------------------------------------

    var selectedVideos by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }


    // -------------------------------------------------------------
    // ENTRY DETAILS
    // -------------------------------------------------------------

    var title by remember {
        mutableStateOf("")
    }

    var location by remember {
        mutableStateOf("")
    }

    var notes by remember {
        mutableStateOf("")
    }

    var rating by remember {
        mutableIntStateOf(0)
    }

    // -------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------

    var validationError by remember {
        mutableStateOf<String?>(null)
    }


    // -------------------------------------------------------------
    // ANDROID PHOTO PICKER
    // -------------------------------------------------------------

    val picturePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(5)
        ) { uris ->

            if (uris.isNotEmpty()) {
                selectedPictures = uris
            }
        }


    // -------------------------------------------------------------
    // ANDROID VIDEO PICKER
    // -------------------------------------------------------------

    val videoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(3)
        ) { uris ->

            if (uris.isNotEmpty()) {
                selectedVideos = uris
            }
        }


    // -------------------------------------------------------------
    // MAIN SCREEN
    // -------------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WinxWhite)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // ---------------------------------------------------------
            // HEADER
            // ---------------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Entry",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            // ---------------------------------------------------------
            // MEDIA OPTIONS
            // ---------------------------------------------------------

            Text(
                text = "Add to your journey",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // -----------------------------------------------------
                // PICTURES
                // -----------------------------------------------------

                MediaOption(
                    title = "Pictures",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Add pictures",
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    backgroundColor = WinxBlue,
                    onClick = {

                        picturePickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )


                // -----------------------------------------------------
                // VIDEOS
                // -----------------------------------------------------

                MediaOption(
                    title = "Videos",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = "Add videos",
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    backgroundColor = WinxPurple,
                    onClick = {

                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.VideoOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )


                // -----------------------------------------------------
                // COUNTRIES
                // -----------------------------------------------------

                MediaOption(
                    title = "Countries",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Add country",
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    backgroundColor = WinxOrange,
                    onClick = {
                        validationError = null
                        onCountriesClick()
                    },
                    modifier = Modifier.weight(1f)
                )
            }


            Spacer(
                modifier = Modifier.height(22.dp)
            )


            // ---------------------------------------------------------
            // MEDIA UPLOAD / PREVIEW AREA
            // ---------------------------------------------------------

            if (
                selectedPictures.isEmpty() &&
                selectedVideos.isEmpty()
            ) {

                // -----------------------------------------------------
                // EMPTY UPLOAD AREA
                // -----------------------------------------------------

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable {

                            picturePickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WinxDarkBlue
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(
                                    RoundedCornerShape(16.dp)
                                )
                                .background(WinxBlue),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Upload pictures",
                                tint = WinxWhite,
                                modifier = Modifier.size(30.dp)
                            )
                        }


                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )


                        Text(
                            text = "Add photos or videos",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = WinxWhite
                        )


                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )


                        Text(
                            text = "Tap here or choose Pictures above",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

            } else {

                // -----------------------------------------------------
                // SELECTED PICTURES
                // -----------------------------------------------------

                if (selectedPictures.isNotEmpty()) {

                    Column {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "${selectedPictures.size} picture(s) selected",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = WinxDarkBlue
                            )


                            TextButton(
                                onClick = {

                                    picturePickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            ) {

                                Text(
                                    text = "Add More",
                                    color = WinxBlue
                                )
                            }
                        }


                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            items(
                                items = selectedPictures
                            ) { uri ->

                                val bitmap = remember(uri) {

                                    context.contentResolver
                                        .openInputStream(uri)
                                        ?.use { inputStream ->

                                            BitmapFactory.decodeStream(
                                                inputStream
                                            )
                                        }
                                }


                                if (bitmap != null) {

                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Selected picture",
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(
                                                RoundedCornerShape(14.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }


                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }


                // -----------------------------------------------------
                // SELECTED VIDEOS
                // -----------------------------------------------------

                if (selectedVideos.isNotEmpty()) {

                    Column {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "${selectedVideos.size} video(s) selected",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = WinxDarkBlue
                            )


                            TextButton(
                                onClick = {

                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.VideoOnly
                                        )
                                    )
                                }
                            ) {

                                Text(
                                    text = "Add More",
                                    color = WinxBlue
                                )
                            }
                        }


                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        // -------------------------------------------------
                        // VIDEO PREVIEW
                        // -------------------------------------------------

                        key(selectedVideos.first()) {

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black
                                )
                            ) {

                                AndroidView(
                                    factory = { androidContext ->

                                        VideoView(androidContext).apply {

                                            setVideoURI(
                                                selectedVideos.first()
                                            )

                                            val mediaController =
                                                MediaController(androidContext)

                                            mediaController.setAnchorView(this)

                                            setMediaController(
                                                mediaController
                                            )

                                            setOnPreparedListener { player ->

                                                player.isLooping = true
                                            }

                                            start()
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }


                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )


                        Text(
                            text = "Tap the video controls to play or pause.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }


                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(24.dp)
            )


            // ---------------------------------------------------------
            // BASIC DETAILS
            // ---------------------------------------------------------

            Text(
                text = "Basic Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // ---------------------------------------------------------
            // ENTRY TITLE
            // ---------------------------------------------------------

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Entry Title")
                },
                placeholder = {
                    Text("e.g. Amazing day in Cape Town")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // ---------------------------------------------------------
            // LOCATION
            // ---------------------------------------------------------

            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    validationError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Location")
                },
                placeholder = {
                    Text("Where did you go?")
                },
                leadingIcon = {

                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // ---------------------------------------------------------
            // SELECTED COUNTRY
            // ---------------------------------------------------------

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = true,
                        onClick = {
                            validationError = null
                            onCountriesClick()
                        }
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WinxLightPink
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 15.dp
                        ),
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


                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Country",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = selectedCountry.ifEmpty {
                                "Select a country"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedCountry.isEmpty()) {
                                Color.Gray
                            } else {
                                WinxDarkBlue
                            }
                        )
                    }


                    TextButton(
                        onClick = {
                            validationError = null
                            onCountriesClick()
                        }
                    ) {

                        Text(
                            text = if (selectedCountry.isEmpty()) {
                                "Add"
                            } else {
                                "Change"
                            },
                            color = WinxBlue
                        )
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            // ---------------------------------------------------------
            // DATE
            // ---------------------------------------------------------

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WinxLightPink
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 15.dp
                        ),
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


                        Text(
                            text = displayDate,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WinxDarkBlue
                        )
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            // ---------------------------------------------------------
            // RATING
            // ---------------------------------------------------------

            Text(
                text = "Rate your experience",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = WinxDarkBlue
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                for (star in 1..5) {

                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Rating $star",
                        tint = if (star <= rating) {
                            WinxOrange
                        } else {
                            Color.LightGray
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {

                                rating = star
                                validationError = null
                            }
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            // ---------------------------------------------------------
            // NOTES
            // ---------------------------------------------------------

            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    validationError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                label = {
                    Text("Notes")
                },
                placeholder = {
                    Text("Tell us about your experience...")
                },
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )


            // ---------------------------------------------------------
            // VALIDATION ERROR
            // ---------------------------------------------------------

            if (validationError != null) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = validationError!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

            } else {

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }


            // ---------------------------------------------------------
            // ACTION BUTTONS
            // -------------------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // -----------------------------------------------------
                // CANCEL
                // -----------------------------------------------------

                TextButton(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f)
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = WinxDarkBlue,
                        modifier = Modifier.size(18.dp)
                    )


                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )


                    Text(
                        text = "Cancel",
                        color = WinxDarkBlue
                    )
                }


                // -----------------------------------------------------
                // SAVE
                // -----------------------------------------------------

                Button(
                    onClick = {

                        validationError = when {

                            title.isBlank() ->
                                "Please enter an entry title."

                            location.isBlank() ->
                                "Please enter a location."

                            selectedCountry.isBlank() ->
                                "Please select a country."

                            rating !in 1..5 ->
                                "Please select a rating from 1 to 5 stars."

                            notes.isBlank() ->
                                "Please enter some notes about your experience."

                            else -> null
                        }

                        if (validationError == null) {

                            val newEntry = TravelEntry(
                                title = title.trim(),
                                location = location.trim(),
                                country = selectedCountry.trim(),
                                date = entryDate,
                                rating = rating,
                                notes = notes.trim()
                            )

                            onSaveClick(newEntry)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WinxPurple
                    )
                ) {

                    Text(
                        text = "Save Entry",
                        color = WinxDarkBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }


        // -------------------------------------------------------------
        // BOTTOM NAVIGATION
        // -------------------------------------------------------------

        EntryBottomNavigation(
            onEntryClick = {},
            onCountriesClick = onCountriesClick
        )
    }
}


// =====================================================================
// MEDIA OPTION
// =====================================================================

@Composable
private fun MediaOption(
    title: String,
    icon: @Composable () -> Unit,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .height(92.dp)
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                contentAlignment = Alignment.Center
            ) {

                icon()
            }


            Spacer(
                modifier = Modifier.height(7.dp)
            )


            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (backgroundColor == WinxOrange) {
                    WinxDarkBlue
                } else {
                    WinxWhite
                }
            )
        }
    }
}


// =====================================================================
// ENTRY BOTTOM NAVIGATION
// =====================================================================

@Composable
private fun EntryBottomNavigation(
    onEntryClick: () -> Unit,
    onCountriesClick: () -> Unit
) {

    androidx.compose.material3.HorizontalDivider(
        color = Color(0xFFF0F0F0)
    )


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(68.dp)
            .background(WinxWhite),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {

        EntryNavigationItem(
            icon = Icons.Outlined.Image,
            label = "Entry",
            selected = true,
            onClick = onEntryClick
        )


        EntryNavigationItem(
            icon = Icons.Outlined.LocationOn,
            label = "Countries",
            onClick = onCountriesClick
        )
    }
}


// =====================================================================
// ENTRY NAVIGATION ITEM
// =====================================================================

@Composable
private fun EntryNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {

    TextButton(
        onClick = onClick
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) {
                    WinxBlue
                } else {
                    Color.Gray
                },
                modifier = Modifier.size(23.dp)
            )


            Spacer(
                modifier = Modifier.height(2.dp)
            )


            Text(
                text = label,
                fontSize = 10.sp,
                color = if (selected) {
                    WinxBlue
                } else {
                    Color.Gray
                }
            )
        }
    }
}