package com.daniil.calculator.calculatorscreen.history

import android.content.ClipData
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.daniil.calculator.R
import com.daniil.calculator.calculatorscreen.CalculatorScreenModel
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import com.daniil.csb.SettingsProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun HistoryTimeHeader(
    modifier: Modifier = Modifier,
    index: Int,
    key: String,
    closeAll: MutableState<Boolean?>,
    calculatorScreenModel: CalculatorScreenModel,
    timeSorted: Map<String, List<HistoryData>>,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
) {

    val context = LocalContext.current
    val expanded = rememberSaveable { mutableStateOf(!key.contains("-")) }

    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    val coroutine = rememberCoroutineScope()

    val arrowAnimation = animateFloatAsState(
        targetValue = if (expanded.value) 180f else 0f
    )
    val paddingAnimation = animateDpAsState(
        targetValue = if (expanded.value) 12.dp else 0.dp,
        animationSpec = tween(400)
    )
    var selectedMenuOpen = remember { mutableStateOf<Int?>(null) }

    fun onClick() {
        expanded.value = !expanded.value
        closeAll.value = null
        if (expanded.value) {
            timeSorted.keys.toList().let {
                coroutine.launch {
                    delay(100)
                    calculatorScreenModel.historyScrollState
                        .animateScrollToItem(
                            it.indexOf(key)
                                .coerceIn(0, timeSorted.keys.size)
                        )
                }
            }
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = paddingAnimation.value)
            .padding(top = if (index == timeSorted.keys.size - 1) 12.dp else 0.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pinnedCount = timeSorted[key]?.count { it.pinned } ?: 0
            if (pinnedCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = pinnedCount.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Icon(
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(40f),
                        painter = painterResource(R.drawable.pinned_icon),
                        contentDescription = "Pinned count"
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .combinedClickable(
                        onClick = {
                            onClick()
                        },
                        onLongClick = {
                            dropdownMenuExpanded = true
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier,
                        text = key,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        modifier = Modifier
                            .graphicsLayer(rotationZ = arrowAnimation.value),
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand/Collapse"
                    )

                    val dropDownButtonList = listOf(
                        UniversalDropDownItem(
                            title =
                                if (closeAll.value == true) stringResource(R.string.open_all)
                                else stringResource(R.string.close_all),
                            iconResource = R.drawable.view1_icon,
                            onClick = {
                                closeAll.value = closeAll.value?.let { !it } ?: true
                            },
                            autoClose = false
                        ),
                        UniversalDropDownItem(
                            title = stringResource(R.string.delete),
                            iconResource = R.drawable.delete_icon,
                            onClick = {
                                timeSorted[key]?.forEach {
                                    calculatorScreenModel.calckHistory.removeHistory(it)
                                }
                            }
                        )
                    )
                    UniversalDropDownMenu(
                        expanded = dropdownMenuExpanded,
                        buttonList = dropDownButtonList,
                        onDismissRequest = {
                            dropdownMenuExpanded = false
                        }
                    )
                }


            }
        }
        Spacer(modifier = Modifier.height(4.dp))


        LaunchedEffect(timeSorted.values.size) {
            if (SettingsProvider.getValue<Boolean>("first_add_history").value) {
                delay(100)
                selectedMenuOpen.value = 0
                delay(700)
                selectedMenuOpen.value = null
                SettingsProvider.setValue<Boolean>("first_add_history", false)
            }
        }
        LaunchedEffect(closeAll.value) {
            if (closeAll.value != null) {
                expanded.value = !closeAll.value!!
            }
        }

        if (expanded.value) {
            val content = run {
                val list = timeSorted[key]?.reversed()
                val pair = list?.partition { it.pinned }
                (pair?.first ?: emptyList()) + (pair?.second ?: emptyList())
            }

            content.forEachIndexed { index, item ->
                val addCommentAlertShow = remember { mutableStateOf(false) }

                SwipeableItemWithActions(
                    isRevealed = index == selectedMenuOpen.value,
                    onExpanded = { selectedMenuOpen.value= index },
                    onCollapsed = { selectedMenuOpen.value = null },
                    actions = {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ActionButton(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                icon = ImageVector.vectorResource(if (item.pinned) R.drawable.unpin_icon else R.drawable.pinned_icon)
                            ) {
                                calculatorScreenModel.calckHistory.pinnedItem(item)
                                calculatorScreenModel.saveClack(context)
                                selectedMenuOpen.value = null
                            }
                            ActionButton(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                icon = ImageVector.vectorResource(R.drawable.comment_icon)
                            ) {
                                addCommentAlertShow.value = true
                            }
                            ActionButton(
                                color = MaterialTheme.colorScheme.errorContainer,
                                icon = ImageVector.vectorResource(R.drawable.delete_icon)
                            ) {
                                calculatorScreenModel.calckHistory.removeHistory(item)
                                calculatorScreenModel.saveClack(context)
                                selectedMenuOpen.value = null
                            }
                        }

                    }
                ) {
                    HistoryItemUI(
                        modifier = modifier
                            .padding(horizontal = 6.dp),
                        selectedMenuOpen = selectedMenuOpen,
                        historyData = item,
                        index= index,
                        horizontalAlignment = horizontalAlignment,
                        calculatorScreenModel = calculatorScreenModel,
                        addCommentAlertShow = addCommentAlertShow
                    )
                }
                AddCommentAlert(
                    expanded = addCommentAlertShow.value,
                    value = item.comment.orEmpty(),
                    onDismissRequest = {
                        addCommentAlertShow.value = false
                    },
                    onConfirm = {
                        addCommentAlertShow.value = false
                        calculatorScreenModel.calckHistory.addComment(item, it.ifEmpty { null })
                    }
                )


            }
        }


    }

}

@Composable
private fun RowScope.ActionButton(
    modifier: Modifier = Modifier,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    var isClick by remember { mutableStateOf(false) }
    val animateClick by animateFloatAsState(
        if (isClick) 0.8f else 1f
    )
    LaunchedEffect(isClick) {
        delay(100)
        isClick = false
    }
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val effect = VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
    val vibrationEnabled by SettingsProvider.getValue<Boolean>("button_vibration_enable").collectAsState()

    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .weight(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .clickable {
                if (vibrationEnabled) vibrator.vibrate(effect)
                isClick = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.graphicsLayer {
                scaleX = animateClick
                scaleY = animateClick
            },
            imageVector = icon,
            contentDescription = "icon"
        )
    }
}

@Composable
private fun HistoryItemUI(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
    calculatorScreenModel: CalculatorScreenModel,
    historyData: HistoryData,
    index: Int,
    addCommentAlertShow: MutableState<Boolean>,
    selectedMenuOpen: MutableState<Int?>
    ) {
    val calckBlock by calculatorScreenModel.calckBlock.collectAsState()
    val predictive by calculatorScreenModel.predictiveCalckBlock.collectAsState()
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val clipboardManager = LocalClipboard.current

    var widthDp by remember { mutableStateOf(0.dp) }

    var copyData by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = horizontalAlignment
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (historyData.pinned) {
                IconButton(
                    onClick = {
                        calculatorScreenModel.calckHistory.pinnedItem(historyData)
                        calculatorScreenModel.saveClack(context)
                    }
                ) {
                    Icon(
                        modifier = Modifier.rotate(40f),
                        painter = painterResource(R.drawable.pinned_icon),
                        contentDescription = "Pinned"
                    )
                }
            }
            if (historyData.comment != null) {
                var commentDropdownMenuExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .combinedClickable(
                            onClick = {
                                addCommentAlertShow.value = true
                            },
                            onLongClick = {
                                commentDropdownMenuExpanded = true
                            }
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        modifier = Modifier.padding(6.dp),
                        text = historyData.comment,
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                val dropDownButtonList = listOf(
                    UniversalDropDownItem(
                        title = stringResource(R.string.copy),
                        iconResource = R.drawable.copy_standart,
                        onClick = {
                            coroutine.launch {
                                clipboardManager.setClipEntry(
                                    ClipEntry(
                                        ClipData.newPlainText(
                                            "history_copy",
                                            historyData.comment
                                        )
                                    )
                                )
                            }
                        }

                    ),
                    UniversalDropDownItem(
                        title = stringResource(R.string.delete),
                        iconResource = R.drawable.delete_icon,
                        onClick = {
                            calculatorScreenModel.calckHistory.addComment(historyData, null)
                            calculatorScreenModel.saveClack(context)
                        }
                    )
                )
                UniversalDropDownMenu(
                    expanded = commentDropdownMenuExpanded,
                    buttonList = dropDownButtonList,
                    onDismissRequest = {
                        commentDropdownMenuExpanded = false
                    }
                )

            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                modifier = modifier,
                horizontalAlignment = horizontalAlignment
            ) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .combinedClickable(
                            onClick = {
                                calculatorScreenModel.setCalck(historyData.content)
                            },
                            onLongClick = {
                                copyData = historyData.content
                                dropdownMenuExpanded = true
                            }
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        historyData.content,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (calckBlock == historyData.result || predictive == historyData.result)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }



                Column(
                    modifier = Modifier.clip(MaterialTheme.shapes.medium),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { layoutCoordinates ->
                                val widthPx = layoutCoordinates.size.width
                                val heightPx = layoutCoordinates.size.height
                                widthDp = with(density) { widthPx.toDp() }
                                val heightDp = with(density) { heightPx.toDp() }
                            }
                            .combinedClickable(
                                onClick = {
                                    calculatorScreenModel.setCalck(historyData.result)
                                },
                                onLongClick = {
                                    copyData = historyData.result
                                    dropdownMenuExpanded = true
                                }
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {

                        Text(
                            modifier = Modifier,
                            text = "= " + historyData.result,
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (calckBlock == historyData.result || predictive == historyData.result)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        )

                    }


                    val dropDownButtonList = listOf(
                        UniversalDropDownItem(
                            title = stringResource(R.string.copy),
                            iconResource = R.drawable.copy_standart,
                            onClick = {
                                coroutine.launch {
                                    clipboardManager.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                "history_copy",
                                                copyData
                                            )
                                        )
                                    )
                                }
                            }

                        ),
//                        UniversalDropDownItem(
//                            title = stringResource(R.string.archive),
//                            iconResource = R.drawable.archive_icon,
//                            onClick = {
//
//                            }
//                        )
                    ) + if (selectedMenuOpen.value != index) {
                        UniversalDropDownItem(
                            title = stringResource(R.string.open_menu),
                            iconResource = R.drawable.menu_open_icon,
                            onClick = { selectedMenuOpen.value = index }
                        )
                    } else UniversalDropDownItem.None
                    UniversalDropDownMenu(
                        expanded = dropdownMenuExpanded,
                        buttonList = dropDownButtonList,
                        onDismissRequest = {
                            dropdownMenuExpanded = false
                        }
                    )
                }


            }

        }

        if (historyData.comment != null || historyData.pinned) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)

            )
        } else {
            HorizontalDivider(
                modifier = Modifier
                    .width(widthDp)
                    .padding(vertical = 6.dp)

            )
        }
    }

}


@Composable
private fun AddCommentAlert(
    expanded: Boolean,
    value: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    if (expanded) {
        val maxChar = 30
        var value by remember { mutableStateOf(value) }

        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = {
                Text(stringResource(R.string.comment))
            },
            text = {
                OutlinedTextField(
                    label = { Text("${value.count()}/$maxChar") },
                    suffix = {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    value = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    },
                    value = value,
                    onValueChange = { value = it.take(maxChar) },
                    singleLine = true
                )
            },
            onDismissRequest = {
                onDismissRequest()
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismissRequest() }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onConfirm(value) }
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }


}