package com.winx.app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MediaPickerScreen(
    onMediaSelected: (Uri, String) -> Unit = { _, _ -> }
) {
    var selectedUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                selectedUri = uri
                onMediaSelected(uri, "Photo")
            }
        }

    val videoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                selectedUri = uri
                onMediaSelected(uri, "Video")
            }
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Add Memories"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = {
                    photoPickerLauncher.launch("image/*")
                }
            ) {
                Text("Add Photo")
            }

            Button(
                onClick = {
                    videoPickerLauncher.launch("video/*")
                }
            ) {
                Text("Add Video")
            }
        }

        selectedUri?.let {
            Text(
                text = "Selected: $it"
            )
        }
    }
}
