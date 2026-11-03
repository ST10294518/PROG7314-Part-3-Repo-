package com.winx.app.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.winx.app.R
import kotlinx.coroutines.tasks.await


class GoogleAuthManager(
    private val context: Context
) {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val credentialManager =
        CredentialManager.create(context)

    suspend fun signInWithGoogle(): Result<String> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(
                    context.getString(R.string.default_web_client_id)
                )
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential

            if (
                credential.type ==
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential =
                    GoogleIdTokenCredential.createFrom(
                        credential.data
                    )

                val firebaseCredential =
                    GoogleAuthProvider.getCredential(
                        googleCredential.idToken,
                        null
                    )

                val authResult = firebaseAuth
                    .signInWithCredential(firebaseCredential)
                    .await()

                val user = authResult.user

                if (user != null) {
                    Result.success(
                        user.email ?: user.uid
                    )
                } else {
                    Result.failure(
                        Exception("Firebase user was not returned.")
                    )
                }
            } else {
                Result.failure(
                    Exception("Unexpected Google credential type.")
                )
            }

        } catch (exception: GetCredentialException) {
            Result.failure(exception)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}