package com.winx.app.screens

import com.winx.app.utils.T

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxLightPink
import com.winx.app.ui.theme.WinxOrange
import com.winx.app.ui.theme.WinxWhite

@Composable
fun EntriesScreen(
    entries: List<TravelEntry> = emptyList(),
    onBack: () -> Unit = {},
    onAddEntryClick: () -> Unit = {},
    onEntryClick: (TravelEntry) -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WinxWhite)
    ) {

        // ---------------------------------------------------------
        // HEADER
        // ---------------------------------------------------------

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WinxDarkBlue)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WinxWhite
                )
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = T("My Entries"),
                color = WinxWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = T("Your travel memories"),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            }
        }


        // ---------------------------------------------------------
        // ENTRIES
        // ---------------------------------------------------------

        if (entries.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "No entries",
                    tint = WinxBlue,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = T("No entries yet"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = T("Start capturing your journey by creating your first entry."),
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = onAddEntryClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WinxOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add entry"
                    )

                    Spacer(
                        modifier = Modifier.size(4.dp)
                    )

                    Text(
                        text = T("Add Entry"),
                        color = WinxDarkBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                items(entries) { entry ->

                    TravelEntryCard(
                        entry = entry,
                        onClick = {
                            onEntryClick(entry)
                        }
                    )
                }
            }
        }
    }
}


// =====================================================================
// TRAVEL ENTRY CARD
// =====================================================================

@Composable
private fun TravelEntryCard(
    entry: TravelEntry,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WinxLightPink
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = entry.title.ifBlank {
                    "Untitled Entry"
                },
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = WinxBlue
                )

                Spacer(
                    modifier = Modifier.size(3.dp)
                )

                Text(
                    text = if (
                        entry.location.isNotBlank() &&
                        entry.country.isNotBlank()
                    ) {
                        "${entry.location}, ${entry.country}"
                    } else if (entry.location.isNotBlank()) {
                        entry.location
                    } else if (entry.country.isNotBlank()) {
                        entry.country
                    } else {
                        "Location not specified"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = if (entry.rating > 0) {
                    "Rating: ${"★".repeat(entry.rating)}"
                } else {
                    "No rating"
                },
                fontSize = 14.sp,
                color = WinxOrange,
                fontWeight = FontWeight.SemiBold
            )

            if (entry.notes.isNotBlank()) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = entry.notes,
                    fontSize = 14.sp,
                    color = WinxDarkBlue
                )
            }
        }
    }
}