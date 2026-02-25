package com.daniil.calculator.universal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.*


@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    hour: Int = 12,
    minute: Int = 0,
    onTimeChanged: (hour: Int, minute: Int) -> Unit

) {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    Row(
        modifier = modifier.height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        ScrollPickerList(
            modifier = Modifier.weight(1f),
            infiniteCycle = true,
            items = hours.map { it.toString().padStart(2, '0') },
            selectedIndex = hour,
            onIndexChanged = { index ->
                onTimeChanged(index, minute)
            }
        )
        Text(
            ":",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        ScrollPickerList(
            modifier = Modifier.weight(1f),
            items = minutes.map { it.toString().padStart(2, '0') },
            infiniteCycle = true,
            selectedIndex = minute,
            onIndexChanged = { index ->
                onTimeChanged(hour, index)
            }
        )
    }
}


@Composable
private fun ScrollPickerList(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit,
    infiniteCycle: Boolean = false,
    onCycleComplete: ((direction: Int) -> Unit)? = null
) {
    val visibleCount = 3
    val midIndex = visibleCount / 2
    val itemCount = items.size

    val displayItems = remember(infiniteCycle, items) {
        if (infiniteCycle) List(itemCount * 20) { i -> items[i % itemCount] } else items
    }

    val totalSize = displayItems.size

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (infiniteCycle)
            totalSize / 2 + selectedIndex - midIndex
        else
            (selectedIndex - midIndex).coerceAtLeast(0)
    )

    var currentOffset by remember { mutableIntStateOf((listState.firstVisibleItemIndex + midIndex) / itemCount) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex + midIndex
            val realIndex = (centerIndex % itemCount + itemCount) % itemCount
            onIndexChanged(realIndex)

        }
    }

    // === Перевірка переходу кола ===
    LaunchedEffect(listState.isScrollInProgress) {
        if (infiniteCycle && !listState.isScrollInProgress) {

            val currentCycle = (listState.firstVisibleItemIndex + midIndex) / itemCount
            val centerIndex = listState.firstVisibleItemIndex + midIndex
            val realIndex = (centerIndex % itemCount + itemCount) % itemCount
            val base = (centerIndex / itemCount) * itemCount
            val targetIndex = base + realIndex - midIndex


            listState.animateScrollToItem(targetIndex)
            if (currentCycle > currentOffset) {
                onCycleComplete?.invoke(currentCycle - currentOffset)
                currentOffset = currentCycle
                listState.animateScrollToItem(targetIndex)
            } else if (currentCycle < currentOffset) {
                onCycleComplete?.invoke(currentCycle - currentOffset)
                currentOffset = currentCycle
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            val currentCycle = (listState.firstVisibleItemIndex + midIndex) / itemCount
            val target = if (infiniteCycle) {
                val base = (currentCycle * itemCount)
                base + selectedIndex - midIndex
            } else selectedIndex - midIndex

            if (target in 0 until totalSize) {
                coroutineScope.launch {
                    listState.animateScrollToItem(target)
                }

                val centerIndex = listState.firstVisibleItemIndex + midIndex
                val realIndex = (centerIndex % itemCount + itemCount) % itemCount
                val targetIndex = if (infiniteCycle) {
                    val base = (centerIndex / itemCount) * itemCount
                    base + realIndex - midIndex
                } else {
                    (realIndex - midIndex).coerceIn(0, totalSize - 1)
                }
                listState.animateScrollToItem(targetIndex)

                currentOffset = currentCycle
            } else {

            }
        }
    }

    // === Візуалізація ===
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            items(totalSize) { index ->
                val offset = (index - (listState.firstVisibleItemIndex + midIndex)).toFloat()
                val alpha = (1f - (abs(offset) / midIndex)).coerceIn(0.3f, 1f)
                val size = 24
                val isSelected = offset == 0f
                if (index % itemCount == 0 && infiniteCycle) {
                    HorizontalDivider()
                }
                Text(
                    modifier = Modifier
                        .padding(vertical = 6.dp),
                    text = displayItems[index].replaceFirstChar { it.uppercase() },
                    fontSize = size.sp,
                    style = if (isSelected)
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                )
            }
        }
    }
}






@Composable
fun CircularTimeIndicator(
    modifier: Modifier = Modifier,
    from: LocalTime,
    to: LocalTime,

) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            val canvasWidth = size.width
            val canvasHeight = size.height
            val cx = canvasWidth / 2f
            val cy = canvasHeight / 2f

            val ringWidth = 32.dp.toPx()
            val handleRadius = 12.dp.toPx()

            val radius = min(cx, cy) - ringWidth / 2f - handleRadius

            fun timeToAngle(t: LocalTime): Float {
                val secs = t.toSecondOfDay().toDouble()
                val frac = secs / 86400.0
                return (frac * 360f).toFloat()
            }

            fun drawArcStart(angleTop: Float) = angleTop - 90f

            fun angleToOffset(angleFromTop: Float, r: Float): Offset {
                val rad = Math.toRadians((angleFromTop - 90f).toDouble())
                return Offset(
                    (cx + r * cos(rad)).toFloat(),
                    (cy + r * sin(rad)).toFloat()
                )
            }

            val startAngle = timeToAngle(from)
            val endAngle = timeToAngle(to)
            val sweep = ((endAngle - startAngle + 360f) % 360f).let { if (it == 0f) 360f else it }

            // --- Dark background ring (like iOS Sleep mode) ---
            drawCircle(
                color = Color(0xFF2C2C2E),
                radius = radius + ringWidth / 2f,
                center = Offset(cx, cy),
                style = Stroke(width = ringWidth, cap = StrokeCap.Round)
            )

            // --- Blue active arc ---
            drawArc(
                color = Color(0xFF0A84FF),
                startAngle = drawArcStart(startAngle),
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(cx - radius, cy - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = ringWidth, cap = StrokeCap.Round)
            )

            // --- Handles on both ends ---
            val startPos = angleToOffset(startAngle, radius)
            val endPos = angleToOffset((startAngle + sweep) % 360f, radius)

            drawCircle(Color.White, handleRadius, center = startPos)
            drawCircle(Color.White, handleRadius, center = endPos)
        }

        // ---- Center text ----
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "%02d:%02d".format(from.hour, from.minute),
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.material3.Text(
                text = "%02d:%02d".format(to.hour, to.minute),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 22.sp
            )
        }
    }
}
