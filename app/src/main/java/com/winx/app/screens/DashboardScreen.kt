package com.winx.app.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxLightPink
import com.winx.app.ui.theme.WinxOrange
import com.winx.app.ui.theme.WinxPink
import com.winx.app.ui.theme.WinxPurple
import com.winx.app.ui.theme.WinxWhite
import com.winx.app.utils.T
import com.winx.app.utils.winxGreeting
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun DashboardScreen(
    entries: List<TravelEntry> = emptyList(),
    isLoadingEntries: Boolean = false,
    entriesError: String? = null,
    onRetryEntries: () -> Unit = {},
    onAddEntryClick: () -> Unit = {},
    onAddCountryClick: () -> Unit = {},
    onEntryClick: () -> Unit = {},
    onEntryDetailsClick: (TravelEntry) -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {

    val context = LocalContext.current
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    val profilePreferences =
        context.getSharedPreferences(
            "winx_profile",
            Context.MODE_PRIVATE
        )

    val displayName =
        profilePreferences
            .getString("name", null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: firebaseUser
                ?.displayName
                ?.trim()
                ?.takeIf { it.isNotBlank() }
            ?: "User"

    val today = LocalDate.now()

    val recentEntries =
        entries
            .sortedByDescending { parseEntryDate(it.date) }
            .take(2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WinxWhite)
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // =============================================================
            // TOP BAR
            // =============================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = T("Winx"),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(WinxLightDashboard)
                            .clickable {
                                onNotificationsClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.NotificationsNone,
                            contentDescription =
                                "Notifications",
                            tint = WinxDarkBlue,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(WinxPink)
                            .clickable {
                                onSettingsClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Person,
                            contentDescription =
                                "Profile",
                            tint = WinxDarkBlue,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =============================================================
            // WELCOME CARD
            // =============================================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WinxOrange
                )
            ) {

                Column(
                    modifier = Modifier.padding(22.dp)
                ) {

                    Text(
                        text = winxGreeting(displayName),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = WinxDarkBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = T("Welcome Back"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WinxDarkBlue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = T(
                            "Every Place has a story,\n" +
                                    "what will you capture today?"
                        ),
                        fontSize = 15.sp,
                        color = WinxDarkBlue,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        Button(
                            onClick = onAddEntryClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        WinxDarkBlue
                                )
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined.Add,
                                contentDescription = null,
                                tint = WinxWhite,
                                modifier =
                                    Modifier.size(20.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )

                            Text(
                                text = T("Add Entry"),
                                color = WinxWhite
                            )
                        }

                        Button(
                            onClick = onAddCountryClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        WinxPurple
                                )
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined.Public,
                                contentDescription = null,
                                tint = WinxDarkBlue,
                                modifier =
                                    Modifier.size(20.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )

                            Text(
                                text = T("Add Country"),
                                color = WinxDarkBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // =============================================================
            // LIVE CALENDAR PREVIEW
            // =============================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = T("Calendar"),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )

                TextButton(
                    onClick = onCalendarClick
                ) {

                    Text(
                        text = T("View All"),
                        color = WinxBlue
                    )

                    Icon(
                        imageVector =
                            Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = WinxBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DashboardCalendarPreview(
                today = today,
                entries = entries
            )

            Spacer(modifier = Modifier.height(24.dp))

            // =============================================================
            // LIVE ENTRIES
            // =============================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = T("Recent Entries"),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )

                TextButton(
                    onClick = onEntryClick
                ) {

                    Text(
                        text = T("View All"),
                        color = WinxBlue
                    )

                    Icon(
                        imageVector =
                            Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = WinxBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {

                isLoadingEntries -> {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator(
                            color = WinxBlue
                        )
                    }
                }

                entriesError != null -> {

                    DashboardErrorCard(
                        message = entriesError,
                        onRetry = onRetryEntries
                    )
                }

                recentEntries.isEmpty() -> {

                    EmptyEntriesCard(
                        onAddEntryClick = onAddEntryClick
                    )
                }

                else -> {

                    recentEntries.forEachIndexed {
                            index,
                            entry ->

                        LiveEntryCard(
                            entry = entry,
                            backgroundColor =
                                if (index % 2 == 0) {
                                    WinxBlue
                                } else {
                                    WinxPurple
                                },
                            onClick = {
                                onEntryDetailsClick(entry)
                            }
                        )

                        if (
                            index <
                            recentEntries.lastIndex
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // =============================================================
        // EXISTING BOTTOM NAVIGATION
        // =============================================================

        BottomNavigationBar(
            selectedItem = "Dashboard",
            onEntryClick = onEntryClick,
            onCalendarClick = onCalendarClick,
            onLibraryClick = onLibraryClick,
            onSettingsClick = onSettingsClick
        )
    }
}


// =====================================================================
// LIVE CALENDAR PREVIEW
// =====================================================================

@Composable
private fun DashboardCalendarPreview(
    today: LocalDate,
    entries: List<TravelEntry>
) {

    val startDate = today.minusDays(3)

    val previewDates =
        (0L..6L).map {
            startDate.plusDays(it)
        }

    val entryDates =
        entries
            .mapNotNull {
                parseEntryDate(it.date)
            }
            .toSet()

    val monthTitle =
        today.month.getDisplayName(
            TextStyle.FULL,
            Locale.getDefault()
        ) + " " + today.year

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = WinxLightPink
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = monthTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                previewDates.forEach { date ->

                    CalendarDay(
                        day =
                            date.dayOfWeek
                                .getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.getDefault()
                                )
                                .take(1),
                        date =
                            date.dayOfMonth.toString(),
                        selected = date == today,
                        hasEntry = entryDates.contains(date)
                    )
                }
            }
        }
    }
}


// =====================================================================
// CALENDAR DAY
// =====================================================================

@Composable
private fun CalendarDay(
    day: String,
    date: String,
    selected: Boolean = false,
    hasEntry: Boolean = false
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = day,
            fontSize = 12.sp,
            color = WinxGreyDashboard
        )

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        selected -> WinxBlue
                        hasEntry ->
                            WinxPurple.copy(
                                alpha = 0.35f
                            )

                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = date,
                fontSize = 13.sp,
                fontWeight =
                    if (selected || hasEntry) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                color =
                    if (selected) {
                        WinxWhite
                    } else {
                        WinxDarkBlue
                    }
            )
        }
    }
}


// =====================================================================
// LIVE ENTRY CARD
// =====================================================================

@Composable
private fun LiveEntryCard(
    entry: TravelEntry,
    backgroundColor: Color,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                backgroundColor.copy(
                    alpha = 0.12f
                )
        )
    ) {

        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(backgroundColor),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Description,
                    contentDescription = "Entry",
                    tint = WinxWhite,
                    modifier =
                        Modifier.size(30.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = entry.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        buildString {

                            if (
                                entry.location
                                    .isNotBlank()
                            ) {
                                append(entry.location)
                            }

                            if (
                                entry.country
                                    .isNotBlank()
                            ) {

                                if (isNotEmpty()) {
                                    append(", ")
                                }

                                append(entry.country)
                            }
                        },
                    fontSize = 13.sp,
                    color = WinxGreyDashboard
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = formatEntryDate(
                        entry.date
                    ),
                    fontSize = 11.sp,
                    color = WinxBlue
                )

                if (entry.notes.isNotBlank()) {

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text = entry.notes,
                        fontSize = 12.sp,
                        color = WinxGreyDashboard,
                        maxLines = 2
                    )
                }
            }

            Icon(
                imageVector =
                    Icons.Outlined.ChevronRight,
                contentDescription =
                    "Open entry",
                tint = WinxDarkBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


// =====================================================================
// EMPTY STATE
// =====================================================================

@Composable
private fun EmptyEntriesCard(
    onAddEntryClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = WinxLightPink
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Description,
                contentDescription = null,
                tint = WinxBlue,
                modifier = Modifier.size(38.dp)
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = T("No entries yet"),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = T(
                    "Add your first travel entry to see it here."
                ),
                fontSize = 13.sp,
                color = WinxGreyDashboard
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onAddEntryClick,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            WinxDarkBlue
                    )
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = T("Add Entry")
                )
            }
        }
    }
}


// =====================================================================
// API ERROR STATE
// =====================================================================

@Composable
private fun DashboardErrorCard(
    message: String,
    onRetry: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = WinxLightPink
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = T(
                    "We couldn't load your entries."
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = message,
                fontSize = 12.sp,
                color = WinxGreyDashboard
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onRetry,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            WinxDarkBlue
                    )
            ) {

                Text(
                    text = T("Retry")
                )
            }
        }
    }
}


// =====================================================================
// DATE HELPERS
// =====================================================================

private fun parseEntryDate(
    value: String
): LocalDate? {

    if (value.isBlank()) {
        return null
    }

    val formats = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern(
            "dd MMMM yyyy",
            Locale.ENGLISH
        ),
        DateTimeFormatter.ofPattern(
            "d MMMM yyyy",
            Locale.ENGLISH
        )
    )

    for (formatter in formats) {

        try {

            return if (
                formatter ==
                DateTimeFormatter.ISO_DATE_TIME
            ) {
                java.time.LocalDateTime
                    .parse(value, formatter)
                    .toLocalDate()
            } else {
                LocalDate.parse(
                    value,
                    formatter
                )
            }

        } catch (_: Exception) {
            // Try next supported format.
        }
    }

    // ASP.NET may return:
    // 2026-09-21T00:00:00
    return try {

        LocalDate.parse(
            value.take(10),
            DateTimeFormatter.ISO_LOCAL_DATE
        )

    } catch (_: Exception) {
        null
    }
}


private fun formatEntryDate(
    value: String
): String {

    val date =
        parseEntryDate(value)
            ?: return value

    return date.format(
        DateTimeFormatter.ofPattern(
            "d MMMM yyyy",
            Locale.ENGLISH
        )
    )
}


// =====================================================================
// EXISTING BOTTOM NAVIGATION
// =====================================================================

@Composable
fun BottomNavigationBar(
    selectedItem: String = "Dashboard",
    onDashboardClick: () -> Unit = {},
    onEntryClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(WinxWhite)
    ) {

        HorizontalDivider(
            color = WinxLightDashboard
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement =
                Arrangement.SpaceAround,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            NavigationItem(
                icon = Icons.Outlined.Home,
                label = "Dashboard",
                selected =
                    selectedItem == "Dashboard",
                onClick = onDashboardClick
            )

            NavigationItem(
                icon =
                    Icons.Outlined.Description,
                label = "Entry",
                selected =
                    selectedItem == "Entry",
                onClick = onEntryClick
            )

            NavigationItem(
                icon =
                    Icons.Outlined.CalendarMonth,
                label = "Calendar",
                selected =
                    selectedItem == "Calendar",
                onClick = onCalendarClick
            )

            NavigationItem(
                icon = Icons.Outlined.Folder,
                label = "Library",
                selected =
                    selectedItem == "Library",
                onClick = onLibraryClick
            )

            NavigationItem(
                icon = Icons.Outlined.Settings,
                label = "Settings",
                selected =
                    selectedItem == "Settings",
                onClick = onSettingsClick
            )
        }
    }
}


// =====================================================================
// NAVIGATION ITEM
// =====================================================================

@Composable
private fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {

    TextButton(
        onClick = onClick
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = label,
                tint =
                    if (selected) {
                        WinxBlue
                    } else {
                        WinxGreyDashboard
                    },
                modifier = Modifier.size(23.dp)
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = label,
                fontSize = 10.sp,
                color =
                    if (selected) {
                        WinxBlue
                    } else {
                        WinxGreyDashboard
                    }
            )
        }
    }
}


// =====================================================================
// DASHBOARD SUPPORTING COLOURS
// =====================================================================

private val WinxLightDashboard =
    Color(0xFFF5F5F5)

private val WinxGreyDashboard =
    Color(0xFF777777)