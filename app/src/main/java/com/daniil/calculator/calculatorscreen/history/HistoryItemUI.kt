package com.daniil.calculator.calculatorscreen.history

import android.content.ClipData
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.calculatorscreen.CalculatorScreenModel
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.forEach


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

        closeAll.value?.let { expanded.value = !it }
        if (expanded.value) {
            val content = run {
                val list = timeSorted[key]?.reversed()
                val pair = list?.partition { it.pinned }
                (pair?.first ?: emptyList()) + (pair?.second ?: emptyList())
            }

            content.forEach { item ->
                HistoryItemUI(
                    modifier = modifier
                        .padding(horizontal = 6.dp),
                    historyData = item,
                    horizontalAlignment = horizontalAlignment,
                    calculatorScreenModel = calculatorScreenModel
                )
            }
        }


    }

}

@Composable
private fun HistoryItemUI(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
    calculatorScreenModel: CalculatorScreenModel,
    historyData: HistoryData,
) {
    val calckBlock by calculatorScreenModel.calckBlock.collectAsState()
    val predictive by calculatorScreenModel.predictiveCalckBlock.collectAsState()
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var addCommentAlertShow by remember { mutableStateOf(false) }

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
                            onClick =  {
                                addCommentAlertShow = true
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
                        UniversalDropDownItem(
                            title = if (historyData.pinned) stringResource(R.string.unpin) else stringResource(
                                R.string.pin
                            ),
                            iconResource = if (historyData.pinned) R.drawable.unpin_icon else R.drawable.pinned_icon,
                            onClick = {
                                calculatorScreenModel.calckHistory.pinnedItem(historyData)
                                calculatorScreenModel.saveClack(context)

                            }
                        ),
                        UniversalDropDownItem(
                            title = stringResource(R.string.comment),
                            iconResource = R.drawable.comment_icon,
                            onClick = {
                                addCommentAlertShow = true
                            }
                        ),
                        UniversalDropDownItem(
                            title = stringResource(R.string.delete),
                            iconResource = R.drawable.delete_icon,
                            onClick = {
                                calculatorScreenModel.calckHistory.removeHistory(historyData)
                                calculatorScreenModel.saveClack(context)

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
    AddCommentAlert(
        expanded = addCommentAlertShow,
        value = historyData.comment ?: "",
        onDismissRequest = {
            addCommentAlertShow = false
        },
        onConfirm = {
            addCommentAlertShow = false
            calculatorScreenModel.calckHistory.addComment(historyData, it.ifEmpty { null })
        }
    )
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