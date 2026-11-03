package com.winx.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("About Us")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Text(
                text = "WINX",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Our Story",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "WINX was created for explorers, food lovers, and memory collectors. Every trip is more than a destination — it is a collection of moments, flavours, places and experiences worth remembering."
            )

            Text(
                text = "Our Mission",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Our mission is to help users capture their journeys, organise their travel memories and keep meaningful experiences available for the future."
            )

            Text(
                text = "Why We Exist",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Every trip has a story worth telling. WINX provides a place to document experiences, discover places and build a personal collection of memories."
            )

            Text(
                text = "Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "• Travel entries\n• Countries and destinations\n• Photos and videos\n• Calendar planning\n• Cloud API communication\n• Personal settings and security"
            )

            Text(
                text =
                    "Thank you for being part of the WINX community."
            )
        }
    }
}
