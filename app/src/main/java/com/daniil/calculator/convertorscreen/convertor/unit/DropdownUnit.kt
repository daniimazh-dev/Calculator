package com.daniil.calculator.convertorscreen.convertor.unit

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager

@Composable
fun DropdownUnit(
    expanded: Boolean,
    onItemSelected: (unit: ConvertorUnit) -> Unit,
    unitList: List<ConvertorUnit>,
    currentUnit: ConvertorUnit,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit,

    ) {
    val startIndex = unitList.indexOf(currentUnit)
    var selectedIndex by remember(startIndex) { mutableStateOf(startIndex) }
    val itemSize = 32.dp
    val itemHeightPx = with(LocalDensity.current) { itemSize.toPx() }

    val context = LocalContext.current

    val vibrationEnabled = DynamicSettingsManager.getValue("button_vibration_enable").toBoolean()
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val effect = VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)


    Box(
        modifier = Modifier.pointerInput(expanded) {
            if (!expanded) return@pointerInput

            awaitPointerEventScope {
                val startY = awaitPointerEvent().changes.first().position.y
                onItemSelected(unitList[startIndex])

                while (expanded) {
                    val event = awaitPointerEvent()
                    val position = event.changes.first().position

                    val dy = position.y - startY

                    val index =
                        (startIndex + dy / itemHeightPx)
                            .toInt()
                            .coerceIn(0, unitList.lastIndex)

                    if (selectedIndex != index) {
                        if (vibrationEnabled) vibrator.vibrate(effect)
                        selectedIndex = index
                        onItemSelected(unitList[selectedIndex])
                    }
                }
            }
        }
    ) {
        content()

        Popup(
            alignment = Alignment.CenterStart,
            offset = IntOffset(
                x = 0,
                y = Offset.Zero.y.toInt()
            ),
            onDismissRequest = {

            }
        ) {
            AnimatedVisibility(
                modifier = Modifier,
                visible = expanded,
                enter = fadeIn() + expandIn(),
                exit = shrinkOut() + fadeOut(),
            ) {
                LaunchedEffect(selectedIndex) {
                    lazyListState.animateScrollToItem(
                        (selectedIndex - 1).coerceIn(
                            0,
                            unitList.lastIndex
                        )
                    )

                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .height(itemSize * 4)
                            .width(240.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = lazyListState,
                        horizontalAlignment = horizontalAlignment
                    ) {
                        itemsIndexed(unitList) { index, unit ->
                            val text = remember { when {
                                unit.symbol.isBlank() -> unit.name
                                unit.name.isBlank() -> unit.symbol
                                else -> unit.name + " (${unit.symbol})"
                            } }
                            MenuItem(
                                text = text,
                                selected = selectedIndex == index,
                                itemSize = itemSize
                            )
                        }
                    }
//                    Box(
//                        modifier = Modifier
//                            .border(
//                                width = 2.dp,
//                                color =MaterialTheme.colorScheme.error,
//                                shape = MaterialTheme.shapes.large
//                            )
//                    ) {
//                        Text(
//                            text = stringResource(R.string.cancel)
//                        )
//                    }
                }

            }


        }


    }

}


@Composable
private fun MenuItem(
    text: String,
    selected: Boolean,
    itemSize: Dp,
) {
    val animateSize by animateFloatAsState(
        if (selected) 1.1f else 1f
    )
    Box(
        modifier = Modifier
            .height(itemSize)
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            modifier = Modifier.graphicsLayer(
                scaleX = animateSize,
                scaleY = animateSize
            ),
            text = text,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified,
            overflow = TextOverflow.Ellipsis
        )
    }


}
