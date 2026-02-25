package com.daniil.calculator

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.min


// ----------------- Navigator core -----------------

class Navigator internal constructor(
    private val registry: Map<Any, ScreenParam>
) {
    private val _stack = mutableStateListOf<Any>()
    val stack: List<Any> get() = _stack

    var lastAction by mutableStateOf<NavAction>(NavAction.Push) // для push/pop анімацій

    fun push(route: Any) {
        if (registry.containsKey(route)) {
            if (registry[route]?.single == true) _stack.remove(route)
            lastAction = NavAction.Push
            _stack.add(route)

        }
    }

    fun popTo(route: Any) {
        if (registry.containsKey(route)) {
            _stack.removeRange(_stack.lastIndex, _stack.indexOf(route))
        }
    }

    fun pop() {
        if (_stack.size > 1) {
            lastAction = NavAction.Pop
            _stack.removeAt(_stack.size - 1)
        }
    }

    internal fun screenFor(route: Any): ScreenNavigatorScope? = registry[route]?.content
}

enum class NavAction { Push, Pop }

typealias ScreenNavigatorScope = @Composable (navigator: Navigator) -> Unit


data class ScreenParam(
    val key: Any,
    val content: ScreenNavigatorScope,
    val single: Boolean
)

// ----------------- Builder DSL -----------------

class ScreenRegistryBuilder {
    private val screens = mutableMapOf<Any, ScreenParam>()

    var alwaysSingle = false
    fun screen(
        route: Any,
        single: Boolean = alwaysSingle,
        content: ScreenNavigatorScope,

        ) {
        screens[route] = ScreenParam(
            key = route,
            content = content,
            single = single
        )
    }

    fun build(): Map<Any, ScreenParam> = screens
}

@SuppressLint("ComposableNaming")
@Composable
fun rememberScreenNavigation(
    startDestination: Any,
    content: ScreenRegistryBuilder.() -> Unit
): Navigator {
    val registry = remember { ScreenRegistryBuilder().apply(content).build() }
    val navigator = remember { Navigator(registry) }
    navigator.push(startDestination)

    return navigator
}

// ----------------- Navigator Composable -----------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScreenNavigator(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    animationSpeed: Int = 400,
    enter: @Composable (Any, Any?) -> EnterTransition = { _, _ ->
        slideInHorizontally(
            animationSpec = tween(animationSpeed),
            initialOffsetX = { it }
        )
    },
    exit: @Composable (Any, Any?) -> ExitTransition = { _, _ ->
        slideOutHorizontally(
            animationSpec = tween(animationSpeed),
            targetOffsetX = { -it }
        )
    }
) {
    val stack = navigator.stack
    if (stack.isEmpty()) return

    val lastStack = stack.last()
    val previousStack = stack.getOrNull(stack.lastIndex - 1)

    BackHandler(previousStack != null) {
        navigator.pop()
    }
    val transmission = if (navigator.lastAction == NavAction.Push) {
        enter(lastStack, previousStack).togetherWith(exit(lastStack, previousStack))
    } else {
        (enter(previousStack ?: lastStack, lastStack)).togetherWith(
            (exit(
                previousStack ?: lastStack, lastStack
            ))
        )

    }

    AnimatedContent(
        targetState = lastStack,
        transitionSpec = { transmission },
        modifier = modifier,
        label = "ScreenNavigatorAnimation"
    ) { route ->
        navigator.screenFor(route)?.invoke(navigator)
    }
}
