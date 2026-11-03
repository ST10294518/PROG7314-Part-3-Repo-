package com.winx.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyDeletedScreen(
    onBack: () -> Unit
) {

    val deletedItems = remember {

        mutableStateListOf(
            "Ithaa Undersea Restaurant",
            "El Diablo Restaurant",
            "Modern Toilet Restaurant",
            "Dinner in the Sky"
        )
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Recently Deleted")
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

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Items are deleted for 30 days."
            )

            Text(
                text = "After 30 days, deleted items can no longer be restored."
            )


            deletedItems.forEach { item ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deleted"
                        )

                        Text(
                            text = item,

                            modifier = Modifier
                                .padding(start = 12.dp)
                                .weight(1f)
                        )

                        Button(
                            onClick = {
                                deletedItems.remove(item)
                            }
                        ) {

                            Text("Restore")
                        }
                    }
                }
            }


            if (deletedItems.isNotEmpty()) {

                Button(

                    onClick = {
                        deletedItems.clear()
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Empty Deleted Items")
                }
            }
        }
    }
}