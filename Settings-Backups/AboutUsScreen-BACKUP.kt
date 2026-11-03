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

                    IconButton(
                        onClick = onBack
                    ) {

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

            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {


            // -----------------------------------------------------
            // WINX
            // -----------------------------------------------------

            Text(

                text = "Winx",

                style =
                    MaterialTheme
                        .typography
                        .displaySmall
            )


            // -----------------------------------------------------
            // OUR STORY
            // -----------------------------------------------------

            Text(
                text = "Our Story"
            )

            Text(
                text = "WINX was created for explorers, food lovers, and memory collectors. We know that every trip is more than just a destination — it is a collection of moments, flavours, and experiences that deserve to be remembered."
            )


            // -----------------------------------------------------
            // OUR MISSION
            // -----------------------------------------------------

            Text(
                text = "Our Mission"
            )

            Text(
                text = "Our mission is to help you capture every moment of your journey and keep those memories alive forever."
            )


            // -----------------------------------------------------
            // WHY WE EXIST
            // -----------------------------------------------------

            Text(
                text = "Why We Exist"
            )

            Text(
                text = "We believe that every trip has a story worth telling. WINX makes it easier for you to document your adventures, discover new places, and inspire others to explore the world."
            )


            // -----------------------------------------------------
            // WE'RE HERE FOR YOU
            // -----------------------------------------------------

            Text(
                text = "We're Here For You"
            )

            Text(
                text = "Your journey matters to us. WINX is designed to help make every trip more meaningful, organised, and unforgettable."
            )


            Text(
                text = "Thank you for being part of our community."
            )
        }
    }
}