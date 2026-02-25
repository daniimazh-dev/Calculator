package com.daniil.calculator.convertorscreen.convertor

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.buttonpanel.ConvertorCalckBlockPanel
import com.daniil.calculator.convertorscreen.convertor.buttonpanel.ConvertorCalckButtonPanel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.AlertDialogExperimentalWarning
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.AlertDialogUnavailableWarning
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.ConvertorContent
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.StandardConvertor
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.findOfId
import com.daniil.calculator.convertorscreen.convertor.topbar.ConvertorCalckTopBar
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorReleseState
import com.daniil.calculator.convertorscreen.report.ReportSheetContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.ConvertorCalckScreen(
    modifier: Modifier = Modifier,
    convertorButtonData: ConvertorData,
    convertorScreenModel: ConvertorScreenModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val buttonStack by convertorScreenModel.calckButtons.collectAsState()
    val viewListMode by convertorScreenModel.viewConvertorMode.collectAsState()

    val type = remember { convertorButtonData.release }

    val snackbarHostState = remember { SnackbarHostState() }

    val convertorData =
        convertorScreenModel.customConvertorManager.getImplementation(convertorButtonData.id)?.buildResult
            ?: convertorScreenModel.customConvertorManager
                .localImplementation(StandardConvertor(convertorButtonData, convertorScreenModel, context))

    val viewScreen = convertorData.convertorScreen.viewScreens.findOfId(viewListMode)

    BackHandler(true) {
        convertorScreenModel.goToHome()
    }

    if (type == ConvertorReleseState.Experimental || type == ConvertorReleseState.Unavailable) {
        var warningAlertShow by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {

            delay(500)
            warningAlertShow = true
        }
        when (type) {
            ConvertorReleseState.Experimental -> {
                AlertDialogExperimentalWarning(
                    expanded = warningAlertShow,
                    onDismissRequest = {
                        warningAlertShow = false
                    }
                )
            }

            ConvertorReleseState.Unavailable -> {
                AlertDialogUnavailableWarning(
                    expanded = warningAlertShow,
                    onDismissRequest = {
                        warningAlertShow = false
                        convertorScreenModel.goToHome()

                    }
                )
            }

            else -> {}
        }

    }


    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )


    val isReportSheetShow by convertorScreenModel.reportErrorSheetShow.collectAsState()
    if (isReportSheetShow) {
        ModalBottomSheet(
            dragHandle = {
                IconButton(onClick = {
                    convertorScreenModel.reportErrorSheetShow.value = false
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "close"
                    )
                }
            },
            contentWindowInsets = { WindowInsets.safeContent },
            sheetState = sheetState,
            onDismissRequest = {
                convertorScreenModel.reportErrorSheetShow.value = false
            }
        ) {
            ReportSheetContent(
                modifier = Modifier,
                convertorScreenModel = convertorScreenModel,
                convertorData = convertorButtonData,
                onDismissRequest = {
                    convertorScreenModel.reportErrorSheetShow.value = false
                }
            )
        }
    }


    val scaffoldSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = scaffoldSheetState
    )


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



    if (isPortrait) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = convertorButtonData.id),
                    animatedVisibilityScope = animatedVisibilityScope,
//                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
//                    placeholderSize = SharedTransitionScope.PlaceholderSize.AnimatedSize,
                )
        ) {
            var sheetPeekHeight by remember { mutableStateOf(IntSize.Zero) }

            if (viewScreen?.butonPanel?.showKeyboard?.value == true) {
                BottomSheetScaffold(
                    containerColor = Color.Transparent,
                    scaffoldState = bottomSheetScaffoldState,
                    sheetPeekHeight = 64.dp,
                    sheetContent = {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                        ) {
                            convertorData.apply {
                                if (viewScreen.showCalckPanel.value) {
                                    ConvertorCalckBlockPanel(
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        convertorScreenModel = convertorScreenModel,
                                        convertorData = convertorButtonData,
                                        onChangeSize = {

                                        }
                                    )
                                }
                            }

                        }

                        ConvertorCalckButtonPanel(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .padding(top = 6.dp),
                            buttonStack = buttonStack
                        )
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
                            .fillMaxSize()
                            .padding(sheetPadding),
                    ) {
                        ConvertorCalckTopBar(

                            convertorScreenModel = convertorScreenModel,
                            convertorButtonData = convertorButtonData,
                            convertorData = convertorData
                        )
                        ConvertorContent(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            convertorScreenModel = convertorScreenModel,
                            convertorButtonData = convertorButtonData,
                            convertorData = convertorData
                        )

                    }

                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
//                        .padding(sheetPadding),
                ) {
                    ConvertorCalckTopBar(
                        convertorScreenModel = convertorScreenModel,
                        convertorButtonData = convertorButtonData,
                        convertorData = convertorData
                    )
                    ConvertorContent(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        convertorScreenModel = convertorScreenModel,
                        convertorButtonData = convertorButtonData,
                        convertorData = convertorData
                    )

                }
            }

        }
    } else {
        val widthPx = LocalWindowInfo.current.containerSize.width
        val density = LocalDensity.current
        val screenSize = with(density) { widthPx.toDp() }
        val componentOffset by convertorScreenModel.componentOffset.collectAsState()
        var localComponentOffset by remember { mutableStateOf(componentOffset) }
        var isDrag by remember { mutableStateOf(false) }

        val leftOffsetModifier =
            if (viewScreen?.butonPanel?.showKeyboard?.value == true) {
                Modifier.width(
                    (screenSize.value / 2 + localComponentOffset).dp
                )
            } else {
                Modifier.fillMaxWidth()
            }
        val rightOffsetModifier =
            if (viewScreen?.butonPanel?.showKeyboard?.value == true) {
                Modifier.width(
                    (screenSize.value / 2 - localComponentOffset).dp
                )
            } else {
                Modifier
            }



        Row(
            modifier = modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = convertorButtonData.id),
                    animatedVisibilityScope = animatedVisibilityScope,
//                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
//                    placeholderSize = SharedTransitionScope.PlaceholderSize.AnimatedSize,
                )
        ) {

            Column(
                modifier = leftOffsetModifier
            ) {
                if (!isDrag) {
                    ConvertorCalckTopBar(
                        convertorScreenModel = convertorScreenModel,
                        convertorButtonData = convertorButtonData,
                        convertorData = convertorData
                    )
                    ConvertorContent(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        convertorScreenModel = convertorScreenModel,
                        convertorButtonData = convertorButtonData,
                        convertorData = convertorData
                    )
                } else {
                    Box(
                        modifier = leftOffsetModifier
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(64.dp),
                            painter = painterResource(R.drawable.directory_sync_icon),
                            contentDescription = "convertorIcon"

                        )
                    }
                }
            }


            if (viewScreen?.butonPanel?.showKeyboard?.value == true) {
                Box(
                    modifier = rightOffsetModifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {

                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 6.dp)
                                .pointerInput(Unit) {
                                    val changeSizeRightK = 8f
                                    val changeSizeLeftK = 8f
                                    val changeSpeed = 2.5f
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            convertorScreenModel.componentOffset.value = localComponentOffset
                                            isDrag = false
                                        }
                                    ) { change, dragAmount ->
                                        isDrag = true

                                        localComponentOffset += dragAmount / changeSpeed

                                        if (localComponentOffset < -(screenSize.value / changeSizeLeftK)) {
                                            localComponentOffset = -(screenSize.value / changeSizeLeftK)
                                            return@detectHorizontalDragGestures
                                        }
                                        if (localComponentOffset > screenSize.value / changeSizeRightK) {
                                            localComponentOffset = screenSize.value / changeSizeRightK
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
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight(),
//                                    .padding(6.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize(),
                                ) {
                                    if (viewScreen.showCalckPanel.value) {
                                        ConvertorCalckBlockPanel(
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                            convertorScreenModel = convertorScreenModel,
                                            convertorData = convertorButtonData,
                                            onChangeSize = {}
                                        )
                                    }
                                }
                                ConvertorCalckButtonPanel(
                                    modifier = Modifier
                                        .padding(6.dp),
                                    buttonStack = buttonStack
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
                                    contentDescription = "calculatorIcon"

                                )
                            }
                        }


                    }
                }
            }
        }


    }

}






