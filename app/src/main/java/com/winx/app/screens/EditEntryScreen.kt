package com.winx.app.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxOrange
import com.winx.app.ui.theme.WinxPurple
import com.winx.app.ui.theme.WinxWhite
import com.winx.app.ui.theme.WinxBlue

@Composable
fun EditEntryScreen(
    entry: TravelEntry,
    onBackClick: () -> Unit = {},
    onSaveClick: (TravelEntry) -> Unit = {}
) {

    // =============================================================
    // FORM STATE
    // =============================================================

    var title by remember {
        mutableStateOf(entry.title)
    }

    var location by remember {
        mutableStateOf(entry.location)
    }

    var country by remember {
        mutableStateOf(entry.country)
    }

    var date by remember {
        mutableStateOf(entry.date.take(10))
    }

    var notes by remember {
        mutableStateOf(entry.notes)
    }

    var rating by remember {
        mutableIntStateOf(entry.rating)
    }

    // Validation error message
    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = WinxDarkBlue,
        unfocusedTextColor = WinxDarkBlue,
        focusedLabelColor = WinxDarkBlue,
        unfocusedLabelColor = WinxDarkBlue,
        cursorColor = WinxDarkBlue,
        focusedBorderColor = WinxBlue,
        unfocusedBorderColor = WinxDarkBlue.copy(alpha = 0.45f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WinxWhite)
    ) {

        // =============================================================
        // HEADER
        // =============================================================

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
                text = "Edit Entry",
                color = WinxWhite,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // =============================================================
        // FORM
        // =============================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text(
                text = "Update your memory",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // ---------------------------------------------------------
            // TITLE
            // ---------------------------------------------------------

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it

                    // Clear validation message when user edits field
                    validationError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Entry Title")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
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

                    // Clear validation message when user edits field
                    validationError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Location")
                },
                leadingIcon = {

                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // ---------------------------------------------------------
            // COUNTRY
            // ---------------------------------------------------------

            OutlinedTextField(
                value = country,
                onValueChange = {
                    country = it

                    // Clear validation message when user edits field
                    validationError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Country")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // ---------------------------------------------------------
            // DATE
            // ---------------------------------------------------------

            OutlinedTextField(
                value = date,
                onValueChange = {
                    date = it
                    validationError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Date (yyyy-MM-dd)")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // ---------------------------------------------------------
            // RATING
            // ---------------------------------------------------------

            Text(
                text = "Rating",
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
                            .padding(1.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                for (star in 1..5) {

                    TextButton(
                        onClick = {
                            rating = star

                            // Clear validation message when rating selected
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "$star",
                            color = if (star == rating) {
                                WinxOrange
                            } else {
                                WinxDarkBlue
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
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

                    // Clear validation message when user edits field
                    validationError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                label = {
                    Text("Notes")
                },
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            // =========================================================
            // VALIDATION MESSAGE
            // =========================================================

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

            // =========================================================
            // ACTIONS
            // =========================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // -----------------------------------------------------
                // CANCEL
                // -----------------------------------------------------

                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Cancel",
                        color = WinxDarkBlue
                    )
                }

                // -----------------------------------------------------
                // UPDATE ENTRY
                // -----------------------------------------------------

                Button(
                    onClick = {

                        // =================================================
                        // VALIDATION
                        // =================================================

                        validationError = when {

                            title.isBlank() ->
                                "Please enter an entry title."

                            location.isBlank() ->
                                "Please enter a location."

                            country.isBlank() ->
                                "Please enter a country."

                            !date.matches(Regex("""\d{4}-\d{2}-\d{2}""")) ->
                                "Please enter the date in yyyy-MM-dd format."

                            rating !in 1..5 ->
                                "Please select a rating from 1 to 5 stars."

                            notes.isBlank() ->
                                "Please enter some notes about your experience."

                            else ->
                                null
                        }

                        // =================================================
                        // ONLY UPDATE IF VALID
                        // =================================================

                        if (validationError == null) {

                            val updatedEntry = entry.copy(
                                title = title.trim(),
                                location = location.trim(),
                                country = country.trim(),
                                date = date.trim(),
                                rating = rating,
                                notes = notes.trim()
                            )

                            onSaveClick(updatedEntry)
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
                        text = "Update Entry",
                        color = WinxDarkBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}