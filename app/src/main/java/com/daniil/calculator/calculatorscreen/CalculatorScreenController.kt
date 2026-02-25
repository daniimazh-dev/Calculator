package com.daniil.calculator.calculatorscreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.openScreen

@Composable
fun CalculatorScreenController(
    calckScreenModel: CalculatorScreenModel,
    convertorScreenModel: ConvertorScreenModel,
) {
    val currentScreen by calckScreenModel.currentScreen.collectAsState()
    AnimatedContent(currentScreen) {screen ->
        when (screen) {
            CalculatorScreensRoute.Calculator -> {
                CalculatorScreen(
                    calckScreenModel = calckScreenModel,
                    convertorScreenModel = convertorScreenModel,
                )
            }
            CalculatorScreensRoute.History -> {

            }
        }
    }
}