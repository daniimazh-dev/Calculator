package com.daniil.calculator.universal

import android.graphics.RenderEffect.createRuntimeShaderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    reversedLayout: Boolean = false,
    width: Dp = 6.dp,
    cornerRadius: Dp = 3.dp,
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(
            durationMillis = if (state.isScrollInProgress) 150 else 600,
            easing = LinearOutSlowInEasing
        )
    )

    val scrollbarColor = MaterialTheme.colorScheme.primary
    val scrollbarBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    val layoutInfo = state.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo

    val targetThumbHeightPx = remember(totalItems, visibleItems) {
        0f
    }
    val targetThumbOffsetPx = remember(totalItems, visibleItems) {
        0f
    }

    val visibleRatioFraction = if (totalItems > 0 && visibleItems.isNotEmpty()) {
        (visibleItems.size.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val firstVisible = visibleItems.firstOrNull()
    val targetScrollFraction = if (totalItems > 0 && firstVisible != null) {
        val itemIndex = firstVisible.index.toFloat()
        val itemSize = firstVisible.size.coerceAtLeast(1).toFloat()
        val offsetInItem = -firstVisible.offset.toFloat()
        val partial = (offsetInItem / itemSize).coerceIn(0f, 1f)
        (itemIndex + partial) / totalItems.toFloat()
    } else 0f

    val animatedScrollFraction by animateFloatAsState(
        targetValue = targetScrollFraction.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val animatedVisibleRatio by animateFloatAsState(
        targetValue = visibleRatioFraction.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    return drawWithContent {
        drawContent()

        if (totalItems == 0 || visibleItems.isEmpty()) return@drawWithContent

        val canvasHeight = size.height
        val canvasWidth = size.width

        val minThumbHeight = 20f
        val thumbHeight = (canvasHeight * animatedVisibleRatio).coerceAtLeast(minThumbHeight)

        val maxTop = canvasHeight - thumbHeight
        val rawOffsetY = animatedScrollFraction * canvasHeight
        val thumbOffsetY =
            try {
                rawOffsetY.coerceIn(0f, maxTop)
            } catch (e: IllegalArgumentException) {
                return@drawWithContent
            }

        val finalOffsetY = if (reversedLayout) {
            (canvasHeight - thumbHeight - thumbOffsetY).coerceIn(0f, maxTop)
        } else {
            thumbOffsetY
        }

        if (alpha > 0f) {
            // background (track)
            drawRoundRect(
                color = scrollbarBackground,
                topLeft = Offset(canvasWidth - width.toPx(), 0f),
                size = Size(width.toPx(), canvasHeight),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                alpha = alpha * 0.5f
            )

            // scrollbar thumb (gradient)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        scrollbarColor.copy(alpha = 0.8f),
                        scrollbarColor.copy(alpha = 0.5f)
                    )
                ),
                topLeft = Offset(canvasWidth - width.toPx(), finalOffsetY),
                size = Size(width.toPx(), thumbHeight),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                alpha = alpha
            )

            // highlight shadow
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = alpha * 0.1f),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(canvasWidth - width.toPx(), finalOffsetY),
                size = Size(width.toPx(), thumbHeight),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }
    }
}



private const val WAVE_SHADER = """
uniform shader content;

uniform float progress;
uniform float amplitude;
uniform float width;
uniform float height;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord;

    float2 center = float2(width * 0.5, height * 0.5);

    // нормалізація Y (0..1)
    float yNorm = uv.y / height;

    // мʼякий falloff від центру
    float falloff = smoothstep(0.0, 0.5, 0.5 - abs(yNorm - 0.5));

    // хвиля
    float wave = sin((yNorm * 8.0) - (progress * 6.28318));

    // сила хвилі
    float strength = wave * amplitude * falloff;

    // зміщення X (основна хвиля)
    uv.x += strength * 40.0;

    // невеликий просторовий Y-зсув (глибина)
    uv.y += sin((uv.x / width * 6.0) + progress * 4.0) * amplitude * falloff * 8.0;

    return content.eval(uv);
}
"""



fun Modifier.esterEggWave(
    enabled: Boolean,
    progress: Float,
    amplitude: Float = 1f
): Modifier = graphicsLayer {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@graphicsLayer

    val shader = RuntimeShader(WAVE_SHADER)

    shader.setFloatUniform("progress", progress)
    shader.setFloatUniform("amplitude", amplitude)
    shader.setFloatUniform("width", size.width)
    shader.setFloatUniform("height", size.height)

    if (enabled) {
        renderEffect = createRuntimeShaderEffect(
            shader,
            "content"
        ).asComposeRenderEffect()
    }
}


