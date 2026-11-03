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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val WinxPink = Color(0xFFF4B8D0)
private val WinxBlue = Color(0xFF12A4D0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onRecentlyDeletedClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onLogout: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = T("Settings"),
                        fontWeight = FontWeight.Bold
                    )
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
                .background(WinxPink)
                .padding(paddingValues)
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                ),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // -----------------------------------------------------
            // SETTINGS TITLE
            // -----------------------------------------------------

            Text(
                text = T("Settings"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = T("Manage your Winx preferences."),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )


            // -----------------------------------------------------
            // PROFILE
            // -----------------------------------------------------

            SettingsItem(
                icon = Icons.Default.Person,
                title = "Profile",
                description = "View and manage your personal information.",
                onClick = onProfileClick
            )


            // -----------------------------------------------------
            // LANGUAGE
            // -----------------------------------------------------

            SettingsItem(
                icon = Icons.Default.Language,
                title = "Language",
                description = "Choose your preferred language.",
                onClick = onLanguageClick
            )


            // -----------------------------------------------------
            // RECENTLY DELETED
            // -----------------------------------------------------

            SettingsItem(
                icon = Icons.Default.RestoreFromTrash,
                title = "Recently Deleted",
                description = "View and restore recently deleted items.",
                onClick = onRecentlyDeletedClick
            )


            // -----------------------------------------------------
            // SECURITY
            // -----------------------------------------------------

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Security",
                description = "Manage your security and privacy settings.",
                onClick = onSecurityClick
            )


            // -----------------------------------------------------
            // NOTIFICATIONS
            // -----------------------------------------------------

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "View and manage your notifications.",
                onClick = onNotificationsClick
            )


            // -----------------------------------------------------
            // ABOUT US
            // -----------------------------------------------------

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About Us",
                description = "Learn more about Winx and our mission.",
                onClick = onAboutUsClick
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            // -----------------------------------------------------
            // LOG OUT
            // -----------------------------------------------------

            OutlinedButton(

                onClick = onLogout,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(24.dp)

            ) {

                Text(
                    text = T("Log Out"),
                    color = WinxBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// =================================================================
// SETTINGS ITEM
// =================================================================

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(8.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            // -----------------------------------------------------
            // ICON
            // -----------------------------------------------------

            Icon(

                imageVector = icon,

                contentDescription = title,

                tint = WinxBlue,

                modifier = Modifier.size(28.dp)
            )


            Spacer(
                modifier = Modifier.size(12.dp)
            )


            // -----------------------------------------------------
            // TEXT
            // -----------------------------------------------------

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            // -----------------------------------------------------
            // ARROW
            // -----------------------------------------------------

            Icon(

                imageVector = Icons.Default.ChevronRight,

                contentDescription = "Open $title"
            )
        }
    }
}