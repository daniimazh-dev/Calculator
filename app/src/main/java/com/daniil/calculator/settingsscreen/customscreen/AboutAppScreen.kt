package com.daniil.calculator.settingsscreen.customscreen

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.daniil.calculator.settingsscreen.SettingsScreenModel
import com.daniil.calculator.settingsscreen.screen.DefaultContainer
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import com.daniil.calculator.ui.theme.appLogoBackground
import com.daniil.calculator.ui.theme.appLogoBackground2
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun AboutAppScree(
    setting: DynamicSetting,
    settingsScreenModel: SettingsScreenModel,
) {
    val settings =
        DynamicSettingsManager.getAll((setting.value ?: setting.defaultValue).toString())
    var transition by remember { mutableStateOf(false) }
    val animateBrash by animateFloatAsState(
        if (transition) 0.6f else 0.0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = LinearOutSlowInEasing
        )
    )
    val infiniteTransition = rememberInfiniteTransition()
    val colorOffset by infiniteTransition.animateColor(
        initialValue = appLogoBackground,
        targetValue = appLogoBackground2,
        animationSpec = infiniteRepeatable(
            tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    LaunchedEffect(Unit) {
        delay(150)
        transition = true
    }
    DynamicSettingRenderManager.DefaultContainer(
        modifier = Modifier.background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    animateBrash - 0.5f to colorOffset,
                    animateBrash to Color.Transparent
                )
            )
        ),
        title = setting.title,
        settingsScreenModel = settingsScreenModel,
        settings = settings,

    )
}