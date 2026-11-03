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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.winx.app.R
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxWhite

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    googleSignInLoading: Boolean = false,
    googleSignInError: String? = null
) {

    // -------------------------------------------------------------
    // FIREBASE AUTHENTICATION
    // -------------------------------------------------------------

    val firebaseAuth = remember {
        FirebaseAuth.getInstance()
    }


    // -------------------------------------------------------------
    // FORM STATE
    // -------------------------------------------------------------

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var loginLoading by remember {
        mutableStateOf(false)
    }

    var loginError by remember {
        mutableStateOf<String?>(null)
    }

    var resetMessage by remember {
        mutableStateOf<String?>(null)
    }


    // -------------------------------------------------------------
    // SCREEN
    // -------------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        // ---------------------------------------------------------
        // LOGO
        // ---------------------------------------------------------

        Image(
            painter = painterResource(
                id = R.drawable.winx_logo
            ),
            contentDescription = "Winx logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentScale = ContentScale.Fit
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Text(
            text = T("Capture places. Cherish moments.\n") +
                        "Remember every journey.",
            style = MaterialTheme.typography.bodyLarge,
            color = WinxDarkBlue
        )


        Spacer(
            modifier = Modifier.height(36.dp)
        )


        Text(
            text = T("Welcome Back !!"),
            style = MaterialTheme.typography.headlineMedium,
            color = WinxDarkBlue
        )


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // ---------------------------------------------------------
        // EMAIL
        // ---------------------------------------------------------

        OutlinedTextField(
            value = email,
            onValueChange = {

                email = it

                loginError = null
                resetMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(T("Email"))
            },
            singleLine = true,
            colors = winxFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ---------------------------------------------------------
        // PASSWORD
        // ---------------------------------------------------------

        OutlinedTextField(
            value = password,
            onValueChange = {

                password = it

                loginError = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(T("Password"))
            },
            singleLine = true,
            visualTransformation =
                PasswordVisualTransformation(),
            colors = winxFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // ---------------------------------------------------------
        // FORGOT PASSWORD
        // ---------------------------------------------------------

        TextButton(
            onClick = {

                val cleanEmail =
                    email.trim()

                loginError = null
                resetMessage = null

                when {

                    cleanEmail.isBlank() -> {

                        loginError =
                            "Enter your email address first."
                    }


                    !Patterns.EMAIL_ADDRESS
                        .matcher(cleanEmail)
                        .matches() -> {

                        loginError =
                            "Please enter a valid email address."
                    }


                    else -> {

                        firebaseAuth
                            .sendPasswordResetEmail(
                                cleanEmail
                            )
                            .addOnSuccessListener {

                                resetMessage =
                                    "Password reset email sent. Check your inbox."
                            }
                            .addOnFailureListener { exception ->

                                loginError =
                                    exception.localizedMessage
                                        ?: "Unable to send password reset email."
                            }
                    }
                }
            },
            modifier = Modifier.align(
                Alignment.End
            )
        ) {

            Text(
                text = T("Forgot Password?"),
                color = WinxBlue
            )
        }


        // ---------------------------------------------------------
        // LOGIN ERROR
        // ---------------------------------------------------------

        if (loginError != null) {

            Text(
                text = loginError!!,
                color =
                    MaterialTheme.colorScheme.error,
                style =
                    MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }


        // ---------------------------------------------------------
        // PASSWORD RESET SUCCESS
        // ---------------------------------------------------------

        if (resetMessage != null) {

            Text(
                text = resetMessage!!,
                color = WinxBlue,
                style =
                    MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // ---------------------------------------------------------
        // LOGIN BUTTON
        // ---------------------------------------------------------

        Button(
            onClick = {

                val cleanEmail =
                    email.trim()

                loginError = null
                resetMessage = null


                when {

                    cleanEmail.isBlank() -> {

                        loginError =
                            "Email is required."
                    }


                    !Patterns.EMAIL_ADDRESS
                        .matcher(cleanEmail)
                        .matches() -> {

                        loginError =
                            "Please enter a valid email address."
                    }


                    password.isBlank() -> {

                        loginError =
                            "Password is required."
                    }


                    else -> {

                        loginLoading = true

                        firebaseAuth
                            .signInWithEmailAndPassword(
                                cleanEmail,
                                password
                            )
                            .addOnCompleteListener { task ->

                                loginLoading = false

                                if (task.isSuccessful) {

                                    loginError = null

                                    onLoginClick()

                                } else {

                                    loginError =
                                        task.exception
                                            ?.localizedMessage
                                            ?: "Login failed. Check your email and password."
                                }
                            }
                    }
                }
            },

            enabled =
                !loginLoading &&
                        !googleSignInLoading,

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape =
                RoundedCornerShape(12.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor = WinxBlue,
                    contentColor = WinxWhite
                )
        ) {

            if (loginLoading) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.height(24.dp),
                    color = WinxWhite
                )

            } else {

                Text(
                    text = T("Login"),
                    style =
                        MaterialTheme.typography.labelLarge
                )
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        HorizontalDivider()


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Text(
            text = T("OR"),
            style =
                MaterialTheme.typography.bodyMedium,
            color = WinxDarkBlue
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // ---------------------------------------------------------
        // GOOGLE SIGN-IN
        // ---------------------------------------------------------

        OutlinedButton(
            onClick = {

                loginError = null
                resetMessage = null

                onGoogleSignInClick()
            },

            enabled =
                !googleSignInLoading &&
                        !loginLoading,

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape =
                RoundedCornerShape(12.dp)
        ) {

            if (googleSignInLoading) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.height(24.dp)
                )

            } else {

                Text(
                    text = T("Continue with Google"),
                    color = WinxDarkBlue
                )
            }
        }


        // ---------------------------------------------------------
        // GOOGLE ERROR
        // ---------------------------------------------------------

        if (googleSignInError != null) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = googleSignInError,
                color =
                    MaterialTheme.colorScheme.error,
                style =
                    MaterialTheme.typography.bodySmall
            )
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ---------------------------------------------------------
        // CREATE ACCOUNT
        // ---------------------------------------------------------

        TextButton(
            onClick = onCreateAccountClick,
            enabled =
                !loginLoading &&
                        !googleSignInLoading
        ) {

            Text(
                text = T("Create Account"),
                color = WinxDarkBlue
            )
        }
    }
}