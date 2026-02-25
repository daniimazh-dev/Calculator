package com.daniil.calculator.convertorscreen

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.daniil.calculator.convertorscreen.convertor.ConvertorCalckScreen
import com.daniil.calculator.convertorscreen.homescreen.ConvertorHomeScreen
import com.daniil.calculator.settingsscreen.customscreen.logs.LogsScreen


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConvertorScreenController(
    convertorScreenModel: ConvertorScreenModel,
) {

    val currentConvertor by convertorScreenModel.currentConvertor.collectAsState()
    val currentScreen by convertorScreenModel.currentScreen.collectAsState()
    var oneLaunch = rememberSaveable() { true }
    if (oneLaunch) {
        oneLaunch = false
        Log.w("MyLog", "Register")
        convertorScreenModel.RegisterCustomConvertor()
    }



    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )


    SharedTransitionLayout {
        AnimatedContent(
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        scaleIn(initialScale = 1.1f, animationSpec = tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            },
            targetState = currentScreen
        ) {
            when (it) {
                ConvertorScreens.Home -> {
                    ConvertorHomeScreen(
                        convertorScreenModel = convertorScreenModel,
                        animatedVisibilityScope = this@AnimatedContent,
                    )
                }

                ConvertorScreens.Convertor -> {
                    val rememberActive = remember(transition.targetState) { currentConvertor }
                    if (rememberActive != null) {
                        ConvertorCalckScreen(
                            convertorButtonData = rememberActive,
                            animatedVisibilityScope = this@AnimatedContent,
                            convertorScreenModel = convertorScreenModel,
                        )
                    } else {
                        convertorScreenModel.goToHome()
                    }

                }
            }
        }

    }
}