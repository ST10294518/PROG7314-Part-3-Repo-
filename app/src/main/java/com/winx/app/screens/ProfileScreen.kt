package com.winx.app.screens

import com.winx.app.utils.T
import com.winx.app.utils.winxFieldColors

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "winx_profile",
            Context.MODE_PRIVATE
        )
    }

    val user = FirebaseAuth.getInstance().currentUser

    var name by remember {
        mutableStateOf(
            preferences.getString(
                "name",
                user?.displayName ?: ""
            ) ?: ""
        )
    }

    val email = user?.email ?: ""

    var phone by remember {
        mutableStateOf(
            preferences.getString(
                "phone",
                ""
            ) ?: ""
        )
    }

    var dateOfBirth by remember {
        mutableStateOf(
            preferences.getString(
                "date_of_birth",
                ""
            ) ?: ""
        )
    }

    var country by remember {
        mutableStateOf(
            preferences.getString(
                "country",
                ""
            ) ?: ""
        )
    }

    var editing by remember {
        mutableStateOf(false)
    }

    var saved by remember {
        mutableStateOf(false)
    }

    var profileError by remember {
        mutableStateOf("")
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(T("Profile"))
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
                },

                actions = {

                    IconButton(
                        onClick = {
                            editing = !editing
                            saved = false
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile"
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

            // -----------------------------------------------------
            // PROFILE ICON
            // -----------------------------------------------------

            Icon(

                imageVector = Icons.Default.Person,

                contentDescription = "Profile",

                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape),

                tint = MaterialTheme.colorScheme.primary
            )


            Text(
                text = T("View and manage your personal information."),
                style = MaterialTheme.typography.bodySmall
            )


            // -----------------------------------------------------
            // NAME
            // -----------------------------------------------------

            OutlinedTextField(

                value = name,

                onValueChange = {
                    name = it
                    saved = false
                    profileError = ""
                },

                label = {
                    Text(T("Full Name"))
                },

                enabled = editing,

                modifier = Modifier.fillMaxWidth(),
                colors = winxFieldColors()
            )


            // -----------------------------------------------------
            // EMAIL
            // -----------------------------------------------------

            OutlinedTextField(

                value = email,

                onValueChange = {},

                label = {
                    Text(T("Email Address"))
                },

                enabled = false,

                modifier = Modifier.fillMaxWidth(),
                colors = winxFieldColors()
            )


            // -----------------------------------------------------
            // PHONE
            // -----------------------------------------------------

            OutlinedTextField(

                value = phone,

                onValueChange = {
                    phone = it
                    saved = false
                    profileError = ""
                },

                label = {
                    Text(T("Phone Number"))
                },

                enabled = editing,

                modifier = Modifier.fillMaxWidth(),
                colors = winxFieldColors()
            )


            // -----------------------------------------------------
            // DATE OF BIRTH
            // -----------------------------------------------------

            OutlinedTextField(

                value = dateOfBirth,

                onValueChange = {
                    dateOfBirth = it
                    saved = false
                    profileError = ""
                },

                label = {
                    Text(T("Date of Birth"))
                },

                enabled = editing,

                modifier = Modifier.fillMaxWidth(),
                colors = winxFieldColors()
            )


            // -----------------------------------------------------
            // COUNTRY
            // -----------------------------------------------------

            OutlinedTextField(

                value = country,

                onValueChange = {
                    country = it
                    saved = false
                    profileError = ""
                },

                label = {
                    Text(T("Country"))
                },

                enabled = editing,

                modifier = Modifier.fillMaxWidth(),
                colors = winxFieldColors()
            )


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            // -----------------------------------------------------
            // SAVE PROFILE
            // -----------------------------------------------------

            if (editing) {

                Button(

                    onClick = {

                        val normalizedPhone = phone.filter { it.isDigit() || it == '+' }
                        val phoneDigits = normalizedPhone.count { it.isDigit() }

                        when {
                            name.trim().isBlank() -> {
                                saved = false
                                profileError = "Please enter your full name."
                            }
                            phoneDigits !in 7..15 -> {
                                saved = false
                                profileError = "Enter a valid phone number (7–15 digits)."
                            }
                            country.trim().isBlank() -> {
                                saved = false
                                profileError = "Please enter your country."
                            }
                            else -> {
                                phone = normalizedPhone
                                profileError = ""

                                preferences
                            .edit()
                            .putString(
                                "name",
                                name
                            )
                            .putString(
                                "phone",
                                phone
                            )
                            .putString(
                                "date_of_birth",
                                dateOfBirth
                            )
                            .putString(
                                "country",
                                country
                            )
                            .apply()


                        user?.updateProfile(

                            UserProfileChangeRequest
                                .Builder()
                                .setDisplayName(name)
                                .build()

                        )?.addOnCompleteListener {

                            editing = false
                            saved = true
                        }
                            }
                        }
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(T("Save Profile"))
                }
            }


            if (profileError.isNotBlank()) {
                Text(
                    text = profileError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }


            // -----------------------------------------------------
            // SUCCESS MESSAGE
            // -----------------------------------------------------

            if (saved) {

                Text(
                    text = T("Profile saved successfully."),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}