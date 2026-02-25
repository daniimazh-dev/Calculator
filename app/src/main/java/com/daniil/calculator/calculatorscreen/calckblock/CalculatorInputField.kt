package com.daniil.calculator.calculatorscreen.calckblock

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.universal.Copy
import com.daniil.calculator.universal.Paste
import com.daniil.calculator.universal.UniversalDropDownMenu
import com.daniil.calculator.universal.plus
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalculatorInputField(
    tokens: List<String>,
    modifier: Modifier = Modifier,
    selectedTokenIndex: Int? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    onTokenClicked: (index: Int, token: String) -> Unit = { _, _ -> },
    onChangeToken: (index: Int, token: String) -> Unit = { _, _ -> },
    onCancelSelection: () -> Unit = {},

) {

    val previousTokens = remember { mutableStateOf(tokens) }
    SideEffect {
        previousTokens.value = tokens
    }


    BackHandler(selectedTokenIndex != null) {
        onCancelSelection()
    }


    FlowRow(
        modifier = modifier
            .padding(4.dp)
    ) {
        (tokens + listOf("")).forEachIndexed { index, token ->
            var dropdownMenuExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .alpha(
                        if (selectedTokenIndex == null || selectedTokenIndex == index) 1f else 0.5f
                    )
                    .border(
                        if (selectedTokenIndex == index) 2.dp else 0.dp,
                        if (selectedTokenIndex == index)
                            MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent,
                        MaterialTheme.shapes.small
                    )
            ) {


                Row(
                    modifier = Modifier
                        .padding(
                            horizontal = when {
                                selectedTokenIndex == index ->  6.dp
                                token.isBlank() -> 0.dp
                                else -> 2.dp
                            }
                        )
                        .combinedClickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                onTokenClicked(index, token)
                            },
                            onLongClick = {
                                dropdownMenuExpanded = true
                            }
                        )

                ) {

                    val oldToken = previousTokens.value.getOrNull(index).orEmpty()
//                    val maxLength = maxOf(oldToken.length, token.length) + 1
                    val resultToken = if (oldToken.length > token.length) oldToken else token
                    AutoSizeAnimatedToken(
                        token = resultToken,
                        baseTextStyle = textStyle,
                    )

                    UniversalDropDownMenu(
                        expanded = dropdownMenuExpanded,
                        buttonList = Copy(token)
                                + Paste { str ->
                                    if (str.isNullOrBlank()) return@Paste
                                    onChangeToken(index, str)
                                },
                        enabled = selectedTokenIndex == index,
                        onDismissRequest = {
                            dropdownMenuExpanded = false
                        }
                    )
                }

            }
        }
    }
}


@Composable
private fun AutoSizeAnimatedToken(
    token: String,
    modifier: Modifier = Modifier,
    baseTextStyle: TextStyle,
    minFontSize: TextUnit = 12.sp
) {
    BoxWithConstraints(modifier) {

        val measurer = rememberTextMeasurer()

        val targetFontSize = remember(token, constraints.maxWidth) {
            calculateBinaryFontSize(
                token = token,
                measurer = measurer,
                baseTextStyle = baseTextStyle,
                maxWidthPx = constraints.maxWidth,
                minFontSize = minFontSize
            )
        }

        val animatedFontSize by animateFloatAsState(
            targetValue = targetFontSize.value,
            label = "fontSizeAnim"
        )

        val animatedStyle = baseTextStyle.copy(
            fontSize = animatedFontSize.sp
        )

        Row {
            repeat(token.length + 1) {
                AnimatedChar(
                    char = token.getOrNull(it),
                    textStyle = animatedStyle
                )
            }
        }
    }
}


private fun measureTokenWidth(
    token: String,
    measurer: TextMeasurer,
    textStyle: TextStyle,
): Int {
    return token.sumOf { char ->
        CharWidthCache.getOrPut(char, textStyle.fontSize) {
            measurer.measure(
                text = char.toString(),
                style = textStyle
            ).size.width
        }
    }
}


private object CharWidthCache {
    private val map = mutableMapOf<Pair<Char, TextUnit>, Int>()

    fun getOrPut(
        char: Char,
        fontSize: TextUnit,
        measure: () -> Int
    ): Int {
        return map.getOrPut(char to fontSize, measure)
    }
}


private fun calculateBinaryFontSize(
    token: String,
    measurer: TextMeasurer,
    baseTextStyle: TextStyle,
    maxWidthPx: Int,
    minFontSize: TextUnit
): TextUnit {

    var low = minFontSize.value
    var high = baseTextStyle.fontSize.value
    var best = low

    while (low <= high) {
        val mid = (low + high) / 2f

        val width = measureTokenWidth(
            token = token,
            measurer = measurer,
            textStyle = baseTextStyle.copy(fontSize = mid.sp),

        )

        if (width <= maxWidthPx) {
            best = mid
            low = mid + 0.5f
        } else {
            high = mid - 0.5f
        }
    }

    return best.sp
}


@Composable
private fun AnimatedChar(
    char: Char?,
    textStyle: TextStyle,
) {
    AnimatedContent(
        targetState = char,
        transitionSpec = {
            (slideInHorizontally { it } + fadeIn() + scaleIn(initialScale = 0.5f))
                .togetherWith(slideOutHorizontally { -it } + fadeOut() + scaleOut())
                .using(SizeTransform(clip = false))
        },
        label = "CharAnim"
    ) { targetChar ->
        if (targetChar != null) {
            Text(
                text = targetChar.toString(),
                style = textStyle
            )
        }
    }
}
