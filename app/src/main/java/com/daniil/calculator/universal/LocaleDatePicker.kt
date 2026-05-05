package com.daniil.calculator.universal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

@Composable
fun LocaleDataPickerDialog(
    input: LocalDate,
    onDismiss: (LocalDate) -> Unit,
    dateStart: LocalDate,
    dateLast: LocalDate = LocalDate.now(),
    onConfirm: (localeDate: LocalDate) -> Unit,
) {
    var localeDate by remember { mutableStateOf<LocalDate>(input) }


    AlertDialog(
        onDismissRequest = { onDismiss(localeDate) },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(localeDate) },
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(localeDate) }) { Text(stringResource(R.string.cancel)) }
        },
        title = {
            Text(stringResource(R.string.select_date))
        },
        text = {
            LocalDatePicker(
                dateStart = dateStart,
                dateLast = dateLast,
                localDate = localeDate,
                onDateSelected = { date ->
                    localeDate = date
                },
            )
        }
    )
}

@Composable
fun LocalDatePicker(
    dateStart: LocalDate,
    dateLast: LocalDate = LocalDate.now(),
    localDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {

    val currentDay = localDate.dayOfMonth
    val currentMonth = localDate.month // Month
    val currentYear = localDate.year

    val yearsRange = remember { (dateStart.year..dateLast.year).toList() }
    val months = remember(currentYear) {
        when (currentYear) {
            yearsRange.first() -> 1..dateStart.month.value
            yearsRange.last() -> 1..dateLast.month.value
            else -> 1..12
        }.toList()
    }

    val daysInMonth = remember(currentMonth, currentYear) {
        if (dateStart.month == currentMonth && dateStart.year == currentYear) dateStart.dayOfMonth
        else if (dateLast.month == currentMonth && dateLast.year == currentYear) dateLast.dayOfMonth
        else YearMonth.of(currentYear, currentMonth).lengthOfMonth()
    }
    val days = remember(daysInMonth) { (1..daysInMonth).toList() }
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // День

            ScrollPickerList(
                modifier = Modifier.weight(1f),
                items = days.map { it.toString().padStart(2, '0') },
                selectedIndex = (currentDay - 1).coerceIn(0, days.size - 1),
                onIndexChanged = { dayIndex ->
                    val newDay = days[dayIndex]
                    val maxDay = YearMonth.of(currentYear, currentMonth).lengthOfMonth()
                    val newDate =
                        LocalDate.of(currentYear, currentMonth, newDay.coerceAtMost(maxDay))
                    onDateSelected(newDate)
                }
            )


            ScrollPickerList(
                modifier = Modifier.weight(1f),
                items = months.map { it.toString().padStart(2, '0') },
                selectedIndex = currentMonth.ordinal,
                onIndexChanged = { monthIndex ->
                    val newMonth = months[monthIndex % months.size]
                    val maxDay = YearMonth.of(currentYear, newMonth).lengthOfMonth()
                    val newDate =
                        LocalDate.of(currentYear, newMonth, currentDay.coerceAtMost(maxDay))
                    onDateSelected(newDate)
                }
            )

            ScrollPickerList(
                modifier = Modifier.weight(1f),
                items = yearsRange.map { it.toString() },
                selectedIndex = yearsRange.indexOf(currentYear).coerceAtLeast(0),
                onIndexChanged = { yearIndex ->
                    val newYear = yearsRange[yearIndex]
                    val maxDay = YearMonth.of(newYear, currentMonth).lengthOfMonth()
                    val newDate =
                        LocalDate.of(newYear, currentMonth, currentDay.coerceAtMost(maxDay))
                    onDateSelected(newDate)
                }
            )
        }
    }
}
@Composable
private fun ScrollPickerList(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit,
) {
    val visibleCount = 5
    val midIndex = visibleCount / 2

    val phantom = ""
    val displayItems = remember(items) {
        List(midIndex) { phantom } + items + List(midIndex+1) { phantom }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex).coerceAtLeast(0)
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val center = listState.firstVisibleItemIndex + midIndex
            val realIndex = center - midIndex
            if (realIndex in items.indices) {
                onIndexChanged(realIndex)
            }
        }
    }

    LaunchedEffect(selectedIndex) {
        coroutineScope.launch {
            listState.animateScrollToItem(
                selectedIndex.coerceIn(0, items.lastIndex)
            )
        }
    }

    Box(
        modifier = modifier.height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(displayItems.size) { index ->
                val center = listState.firstVisibleItemIndex + midIndex
                val offset = index - center
                val alpha =
                    (1f - abs(offset) / midIndex.toFloat()).coerceIn(0.3f, 1f)

                val isSelected = offset == 0
                val text = displayItems[index]

                Text(
                    text = text,
                    modifier = Modifier.padding(vertical = 6.dp),
                    fontSize = if (isSelected) 20.sp else 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (text.isBlank())
                        Color.Transparent
                    else if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }
        }
    }
}