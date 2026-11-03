package com.winx.app.screens

import com.winx.app.utils.T
import com.winx.app.utils.winxFieldColors

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.winx.app.R
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxWhite

@Composable
fun RegisterScreen(
    onCreateAccountClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val firebaseAuth = remember {
        FirebaseAuth.getInstance()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.winx_logo),
            contentDescription = "Winx logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = T("Capture places. Cherish moments.\nRemember every journey."),
            style = MaterialTheme.typography.bodyLarge,
            color = WinxDarkBlue
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = T("Create your account"),
            style = MaterialTheme.typography.headlineMedium,
            color = WinxDarkBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(T("Username"))
            },
            singleLine = true,
            enabled = !isLoading,
            colors = winxFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(T("Email"))
            },
            singleLine = true,
            enabled = !isLoading,
            colors = winxFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(T("Password"))
            },
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            colors = winxFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(T("Confirm Password"))
            },
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            colors = winxFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                val cleanUsername = username.trim()
                val cleanEmail = email.trim()

                when {
                    cleanUsername.isBlank() -> {
                        errorMessage = "Please enter a username."
                    }

                    cleanEmail.isBlank() -> {
                        errorMessage = "Please enter an email address."
                    }

                    !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> {
                        errorMessage = "Please enter a valid email address."
                    }

                    password.isBlank() -> {
                        errorMessage = "Please enter a password."
                    }

                    password.length < 6 -> {
                        errorMessage =
                            "Password must contain at least 6 characters."
                    }

                    confirmPassword.isBlank() -> {
                        errorMessage = "Please confirm your password."
                    }

                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match."
                    }

                    else -> {
                        isLoading = true
                        errorMessage = null

                        firebaseAuth
                            .createUserWithEmailAndPassword(
                                cleanEmail,
                                password
                            )
                            .addOnCompleteListener { task ->

                                if (task.isSuccessful) {

                                    val firebaseUser =
                                        firebaseAuth.currentUser

                                    if (firebaseUser != null) {

                                        val profileUpdates =
                                            UserProfileChangeRequest.Builder()
                                                .setDisplayName(cleanUsername)
                                                .build()

                                        firebaseUser
                                            .updateProfile(profileUpdates)
                                            .addOnCompleteListener {
                                                isLoading = false

                                                // Registration succeeded.
                                                // Only now may the user enter the app.
                                                onCreateAccountClick()
                                            }

                                    } else {
                                        isLoading = false
                                        errorMessage =
                                            "Account was created, but the user could not be loaded."
                                    }

                                } else {
                                    isLoading = false

                                    errorMessage =
                                        task.exception?.localizedMessage
                                            ?: "Account creation failed. Please try again."
                                }
                            }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WinxBlue,
                contentColor = WinxWhite
            )
        ) {

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    strokeWidth = 2.dp,
                    color = WinxWhite
                )
            } else {
                Text(
                    text = T("Create Account"),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}