package com.daniil.calculator.utilites

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign


class CustomOverscrollEffect(val scope: CoroutineScope) : OverscrollEffect {
    private val overscrollOffset = Animatable(0f)
    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        val sameDirection = sign(delta.y) == sign(overscrollOffset.value)
        val consumedByPreScroll =
            if (abs(overscrollOffset.value) > 0.5 && !sameDirection) {
                val prevOverscrollValue = overscrollOffset.value
                val newOverscrollValue = overscrollOffset.value + delta.y
                if (sign(prevOverscrollValue) != sign(newOverscrollValue)) {
                    // sign changed, coerce to start scrolling and exit
                    scope.launch { overscrollOffset.snapTo(0f) }
                    Offset(x = 0f, y = delta.y + prevOverscrollValue)
                } else {
                    scope.launch { overscrollOffset.snapTo(overscrollOffset.value + delta.y) }
                    delta.copy(x = 0f)
                }
            } else {
                Offset.Zero
            }
        val leftForScroll = delta - consumedByPreScroll
        val consumedByScroll = performScroll(leftForScroll)
        if (source == NestedScrollSource.UserInput) {
            val dragDelta = delta.y
            if (abs(dragDelta) > 0.5f) {
                scope.launch {
                    overscrollOffset.snapTo(
                        overscrollOffset.value + dragDelta * 0.1f
                    )
                }
            }
        }

        return consumedByPreScroll + consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        val consumed = performFling(velocity)
        // when the fling happens - we just gradually animate our overscroll to 0
        val remaining = velocity - consumed
        overscrollOffset.animateTo(
            targetValue = 0f,
            initialVelocity = remaining.y,
            animationSpec = spring(),
        )
    }

    override val isInProgress: Boolean
        get() = overscrollOffset.value != 0f

    // Create a LayoutModifierNode that offsets by overscrollOffset.value
    override val node: DelegatableNode =
        object : Modifier.Node(), LayoutModifierNode {
            override fun MeasureScope.measure(
                measurable: Measurable,
                constraints: Constraints,
            ): MeasureResult {
                val placeable = measurable.measure(constraints)
                return layout(placeable.width, placeable.height) {
                    val offsetValue = IntOffset(x = 0, y = overscrollOffset.value.roundToInt())
                    placeable.placeRelativeWithLayer(offsetValue.x, offsetValue.y)
                }
            }
        }
}


@Composable
fun Modifier.customOverscroll(
    orientation: Orientation,
    onNewOverscrollAmount: (Float) -> Unit,
    animationSpec: SpringSpec<Float> = spring(stiffness = Spring.StiffnessLow)
): Modifier {
    val overscrollAmountAnimatable = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        snapshotFlow { overscrollAmountAnimatable.value }.collect {
            onNewOverscrollAmount(it)
        }
    }

    var length by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        snapshotFlow { overscrollAmountAnimatable.value }.collect {
            onNewOverscrollAmount(
                CustomEasing.transform(it / (length * 1.5f)) * length
            )
        }
    }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                scope.launch {
                    overscrollAmountAnimatable.snapTo(targetValue = calculateOverscroll(available))
                }
                return Offset.Zero
            }
            private fun calculateOverscroll(available: Offset): Float {
                val previous = overscrollAmountAnimatable.value
                val newValue = previous + when (orientation) {
                    Orientation.Vertical -> available.y
                    Orientation.Horizontal -> available.x
                }
                return when {
                    previous > 0 -> newValue.coerceAtLeast(0f)
                    previous < 0 -> newValue.coerceAtMost(0f)
                    else -> newValue
                }
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val availableVelocity = when (orientation) {
                    Orientation.Vertical -> available.y
                    Orientation.Horizontal -> available.x
                }

                overscrollAmountAnimatable.animateTo(
                    targetValue = 0f,
                    initialVelocity = availableVelocity,
                    animationSpec = animationSpec
                )

                return available
            }
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollAmountAnimatable.value != 0f && source != NestedScrollSource.SideEffect) {
                    scope.launch {
                        overscrollAmountAnimatable.snapTo(calculateOverscroll(available))
                    }
                    return available
                }

                return super.onPreScroll(available, source)
            }
            override suspend fun onPreFling(available: Velocity): Velocity {
                val availableVelocity = when (orientation) {
                    Orientation.Vertical -> available.y
                    Orientation.Horizontal -> available.x
                }

                if (overscrollAmountAnimatable.value != 0f && availableVelocity != 0f) {
                    val previousSign = overscrollAmountAnimatable.value.sign
                    var consumedVelocity = availableVelocity
                    val predictedEndValue = exponentialDecay<Float>().calculateTargetValue(
                        initialValue = overscrollAmountAnimatable.value,
                        initialVelocity = availableVelocity,
                    )
                    if (predictedEndValue.sign == previousSign) {
                        overscrollAmountAnimatable.animateTo(
                            targetValue = 0f,
                            initialVelocity = availableVelocity,
                            animationSpec = animationSpec,
                        )
                    } else {
                        try {
                            overscrollAmountAnimatable.animateDecay(
                                initialVelocity = availableVelocity,
                                animationSpec = exponentialDecay()
                            ) {
                                if (value.sign != previousSign) {
                                    consumedVelocity -= velocity
                                    scope.launch {
                                        overscrollAmountAnimatable.snapTo(0f)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }

                    return when (orientation) {
                        Orientation.Vertical -> Velocity(0f, consumedVelocity)
                        Orientation.Horizontal -> Velocity(consumedVelocity, 0f)
                    }
                }

                return super.onPreFling(available)
            }

        }
    }


    return this
        .onSizeChanged {
            length = when (orientation) {
                Orientation.Vertical -> it.height.toFloat()
                Orientation.Horizontal -> it.width.toFloat()
            }
        }
        .nestedScroll(nestedScrollConnection)
}

private val CustomEasing: Easing = CubicBezierEasing(0.3f, 0.3f, 0.75f, 0f)


@Composable
fun Modifier.customOverscroll(
    listState: LazyListState,
    onNewOverscrollAmount: (Float) -> Unit,
    animationSpec: SpringSpec<Float> = spring(stiffness = Spring.StiffnessLow)
) = customOverscroll(
    orientation = remember { listState.layoutInfo.orientation },
    onNewOverscrollAmount = onNewOverscrollAmount,
    animationSpec = animationSpec
)

@Composable
fun Modifier.customOverscroll(
    pagerState: PagerState,
    onNewOverscrollAmount: (Float) -> Unit,
    animationSpec: SpringSpec<Float> = spring(stiffness = Spring.StiffnessLow)
) = customOverscroll(
    orientation = remember { pagerState.layoutInfo.orientation },
    onNewOverscrollAmount = onNewOverscrollAmount,
    animationSpec = animationSpec
)