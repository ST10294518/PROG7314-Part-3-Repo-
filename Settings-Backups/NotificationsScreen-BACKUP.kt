package com.winx.app.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {

    val context = androidx.compose.ui.platform.LocalContext.current

    val preferences = remember {

        context.getSharedPreferences(
            "winx_settings",
            Context.MODE_PRIVATE
        )
    }


    var notificationsEnabled by remember {

        mutableStateOf(
            preferences.getBoolean(
                "notifications_enabled",
                true
            )
        )
    }


    val notifications = listOf(

        "New Entry Added" to
                "Your restaurant entry was saved successfully.",

        "Video Uploaded" to
                "Your video has been uploaded successfully.",

        "Country Added" to
                "You added a new country to your countries.",

        "Reminder" to
                "Don't forget to capture your dining experience."
    )


    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Notifications")
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
                .padding(20.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {


            // -----------------------------------------------------
            // NOTIFICATION TOGGLE
            // -----------------------------------------------------

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Notifications,

                        contentDescription =
                            "Notifications"
                    )


                    Text(

                        text = "Stay Updated",

                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )


                    Switch(

                        checked =
                            notificationsEnabled,

                        onCheckedChange = {

                            notificationsEnabled = it

                            preferences
                                .edit()
                                .putBoolean(
                                    "notifications_enabled",
                                    it
                                )
                                .apply()
                        }
                    )
                }
            }


            Text(
                text = "Recent Notifications"
            )


            // -----------------------------------------------------
            // NOTIFICATION LIST
            // -----------------------------------------------------

            notifications.forEach { notification ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(

                        modifier = Modifier.padding(16.dp),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Notifications,

                            contentDescription =
                                "Notification"
                        )


                        Column(

                            modifier = Modifier.padding(
                                start = 12.dp
                            )
                        ) {

                            Text(
                                text = notification.first
                            )

                            Text(
                                text = notification.second
                            )
                        }
                    }
                }
            }
        }
    }
}