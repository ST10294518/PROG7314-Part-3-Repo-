package com.winx.app.data.remote

data class MediaItemDto(
    val id: Int = 0,
    val travelEntryId: Int,
    val mediaType: String,
    val fileName: String,
    val filePath: String
)
