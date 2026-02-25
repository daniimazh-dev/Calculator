@file:OptIn(ExperimentalMaterial3Api::class)

package com.daniil.calculator.universal


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import kotlin.math.abs

@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (startDate: LocalDate?, endDate: LocalDate?) -> Unit,
) {
    var selectedStart by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEnd by remember { mutableStateOf<LocalDate?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedStart, selectedEnd) },
                enabled = selectedStart != null && selectedEnd != null
            ) { Text("Готово") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        },
        text = {
            DateRangeCalendar(
                modifier = Modifier,
                selectedStart = selectedStart,
                selectedEnd = selectedEnd,
                onDateSelected = { date ->
                    if (selectedStart == null || selectedEnd != null) {
                        selectedStart = date
                        selectedEnd = null
                    } else if (date.isAfter(selectedStart)) {
                        selectedEnd = date
                    } else {
                        selectedStart = date
                    }
                }
            )
        }
    )
}

@OptIn(
    ExperimentalAnimationApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun DateRangeCalendar(
    modifier: Modifier = Modifier,
    selectedStart: LocalDate?,
    selectedEnd: LocalDate?,
    calendarPosition: YearMonth = YearMonth.now(),
    onCalendarPositionCahged: (YearMonth) -> Unit = {},
    onDateSelected: (LocalDate) -> Unit,
) {
    val coroutine = rememberCoroutineScope()
    var isDateSelection by remember { mutableStateOf(false) }

    val initialPage = 15
    val pagerState = rememberPagerState(initialPage = initialPage) { initialPage*2+1 }


    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (pagerState.currentPage  != initialPage && !pagerState.isScrollInProgress) {
            val delta = pagerState.currentPage - initialPage // -1 for left swipe, +1 for right swipe
            pagerState.scrollToPage(initialPage)
            onCalendarPositionCahged( calendarPosition.plusMonths(delta.toLong()))
        }

    }

    Column(
        modifier = modifier
            .height(395.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = !isDateSelection,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                IconButton(onClick = {
                    coroutine.launch {
                        pagerState.animateScrollToPage(initialPage-1)
                    }
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Previous month")
                }
            }

            TextButton(
                modifier = Modifier.weight(1f),
                onClick = { isDateSelection = !isDateSelection }
            ) {
                Text(
                    text = calendarPosition.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    )
                        .replaceFirstChar { it.uppercase(Locale.getDefault()) } + " ${calendarPosition.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = !isDateSelection,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                IconButton(onClick = {
                    coroutine.launch {
                        pagerState.animateScrollToPage(initialPage+1)
                    }
                }) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Next month")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(!isDateSelection) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        AnimatedContent(targetState = isDateSelection) { selection ->
            if (selection) {

                MonthYearPicker(
                    selectedMonth = calendarPosition,
                    selectedYear = calendarPosition.year,
                    onSelected = { month, year ->
                        onCalendarPositionCahged(YearMonth.of(year, month))
                    },
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->

                    val targetMonth = remember(calendarPosition, page) {
                        calendarPosition.plusMonths((page - initialPage).toLong())
                    }

                    val firstDay = targetMonth.atDay(1)
                    val daysInMonth = targetMonth.lengthOfMonth()
                    val startOffset = (firstDay.dayOfWeek.value + 6) % 7 // monday=0 ... sunday=6
                    val daysList = (1..daysInMonth).map { targetMonth.atDay(it) }
                    val paddedDays = List(startOffset) { null } + daysList

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        items(paddedDays.size) { index ->
                            val day = paddedDays[index]
                            if (day == null) {
                                Box(Modifier.size(40.dp))
                            } else {
                                val isToday = day == LocalDate.now()
                                val isSelectedStart = day == selectedStart
                                val isSelectedEnd = day == selectedEnd
                                val isInRange = selectedStart != null && selectedEnd != null &&
                                        (day.isAfter(selectedStart) && day.isBefore(selectedEnd))

                                val bgColor = when {
                                    isSelectedStart || isSelectedEnd -> MaterialTheme.colorScheme.primary
                                    isInRange -> MaterialTheme.colorScheme.primaryContainer
                                    else -> Color.Transparent
                                }

                                val borderModifier = if (isToday) {
                                    Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    )
                                } else Modifier

                                Box(
                                    modifier = borderModifier
                                        .padding(2.dp)
                                        .size(40.dp)
                                        .background(bgColor, shape = MaterialTheme.shapes.small)
                                        .clickable { onDateSelected(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.dayOfMonth.toString(),
                                        color = if (isSelectedStart || isSelectedEnd) Color.White else Color.Unspecified
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            AnimatedVisibility(
                visible = calendarPosition != YearMonth.now(),
                enter = fadeIn(tween(400)),
                exit = fadeOut(tween(400))
            ) {
                OutlinedButton(onClick = {
                    onCalendarPositionCahged(YearMonth.now())
                    coroutine.launch {
                        pagerState.scrollToPage(initialPage-1)
                        pagerState.animateScrollToPage(initialPage)
                    }

                }) {
                    Text(LocalDate.now().dayOfMonth.toString())
                }
            }
        }
    }
}

@Composable
private fun MonthYearPicker(
    selectedMonth: YearMonth,
    selectedYear: Int,
    onSelected: (Month, Int) -> Unit,
) {

    val selectedMonth = selectedMonth.month

    val yearsRange = 1920..2130
    val years = remember { yearsRange.toList() }
    val months = Month.entries

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        ScrollPickerList(
            modifier = Modifier.weight(1f),
            infiniteCycle = true,
            onCycleComplete = {
                val year = years[(years.indexOf(selectedYear) + it).coerceIn(0 until years.size)]
                onSelected(selectedMonth, year)
            },
            items = months.map { it.getDisplayName(TextStyle.FULL, Locale.getDefault()) },
            selectedIndex = months.indexOf(selectedMonth),
            onIndexChanged = { index ->
                onSelected(months[index], selectedYear)
            }
        )
        ScrollPickerList(
            modifier = Modifier.weight(1f),
            items = years.map { it.toString() },
            selectedIndex = years.indexOf(selectedYear),
            onIndexChanged = { index ->
                onSelected(selectedMonth, years[index])
            }
        )
    }
}




@Composable
private fun ScrollPickerList(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit,
    infiniteCycle: Boolean = false,
    onCycleComplete: ((direction: Int) -> Unit)? = null,
) {
    val visibleCount = 9
    val midIndex = visibleCount / 2
    val itemCount = items.size

    val displayItems = remember(infiniteCycle, items) {
        if (infiniteCycle) List(1000) { i -> items[i % itemCount] } else items
    }

    val totalSize = displayItems.size

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (infiniteCycle)
            totalSize / 2 + selectedIndex
        else
            (selectedIndex - midIndex).coerceAtLeast(0)
    )

    var currentOffset by remember { mutableIntStateOf((listState.firstVisibleItemIndex + midIndex) / itemCount) }


    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex + midIndex
            val realIndex = (centerIndex % itemCount + itemCount) % itemCount
            onIndexChanged(realIndex)

        }
    }

    // === Перевірка переходу кола ===
    LaunchedEffect(listState.isScrollInProgress) {
        if (infiniteCycle && !listState.isScrollInProgress) {

            val currentCycle = (listState.firstVisibleItemIndex + midIndex) / itemCount
            val centerIndex = listState.firstVisibleItemIndex + midIndex
            val realIndex = (centerIndex % itemCount + itemCount) % itemCount
            val base = (centerIndex / itemCount) * itemCount
            val targetIndex = base + realIndex - midIndex


            listState.animateScrollToItem(targetIndex)
            if (currentCycle > currentOffset) {
                onCycleComplete?.invoke(currentCycle - currentOffset)
                currentOffset = currentCycle
                listState.animateScrollToItem(targetIndex)
            } else if (currentCycle < currentOffset) {
                onCycleComplete?.invoke(currentCycle - currentOffset)
                currentOffset = currentCycle
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    // === Анімаційний скрол при зміні зовні ===
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            val currentCycle = (listState.firstVisibleItemIndex + midIndex) / itemCount
            val target = if (infiniteCycle) {
                val base = (currentCycle * itemCount)
                base + selectedIndex - midIndex
            } else selectedIndex - midIndex

            if (target in 0 until totalSize) {
                coroutineScope.launch {
                    listState.animateScrollToItem(target)
                }

                val centerIndex = listState.firstVisibleItemIndex + midIndex
                val realIndex = (centerIndex % itemCount + itemCount) % itemCount
                val targetIndex = if (infiniteCycle) {
                    val base = (centerIndex / itemCount) * itemCount
                    base + realIndex - midIndex
                } else {
                    (realIndex - midIndex).coerceIn(0, totalSize - 1)
                }
                listState.animateScrollToItem(targetIndex)

                currentOffset = currentCycle
            } else {

            }
        }
    }

    // === Візуалізація ===
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            items(totalSize) { index ->
                val offset = (index - (listState.firstVisibleItemIndex + midIndex)).toFloat()
                val alpha = (1f - (abs(offset) / midIndex)).coerceIn(0.3f, 1f)
                val size = 18
                val isSelected = offset == 0f
                if (index % itemCount == 0 && infiniteCycle) {
                    HorizontalDivider()
                }
                Text(
                    modifier = Modifier
                        .padding(vertical = 6.dp),
                    text = displayItems[index].replaceFirstChar { it.uppercase() },
                    fontSize = size.sp,
                    style = if (isSelected)
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    else
                        MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                )
            }
        }
    }
}

//@Composable
//fun DatePickerAlert(
//    expanded: Boolean,
//    startDate: LocalDate = LocalDate.now(),
//    onDismissRequest: () -> Unit,
//    onSelected: (LocalDate) -> Unit
//) {
//    val localDate = startDate
//
//    if (expanded) {
//        AlertDialog(
//            title = {
//                Text(
//                    text = "Choose date",
//                )
//            },
//            text = {
//
//            },
//            onDismissRequest = {
//                onDismissRequest()
//            },
//            dismissButton = {
//                TextButton(onClick = {
//                    onDismissRequest()
//                }) {
//                    Text("Cancel")
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = {
//                    onSelected()
//                    onDismissRequest()
//                }) {
//                    Text("OK")
//                }
//            },
//        )
//    }
//
//
//}