package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.daniil.calculator.universal.UniversalDropDownItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

class CurrencyConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
    context: Context,
) : StandardConvertor(convertorData, model, context) {
    var descriptionUI = mutableStateOf<ConvertorDescription?>(null)
    var isLoading = mutableStateOf(true)
    var serverError = mutableStateOf(false)
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
                        isLoading.value = true
                        coroutine.launch(Dispatchers.IO) {
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
                    if (!isLoading.value) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (descriptionUI.value?.content != null) {
                                MassageBox(
                                    content = {
                                        descriptionUI.value?.content?.invoke()
                                    },
                                    closeable = false
                                )
                            }

                            expandedContent()
                        }
                    } else {
                        loading()
                    }
                }
                showClackPanel.value = false
                showKeyboard.value = !isLoading.value
                dropdownMenu = customDropDown.toMutableStateList()
            }
            mode(
                id = "List",
                name = stringResource(R.string.diminished),
                painterId = R.drawable.view1_icon
            ) {
                content = {

                    if (!isLoading.value) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (descriptionUI.value?.content != null) {
                                MassageBox(
                                    content = {
                                        descriptionUI.value?.content?.invoke()
                                    },
                                    closeable = false
                                )
                            }

                            diminishedContent()
                        }
                    } else {
                        loading()
                    }
                }
                dropdownMenu = customDropDown.toMutableStateList()
                showKeyboard.value = !isLoading.value
            }
        }
    }

    override suspend fun onStart() {
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
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.height(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                if (serverError.value && againEnabled) {
                    Icon(
                        modifier = Modifier.size(62.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.wifi_lost_icon),
                        contentDescription = "Internet lost"
                    )
                    Text(stringResource(R.string.no_conection_server))
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(62.dp))

                }
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
            }

        }

    }

    private suspend fun loadCurrency(time: LocalDate? = null) {
        val token = UserDataManager.token ?: return
        isLoading.value = true
        val today = LocalDate.now()
        val parameters = convertorScreenModel.currentParameters.value

        val available: List<LocalDate> = try {
            val stringList = currencyApi.getAvailableExchange(UserToken(token)).body()?.available
            stringList?.map {
                LocalDate.parse(it, inputFormatter)
            } ?: listOf(today)
        } catch (_: Exception) {
            listOf(today)
        }.reversed()

        val lastExchangeDate = try {
                val last = convertorScreenModel.getParameter("lastExchangeDate", parameters, null)?.toString()
                LocalDate.parse(last, inputFormatter)
            } catch (_: NullPointerException) {
                null
            }

        var selectTimeString: String? = null
        var selectTime: LocalDate? = null
        if (time == null) {
            val lastUpdate = available.first()
            if (lastExchangeDate != lastUpdate)  {
                convertorScreenModel.saveParameters {
                    setStringData("lastExchangeDate", lastUpdate.format(inputFormatter))
                    setStringData("selectTime", lastUpdate.format(inputFormatter))
                }
            }

            selectTimeString =
                try {
                    convertorScreenModel.getParameter("selectTime", parameters, null)?.toString()
                } catch (_: NullPointerException) {
                    null
                }
            selectTime = selectTimeString?.let {
                try {
                    LocalDate.parse(it, inputFormatter)
                } catch (_: DateTimeParseException) {
                    convertorScreenModel.clearParam("selectTime")
                    null
                }
            } ?: today
            if (available.any { it != selectTime }) {
                convertorScreenModel.saveParameters {
                    setStringData("lastExchangeDate", lastUpdate.format(inputFormatter))
                    setStringData("selectTime", lastUpdate.format(inputFormatter))
                }
                loadCurrency(lastUpdate)
                return
            }
        } else {
            selectTime = time
            selectTimeString = time.format(inputFormatter)
        }


        val rates = try {
            if (selectTimeString != null) {
                ExchangeChach.chach.getOrPut(selectTime) {
                    currencyApi.getExchangeByDate(UserToken(token), selectTimeString).body() ?:
                    error("No exchange rates")
                }
            } else {
                currencyApi.getCurrentExchange(UserToken(token)).body()
            }
        } catch (_: Exception) {
            null
        }


        if (rates == null) {
            serverError.value = true
        } else  {
            isLoading.value = true

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

            isLoading.value = false
        }
        descriptionUI.value = ConvertorDescription {
            MassageContent(
                available = available,
                oldSelected = selectTime,
                onSelected = {
                    isLoading.value = true
                    convertorScreenModel.saveParameters {
                        setStringData("selectTime", it.format(inputFormatter))
                    }
                    coroutine.launch(Dispatchers.IO) {
                        loadCurrency(it)
                    }
                }
            )
        }
        isLoading.value = false
    }

}

private object ExchangeChach {
    val chach = mutableMapOf<LocalDate, ExchangeResponse>()
}

@Composable
private fun MassageContent(
    available: List<LocalDate>?,
    oldSelected: LocalDate,
    onSelected: (LocalDate) -> Unit
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
                symbol = oldSelected.format(outputFormatter)
            ),
            onClick = { datePickerOpen = true }
        )

        UnitAlert(
            title = stringResource(R.string.select_date),
            expanded = datePickerOpen,
            onDismissRequest = {

                datePickerOpen = false
            },
            onSelected = {
                onSelected(LocalDate.parse(it.name, outputFormatter))
                datePickerOpen = false
            },
            unitList = (available ?: listOf(oldSelected)).mapIndexed { index, date ->
                val text = date.format(outputFormatter)
                ConvertorUnit(
                    id = text,
                    name = text,
                    symbol = if (index == 0) stringResource(R.string.actual).lowercase() else ""
                )
            },
            oldSelected = ConvertorUnit(
                id = oldSelected.format(outputFormatter),
                name = oldSelected.format(outputFormatter),
                symbol = ""
            )
        )
    }
}


@Composable
private fun MassageBox(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
    closeable: Boolean,
    onDismissRequest: (() -> Unit)? = null
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
            content()
        }
        if (closeable) {
            IconButton(onClick = {
                onDismissRequest?.invoke()
            }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
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