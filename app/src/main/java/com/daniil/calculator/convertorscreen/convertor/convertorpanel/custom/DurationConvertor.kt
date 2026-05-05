package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.CardContainer
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.BigResult
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallInput
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallResult
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertorImplementation
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.universal.DateRangeCalendar
import com.daniil.calculator.universal.LocaleDataPickerDialog
import com.daniil.calculator.universal.TimePicker
import com.daniil.calculator.utilites.roundTo
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit


class DurationConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
) : CustomConvertorImplementation(convertorData, model) {
    override fun onCreate() {
        super.onCreate()
        setContent {
            mode(
                id = "Calendar",
                name = stringResource(R.string.calendar),
                painterId = R.drawable.calendar_icon
            ) {
                content = {
                    val configuration = LocalConfiguration.current
                    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                    CalendarScreen(
                        isPortrait = isPortrait,
                    )
                }

                showKeyboard.value = false

            }
            mode(
                id = "Time",
                name = stringResource(R.string.time),
                painterId = R.drawable.time_icon
            ) {
                content = {
                    val configuration = LocalConfiguration.current
                    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                    TimeScreen(
                        isPortrait = isPortrait,
                    )
                }

                showKeyboard.value = false

            }
        }
    }

}


@Composable
private fun DurationConvertorImplementation.CalendarScreen(
    modifier: Modifier = Modifier,
    isPortrait: Boolean,
) {

    val activeScreen by convertorScreenModel.currentConvertor.collectAsState()
    val coroutine = rememberCoroutineScope()
    val convertorCore = convertorScreenModel.convertorCore
    val parameters by convertorScreenModel.currentParameters.collectAsState()

    val startDate: String? = convertorScreenModel.getParameter(
        key = "startDate",
        defaultValue = null
    ) as? String?
    val endDate: String? = convertorScreenModel.getParameter(
        key = "endDate",
        defaultValue = null
    ) as? String?

    val startPosition: String = convertorScreenModel.getParameter(
        key = "calendarPosition",
        defaultValue = YearMonth.now().toString()
    ) as String

    val daysBetween = remember(startDate, endDate) {
        if (startDate != null && endDate != null)
            ChronoUnit.DAYS.between(LocalDate.parse(startDate), LocalDate.parse(endDate))
        else null
    }

    fun scrollTo(date: LocalDate) {
        convertorScreenModel.saveParameters {
            setStringData(
                "calendarPosition",
                YearMonth.of(date.year, date.month).toString()
            )
        }
    }

    val unitList by getUnitsAsSate().collectAsState()
    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = unitList,
        convertorData = activeScreen ?: return,
        containerColor = Color.Transparent,
    ) {
        if (isPortrait) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CardContainer(
                    contentPadding = PaddingValues(16.dp)
                ) {
                    DateRangeCalendar(
                        selectedStart = startDate?.let { LocalDate.parse(it) },
                        selectedEnd = endDate?.let { LocalDate.parse(it) },
                        calendarPosition = YearMonth.parse(startPosition),
                        onCalendarPositionCahged = {
                            convertorScreenModel.saveParameters {
                                setStringData("calendarPosition", it.toString())
                            }
                        },
                        onDateSelected = { date ->
                            convertorScreenModel.saveParameters {
                                if (startDate == null || endDate != null) {
                                    setStringData("startDate", date.toString())
                                    setStringData("endDate", null)
                                } else if (date.isAfter(LocalDate.parse(startDate))) {
                                    setStringData("endDate", date.toString())
                                } else {
                                    setStringData("startDate", date.toString())
                                }
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var startLocalDataPickerShow by remember { mutableStateOf(false) }
                    var endLocalDataPickerShow by remember { mutableStateOf(false) }

                    SmallInput(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.start),
                        content = startDate ?: "—",
                        copyPasteMenu = CopyPasteMenu.CopyOnly,
                        contentAlignment = Alignment.Center,
                        onClick = {
                            startLocalDataPickerShow = true
                        }
                    )
                    if (startLocalDataPickerShow) {
                        LocaleDataPickerDialog(
                            input = if (startDate == null) LocalDate.now() else LocalDate.parse(
                                startDate
                            ),
                            dateStart = LocalDate.of(1920, 1, 1),
                            dateLast = LocalDate.of( 2029, 12, 31),
                            onDismiss = {
                                startLocalDataPickerShow = false
                                scrollTo(it)
                            },
                            onConfirm = { date ->
                                convertorScreenModel.saveParameters {
                                    setStringData("startDate", date.toString())
                                }
                                startLocalDataPickerShow = false
                                scrollTo(date)
                            }
                        )
                    }
                    SmallInput(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.end),
                        content = endDate ?: "—",
                        copyPasteMenu = CopyPasteMenu.CopyOnly,
                        contentAlignment = Alignment.Center,
                        onClick = {
                            endLocalDataPickerShow = true
                        }
                    )
                    if (endLocalDataPickerShow) {
                        LocaleDataPickerDialog(
                            dateStart = LocalDate.of(1920, 1, 1),
                            dateLast = LocalDate.of( 2029, 12, 31),
                            input = if (endDate == null) LocalDate.now() else LocalDate.parse(
                                endDate
                            ),
                            onDismiss = {
                                endLocalDataPickerShow = false
                                scrollTo(it)
                            },
                            onConfirm = { date ->
                                convertorScreenModel.saveParameters {
                                    setStringData("endDate", date.toString())
                                }
                                endLocalDataPickerShow = false
                                scrollTo(date)
                            }
                        )
                    }
                }
                GroupedLayout() { groupedValue ->
                    getUnits().forEach { unit ->
                        val result = remember(daysBetween) {
                            try {
                                convertorScreenModel.convertorCore.convert(
                                    value = daysBetween.toString(),
                                    from = getStartUnits()!!,
                                    to = unit,
                                    convertorId = convertorData.id
                                )
                            } catch (e: Exception) {
                                "—"
                            }
                        }
                        SmallResult(
                            modifier = Modifier,
                            groupedValue = groupedValue,
                            title = unit.name,
                            content = if (result == "Error") "—" else result.roundTo(3),
                            copyPasteMenu = CopyPasteMenu.CopyOnly,
                            onClick = {}
                        )
                    }
                }
            }
        } else {
            val scrollState = rememberScrollState()
            val scrollState2 = rememberScrollState()
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                ) {
                    CardContainer(
                        modifier = Modifier,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        DateRangeCalendar(
                            selectedStart = startDate?.let { LocalDate.parse(it) },
                            selectedEnd = endDate?.let { LocalDate.parse(it) },
                            calendarPosition = YearMonth.parse(startPosition),
                            onCalendarPositionCahged = {
                                convertorScreenModel.saveParameters {
                                    setStringData("calendarPosition", it.toString())
                                }
                            },
                            onDateSelected = { date ->
                                convertorScreenModel.saveParameters {

                                    if (startDate == null || endDate != null) {
                                        setStringData("startDate", date.toString())
                                        setStringData("endDate", null)
                                    } else if (date.isAfter(LocalDate.parse(startDate))) {
                                        setStringData("endDate", date.toString())
                                    } else {
                                        setStringData("startDate", date.toString())
                                    }
                                }
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState2)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        var startLocalDataPickerShow by remember { mutableStateOf(false) }
                        var endLocalDataPickerShow by remember { mutableStateOf(false) }

                        SmallInput(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.start),
                            content = startDate ?: "—",
                            copyPasteMenu = CopyPasteMenu.CopyOnly,
                            contentAlignment = Alignment.Center,
                            onClick = {
                                startLocalDataPickerShow = true
                            }
                        )
                        if (startLocalDataPickerShow) {
                            LocaleDataPickerDialog(
                                input = if (startDate == null) LocalDate.now() else LocalDate.parse(startDate),
                                dateStart = LocalDate.of(1920, 1, 1),
                                dateLast = LocalDate.of( 2029, 12, 31),
                                onDismiss = {
                                    startLocalDataPickerShow = false
                                    scrollTo(it)
                                },
                                onConfirm = { date ->
                                    convertorScreenModel.saveParameters {
                                        setStringData("startDate", date.toString())
                                    }
                                    startLocalDataPickerShow = false
                                    scrollTo(date)
                                }
                            )
                        }
                        SmallInput(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.end),
                            content = endDate ?: "—",
                            copyPasteMenu = CopyPasteMenu.CopyOnly,
                            contentAlignment = Alignment.Center,
                            onClick = {
                                endLocalDataPickerShow = true
                            }
                        )
                        if (endLocalDataPickerShow) {

                            LocaleDataPickerDialog(
                                dateStart = LocalDate.of(1920, 1, 1),
                                dateLast = LocalDate.of( 2029, 12, 31),
                                input = if (endDate == null) LocalDate.now() else LocalDate.parse(
                                    endDate
                                ),
                                onDismiss = {
                                    endLocalDataPickerShow = false
                                    scrollTo(it)
                                },
                                onConfirm = { date ->
                                    convertorScreenModel.saveParameters {
                                        setStringData("endDate", date.toString())
                                    }
                                    endLocalDataPickerShow = false
                                    scrollTo(date)
                                }
                            )
                        }
                    }
                    GroupedLayout() { groupedValue ->
                        getUnits().forEach { unit ->

                            val result = remember(daysBetween) {
                                try {
                                    convertorScreenModel.convertorCore.convert(
                                        value = daysBetween.toString(),
                                        from = getStartUnits()!!,
                                        to = unit,
                                        convertorId = convertorData.id
                                    )
                                } catch (e: Exception) {
                                    "—"
                                }
                            }
                            SmallResult(
                                modifier = Modifier,
                                groupedValue = groupedValue,
                                title = unit.name,
                                content = if (result == "Error") "—" else result.roundTo(3),
                                copyPasteMenu = CopyPasteMenu.CopyOnly,
                                onClick = {}
                            )
                        }
                    }
                }

            }

        }

    }

}


@Composable
private fun DurationConvertorImplementation.TimeScreen(
    modifier: Modifier = Modifier,
    isPortrait: Boolean,
) {

    val activeScreen by convertorScreenModel.currentConvertor.collectAsState()
    val parameters by convertorScreenModel.currentParameters.collectAsState()
    val coroutine = rememberCoroutineScope()
    val clipboardManager = LocalClipboard.current


    val startTimeHour: Int = convertorScreenModel.getParameter(
        key = "startTimeHour",
        defaultValue = LocalTime.now().hour
    ).toString().toInt()
    val startTimeMinute: Int = convertorScreenModel.getParameter(
        key = "startTimeMinute",
        defaultValue = LocalTime.now().minute
    ).toString().toInt()
    val endTimeHour: Int = convertorScreenModel.getParameter(
        key = "endTimeHour",
        defaultValue = 12
    ).toString().toInt()
    val endTimeMinute: Int = convertorScreenModel.getParameter(
        key = "endTimeMinute",
        defaultValue = 0
    ).toString().toInt()


    val timeBetween = remember(
        startTimeMinute, startTimeHour, endTimeMinute, endTimeHour
    ) {
        val start = LocalTime.of(startTimeHour, startTimeMinute)
        val end = LocalTime.of(endTimeHour, endTimeMinute)

        val minutes = ChronoUnit.MINUTES.between(start, end)
        if (minutes < 0) minutes + 24 * 60 else minutes
    }


    val scrollState = rememberScrollState()
    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = emptyList(),
        convertorData = activeScreen ?: return,
        containerColor = Color.Transparent,
        scrollState = scrollState
    ) {
        if (isPortrait) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CardContainer(
                    contentPadding = PaddingValues(16.dp)
                ) {
                    TimePicker(
                        hour = startTimeHour,
                        minute = startTimeMinute,
                        onTimeChanged = { hour, minute ->
                            convertorScreenModel.saveParameters {
                                setStringData("startTimeHour", hour.toString())
                                setStringData("startTimeMinute", minute.toString())
                            }
                        }
                    )
                }
                CardContainer(
                    contentPadding = PaddingValues(16.dp)
                ) {
                    TimePicker(
                        hour = endTimeHour,
                        minute = endTimeMinute,
                        onTimeChanged = { hour, minute ->
                            convertorScreenModel.saveParameters {
                                setStringData("endTimeHour", hour.toString())
                                setStringData("endTimeMinute", minute.toString())
                            }
                        }
                    )
                }
                BigResult(
                    modifier = Modifier,
                    title = stringResource(R.string.result),
                    content = formatDuration(timeBetween),
                    copyPasteMenu = CopyPasteMenu.CopyOnly,
                    onClick = {}
                )
            }

        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CardContainer(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        TimePicker(
                            hour = startTimeHour,
                            minute = startTimeMinute,
                            onTimeChanged = { hour, minute ->
                                convertorScreenModel.saveParameters {
                                    setStringData("startTimeHour", hour.toString())
                                    setStringData("startTimeMinute", minute.toString())
                                }
                            }
                        )
                    }
                    CardContainer(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        TimePicker(
                            hour = endTimeHour,
                            minute = endTimeMinute,
                            onTimeChanged = { hour, minute ->
                                convertorScreenModel.saveParameters {
                                    setStringData("endTimeHour", hour.toString())
                                    setStringData("endTimeMinute", minute.toString())
                                }
                            }
                        )
                    }
                }

                BigResult(
                    modifier = Modifier,
                    title = stringResource(R.string.result),

                    content = formatDuration(timeBetween),
                    copyPasteMenu = CopyPasteMenu.CopyOnly,
                    onClick = {}
                )
            }
        }

    }
}

@Composable
private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return buildString {
        if (hours > 0) append("$hours ${stringResource(R.string.hour)}")
        if (hours > 0 && mins > 0) append(" ")
        if (mins > 0 || hours == 0L) append("$mins ${stringResource(R.string.min)}")
    }
}

