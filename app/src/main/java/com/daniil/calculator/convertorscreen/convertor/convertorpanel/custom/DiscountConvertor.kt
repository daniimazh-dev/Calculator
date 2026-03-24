package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.ValidationParam
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.BigResult
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallInput
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.validateValue
import com.daniil.calculator.core.CalculatorCore
import com.daniil.calculator.utilites.roundTo


class DiscountConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
) : CustomConvertorImplementation(convertorData, model) {

    override fun onCreate() {
        super.onCreate()
        setContent {
            singleViewMode = true
            startViewMode = "Discount"

            mode(id ="Discount", painterId = R.drawable.calculator_icon) {
                content = {
                    DiscountConvertor()
                }
                showClackPanel.value = false
            }
        }
    }
}


@Composable
fun DiscountConvertorImplementation.DiscountConvertor(
    modifier: Modifier = Modifier, ) {
    val context = LocalContext.current
    val calckBlock by convertorScreenModel.calckBlock.collectAsState()

    val activeScreen by convertorScreenModel.currentConvertor.collectAsState()
    val parameters by convertorScreenModel.currentParameters.collectAsState()

    val scrollState = rememberScrollState()

    val price: String = convertorScreenModel.getParameter(
        key = "price",
        defaultValue = "0"
    ) as String

    val percent: String = convertorScreenModel.getParameter(
        key = "percent",
        defaultValue = "0"
    ) as String

    val currentUnit: String = convertorScreenModel.getParameter(
        key = "currentUnit",
        defaultValue = "price"
    ) as String


    val result = remember(price, percent) {
        try {
            val value = CalculatorCore.evaluate("$price-$percent%") ?: "—"
            value.roundTo(3)
        } catch (e: Exception) {
            e.printStackTrace()
            "—"
        }
    }
    val saved = remember(price, percent) {
        try {
            (price.toFloat() - result.toFloat()).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "—"
        }
    }
    val unitList by getUnitsAsSate().collectAsState()
    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = unitList,
        convertorData = activeScreen ?: return,
        containerColor = Color.Transparent,
        scrollState = scrollState
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            convertorScreenModel.saveParameters {
                if (currentUnit == "price") setStringData("price", calckBlock)
            }
            SmallInput(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.price),
                content = price,
                copyPasteMenu = CopyPasteMenu.Full,
                selected = currentUnit == "price",
                onPaste = { str ->
                    if (str == null) return@SmallInput
                    val validate = convertorScreenModel.validateValue(str)
                    if (!validate.first) {
                        Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                        return@SmallInput
                    }

                    convertorScreenModel.saveParameters {
                        convertorScreenModel.setCalck(str)
                        setStringData("currentUnit", "price")
                    }

                },
                onClick = {
                    convertorScreenModel.saveParameters {
                        convertorScreenModel.setCalck(price)
                        setStringData("currentUnit", "price")
                    }
                }
            )
            convertorScreenModel.saveParameters {
                if (currentUnit == "percent") setStringData(
                    "percent",
                    calckBlock.toInt().coerceIn(0..100).toString()
                )
            }
            SmallInput(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.discount_precent),
                content = percent,
                suffix = "%",
                copyPasteMenu = CopyPasteMenu.Full,
                selected = currentUnit == "percent",
                onPaste = { str ->
                    if (str == null) return@SmallInput
                    val validate = convertorScreenModel
                        .validateValue(value = str,
                            rules = ValidationParam(rule = ValidationParam.Rule.InRange, args = listOf(0, 100)))
                    if (!validate.first) {
                        Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                        return@SmallInput
                    }
                    convertorScreenModel.saveParameters {
                        convertorScreenModel.setCalck(str)
                        setStringData("currentUnit", "percent")
                    }

                },
                onClick = {
                    convertorScreenModel.saveParameters {
                        convertorScreenModel.setCalck(percent)
                        setStringData("currentUnit", "percent")
                    }
                }
            )
            BigResult(
                modifier = Modifier,
                title = stringResource(R.string.result),
                content = result,
                description = "${stringResource(R.string.saved)}: $saved",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                onClick = {}
            )
        }
    }
}
