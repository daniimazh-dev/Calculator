package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.CardContainer
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallInputWithUnit
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.StandardConvertor
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.validateValue


class AreaConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
    context: Context,
    ) : StandardConvertor(convertorData, model, context) {

    override fun onCreate() {
        super.onCreate()
        setContent {
            startViewMode = "Calculator"
            mode(id ="Calculator", name = stringResource(R.string.calculator), painterId = R.drawable.calculator_icon) {
                showClackPanel.value = false
                content = {
                    AreaConvertor()
                }

            }
            mode(id = "Comparison", name = stringResource(R.string.expanded), painterId = R.drawable.view3_icon) {
                showClackPanel.value = false
                content = {
                    expandedContent()
                }
            }
            mode( id ="List",  name = stringResource(R.string.diminished), painterId =  R.drawable.view1_icon) {
                content = {
                    diminishedContent()
                }

            }

        }
    }
}

@Composable
fun AreaConvertorImplementation.AreaConvertor() {
    val activeScreen by convertorScreenModel.currentConvertor.collectAsState()
    val calckBlock by convertorScreenModel.calckBlock.collectAsState()
    val convertorCore = convertorScreenModel.convertorCore


    val lengthConvertorName = "Length"


    val height =
        convertorScreenModel.getParameter(
            key = "height",
            defaultValue = "0"
        ) as String
    val width =
        convertorScreenModel.getParameter(
            key = "width",
            defaultValue = "0"
        ) as String

    val lengthUnits = remember { convertorCore.getUnits(lengthConvertorName) }

    val heightUnit =
        convertorScreenModel.getParameter(
            key = "heightConvertor",
            defaultValue = convertorCore.getStartUnit(lengthConvertorName)!!
        ) as ConvertorUnit
    val widthUnit =
        convertorScreenModel.getParameter(
            key = "widthConvertor",
            defaultValue = convertorCore.getStartUnit(lengthConvertorName)!!
        ) as ConvertorUnit

    val selectInput: String = convertorScreenModel.getParameter(
        key = "selectInput",
        defaultValue = "height"
    ) as String
    val selectUnit = convertorScreenModel.getParameter(
            key = "selectUnit",
            defaultValue = getStartUnits()!!
    ) as ConvertorUnit


    val currentUnit: ConvertorUnit = convertorScreenModel.getParameter(
        key = "currentUnit",
        defaultValue = getUnits().getOrElse(0) { NullableUnit }
    ) as ConvertorUnit

    val firstUnit: ConvertorUnit = convertorScreenModel.getParameter(
        key = "firstUnit",
        defaultValue = getUnits().getOrElse(0) { NullableUnit }
    ) as ConvertorUnit

    val secondUnit: ConvertorUnit = convertorScreenModel.getParameter(
        key = "secondUnit",
        defaultValue = getUnits().getOrElse(1) { NullableUnit }
    ) as ConvertorUnit

    val scrollState = rememberScrollState()
    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = getUnits(),
        convertorData = convertorData,
        containerColor = Color.Transparent,

        scrollState = scrollState
    ) { innerPadding ->
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectInput == "height") convertorScreenModel.saveParameters {
                    setStringData("height", calckBlock)
                }
                SmallInputWithUnit(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.lenght),
                    content = if (selectInput == "height") calckBlock else height,
                    currentUnit = heightUnit,
                    unitList = lengthUnits,
                    contentPadding = PaddingValues(2.dp),
                    copyPasteMenu = CopyPasteMenu.Full,
                    selected = selectInput == "height",
                    onPaste = { str ->
                        if (str == null) return@SmallInputWithUnit
                        val validate = convertorScreenModel.validateValue(str)
                        if (!validate.first) {
                            Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                            return@SmallInputWithUnit
                        }
                        convertorScreenModel.saveParameters {
                            setStringData("selectInput", "height")
                        }
                        convertorScreenModel.setCalck(str)
                    },
                    onChangeUnit = {
                        convertorScreenModel.saveParameters {
                            setObject("heightConvertor", it)
                        }
                    },
                    onClick = {
                        convertorScreenModel.saveParameters {
                            setStringData("selectInput", "height")
                        }
                        convertorScreenModel.setCalck(height)
                    }
                )
                if (selectInput == "width") convertorScreenModel.saveParameters {
                    setStringData("width", calckBlock)
                }
                SmallInputWithUnit(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.width),
                    content = if (selectInput == "width") calckBlock else width,
                    currentUnit = widthUnit,
                    unitList = lengthUnits,
                    contentPadding = PaddingValues(2.dp),
                    copyPasteMenu = CopyPasteMenu.Full,
                    selected = selectInput == "width",
                    onPaste = { str ->
                        if (str == null) return@SmallInputWithUnit
                        val validate = convertorScreenModel.validateValue(str)
                        if (!validate.first) {
                            Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                            return@SmallInputWithUnit
                        }
                        convertorScreenModel.saveParameters {
                            setStringData("selectInput", "width")
                        }
                        convertorScreenModel.setCalck(str)
                    },
                    onChangeUnit = {
                        convertorScreenModel.saveParameters {
                            setObject("widthConvertor", it)
                        }
                    },
                    onClick = {
                        convertorScreenModel.saveParameters {
                            setStringData("selectInput", "width")
                        }
                        convertorScreenModel.setCalck(width)
                    }
                )
            }
            val heightM = remember(height, heightUnit) {
                try {
                    convertorCore.convert(
                        value = height,
                        from = heightUnit,
                        to = convertorCore.getStartUnit(lengthConvertorName)!!,
                        convertorId = lengthConvertorName
                    )
                } catch (e: Exception) {
                    "0"
                }
            }
            val widthM = remember(width, widthUnit) {
                try {
                    convertorCore.convert(
                        value = width,
                        from = widthUnit,
                        to = convertorCore.getStartUnit(lengthConvertorName)!!,
                        convertorId = lengthConvertorName
                    )
                } catch (e: Exception) {
                    "0"
                }
            }
            val visible = (width.toFloatOrNull()?: 0.0)== 0.0f || (height.toFloatOrNull() ?: 0.0) == 0.0f
            if (!visible) {
                CardContainer(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        ResultArea(
                            height = heightM.toDoubleOrNull() ?: 0.0,
                            width = widthM.toDoubleOrNull() ?: 0.0,
                            widthLabel = (width.toDoubleOrNull() ?: 0.0).toString(),
                            heightLabel = (height.toDoubleOrNull() ?: 0.0).toString(),
                            kSize = 200
                        )
                    }
                }
            }



            val fromData = ((heightM.toDoubleOrNull() ?: 0.0) * (widthM.toDoubleOrNull()
                ?: 0.0)).toString()

            val result = remember(fromData, selectUnit.id) {
                try {
                    convertorCore.convert(
                        value = fromData,
                        from = secondUnit,
                        to = selectUnit,
                        convertorId = convertorData.id
                    )
                } catch (e: Exception) {
                    "—"
                }
            }
            SmallInputWithUnit(
                title = selectUnit.name,
                content = result,
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                onClick = {

                },
                currentUnit = selectUnit,
                onChangeUnit = {
                    convertorScreenModel.saveParameters {
                        setObject("selectUnit", it)
                    }
                }
            )


        }

    }

}

@Composable
private fun ResultArea(
    height: Double,
    width: Double,
    heightLabel: String,
    widthLabel: String,
    kSize: Int = 200,
) {
    val total = height + width
    val sizeHeight = (height / total) * kSize
    val sizeWidth = (width / total) * kSize
    val visible = width == 0.0 || height == 0.0
    AnimatedVisibility(!visible) {
        Row() {
            Text(
                modifier = Modifier
                    .offset(y = 12.dp, x = 8.dp)
                    .rotate(-90f),
                text = heightLabel
            )
            Column {

                Box(
                    modifier = Modifier
                        .height(animateDpAsState(sizeHeight.coerceAtLeast(2.0).dp).value)
                        .width(animateDpAsState(sizeWidth.coerceAtLeast(2.0).dp).value)
                        .border(4.dp, MaterialTheme.colorScheme.onPrimary)
                ) {

                }
                Text(widthLabel)
            }
            Spacer(modifier = Modifier.width(24.dp))

        }
    }


}

