package com.daniil.calculator.universal

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonUi
import com.daniil.csb.SettingsProvider
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ButtonUI(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    buttonData: ButtonData
) {
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    var isClicked by remember { mutableStateOf(false) }
    var animated by remember { mutableStateOf(false) }
    val isPressed = interactionSource.collectIsPressedAsState()

    val animateSize = animateFloatAsState(
        targetValue = if (isClicked || isPressed.value) 0.25f else 0f,
        animationSpec = tween(300)
    )

    val color = when (buttonData.type) {
        ButtonUi.Default -> MaterialTheme.colorScheme.onPrimary
        ButtonUi.Second -> MaterialTheme.colorScheme.primaryContainer
        ButtonUi.Special -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ButtonUi.Function -> Color.Transparent
        ButtonUi.Empty -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    LaunchedEffect(isPressed.value) {
        var dealy = 200
        while (isPressed.value) {
            dealy -= 20
            if (dealy < 50) dealy = 50
            buttonData.onPressed()
            delay(dealy.toLong())
        }

    }
    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(150)
            isClicked = false

        }
    }
    LaunchedEffect(animated) {
        delay(750)
        animated = false
    }

    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val effect = VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)


    val vibrationEnabled by SettingsProvider.getValue<Boolean>("button_vibration_enable").collectAsState()
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(color)
            .animateContentSize()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    if (vibrationEnabled && ButtonUi.Empty != buttonData.type)
                        vibrator.vibrate(effect)
                    buttonData.onClick()
                    isClicked = true
                    buttonData.lottieJson?.let { animated = true }
                },
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            if (buttonData.lottieJson != null) {
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.Asset(
                        buttonData.lottieJson
                    )
                )

                LottieAnimation(
                    composition = composition,
                    reverseOnRepeat = true,
                    isPlaying = animated
                )
            } else if (buttonData.painterIcon != null) {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                        scaleX = 1f - animateSize.value
                        scaleY = 1f - animateSize.value
                    },
                    painter = painterResource(buttonData.painterIcon),
                    contentDescription = buttonData.content
                )
            } else {
                Text(
                    modifier = Modifier.graphicsLayer {
                        scaleX = 1f - animateSize.value
                        scaleY = 1f - animateSize.value
                    },
                    text = buttonData.content,
                    fontSize = 32.sp
                )
            }

        }

    }

}

