package com.winx.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxDarkBlue
import com.winx.app.ui.theme.WinxLightPink
import com.winx.app.ui.theme.WinxOrange
import com.winx.app.ui.theme.WinxWhite

@Composable
fun CountriesScreen(
    onSaveClick: (String) -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }

    val countries = listOf(
        "South Africa",
        "Italy",
        "France",
        "Germany",
        "India",
        "United Kingdom",
        "United States",
        "Spain",
        "Portugal",
        "Greece",
        "Japan",
        "Australia",
        "Brazil",
        "Canada",
        "Thailand"
    )

    val filteredCountries = countries.filter {
        it.contains(searchText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WinxWhite)
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WinxDarkBlue)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Country",
                color = WinxWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onCancelClick
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = WinxWhite
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text(
                text = "Choose a country",
                color = WinxDarkBlue,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select the country connected to this entry.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Search field
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Search country")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selected country
            if (selectedCountry.isNotEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = WinxLightPink
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "Selected country",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = selectedCountry,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = WinxDarkBlue
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = WinxBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Country list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(filteredCountries) { country ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCountry = country
                            },
                        colors = CardDefaults.cardColors(
                            containerColor =
                                if (country == selectedCountry) {
                                    WinxLightPink
                                } else {
                                    Color.White
                                }
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = country,
                                modifier = Modifier.weight(1f),
                                fontSize = 16.sp,
                                color = WinxDarkBlue
                            )

                            if (country == selectedCountry) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = WinxBlue
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    if (selectedCountry.isNotEmpty()) {
                        onSaveClick(selectedCountry)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = selectedCountry.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WinxOrange,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(14.dp)
            ) {

                Text(
                    text = "Save Country",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinxWhite
                )
            }
        }
    }
}