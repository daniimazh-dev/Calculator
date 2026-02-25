package com.daniil.calculator.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {


    val useCustomColors = DynamicSettingsManager.getValueState("custom_color_scheme").value.toBoolean()

    val trigger = DynamicSettingsManager.getValueState("color_accent").value
    val primary = remember(trigger, isSystemInDarkTheme()) {
        mutableStateOf(
            DynamicSettingsManager.getValue("color_accent")?.toInt()
                ?.let { Color(it) } ?: Purple80
        )

    }
    ThemeState.baseColor.value = primary.value


    val colorScheme = if (useCustomColors) {
        ThemeState.collect(getThemeMode().value)
    } else {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


@Composable
fun getThemeMode(): State<Boolean> {
    val mode = DynamicSettingsManager.getValueState("theme_mode").value
    val isDark = isSystemInDarkTheme()
    val isDarkTheme = remember(mode) {
        mutableStateOf(
            when (mode) {
                "System" -> isDark
                "Light" -> false
                "Dark" -> true
                else -> isDark
            }
        )
    }
    return isDarkTheme
}

object ThemeState {
    var baseColor = mutableStateOf(Purple80)

    @Composable
    fun collect(isDarkTheme: Boolean): ColorScheme {
        return generateHslColorScheme(baseColor.value, isDarkTheme)
    }
}
