package com.winx.app.data.remote

data class TravelEntryDto(
    val id: Int = 0,
    val title: String,
    val location: String,
    val country: String,
    val date: String,
    val rating: Int,
    val notes: String
)
