package com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayoutScope
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedValue
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.BigInputWithUnit
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallResult
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.ConvertorConstructorScope
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.validateValue
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.utilites.CustomOverscrollEffect
import com.daniil.calculator.utilites.roundTo
import kotlinx.coroutines.launch


open class StandardConvertor(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
    val context: Context,
) : CustomConvertorImplementation(
    convertorData,
    model
) {

    override fun onCreate() {
        setContent {
            startViewMode = "Comparison"
            mode(
                id = "Comparison",
                name = stringResource(R.string.expanded),
                painterId = R.drawable.view3_icon
            ) {
                showClackPanel.value = false
                content = {
                    expandedContent()
                }
            }
//            mode(
//                id = "List",
//                name = stringResource(R.string.diminished),
//                painterId = R.drawable.view1_icon
//            ) {
//                content = {
//                    diminishedContent()
//
//                }
//            }
        }
    }

    @Composable
    fun defaultDropdownList(): List<UniversalDropDownItem> = listOf(
        UniversalDropDownItem(
            title = stringResource(R.string.report),
            iconResource = R.drawable.report_icon,
            onClick = {
                convertorScreenModel.reportErrorSheetShow.value = true
            }
        ),
        UniversalDropDownItem(
            title = stringResource(R.string.clear_param),
            iconResource = R.drawable.delete_icon,
            onClick = {
                convertorScreenModel.clearParam(convertorData.id, null)
                convertorScreenModel.setCalck("0")
                convertorScreenModel.convertorCore.clearSavedUnit(convertorData.id)
                convertorScreenModel.goToHome()
            }
        ),
    )

    @Composable
    fun ConvertorConstructorScope.expandedContent() {
        val activeScreen by convertorScreenModel.currentConvertor.collectAsState()
        val scrollState = rememberScrollState()
        val convertorCore = convertorScreenModel.convertorCore
        val calckBlock by convertorScreenModel.calckBlock.collectAsState()

        val globalUnit by convertorScreenModel.currentUnit.collectAsState()


        val firstUnit: ConvertorUnit = convertorScreenModel.getParameter(
            key = "firstUnit",
            defaultValue = getUnits().getOrElse(0) { NullableUnit }
        ) as ConvertorUnit

        val secondUnit: ConvertorUnit = convertorScreenModel.getParameter(
            key = "secondUnit",
            defaultValue = getUnits().getOrElse(1) { NullableUnit }
        ) as ConvertorUnit

        val currentUnit: ConvertorUnit = convertorScreenModel.getParameter(
            key = "currentUnit",
            defaultValue = getUnits().getOrElse(0) { NullableUnit }
        ) as ConvertorUnit


        val result = try {
            convertorCore.convert(
                value = calckBlock,
                from = currentUnit,
                to = if (currentUnit == firstUnit) secondUnit else firstUnit,
                convertorId = convertorData.id
            )
        } catch (e: Exception) {
            "—"
        }

        var swapPosition by remember { mutableStateOf(false) }
        var swapClicked by remember { mutableStateOf(false) }
//        var swapClicked by remember { mutableStateOf(false) }
        val animatedSwapIcon by animateFloatAsState(
            if (swapPosition) 0f else 180f
        )

        var firstSize by remember { mutableStateOf(IntSize.Zero) }
        var secondSize by remember { mutableStateOf(IntSize.Zero) }

        val animatedSwapTranslationFirst = remember { Animatable(0f) }
        val animatedSwapTranslationSecond = remember { Animatable(0f) }

        LaunchedEffect(swapClicked) {
            val swapOffsetPxFirst = firstSize.height.toFloat()
            val swapOffsetPxSecond = secondSize.height.toFloat()
            val additionalPaddingOffset = 10f
            val animationSpeed = 300

            if (swapClicked) {
                launch {

                    animatedSwapTranslationFirst.animateTo(
                        targetValue = swapOffsetPxSecond + additionalPaddingOffset,
                        animationSpec = tween(animationSpeed)
                    )
                }

                animatedSwapTranslationSecond.animateTo(
                    targetValue = -swapOffsetPxFirst - additionalPaddingOffset,
                    animationSpec = tween(animationSpeed)
                )
                convertorScreenModel.saveParameters {
                    setObject("secondUnit", firstUnit)
                    setObject("firstUnit", secondUnit)
                    convertorScreenModel.setCurrentUnit(currentUnit)
                }
                animatedSwapTranslationFirst.snapTo(0f)
                animatedSwapTranslationSecond.snapTo(0f)
                swapClicked = false
            }
        }
        val unitList by getUnitsAsSate().collectAsState()
        ConvertorLayout(
            convertorScreenModel = convertorScreenModel,
            unitList = unitList,
            convertorData = activeScreen ?: return,
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {


                GroupedLayout(
                    modifier = Modifier.fillMaxWidth()
                ) { groupedValue ->

                    BigInputWithUnit(
                        modifier = Modifier
                            .graphicsLayer(
                                translationY = animatedSwapTranslationFirst.value,
                            )
                            .onGloballyPositioned {
                                firstSize = it.size
                            },
                        groupedValue = groupedValue,
                        title = firstUnit.name,
                        content = if (currentUnit == firstUnit) calckBlock else result.roundTo(3),
                        currentUnit = firstUnit,
                        selected = currentUnit == firstUnit,
                        copyPasteMenu = CopyPasteMenu.Full,
                        onPaste = { str ->
                            if (str == null) return@BigInputWithUnit
                            val validate = convertorScreenModel.validateValue(str)
                            if (!validate.first) {
                                Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                                return@BigInputWithUnit
                            }
                            convertorScreenModel.setCalck(str)
                            convertorScreenModel.saveParameters {
                                setObject("currentUnit", firstUnit)
                                convertorScreenModel.setCurrentUnit(firstUnit)
                            }

                        },
                        onChangeUnit = {
                            convertorScreenModel.saveParameters {
                                if (currentUnit == firstUnit) {
                                    setObject("currentUnit", it)
                                }
                                if (it == secondUnit) {
                                    swapClicked = true
                                } else {
                                    setObject("firstUnit", it)
                                }

                                convertorScreenModel.setCurrentUnit(it)
                            }
                        },
                        onClick = {
                            if (currentUnit != firstUnit) {
                                convertorScreenModel.setCalck(result)
                                convertorScreenModel.saveParameters {
                                    setObject("currentUnit", firstUnit)
                                    convertorScreenModel.setCurrentUnit(firstUnit, true)
                                }
                            }
                        }
                    )



                    BigInputWithUnit(
                        modifier = Modifier
                            .graphicsLayer(
                                translationY = animatedSwapTranslationSecond.value,
                            )
                            .onGloballyPositioned {
                                secondSize = it.size
                            },
                        groupedValue = groupedValue,
                        title = secondUnit.name,
                        content = if (currentUnit == secondUnit) calckBlock else result.roundTo(
                            3
                        ),
                        selected = currentUnit == secondUnit,
                        copyPasteMenu = CopyPasteMenu.Full,
                        currentUnit = secondUnit,
                        onPaste = { str ->
                            if (str == null) return@BigInputWithUnit
                            val validate = convertorScreenModel.validateValue(str)
                            if (!validate.first) {
                                Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                                return@BigInputWithUnit
                            }

                            convertorScreenModel.setCalck(str)
                            convertorScreenModel.saveParameters {
                                setObject("currentUnit", secondUnit)
                                convertorScreenModel.setCurrentUnit(secondUnit)
                            }

                        },
                        onChangeUnit = {
                            convertorScreenModel.saveParameters {
                                if (currentUnit == secondUnit) {
                                    setObject("currentUnit", it)
                                }

                                if (it == firstUnit) {
                                    swapClicked = true
                                } else {
                                    setObject("secondUnit", it)
                                }

                                convertorScreenModel.setCurrentUnit(it)
                            }

                        },
                        onClick = {
                            if (currentUnit != secondUnit) {
                                convertorScreenModel.setCalck(result)
                                convertorScreenModel.saveParameters {
                                    setObject("currentUnit", secondUnit)
                                    convertorScreenModel.setCurrentUnit(secondUnit, true)
                                }
                            }
                        }
                    )
                }


                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .offset(x = -(32.dp))
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            if (!swapClicked) {
                                swapClicked = true
                                swapPosition = !swapPosition
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(
                                rotationX = animatedSwapIcon
                            ),
                        imageVector = ImageVector.vectorResource(R.drawable.swap_horizontal_icon),
                        contentDescription = "Swap",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

            }
        }
    }

    @Composable
    fun ConvertorConstructorScope.diminishedContent() {
        val lazyListState = rememberLazyListState()
        val activeScreen by convertorScreenModel.currentConvertor.collectAsState()
        val convertorCore = convertorScreenModel.convertorCore
        val calckBlock by convertorScreenModel.calckBlock.collectAsState()
        val parameters by convertorScreenModel.currentParameters.collectAsState()
        val globalUnit by convertorScreenModel.currentUnit.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        val currentUnit: ConvertorUnit = convertorScreenModel.getParameter(
            key = "currentUnit",
            defaultValue = getUnits().getOrElse(0) { NullableUnit }
        ) as ConvertorUnit

        val firstUnit: ConvertorUnit = convertorScreenModel.getParameter(
            key = "firstUnit",
            defaultValue = getUnits().getOrElse(0) { NullableUnit }
        ) as ConvertorUnit

        val secondUnit: ConvertorUnit = convertorScreenModel.getParameter(
            key = "secondUnit",
            defaultValue = getUnits().getOrElse(1) { NullableUnit }
        ) as ConvertorUnit

        val unitWithoutSelected by remember(currentUnit) {
            derivedStateOf {
                val list = getUnits().toMutableList()
                list.remove(currentUnit)
                list
            }
        }


        context(scope: ConvertorLayoutScope)
        @Composable
        fun contentItem(
            modifier: Modifier = Modifier,
            unit: ConvertorUnit,
            groupedValue: GroupedValue?,
        ) = with(scope) {
            val result = try {
                convertorCore.convert(
                    value = calckBlock,
                    from = currentUnit,
                    to = unit,
                    convertorId = convertorData.id
                )
            } catch (e: Exception) {
                "—"
            }

            fun change() {
                convertorScreenModel.saveParameters {
                    when (currentUnit) {
                        firstUnit -> {
                            if (secondUnit == unit) setObject(
                                "secondUnit",
                                firstUnit
                            )
                            setObject("firstUnit", unit)
                        }

                        secondUnit -> {
                            if (firstUnit == unit) setObject(
                                "firstUnit",
                                secondUnit
                            )
                            setObject("secondUnit", unit)
                        }
                    }
                    convertorScreenModel.setCurrentUnit(unit)
                    setObject("currentUnit", unit)
                }
            }

            val isSelected = currentUnit == unit
            var show by remember { mutableStateOf(true) }

            SmallResult(
                modifier = modifier,
                groupedValue = groupedValue,
                title = unit.name,
                content = result,
                suffix = unit.symbol,
                copyPasteMenu = CopyPasteMenu.Full,
                onPaste = { str ->
                    if (str == null) return@SmallResult
                    val validate = convertorScreenModel.validateValue(str)
                    if (!validate.first) {
                        Toast.makeText(context, validate.second, Toast.LENGTH_SHORT)
                            .show()
                        return@SmallResult
                    }
                    convertorScreenModel.setCalck(str)
                    change()
                },
                onClick = {
                    change()
                }
            )
        }

        var oldCurrentUnit by remember { mutableStateOf(currentUnit) }
        val unitList by getUnitsAsSate().collectAsState()
        ConvertorLayout(
            convertorScreenModel = convertorScreenModel,
            unitList = getUnits(),
            convertorData = activeScreen ?: return,
            containerColor = Color.Transparent,
            horizontalAlignment = Alignment.End
        ) { innerPadding ->
            val changeAnimationSize = remember { Animatable(1f) }
            val changeAnimationY = remember { Animatable(0f) }
            var elementSize by remember { mutableStateOf(IntSize.Zero) }


            var animate by remember { mutableStateOf(false) }

            LaunchedEffect(currentUnit) {
                if (oldCurrentUnit.id == currentUnit.id) return@LaunchedEffect
                animate = true
                changeAnimationSize.animateTo(0.9f)
                changeAnimationY.animateTo(elementSize.width.toFloat(), tween(300))
                changeAnimationSize.animateTo(1f)
                oldCurrentUnit = currentUnit
                changeAnimationY.snapTo(0f)
                animate = false

            }
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.shapes.large
                    )
            ) {
                if (animate) {
                    SmallResult(
                        modifier = Modifier.graphicsLayer(
                            translationX = changeAnimationY.value - elementSize.width,
                            scaleX = changeAnimationSize.value,
                            scaleY = changeAnimationSize.value
                        ),
                        title = currentUnit.name,
                        content = calckBlock,
                        suffix = currentUnit.symbol,
                        selected = true,
                        onClick = {}
                    )
                }
                SmallResult(
                    modifier = Modifier
                        .onGloballyPositioned {
                            elementSize = it.size
                        }
                        .graphicsLayer(
                            translationX = changeAnimationY.value,
                            scaleX = changeAnimationSize.value,
                            scaleY = changeAnimationSize.value
                        ),
                    title = oldCurrentUnit.name,
                    content = calckBlock,
                    suffix = oldCurrentUnit.symbol,
                    copyPasteMenu = CopyPasteMenu.Full,
                    selected = true,
                    onPaste = { str ->
                        if (str == null) return@SmallResult
                        val validate = convertorScreenModel.validateValue(str)
                        if (!validate.first) {
                            Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                            return@SmallResult
                        }
                        convertorScreenModel.setCalck(str)
                    },
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            GroupedLayout() { groupedValue ->
                val scope = rememberCoroutineScope()
                val overscroll = remember(scope) { CustomOverscrollEffect(scope) }
                LazyColumn(
                    state = lazyListState,
                    overscrollEffect = overscroll,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {

                    items(
                        items = unitWithoutSelected,
                        key = { it.id }
                    ) { unit ->
                        contentItem(Modifier.animateItem(), unit, groupedValue)
                    }


                }


            }


        }


    }
}
