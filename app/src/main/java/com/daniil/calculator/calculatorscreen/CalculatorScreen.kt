package com.daniil.calculator.calculatorscreen

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniil.calculator.R
import com.daniil.calculator.calculatorscreen.buttons.ButtonPanel
import com.daniil.calculator.calculatorscreen.calckblock.CalckBlockPanel
import com.daniil.calculator.calculatorscreen.history.HistoryBlock
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.openScreen
import com.daniil.calculator.universal.esterEggWave
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    calckScreenModel: CalculatorScreenModel,
    convertorScreenModel: ConvertorScreenModel,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT


    val buttons by calckScreenModel.buttons.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true
    )

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )


    val widthPx = LocalWindowInfo.current.containerSize.width
    val heightPx = LocalWindowInfo.current.containerSize.width
    val density = LocalDensity.current

    val screenWidth = with(density) { widthPx.toDp() }
    val screenHeight = with(density) { heightPx.toDp() }



    if (isPortrait) {
        val bottomSheetExpanded =
            remember(bottomSheetScaffoldState.bottomSheetState.currentValue) {
                mutableStateOf(bottomSheetScaffoldState.bottomSheetState.currentValue)
            }
        val arrowAnimation = animateFloatAsState(
            if (bottomSheetScaffoldState
                    .bottomSheetState
                    .currentValue == SheetValue.Expanded
            ) 180f else 0f
        )
        val waveEffect by calckScreenModel.waveEffect.collectAsState()
        val infiniteAnimation = rememberInfiniteTransition()
        LaunchedEffect(waveEffect) {
            delay(5000)
            calckScreenModel.waveEffect.value = false
        }
        val progress by infiniteAnimation.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )


        var sheetPeekHeight by remember { mutableStateOf<IntSize>(IntSize.Zero) }
        BottomSheetScaffold(

            modifier = Modifier.esterEggWave(
                enabled = waveEffect,
                progress = progress,
            ),
            containerColor = Color.Transparent,
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = sheetPeekHeight.height.dp / 2,
            sheetContent = {

                CalckBlockPanel(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    calculatorScreenModel = calckScreenModel,
                    convertorScreenModel = convertorScreenModel,
                    onSizeChange = {
                        sheetPeekHeight = it
                    }
                )

                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ButtonPanel(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        calckScreenModel = calckScreenModel,
                        buttonSize = screenHeight / 6
                    )
                }

            },
            sheetDragHandle = {
                val coroutine = rememberCoroutineScope()
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer(rotationZ = arrowAnimation.value)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            coroutine.launch {
                                if (bottomSheetExpanded.value == SheetValue.Expanded) {
                                    bottomSheetScaffoldState.bottomSheetState.partialExpand()
                                } else {
                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                }
                            }

                        },

                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Expand/Collapse"
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { sheetPadding ->
            Column(
                modifier = Modifier
                    .padding(sheetPadding)
                    .padding(horizontal = 16.dp),
            ) {
                HistoryBlock(
                    calculatorScreenModel = calckScreenModel
                )
            }

        }
    } else {
        val componentOffset by calckScreenModel.componentOffset.collectAsState()

        var localButtonsOffset by remember { mutableStateOf(componentOffset) }
        var localHistoryOffset by remember { mutableStateOf(0f) }

        var isDrag by remember { mutableStateOf(false) }


        Row {

            Column(
                modifier = Modifier
                    .width((screenWidth.value / 2 + localButtonsOffset).dp),
            ) {
                var panelHeight by remember { mutableStateOf(IntSize.Zero) }
                if (!isDrag) {
                    CalckBlockPanel(
                        modifier = Modifier.padding(6.dp),
                        calculatorScreenModel = calckScreenModel,
                        convertorScreenModel = convertorScreenModel,
                        onSizeChange = {
                            panelHeight = it
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(panelHeight.height.dp / 2),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(R.drawable.equal_icon),
                            contentDescription = "equalIcon"
                        )
                    }
                }

                HorizontalDivider()

                if (!isDrag) {
                    HistoryBlock(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.End,
                        calculatorScreenModel = calckScreenModel
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(R.drawable.history_icon),
                            contentDescription = "historyIcon"
                        )
                    }
                }


            }

            Box(
                modifier = Modifier
                    .width((screenWidth.value / 2 - localButtonsOffset).dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 6.dp)
                            .pointerInput(Unit) {
                                val changeSizeRightK = 8f
                                val changeSizeLeftK = 4f
                                val changeSpeed = 2.5f
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        calckScreenModel.componentOffset.value =
                                            localButtonsOffset
                                        isDrag = false
                                    }
                                ) { change, dragAmount ->
                                    isDrag = true

                                    localButtonsOffset += dragAmount / changeSpeed

                                    if (localButtonsOffset < -(screenWidth.value / changeSizeLeftK)) {
                                        localButtonsOffset = -(screenWidth.value / changeSizeLeftK)
                                        return@detectHorizontalDragGestures
                                    }
                                    if (localButtonsOffset > screenWidth.value / changeSizeRightK) {
                                        localButtonsOffset = screenWidth.value / changeSizeRightK
                                        return@detectHorizontalDragGestures

                                    }
                                    change.consume()
                                }
                            },

                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .height(animateDpAsState(if (isDrag) 26.dp else 16.dp).value)
                                .width(6.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    if (!isDrag) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            ButtonPanel(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                buttonSize = 64.dp,
                                calckScreenModel = calckScreenModel,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                modifier = Modifier.size(64.dp),
                                painter = painterResource(R.drawable.calculator_icon),
                                contentDescription = "calculatorButtonsIcon"
                            )
                        }
                    }


                }

            }


        }

    }


}