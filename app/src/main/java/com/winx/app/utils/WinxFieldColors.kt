package com.winx.app.utils

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import com.winx.app.ui.theme.WinxBlue
import com.winx.app.ui.theme.WinxBlack
import com.winx.app.ui.theme.WinxDarkBlue

@Composable
fun winxFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = WinxBlack,
    unfocusedTextColor = WinxBlack,
    disabledTextColor = WinxBlack,
    focusedLabelColor = WinxBlue,
    unfocusedLabelColor = WinxDarkBlue,
    disabledLabelColor = WinxDarkBlue,
    focusedBorderColor = WinxBlue,
    unfocusedBorderColor = WinxBlue,
    disabledBorderColor = WinxBlue.copy(alpha = 0.55f),
    cursorColor = WinxBlue
)
