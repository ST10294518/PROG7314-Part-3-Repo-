package com.winx.app.navigation

import android.util.Log
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.winx.app.auth.GoogleAuthManager
import com.winx.app.data.remote.TravelEntryRepository
import com.winx.app.data.remote.MediaRepository

import com.winx.app.screens.AboutUsScreen
import com.winx.app.screens.CalendarScreen
import com.winx.app.screens.CountriesScreen
import com.winx.app.screens.DashboardScreen
import com.winx.app.screens.EditEntryScreen
import com.winx.app.screens.EntriesScreen
import com.winx.app.screens.EntryDetailsScreen
import com.winx.app.screens.EntryScreen
import com.winx.app.screens.LanguageScreen
import com.winx.app.screens.LibraryScreen
import com.winx.app.screens.LoginScreen
import com.winx.app.screens.NotificationsScreen
import com.winx.app.screens.ProfileScreen
import com.winx.app.screens.RecentlyDeletedScreen
import com.winx.app.screens.RegisterScreen
import com.winx.app.screens.SecurityScreen
import com.winx.app.screens.SettingsScreen
import com.winx.app.screens.TravelEntry
import com.winx.app.screens.WelcomeScreen
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    // =============================================================
    // CONTEXT / COROUTINES
    // =============================================================

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val mediaRepository = remember { MediaRepository() }

    // =============================================================
    // GOOGLE AUTHENTICATION
    // =============================================================

    val googleAuthManager = remember(context) {
        GoogleAuthManager(context)
    }

    var googleSignInLoading by remember {
        mutableStateOf(false)
    }

    var googleSignInError by remember {
        mutableStateOf<String?>(null)
    }

    // Forces the current Compose destination to refresh after a language change.
    var languageRefreshKey by remember { mutableIntStateOf(0) }

    // =============================================================
    // REST API REPOSITORY
    // =============================================================

    val repository = remember(context) {
    TravelEntryRepository(context)
}

    // =============================================================
    // SELECTED COUNTRY
    // =============================================================

    var selectedCountry by remember {
        mutableStateOf("")
    }

    // =============================================================
    // SAVED ENTRIES
    // =============================================================

    var entries by remember {
        mutableStateOf<List<TravelEntry>>(emptyList())
    }

    // =============================================================
    // CURRENT ENTRY
    // =============================================================

    var selectedEntry by remember {
        mutableStateOf<TravelEntry?>(null)
    }

    // =============================================================
    // API ERROR
    // =============================================================

    var apiError by remember {
        mutableStateOf<String?>(null)
    }

    var entriesLoading by remember {
        mutableStateOf(false)
    }

    var syncInProgress by remember {
    mutableStateOf(false)
}

    // =============================================================
    // DELETE CONFIRMATION
    // =============================================================

    var entryToDelete by remember {
        mutableStateOf<TravelEntry?>(null)
    }

    // =============================================================
    // LOAD ENTRIES FROM REST API
    // =============================================================

    fun loadEntries() {
        // Set this before launching so repeated taps cannot queue overlapping loads.
        if (entriesLoading) return
        entriesLoading = true

        coroutineScope.launch {
            try {
                Log.d("AppNavigation", "Refreshing travel entries")

                if (!syncInProgress) {
                    syncInProgress = true
                    try {
                        // Reconcile uploads first: they may produce pending deletions.
                        repository.syncPendingNewEntries()
                            .onSuccess { count ->
                                Log.d("AppNavigation", "Pending uploads synced: $count")
                            }
                            .onFailure { error ->
                                Log.w("AppNavigation", "Upload sync failed", error)
                            }

                        // Also processes deletions recovered during the upload pass.
                        repository.syncPendingDeletions()
                            .onSuccess { count ->
                                Log.d("AppNavigation", "Pending deletions synced: $count")
                            }
                            .onFailure { error ->
                                Log.w("AppNavigation", "Deletion sync failed", error)
                            }

                        // Travel entries must have server IDs before queued media can upload.
                        mediaRepository.syncPendingMediaUploads(context.applicationContext)
                            .onSuccess { count ->
                                Log.d("AppNavigation", "Pending media synced: $count")
                            }
                            .onFailure { error ->
                                Log.w("AppNavigation", "Media sync failed", error)
                            }
                    } finally {
                        syncInProgress = false
                    }
                }

                repository.getEntries()
                    .onSuccess { loaded ->
                        entries = loaded

                        // Keep the selected entry aligned with Room/API refreshes.
                        // An offline entry changes from a negative local ID to a
                        // positive server ID after successful synchronisation.
                        selectedEntry?.let { selected ->
                            val refreshed = selected.clientId
                                ?.let { uuid -> loaded.find { it.clientId == uuid } }
                                ?: loaded.find { it.id == selected.id }
                            if (refreshed != null) {
                                selectedEntry = refreshed
                            }
                        }

                        apiError = null
                        Log.d("AppNavigation", "Entries loaded: ${loaded.size}")
                    }
                    .onFailure { error ->
                        apiError = error.message ?: "Unable to load travel entries."
                        Log.e("AppNavigation", "Failed to load entries", error)
                    }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                apiError = error.message ?: "Unable to refresh travel entries."
                Log.e("AppNavigation", "Unexpected refresh failure", error)
            } finally {
                entriesLoading = false
            }
        }
    }

    // Keep the latest refresh function available to lifecycle/network callbacks.
    val refreshEntries = rememberUpdatedState(newValue = { loadEntries() })

    // Refresh on returning to the app and when Android reports a network return.
    // This is foreground synchronization; WorkManager is needed for closed-app jobs.
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshEntries.value()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val connectivityManager = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                coroutineScope.launch { refreshEntries.value() }
            }
        }
        var registered = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                connectivityManager?.registerDefaultNetworkCallback(callback)
                registered = connectivityManager != null
            } catch (error: SecurityException) {
                Log.w("AppNavigation", "Network monitoring requires ACCESS_NETWORK_STATE", error)
            } catch (error: Exception) {
                Log.w("AppNavigation", "Unable to register network callback", error)
            }
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (registered) {
                try {
                    connectivityManager?.unregisterNetworkCallback(callback)
                } catch (error: Exception) {
                    Log.w("AppNavigation", "Unable to unregister network callback", error)
                }
            }
        }
    }

    // =============================================================
    // NAVIGATION
    // =============================================================

    @Suppress("UNUSED_VARIABLE")
    val currentLanguageRefresh = languageRefreshKey

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {

        // =========================================================
        // WELCOME
        // =========================================================

        composable("welcome") {

            WelcomeScreen(
                onBeginClick = {
                    navController.navigate("login")
                }
            )
        }

        // =========================================================
        // LOGIN
        // =========================================================

        composable("login") {

            LoginScreen(

                onLoginClick = {

                    navController.navigate("dashboard") {

                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },

                onCreateAccountClick = {
                    navController.navigate("register")
                },

                onGoogleSignInClick = {

                    if (!googleSignInLoading) {

                        googleSignInLoading = true
                        googleSignInError = null

                        coroutineScope.launch {

                            val result =
                                googleAuthManager.signInWithGoogle()

                            result
                                .onSuccess {

                                    googleSignInLoading = false

                                    navController.navigate("dashboard") {

                                        popUpTo("login") {
                                            inclusive = true
                                        }
                                    }
                                }
                                .onFailure { exception ->

                                    googleSignInLoading = false

                                    googleSignInError =
                                        exception.message
                                            ?: "Google sign-in failed."
                                }
                        }
                    }
                },

                googleSignInLoading = googleSignInLoading,

                googleSignInError = googleSignInError
            )
        }

        // =========================================================
        // REGISTER
        // =========================================================

        composable("register") {

            RegisterScreen(

                onCreateAccountClick = {

                    navController.navigate("login") {

                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // =========================================================
        // DASHBOARD
        // =========================================================

        composable("dashboard") {

            LaunchedEffect(Unit) {
                loadEntries()
            }

            DashboardScreen(
                entries = entries,
                isLoadingEntries = entriesLoading,
                entriesError = apiError,
                onRetryEntries = {
                    loadEntries()
                },
                onEntryDetailsClick = { entry ->
                    selectedEntry = entry
                    navController.navigate("entryDetails")
                },

                onAddEntryClick = {
                    navController.navigate("entry")
                },

                onAddCountryClick = {
                    navController.navigate("countries")
                },

                onEntryClick = {

                    navController.navigate("entries")
                },

                onCalendarClick = {

                    navController.navigate("calendar") {
                        launchSingleTop = true
                    }
                },

                onLibraryClick = {

                    navController.navigate("library") {
                        launchSingleTop = true
                    }
                },

                onSettingsClick = {
                    navController.navigate("settings")
                },

                onNotificationsClick = {
                    navController.navigate("notifications")
                }
            )
        }

        // =========================================================
        // SETTINGS
        // =========================================================

        composable("settings") {

            SettingsScreen(

                onBack = {
                    navController.popBackStack()
                },

                onProfileClick = {
                    navController.navigate("profile")
                },

                onLanguageClick = {
                    navController.navigate("language")
                },

                onRecentlyDeletedClick = {
                    navController.navigate("recentlyDeleted")
                },

                onSecurityClick = {
                    navController.navigate("security")
                },

                onNotificationsClick = {
                    navController.navigate("notifications")
                },

                onAboutUsClick = {
                    navController.navigate("aboutUs")
                },

                onLogout = {

                    googleAuthManager.signOut()

                    navController.navigate("welcome") {

                        popUpTo("settings") {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        // =========================================================
        // PROFILE
        // =========================================================

        composable("profile") {

            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // =========================================================
        // LANGUAGE
        // =========================================================

        composable("language") {

            LanguageScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLanguageChanged = {
                    languageRefreshKey++
                }
            )
        }

        // =========================================================
        // RECENTLY DELETED
        // =========================================================

        composable("recentlyDeleted") {

            RecentlyDeletedScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // =========================================================
        // SECURITY
        // =========================================================

        composable("security") {

            SecurityScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // =========================================================
        // NOTIFICATIONS
        // =========================================================

        composable("notifications") {

            NotificationsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // =========================================================
        // ABOUT US
        // =========================================================

        composable("aboutUs") {

            AboutUsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // =========================================================
        // MY ENTRIES
        // =========================================================

        composable("entries") {

            LaunchedEffect(Unit) {
                loadEntries()
            }

            EntriesScreen(

                entries = entries,

                onBack = {
                    navController.popBackStack()
                },

                onAddEntryClick = {
                    navController.navigate("entry")
                },

                onEntryClick = { entry ->

                    selectedEntry = entry

                    navController.navigate("entryDetails")
                }
            )
        }

        // =========================================================
        // ENTRY DETAILS
        // =========================================================

        composable("entryDetails") {

            selectedEntry?.let { entry ->

                EntryDetailsScreen(

                    entry = entry,

                    onBackClick = {
                        navController.popBackStack()
                    },

                    // -------------------------------------------------
                    // EDIT
                    // -------------------------------------------------

                    onEditClick = {

                        navController.navigate("editEntry")
                    },

                    // -------------------------------------------------
                    // DELETE
                    // -------------------------------------------------

                    onDeleteClick = {

                        entryToDelete = entry
                    }
                )
            }
        }

        // =========================================================
        // EDIT ENTRY
        // =========================================================

        composable("editEntry") {

            selectedEntry?.let { entry ->

                EditEntryScreen(

                    entry = entry,

                    onBackClick = {
                        navController.popBackStack()
                    },

                    onSaveClick = { updatedEntry ->

                        coroutineScope.launch {

                            Log.d(
                                "AppNavigation",
                                "Updating entry ID=${updatedEntry.id}"
                            )

                            repository
                                .updateEntry(updatedEntry)
                                .onSuccess {

                                    Log.d(
                                        "AppNavigation",
                                        "Entry updated successfully. ID=${updatedEntry.id}"
                                    )

                                    // Update local state.
                                    entries =
                                        entries.map { existingEntry ->

                                            if (
                                                existingEntry.id ==
                                                updatedEntry.id
                                            ) {
                                                updatedEntry
                                            } else {
                                                existingEntry
                                            }
                                        }

                                    selectedEntry = updatedEntry

                                    apiError = null

                                    // Return to Entry Details.
                                    navController.popBackStack()
                                }
                                .onFailure { exception ->

                                    Log.e(
                                        "AppNavigation",
                                        "Failed to update entry ID=${updatedEntry.id}",
                                        exception
                                    )

                                    apiError =
                                        exception.message
                                            ?: "Unable to update the entry."
                                }
                        }
                    }
                )
            }
        }

        // =========================================================
        // CREATE ENTRY
        // =========================================================

        composable("entry") {

            EntryScreen(

                selectedCountry = selectedCountry,

                onCountriesClick = {
                    navController.navigate("countries")
                },

                // -------------------------------------------------
                // CREATE THROUGH REST API
                // -------------------------------------------------

                onSaveClick = { newEntry ->
                    coroutineScope.launch {
                        Log.d("AppNavigation", "Creating new entry: ${newEntry.title}")

                        val entryWithClientId = newEntry.copy(
                            clientId = newEntry.clientId ?: UUID.randomUUID().toString()
                        )

                        repository.createEntry(entryWithClientId)
                            .onSuccess { createdEntry ->
                                Log.d(
                                    "AppNavigation",
                                    "Entry created successfully. ID=${createdEntry.id}"
                                )
                                entries = entries + createdEntry
                                selectedEntry = createdEntry
                                apiError = null

                                navController.navigate("entries") {
                                    popUpTo("entry") { inclusive = true }
                                }
                            }
                            .onFailure { exception ->
                                if (exception is java.io.IOException) {
                                    Log.w(
                                        "AppNavigation",
                                        "Network save failed. Attempting offline save.",
                                        exception
                                    )
                                    repository.createEntryOffline(entryWithClientId)
                                        .onSuccess { savedEntry ->
                                            entries = entries + savedEntry
                                            selectedEntry = savedEntry
                                            apiError = null
                                            Log.d(
                                                "AppNavigation",
                                                "Entry saved offline. ID=${savedEntry.id}"
                                            )
                                            navController.navigate("entries") {
                                                popUpTo("entry") { inclusive = true }
                                            }
                                        }
                                        .onFailure { localException ->
                                            Log.e(
                                                "AppNavigation",
                                                "Offline save failed.",
                                                localException
                                            )
                                            apiError = localException.message
                                                ?: "Unable to save the entry locally."
                                        }
                                } else {
                                    Log.e(
                                        "AppNavigation",
                                        "Entry creation failed; not queueing offline.",
                                        exception
                                    )
                                    apiError = if (exception is retrofit2.HttpException) {
                                        "Unable to save entry. HTTP ${exception.code()}"
                                    } else {
                                        exception.message ?: "Unable to save the entry."
                                    }
                                }
                            }
                    }
                },

                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }

        // =========================================================
        // COUNTRIES
        // =========================================================

        composable("countries") {

            CountriesScreen(

                onSaveClick = { country ->

                    selectedCountry = country

                    navController.popBackStack()
                },

                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }

        // =========================================================
        // CALENDAR
        // =========================================================

        composable("calendar") {

            LaunchedEffect(Unit) {
                loadEntries()
            }

            CalendarScreen(
                entries = entries,
                isLoading = entriesLoading,
                errorMessage = apiError,
                onRetry = {
                    loadEntries()
                },
                onEntryDetailsClick = { entry ->
                    selectedEntry = entry
                    navController.navigate("entryDetails")
                },

                onDashboardClick = {

                    navController.navigate("dashboard") {

                        popUpTo("dashboard") {
                            inclusive = false
                        }

                        launchSingleTop = true
                    }
                },

                onEntryClick = {

                    navController.navigate("entry") {
                        launchSingleTop = true
                    }
                },

                onLibraryClick = {

                    navController.navigate("library") {
                        launchSingleTop = true
                    }
                },

                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        // =========================================================
        // LIBRARY
        // =========================================================

        composable("library") {

            LibraryScreen(

                onDashboardClick = {

                    navController.navigate("dashboard") {

                        popUpTo("dashboard") {
                            inclusive = false
                        }

                        launchSingleTop = true
                    }
                },

                onEntriesClick = {
                    navController.navigate("entries") {
                        launchSingleTop = true
                    }
                },

                onEntryDetailsClick = { entry ->
                    selectedEntry = entry
                    navController.navigate("entryDetails")
                },

                onCalendarClick = {

                    navController.navigate("calendar") {
                        launchSingleTop = true
                    }
                },

                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
    }

    // =============================================================
    // DELETE CONFIRMATION DIALOG
    // =============================================================

    entryToDelete?.let { entry ->

        AlertDialog(

            onDismissRequest = {
                entryToDelete = null
            },

            title = {
                Text(
                    text = "Delete Entry?"
                )
            },

            text = {
                Text(
                    text =
                        "Are you sure you want to delete \"${entry.title}\"?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        entryToDelete = null

                        coroutineScope.launch {

                            Log.d(
                                "AppNavigation",
                                "Deleting entry ID=${entry.id}"
                            )

                            repository
                                .deleteEntry(entry.id)
                                .onSuccess {

                                    Log.d(
                                        "AppNavigation",
                                        "Entry deleted successfully. ID=${entry.id}"
                                    )

                                    entries =
                                        entries.filter {
                                            it.id != entry.id
                                        }

                                    selectedEntry = null

                                    apiError = null

                                    navController.popBackStack()
                                }
                                .onFailure { exception ->

                                    Log.e(
                                        "AppNavigation",
                                        "Failed to delete entry ID=${entry.id}",
                                        exception
                                    )

                                    apiError =
                                        exception.message
                                            ?: "Unable to delete the entry."
                                }
                        }
                    }
                ) {

                    Text(
                        text = "Delete"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        entryToDelete = null
                    }
                ) {

                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }

    // =============================================================
    // GENERAL API ERROR DIALOG
    // =============================================================

    apiError?.let { errorMessage ->

        AlertDialog(

            onDismissRequest = {
                apiError = null
            },

            title = {
                Text(
                    text = "API Error"
                )
            },

            text = {
                Text(
                    text = errorMessage
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        apiError = null
                    }
                ) {

                    Text(
                        text = "OK"
                    )
                }
            }
        )
    }
}