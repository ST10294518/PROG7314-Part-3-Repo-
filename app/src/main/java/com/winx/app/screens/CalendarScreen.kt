package com.winx.app.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxLightPink
import com.winx.app.ui.theme.WinxOrange
import com.winx.app.ui.theme.WinxWhite
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    entries: List<TravelEntry> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onEntryDetailsClick: (TravelEntry) -> Unit = {},
    onDashboardClick: () -> Unit = {},
    onEntryClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val today = LocalDate.now()
    var displayedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf(today) }

    val entriesByDate = entries.groupBy { parseCalendarDate(it.date) }
    val selectedEntries = entriesByDate[selectedDate].orEmpty()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = "Calendar",
                onDashboardClick = onDashboardClick,
                onEntryClick = onEntryClick,
                onCalendarClick = {},
                onLibraryClick = onLibraryClick,
                onSettingsClick = onSettingsClick
            )
        },
        containerColor = WinxWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Calendar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WinxLightPink)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { displayedMonth = displayedMonth.minusMonths(1) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = "Previous month",
                                tint = WinxDarkBlue
                            )
                        }

                        Text(
                            text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayedMonth.year}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WinxDarkBlue
                        )

                        IconButton(
                            onClick = { displayedMonth = displayedMonth.plusMonths(1) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = "Next month",
                                tint = WinxDarkBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                            Text(
                                text = it,
                                fontWeight = FontWeight.Bold,
                                color = WinxDarkBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val firstDay = displayedMonth.atDay(1)
                    val leadingEmptyDays = firstDay.dayOfWeek.value - 1
                    val cells = leadingEmptyDays + displayedMonth.lengthOfMonth()
                    val rows = (cells + 6) / 7

                    repeat(rows) { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            repeat(7) { column ->
                                val cellIndex = row * 7 + column
                                val day = cellIndex - leadingEmptyDays + 1

                                if (day in 1..displayedMonth.lengthOfMonth()) {
                                    val date = displayedMonth.atDay(day)
                                    val hasEntry = entriesByDate[date].orEmpty().isNotEmpty()

                                    CalendarDateCell(
                                        date = date,
                                        selected = date == selectedDate,
                                        hasEntry = hasEntry,
                                        onClick = { selectedDate = date },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = selectedDate.format(
                    DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
                ),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = WinxDarkBlue
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = WinxBlue)
                    }
                }

                errorMessage != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WinxLightPink)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "We couldn't load your entries.",
                                fontWeight = FontWeight.Bold,
                                color = WinxDarkBlue
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = errorMessage, color = Color.Gray)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = WinxDarkBlue)
                            ) {
                                Text("Retry", color = WinxWhite)
                            }
                        }
                    }
                }

                selectedEntries.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WinxLightPink)
                    ) {
                        Text(
                            text = "No entries for this date.",
                            modifier = Modifier.padding(20.dp),
                            color = WinxDarkBlue
                        )
                    }
                }

                else -> {
                    selectedEntries.forEach { entry ->
                        CalendarEntryCard(
                            entry = entry,
                            onClick = { onEntryDetailsClick(entry) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalendarDateCell(
    date: LocalDate,
    selected: Boolean,
    hasEntry: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    when {
                        selected -> WinxBlue
                        hasEntry -> WinxOrange.copy(alpha = 0.45f)
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (selected) WinxWhite else WinxDarkBlue,
                fontWeight = if (selected || hasEntry) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (hasEntry) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(WinxBlue)
            )
        } else {
            Spacer(modifier = Modifier.height(7.dp))
        }
    }
}

@Composable
private fun CalendarEntryCard(
    entry: TravelEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WinxBlue.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WinxBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = WinxWhite
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    color = WinxDarkBlue,
                    fontSize = 16.sp
                )
                Text(
                    text = listOf(entry.location, entry.country)
                        .filter { it.isNotBlank() }
                        .joinToString(", "),
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                if (entry.notes.isNotBlank()) {
                    Text(
                        text = entry.notes,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Open entry",
                tint = WinxDarkBlue
            )
        }
    }
}

private fun parseCalendarDate(value: String): LocalDate? {
    if (value.isBlank()) return null

    return try {
        LocalDate.parse(value.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: Exception) {
        try {
            LocalDate.parse(
                value,
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
            )
        } catch (_: Exception) {
            null
        }
    }
}
