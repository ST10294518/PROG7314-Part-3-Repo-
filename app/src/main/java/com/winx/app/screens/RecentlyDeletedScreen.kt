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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyDeletedScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "winx_deleted_items",
            Context.MODE_PRIVATE
        )
    }

    val deletedItems = remember {
        mutableStateListOf<String>().apply {
            val stored = preferences.getStringSet(
                "items",
                emptySet()
            ) ?: emptySet()

            addAll(stored)
        }
    }

    var showEmptyDialog by remember {
        mutableStateOf(false)
    }

    fun saveItems() {
        preferences.edit()
            .putStringSet(
                "items",
                deletedItems.toSet()
            )
            .apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Recently Deleted")
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Recently deleted items are kept here until permanently removed."
            )

            if (deletedItems.isEmpty()) {

                Text(
                    text = "Your Recently Deleted folder is empty."
                )

            } else {

                deletedItems.toList().forEach { item ->

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
                                    saveItems()
                                }
                            ) {
                                Text("Restore")
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        showEmptyDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Empty Deleted Items")
                }
            }
        }
    }

    if (showEmptyDialog) {

        AlertDialog(
            onDismissRequest = {
                showEmptyDialog = false
            },
            title = {
                Text("Empty Recently Deleted?")
            },
            text = {
                Text(
                    "This will permanently remove all items from the Recently Deleted list."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletedItems.clear()
                        saveItems()
                        showEmptyDialog = false
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEmptyDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
