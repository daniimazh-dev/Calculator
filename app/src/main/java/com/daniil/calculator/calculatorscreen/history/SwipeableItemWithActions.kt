package com.daniil.calculator.calculatorscreen.history

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SwipeableItemWithActions(
    isRevealed: Boolean,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    onExpanded: () -> Unit = {},
    onCollapsed: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var actionsFullWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)

    ) {

        val maxActionsWidthPx = constraints.maxWidth * 0.5f
        val limitPx = min(actionsFullWidth, maxActionsWidthPx)

        LaunchedEffect(isRevealed, limitPx) {
            offset.animateTo(if (isRevealed) limitPx else 0f)
        }

        Box() {
            Row(
                modifier = Modifier
                    .alpha(0f)
                    .onSizeChanged {
                        actionsFullWidth = it.width.toFloat()
                    }
            ) {
                actions()
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(with(density) { offset.value.toDp() }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }

            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(-offset.value.roundToInt(), 0) }
                    .pointerInput(limitPx) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    offset.snapTo(
                                        (offset.value - dragAmount)
                                            .coerceIn(0f, limitPx)
                                    )
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offset.value > limitPx / 2) {
                                        offset.animateTo(limitPx)
                                        onExpanded()
                                    } else {
                                        offset.animateTo(0f)
                                        onCollapsed()
                                    }
                                }
                            }
                        )
                    }
            ) {
                content()
            }
        }
    }
}