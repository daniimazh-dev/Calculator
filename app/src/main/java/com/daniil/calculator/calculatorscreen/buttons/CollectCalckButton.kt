package com.daniil.calculator.calculatorscreen.buttons

import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonUi
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import com.daniil.calculator.universal.ButtonData


fun collectCalckButton(darkMode: Boolean): List<ButtonData> {
    val buttons = mutableListOf<ButtonData>()
    buttons.apply {
        add(ButtonData(content = "sin", type = ButtonUi.Second, expanded = false))
        add(ButtonData(content = "cos", type = ButtonUi.Second, expanded = false))
//        add(ButtonData(content = "tan", type = ButtonUi.Second, expanded = false))
        add(ButtonData(content = "log", type = ButtonUi.Second, expanded = false))
        add(ButtonData(content = "(", type = ButtonUi.Second, expanded = false))
        add(ButtonData(content = ")", type = ButtonUi.Second, expanded = false))

        add(
            ButtonData(
                painterIcon = null,
                content = "C",
                type = ButtonUi.Special
            )
        )

        add(ButtonData(content = "^", type = ButtonUi.Second, expanded = false))
        add(ButtonData(content = "%", type = ButtonUi.Second))
        add(ButtonData(content = "/", painterIcon = R.drawable.division_icon, type = ButtonUi.Second))

        add(
            ButtonData(
                painterIcon = R.drawable.backspace_icon,
                content = "delete",
                type = ButtonUi.Special
            )
        )
        add(ButtonData(content = "√", type = ButtonUi.Second, expanded = false))

        add(ButtonData(content = "7", type = ButtonUi.Default))
        add(ButtonData(content = "8", type = ButtonUi.Default))

        add(ButtonData(content = "9", type = ButtonUi.Default))
        add(ButtonData(content = "*", painterIcon = R.drawable.multiplication_icon, type = ButtonUi.Second))
        add(ButtonData(content = "|x|", type = ButtonUi.Second, expanded = false))
        add(ButtonData(content = "4", type = ButtonUi.Default))
        add(ButtonData(content = "5", type = ButtonUi.Default))
        add(ButtonData(content = "6", type = ButtonUi.Default))
        add(ButtonData(content = "-", type = ButtonUi.Second))
        add(ButtonData(content = "π", type = ButtonUi.Second, expanded = false))
        add(ButtonData(content = "1", type = ButtonUi.Default))
        add(ButtonData(content = "2", type = ButtonUi.Default))
        add(ButtonData(content = "3", type = ButtonUi.Default))
        add(ButtonData(content = "+", type = ButtonUi.Second))

        val themeMode = DynamicSettingsManager.getValue("theme_mode")
        val animationJson = when (themeMode) {
            "Dark" -> "lottie_animation/more_animation_light.json"
            "Light" -> "lottie_animation/more_animation_dark.json"
            else -> {
                if (darkMode) "lottie_animation/more_animation_light.json" else "lottie_animation/more_animation_dark.json"
            }
        }

        add(
            ButtonData(
                content = "more/less",
                lottieJson = animationJson,
                type = ButtonUi.Special,
            )
        )
        add(ButtonData(content = "E", type = ButtonUi.Second, expanded = false))
        add(ButtonData(content = "0", type = ButtonUi.Default))
        add(ButtonData(content = ".", type = ButtonUi.Second))

        add(
            ButtonData(
                painterIcon = R.drawable.equal_icon,
                content = "=",
                type = ButtonUi.Special
            )
        )
    }
    return buttons
}