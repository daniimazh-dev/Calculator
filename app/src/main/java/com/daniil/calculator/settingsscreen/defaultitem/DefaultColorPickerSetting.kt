package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastRoundToInt
import com.daniil.calculator.R
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

@Composable
fun DynamicSettingRenderManager.DefaultColorPickerSettingItem(
    setting: DynamicSetting,
) {
    var color = DynamicSettingsManager.getValueState(setting.id).value.let {
        setting.value?.toInt() ?: 0xFF0000
    }
    var dialogOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .settingContainer(setting.enabled) {
                dialogOpen = true
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = setting.title, fontWeight = FontWeight.Bold)
            Text(
                text = setting.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )

        }
        key(dialogOpen) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(DynamicSettingsManager.getValue(setting.id)?.toInt() ?:  0xFF0000))
                    .padding(8.dp)

            )
        }

    }

    if (dialogOpen) {
        ColorPickerDialog(
            initialColor = Color(DynamicSettingsManager.getValue(setting.id)?.toInt() ?:  0xFF0000),
            onColorSelected = {
                color = it.toArgb()
                DynamicSettingsManager.setValue(setting.id, color)
                dialogOpen = false
            },
            onDismissRequest = {
                dialogOpen = false
            }
        )
    }

}

fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val hsv = floatArrayOf(
        h.coerceIn(0f, 360f),
        s.coerceIn(0f, 1f),
        v.coerceIn(0f, 1f)
    )
    return Color(AndroidColor.HSVToColor(hsv))
}

@Composable
fun ColorPickerDialog(
    initialColor: Color = Color.Red,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit,
    barHeight: Dp = 28.dp,
    cornerRadius: Dp = 8.dp,
) {
    val hsv = remember {
        FloatArray(3).apply {
            AndroidColor.colorToHSV(initialColor.toArgb(), this)
        }
    }

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var satLight by remember { mutableFloatStateOf(0.5f) }

    val color = remember(hue, satLight) {
        when {
            satLight < 0.5f -> {
                val t = satLight / 0.5f
                androidx.compose.ui.graphics.lerp(Color.White, hsvToColor(hue, 1f, 1f), t)
            }
            else -> {
                val t = (satLight - 0.5f) / 0.5f
                androidx.compose.ui.graphics.lerp(hsvToColor(hue, 1f, 1f), Color.Black, t)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.select_color)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(color, RoundedCornerShape(12.dp))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "HEX: #${color.toArgb().ushr(8).toUInt().toString(16).uppercase().padStart(6, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )


                // Hue
                CustomGradientSlider(
                    value = hue / 360f,
                    onValueChange = { hue = it * 360f },
                    gradient = Brush.horizontalGradient(
                        listOf(
                            Color.Red, Color.Yellow ,Color.Green,
                            Color.Cyan,  Color.Blue, Color.Magenta, Color.Red
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Saturation + Light/Dark
                CustomGradientSlider(
                    value = satLight,
                    onValueChange = { satLight = it },
                    gradient = Brush.horizontalGradient(
                        listOf(
                            Color.White,
                            hsvToColor(hue, 1f, 1f),
                            Color.Black
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )


            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(color) }) {
                Text(stringResource(R.string.ok))

            }

        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}



@Composable
private fun CustomGradientSlider(
    value: Float,              // 0f..1f
    onValueChange: (Float) -> Unit,
    gradient: Brush,
    modifier: Modifier = Modifier,
    height: Dp = 28.dp,
    thumbRadius: Dp = 12.dp,
) {

    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(gradient)
            .pointerInput(Unit) {
                detectDragGestures(

                    onDrag = { change, _ ->
                        val x = change.position.x
                        val fraction = (x / size.width).coerceIn(0f, 1f)
                        onValueChange(fraction)
                    },
                )
            }
    ) {
        val thumbX = constraints.maxWidth * value

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (thumbX - thumbRadius.toPx()).roundToInt(),
                        (constraints.maxHeight / 2 - thumbRadius.toPx()).roundToInt()
                    )
                }
                .size(thumbRadius * 2)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
        )
    }
}

