package com.winx.app.screens

import com.winx.app.utils.T

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    onBack: () -> Unit,
    onLanguageChanged: () -> Unit = {}
) {

    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "winx_settings",
            Context.MODE_PRIVATE
        )
    }

    val languages = listOf(
        "English (US)",
        "isiZulu (Zulu)",
        "Afrikaans"
    )

    var selectedLanguage by remember {

        mutableStateOf(
            preferences.getString(
                "language",
                "English (US)"
            ) ?: "English (US)"
        )
    }

    var saved by remember {
        mutableStateOf(false)
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(T("Language"))
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

            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Language"
            )

            Text(
                text = T("Change App Language")
            )

            Text(
                text = T("Select the language you would like to use throughout the app.")
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )


            languages.forEach { language ->

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(

                        selected =
                            selectedLanguage == language,

                        onClick = {

                            selectedLanguage = language
                            saved = false
                        }
                    )

                    Text(
                        text = language
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            Button(

                onClick = {

                    preferences
                        .edit()
                        .putString(
                            "language",
                            selectedLanguage
                        )
                        .apply()

                    saved = true
                    onLanguageChanged()
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(T("Save Language"))
            }


            if (saved) {

                Text(
                    text = T("Language preference saved.")
                )
            }
        }
    }
}