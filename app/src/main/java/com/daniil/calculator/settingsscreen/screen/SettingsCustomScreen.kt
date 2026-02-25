package com.daniil.calculator.settingsscreen.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import com.daniil.calculator.settingsscreen.SettingsScreenModel
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager

@Composable
fun SettingsCustomScreen(
    settingsScreenModel: SettingsScreenModel,
    content: (@Composable () -> Unit)? = null,
    setting: DynamicSetting,
) {

    BackHandler(true) {
        settingsScreenModel.backStack()
    }
    if (content != null) {
        content()
    } else {
        DynamicSettingRenderManager.DefaultContainer(
            title = setting.title,
            settingsScreenModel = settingsScreenModel,
            settings = emptyList()
        )
    }
}

