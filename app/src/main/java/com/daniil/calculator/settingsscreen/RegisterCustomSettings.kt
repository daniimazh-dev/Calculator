package com.daniil.calculator.settingsscreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.daniil.calculator.R
import com.daniil.calculator.core.UserDataManager
import com.daniil.calculator.settingsscreen.customscreen.AboutAppScree
import com.daniil.calculator.settingsscreen.customscreen.ChangeLogScreen
import com.daniil.calculator.settingsscreen.customscreen.logs.LogsScreen
import com.daniil.calculator.settingsscreen.defaultitem.DefaultCodeBlockSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultCustomScreenSetting
import com.daniil.calculator.settingsscreen.defaultitem.DefaultTextSettingItem
import com.daniil.calculator.settingsscreen.itemtype.customitem.AboutDeveloperSetting
import com.daniil.calculator.settingsscreen.itemtype.customitem.AppLogo
import com.daniil.calculator.settingsscreen.itemtype.customitem.EsterEgg
import com.daniil.calculator.settingsscreen.itemtype.customitem.ImagePicker
import com.daniil.calculator.settingsscreen.itemtype.customitem.LinkSetting
import com.daniil.calculator.settingsscreen.itemtype.customitem.NewVersion
import com.daniil.calculator.settingsscreen.screen.DefaultAppBar
import com.daniil.calculator.settingsscreen.screen.DefaultContainer
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager.registerRendererById
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager.registerRendererByType
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager.setIcon
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import com.daniil.calculator.settingsscreen.settings.manager.SettingType
import com.daniil.calculator.ui.theme.appLogoBackground
import kotlinx.coroutines.delay

@Composable
fun RegisterCustomSettings(
    settingsScreenModel: SettingsScreenModel,
) = with(DynamicSettingsManager) {
    val context = LocalContext.current

    registerRendererByType(SettingType.CustomScreen) { setting ->
        DynamicSettingRenderManager.DefaultCustomScreenSetting(setting) {
            settingsScreenModel.goToCustomScreen(setting.id)
        }
    }
    registerRendererById("about_screen") { setting ->
        AboutAppScree(setting, settingsScreenModel)
    }

    registerRendererById("app_logo") { setting ->
        AppLogo(
            setting = setting,
            settingsScreenModel = settingsScreenModel
        )
    }
    registerRendererById("new_version") { setting ->
        NewVersion(setting)
    }

    setIcon("telegram_icon", R.drawable.telegram)
    setIcon("whatsapp_icon", R.drawable.whatsapp)
    setIcon("github_icon", R.drawable.github)
    setIcon("gmail_icon", R.drawable.gmail)
    setIcon("server_icon", R.drawable.server_icon)

    registerRendererByType("Link") { setting ->
        LinkSetting(
            setting = setting,
            customLink = null,
            enableAlert = true
        )
    }

    registerRendererById("ester_egg_screen") { setting ->
        val settings =
            DynamicSettingsManager.getAll((setting.value ?: setting.defaultValue).toString())
        DynamicSettingRenderManager.DefaultContainer(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.1f to appLogoBackground,
                        0.6f to Color.Transparent
                    )
                )
            ),
            settings = settings,
            title = setting.title,
            settingsScreenModel = settingsScreenModel
        )
    }


    registerRendererByType("ImagePicker") { setting ->
        ImagePicker(setting)
    }

    registerRendererById("experimental_features_screen") { setting ->
        val settings =
            DynamicSettingsManager.getAll((setting.value ?: setting.defaultValue).toString())
        DynamicSettingRenderManager.DefaultContainer(
            settings = settings,
            title = setting.title,
            settingsScreenModel = settingsScreenModel
        )
    }
    registerRendererById("ester_egg_logo") { setting ->
        EsterEgg(setting)
    }
    registerRendererById("about_developer") { setting ->
        AboutDeveloperSetting(setting)
    }


    // DEVELOPER
    registerRendererById("developer_screen") { setting ->
        val settings =
            DynamicSettingsManager.getAll((setting.value ?: setting.defaultValue).toString())
        DynamicSettingRenderManager.DefaultContainer(
            settings = settings,
            title = setting.title,
            settingsScreenModel = settingsScreenModel
        )
    }
    registerRendererById("changeLog_screen") { setting ->
        ChangeLogScreen(setting, settingsScreenModel)
    }
    registerRendererById("logs_screen") { setting ->
        LogsScreen(setting, settingsScreenModel)
    }
    registerRendererById("current_token") { setting ->
        DynamicSettingRenderManager.DefaultTextSettingItem(
            setting.copy(description = UserDataManager.token.toString())
        )
    }
    registerRendererById("drop_token") { setting ->
        DynamicSettingRenderManager.DefaultCodeBlockSettingItem(setting, enableAlert = true) {
            UserDataManager.dropToken(context)
        }
    }

    registerRendererById("select_convertor_list_mode_screen") { setting ->
        val settings =
            DynamicSettingsManager.getAll((setting.value ?: setting.defaultValue).toString())
        DynamicSettingRenderManager.DefaultContainer(
            settings = settings,
            title = setting.title,
            settingsScreenModel = settingsScreenModel
        )
    }


    // STATE
    val customColorsEnabled = getValueState("custom_color_scheme").value.toBoolean()
    enabledSetting("color_accent", customColorsEnabled, false)

    val unavailableConvertor = DynamicSettingsManager.getValueState("experimental_convertor_buttons").value.toBoolean()
    enabledSetting("unavailable_convertor_buttons", unavailableConvertor)

    val developerEnable = DynamicSettingsManager.getValueState("developer_enable").value.toBoolean()
    hideSetting("developer", !developerEnable)

}

@Preview
@Composable
private fun Preview() {

    var transition by remember { mutableStateOf(false) }
    val animateBrash by animateFloatAsState(
        if (transition) 0.5f else 0f,
        animationSpec = tween(600)
    )
    LaunchedEffect(Unit) {
        delay(150)
        transition = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        animateBrash - 0.5f to appLogoBackground,
                        animateBrash to Color.Transparent
                    )
                )
            ),
    ) {

    }
}