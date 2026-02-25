package com.daniil.calculator.convertorscreen.convertor.convertorpanel.register

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.AreaConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.BMIConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.DurationConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.CurrencyConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.DiscountConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.FunctionGraphConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.IPCalculatorConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom.NumerationSystemConvertorImplementation
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.settingsscreen.customscreen.logs.LogManager

@SuppressLint("ComposableNaming")
@Composable
fun registerCustomConvertors(
    convertorScreenModel: ConvertorScreenModel,
) {
    val context = LocalContext.current
    registerConvertor(
        id = "BMI",
        convertorScreenModel = convertorScreenModel
    ) { convertor ->
        BMIConvertorImplementation(convertor, convertorScreenModel)
    }
    registerConvertor(
        id = "Numeration_system",
        convertorScreenModel = convertorScreenModel
    ) { convertor ->
        NumerationSystemConvertorImplementation(convertor, convertorScreenModel, context)
    }
    registerConvertor(
        id = "Area",
        convertorScreenModel = convertorScreenModel
    ) { convertor ->
        AreaConvertorImplementation(convertor, convertorScreenModel, context)
    }
    registerConvertor(
        id = "Duration",
        convertorScreenModel = convertorScreenModel
    ) { convertor ->
        DurationConvertorImplementation(convertor, convertorScreenModel)
    }
    registerConvertor(
        id = "Currency",
        convertorScreenModel = convertorScreenModel
    ) { convertor ->
        CurrencyConvertorImplementation(convertor, convertorScreenModel, context)
    }
    registerConvertor(
        id = "Discount",
        convertorScreenModel = convertorScreenModel
    ) { convertor ->
        DiscountConvertorImplementation(convertor, convertorScreenModel)
    }
    registerConvertor(
        id = "Function_graph",
        convertorScreenModel = convertorScreenModel
    ) { convertor ->
        FunctionGraphConvertorImplementation(convertor, convertorScreenModel)
    }
    registerConvertor(
        id = "IP_calculator",
        convertorScreenModel = convertorScreenModel
    ) { convertor ->
        IPCalculatorConvertorImplementation(convertor, convertorScreenModel)
    }
}

@SuppressLint("ComposableNaming")
@Composable
private fun registerConvertor(
    id: String,
    convertorScreenModel: ConvertorScreenModel,
    factory: (ConvertorData) -> CustomConvertorImplementation,
) {
    convertorScreenModel.getConvertorData(id)?.let { convertor ->
        LogManager.i(
            "Register custom convertors",
            content = "Register convertor: ${convertor.id}"
        )

        val implementation = factory(convertor)

        convertorScreenModel
            .customConvertorManager
            .registerImplementation(id, implementation)
    }
}
