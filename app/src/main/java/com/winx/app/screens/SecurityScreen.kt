package com.winx.app.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "winx_security",
            Context.MODE_PRIVATE
        )
    }

    var currentPassword by remember {
        mutableStateOf("")
    }

    var newPassword by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    var facialRecognition by remember {
        mutableStateOf(
            preferences.getBoolean(
                "facial_recognition",
                false
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Security")
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

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security"
            )

            Text("Change Password")

            OutlinedTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    message = ""
                },
                label = {
                    Text("Current Password")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    message = ""
                },
                label = {
                    Text("New Password")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    message = ""
                },
                label = {
                    Text("Confirm New Password")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {

                    when {
                        currentPassword.isBlank() -> {
                            message = "Enter your current password."
                        }

                        newPassword.length < 6 -> {
                            message =
                                "New password must contain at least 6 characters."
                        }

                        newPassword != confirmPassword -> {
                            message = "Passwords do not match."
                        }

                        else -> {

                            val user =
                                FirebaseAuth
                                    .getInstance()
                                    .currentUser

                            if (user?.email == null) {

                                message =
                                    "Password changes require an email account."

                            } else {

                                val credential =
                                    EmailAuthProvider.getCredential(
                                        user.email!!,
                                        currentPassword
                                    )

                                user.reauthenticate(credential)
                                    .addOnSuccessListener {

                                        user.updatePassword(newPassword)
                                            .addOnSuccessListener {

                                                currentPassword = ""
                                                newPassword = ""
                                                confirmPassword = ""

                                                message =
                                                    "Password updated successfully."
                                            }
                                            .addOnFailureListener {
                                                message =
                                                    it.message
                                                        ?: "Unable to update password."
                                            }
                                    }
                                    .addOnFailureListener {
                                        message =
                                            "Current password is incorrect."
                                    }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Password")
            }

            if (message.isNotBlank()) {
                Text(message)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Biometric authentication"
                )

                Text(
                    text = "Biometric Authentication",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )

                Switch(
                    checked = facialRecognition,
                    onCheckedChange = { enabled ->

                        if (!enabled) {
                            facialRecognition = false

                            preferences.edit()
                                .putBoolean(
                                    "facial_recognition",
                                    false
                                )
                                .apply()

                            message =
                                "Biometric authentication disabled."

                        } else {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                                val activity =
                                    context as? Activity

                                if (activity == null) {
                                    message =
                                        "Unable to start biometric authentication."
                                    return@Switch
                                }

                                val executor =
                                    activity.mainExecutor

                                val biometricPrompt =
                                    android.hardware.biometrics.BiometricPrompt.Builder(
                                        activity
                                    )
                                        .setTitle(
                                            "Enable Biometric Authentication"
                                        )
                                        .setSubtitle(
                                            "Verify your identity"
                                        )
                                        .setDescription(
                                            "Use your registered biometric to enable biometric authentication for Winx."
                                        )
                                        .setNegativeButton(
                                            "Cancel",
                                            executor
                                        ) { _, _ ->
                                            message =
                                                "Biometric setup cancelled."
                                        }
                                        .build()

                                biometricPrompt.authenticate(
                                    android.os.CancellationSignal(),
                                    executor,
                                    object :
                                        android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {

                                        override fun onAuthenticationSucceeded(
                                            result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult
                                        ) {
                                            facialRecognition = true

                                            preferences.edit()
                                                .putBoolean(
                                                    "facial_recognition",
                                                    true
                                                )
                                                .apply()

                                            message =
                                                "Biometric authentication enabled."
                                        }

                                        override fun onAuthenticationFailed() {
                                            message =
                                                "Biometric verification failed."
                                        }

                                        override fun onAuthenticationError(
                                            errorCode: Int,
                                            errString: CharSequence
                                        ) {
                                            message =
                                                errString.toString()
                                        }
                                    }
                                )

                            } else {
                                message =
                                    "Biometric authentication requires Android 9 or later."
                            }
                        }
                    }
                )
            }

            Text(
                text =
                    if (facialRecognition) {
                        "Biometric authentication is enabled."
                    } else {
                        "Biometric authentication is disabled."
                    }
            )
        }
    }
}
