package com.daniil.calculator.calculatorscreen.history

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.calculatorscreen.CalculatorScreenModel
import com.daniil.calculator.utilites.CustomOverscrollEffect
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


@Composable
fun HistoryBlock(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.End,
    calculatorScreenModel: CalculatorScreenModel
) {
    val context = LocalContext.current
    val history = calculatorScreenModel.calckHistory.getHistory()
    val historyList = remember { mutableStateListOf<HistoryData>() }
    val closeAll = remember { mutableStateOf<Boolean?>(null) }

    historyList.clear()
    historyList.addAll(history)

    val timeSorted by remember {
        derivedStateOf {
            historyList.groupBy {
                getTimeDifference(it.time, context)
            }
        }
    }
    var expandedDeleteAlert by remember { mutableStateOf(false) }

    val calculateButtonClick by calculatorScreenModel.calculateButtonClick.collectAsState()

    LaunchedEffect(calculateButtonClick) {
        delay(100)
        calculatorScreenModel.historyScrollState.animateScrollToItem(
            historyList.size.coerceAtLeast(
                0
            )
        )
    }

    if (historyList.isNotEmpty()) {
        val scope = rememberCoroutineScope()
        val overscroll = remember(scope) { CustomOverscrollEffect(scope) }
        LazyColumn(
            state = calculatorScreenModel.historyScrollState,
            overscrollEffect = overscroll,
            reverseLayout = true,
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.Top
        ) {
            stickyHeader {
                ClearButton {
                    expandedDeleteAlert = true
                }
            }
            itemsIndexed(
                items = timeSorted.keys.toList(),
                key = { index, item -> item }
            ) { index, key ->
                HistoryTimeHeader(
                    modifier = Modifier,
                    index = index,
                    key = key,
                    calculatorScreenModel = calculatorScreenModel,
                    timeSorted = timeSorted,
                    horizontalAlignment = horizontalAlignment,
                    closeAll = closeAll,
                )
            }


        }
    } else {
        EmptyHistoryTitle()
    }

    DeleteAlertDialog(
        expanded = expandedDeleteAlert,
        onDismissRequest = {
            expandedDeleteAlert = false
        },
        onConfirmRequest = {
            calculatorScreenModel.calckHistory.clearHistory()
            expandedDeleteAlert = false
        }
    )
}

@Composable
private fun EmptyHistoryTitle(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxHeight(0.5f)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.empty_history),
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Icon(
                modifier = Modifier
                    .size(64.dp)
                    .padding(6.dp),
                imageVector = ImageVector.vectorResource(R.drawable.history_icon),
                contentDescription = "history icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ClearButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(R.string.clear_history))
        }

    }
}


@Composable
private fun DeleteAlertDialog(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,

    ) {
    if (!expanded) return
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmRequest()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        icon = {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
        },
        title = {
            Text(stringResource(R.string.clear_history))
        },
        text = {

        }
    )
}


fun getTimeDifference(
    targetDate: LocalDateTime,
    context: Context
): String {
    val now = LocalDateTime.now()
    val diff = Duration.between(now, targetDate).toMillis()

    var isAgo = true
    val absDiff = abs(diff)

    val formatter = DateTimeFormatter.ofPattern("dd-MM-yy")

    val seconds = absDiff / 1000 % 60
    val minutes = absDiff / (1000 * 60) % 60
    val hours = absDiff / (1000 * 60 * 60) % 24
    val days = absDiff / (1000 * 60 * 60 * 24)

    val parts = mutableListOf<String>()
    if (days > 3) {
        isAgo = false
        parts.add(targetDate.format(formatter))
    } else if (days > 0) parts.add("$days ${context.getString(R.string.day)}")
    else if (hours > 0) parts.add("$hours ${context.getString(R.string.hour)}")
    else if (minutes > 0) parts.add("$minutes ${context.getString(R.string.min)}")
    else {
        isAgo = false
        parts.add(context.getString(R.string.justnow))
    }


    val timeText = parts.joinToString(" ")

    return if (!isAgo) timeText else "$timeText ${context.getString(R.string.ago)}"
}
