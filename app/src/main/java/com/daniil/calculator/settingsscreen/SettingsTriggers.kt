package com.daniil.calculator.settingsscreen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.daniil.csb.SettingsProvider

@Composable
fun SettingsTriggers() {
    val customColor by SettingsProvider.getValue<Boolean>("custom_color_scheme").collectAsState()
    LaunchedEffect(customColor) {
        SettingsProvider.enable("color_accent", customColor)
    }
    val experimentalConvertor by SettingsProvider.getValue<Boolean>("experimental_convertor_buttons").collectAsState()
    LaunchedEffect(experimentalConvertor) {
        if (!experimentalConvertor) SettingsProvider.setValue("unavailable_convertor_buttons", false)
        SettingsProvider.enable("unavailable_convertor_buttons", experimentalConvertor)
    }
    val localMode by SettingsProvider.getValue<Boolean>("locale_mode").collectAsState()
    LaunchedEffect(localMode) {
        SettingsProvider.enable("ping_test", !localMode)
    }

}