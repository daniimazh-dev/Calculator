package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.StandardConvertor
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.ConvertorDescription
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnitJson
import com.daniil.calculator.convertorscreen.convertor.unit.UnitAlert
import com.daniil.calculator.convertorscreen.convertor.unit.UnitButton
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.core.DaniilServerAPI
import com.daniil.calculator.core.UserDataManager
import com.daniil.calculator.core.UserToken
import com.daniil.calculator.universal.LocalDatePicker
import com.daniil.calculator.universal.LocaleDataPickerDialog
import com.daniil.calculator.universal.UniversalDropDownItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.internal.format
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter


private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

class CurrencyConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
    context: Context,
) : StandardConvertor(convertorData, model, context) {
    var descriptionUI = MutableStateFlow<ConvertorDescription?>(null)
    var isLoading = MutableStateFlow(false)
    var serverError = MutableStateFlow(false)
    val currencyApi = DaniilServerAPI()
    val coroutine = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        setContent {
            val customDropDown = defaultDropdownList() + listOf(
                UniversalDropDownItem(
                    title = stringResource(R.string.update_data),
                    iconResource = R.drawable.directory_sync_icon,
                    onClick = {

                        coroutine.launch(Dispatchers.IO) {
                            serverError.value = true
                            loadCurrency()
                        }
                    }
                )
            )
            mode(
                id = "Comparison",
                name = stringResource(R.string.expanded),
                painterId = R.drawable.view3_icon
            ) {
                content = {
                    if (!isLoading.collectAsState().value && !serverError.collectAsState().value) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            descriptionUI.collectAsState().value?.content?.invoke()
                            expandedContent()
                        }
                    } else {
                        loading()
                    }
                }
                showClackPanel.value = false
//                showKeyboard.value = !isLoading.value
                dropdownMenu = customDropDown.toMutableStateList()
            }
//            mode(
//                id = "List",
//                name = stringResource(R.string.diminished),
//                painterId = R.drawable.view1_icon
//            ) {
//                content = {
//
//                    if (!isLoading.value) {
//                        Column(
//                            verticalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            if (descriptionUI.collectAsState().value?.content != null) {
//                                MassageBox(
//                                    content = {
//                                        descriptionUI.collectAsState().value?.content?.invoke()
//                                    },
//                                    closeable = false
//                                )
//                            }
//
//                            diminishedContent()
//                        }
//                    } else {
//                        loading()
//                    }
//                }
//                dropdownMenu = customDropDown.toMutableStateList()
////                showKeyboard.value = !isLoading.value
//            }
        }
    }

    override suspend fun onStart() {
        Log.i("MyLog", "OnStart")
        super.onStart()
        coroutine.launch(Dispatchers.IO) {
            delay(200)
            loadCurrency()
        }

    }

    @Composable
    private fun loading() {

        var againEnabled by remember { mutableStateOf(true) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.height(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                if (serverError.collectAsState().value && againEnabled) {
                    Icon(
                        modifier = Modifier.size(62.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.wifi_lost_icon),
                        contentDescription = "Internet lost"
                    )
                    Text(stringResource(R.string.no_conection_server))
                    Button(
                        enabled = againEnabled,
                        onClick = {
                            coroutine.launch(Dispatchers.IO) {
                                againEnabled = false
                                loadCurrency()
                                delay(500)
                                againEnabled = true
                            }
                        }) {
                        Text(stringResource(R.string.try_again))
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(62.dp))

                }

            }

        }

    }

    private suspend fun loadCurrency(time: LocalDate? = null): Unit = withContext(Dispatchers.IO) {
        Log.i("MyLog", "load started with time: $time")
        val token = UserDataManager.token.value ?: return@withContext

        isLoading.value = true
        serverError.value = false // Скидаємо помилку при новому завантаженні

        val parameters = convertorScreenModel.currentParameters.value

//        val available: List<LocalDate> = try {
//            val response = currencyApi.getAvailableExchange(UserToken(token)).body()
//            response?.available?.map { LocalDate.parse(it, inputFormatter) }?.sortedDescending()
//                ?: listOf(today)
//        } catch (e: Exception) {
//            Log.e("MyLog", "available failed", e)
//            val set = ExchangeChach.chach.keys.toMutableSet()
//            if (time != null) set.add(time) else set.add(today)
//            set.sorted().reversed().toList()
//        }

        val selectTime: LocalDate = when {
            time != null -> time
            else -> {
                // Спробуємо дістати збережену дату з параметрів
                val savedDateStr =
                    convertorScreenModel.getParameter("selectTime", parameters, null)?.toString()
                val parsedDate =
                    runCatching { LocalDate.parse(savedDateStr, inputFormatter) }.getOrNull()

                // Якщо збереженої дати немає або вона застаріла — беремо найсвіжішу доступну
                parsedDate ?: LocalDate.now()
            }
        }
        // 1. Отримуємо список доступних дат

        // 2. Визначаємо цільову дату (selectTime)


        val selectTimeString = selectTime.format(inputFormatter)

        // 3. Зберігаємо вибрану дату, якщо вона змінилася
        convertorScreenModel.saveParameters {
            setStringData("selectTime", selectTimeString)
            setStringData("lastExchangeDate", LocalDate.now().format(inputFormatter))
        }

        // 4. Отримання курсів (Кеш -> Мережа)
        val rates = try {
            Log.i("MyLog", "Fetching rates for $selectTimeString")
            ExchangeChach.chach[selectTime] ?: currencyApi.getExchangeByDate(
                UserToken(token),
                selectTimeString
            ).body()?.also {
                ExchangeChach.chach[selectTime] = it
            }
        } catch (e: Exception) {
            Log.e("MyLog", "Rates fetch failed", e)
            null
        }

        // 5. Оновлення UI
        withContext(Dispatchers.Main) {
            if (rates == null) {
                Log.e("MyLog", "Rates is null")
                isLoading.value = false
                serverError.value = true
            } else {
                Log.i("MyLog", "Ok")

                // Оновлюємо одиниці
                rates.exchange.forEach {
                    addToUnits(
                        ConvertorUnitJson(
                            name = it.name,
                            id = it.name,
                            symbol = it.symbol,
                            multiplier = it.rate,
                            saveData = true
                        )
                    )
                }

                // Налаштовуємо контент вибору дати
                descriptionUI.value = ConvertorDescription {
                    MassageBox(

                        stratTime = selectTime,
                        onSelected = { newDate ->
                            coroutine.launch(Dispatchers.IO) {
                                loadCurrency(newDate)
                            }
                        }
                    )
                }
                isLoading.value = false
                serverError.value = false
                return@withContext
            }
        }

    }
}

private object ExchangeChach {
    val chach = mutableMapOf<LocalDate, ExchangeResponse>()
}

@Composable
private fun MassageBox(
    modifier: Modifier = Modifier,
    stratTime: LocalDate,
    onSelected: (LocalDate) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .padding(6.dp)
                .padding(start = 10.dp)
                .weight(1f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                var datePickerOpen by remember { mutableStateOf(false) }
                Text(text = stringResource(R.string.excange_date))
                UnitButton(
                    unit = ConvertorUnit(
                        id = "",
                        name = "",
                        symbol = stratTime.format(outputFormatter)
                    ),
                    onClick = { datePickerOpen = true }
                )
                if (datePickerOpen) {
                    LocaleDataPickerDialog(
                        input = stratTime,
                        onDismiss = { datePickerOpen = false },
                        dateStart = LocalDate.of(2000, 1, 1),
                        dateLast = LocalDate.now(),
                        onConfirm = {
                            if (it <= LocalDate.now()) {
                                onSelected(it)
                                datePickerOpen = false
                            }

                        }
                    )
                }

//                UnitAlert(
//                    title = stringResource(R.string.select_date),
//                    expanded = datePickerOpen,
//                    onDismissRequest = {
//
//                        datePickerOpen = false
//                    },
//                    onSelected = {
//                        onSelected(LocalDate.parse(it.name, outputFormatter))
//                        datePickerOpen = false
//                    },
//                    unitList = (available ?: listOf(oldSelected)).mapIndexed { index, date ->
//                        val text = date.format(outputFormatter)
//                        ConvertorUnit(
//                            id = text,
//                            name = text,
//                            symbol =
//                                if (LocalDate.now() == date)
//                                    stringResource(R.string.actual).lowercase() else ""
//                        )
//                    },
//                    oldSelected = ConvertorUnit(
//                        id = oldSelected.format(outputFormatter),
//                        name = oldSelected.format(outputFormatter),
//                        symbol = ""
//                    )
//                )
            }
        }
    }

}


@Serializable
data class AvailableExchange(
    val available: List<String>
)

@Serializable
data class ExchangeData(
    val name: String,
    val rate: Double,
    val symbol: String,
)

@Serializable
data class ExchangeResponse(
    val time: String,
    val exchange: List<ExchangeData>,
)