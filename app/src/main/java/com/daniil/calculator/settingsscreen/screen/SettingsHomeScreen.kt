package com.daniil.calculator.settingsscreen.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.daniil.calculator.settingsscreen.SettingsScreenModel
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    settingsScreenModel: SettingsScreenModel,
) {
    val context = LocalContext.current
    val settings = DynamicSettingsManager.getAll()
    DynamicSettingRenderManager.DefaultContainer(
        lazyListState = settingsScreenModel.lazyListState,
        settings = settings,
        settingsScreenModel = settingsScreenModel,
    )
}