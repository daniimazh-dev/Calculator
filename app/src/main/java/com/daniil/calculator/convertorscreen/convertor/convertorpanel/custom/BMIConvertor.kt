package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.CardContainer
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallInput
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertorImplementation
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.validateValue
import com.daniil.calculator.core.CalculatorCore
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import com.daniil.calculator.utilites.roundTo
import kotlinx.coroutines.launch


class BMIConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
) : CustomConvertorImplementation(convertorData, model) {
    override fun onCreate() {
        super.onCreate()
        setContent {
            singleViewMode = true
            startViewMode = "BMI"
            mode(id ="BMI", painterId = R.drawable.calculator_icon) {
                content = {
                    BMIConvertor()
                }
                showClackPanel.value = false
            }

        }
    }
}


@Composable
fun BMIConvertorImplementation.BMIConvertor() {
    val context = LocalContext.current
    val calckBlock by convertorScreenModel.calckBlock.collectAsState()


    val activeScreen by convertorScreenModel.currentConvertor.collectAsState()
    val convertorCore = convertorScreenModel.convertorCore
    val parameters by convertorScreenModel.currentParameters.collectAsState()


    val clipboardManager = LocalClipboard.current
    val coroutine = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val height: String = convertorScreenModel.getParameter(
        key = "height",
        defaultValue = "0"
    ) as String

    val weight: String = convertorScreenModel.getParameter(
        key = "weight",
        defaultValue = "0"
    ) as String

    val currentUnit: String = convertorScreenModel.getParameter(
        key = "currentUnit",
        defaultValue = "height"
    ) as String


    val result = remember(weight, height) {
        try {
            val value = CalculatorCore.evaluate("${weight}/(${height}/100)^2") ?: "—"
            value.roundTo(1, indication = false)
        } catch (e: Exception) {
            "—"
        }
    }
    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = getUnits(),
        convertorData = activeScreen ?: return,
        scrollState = scrollState,
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentUnit == "height") convertorScreenModel.saveParameters {
                    setStringData("height", calckBlock)
                }
                SmallInput(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.height),
                    content = if (currentUnit == "height") calckBlock else height,
                    suffix = "cm",
                    selected = currentUnit == "height",
                    copyPasteMenu = CopyPasteMenu.Full,
                    onPaste = { str ->
                        if (str == null) return@SmallInput
                        val validate = convertorScreenModel.validateValue(str)
                        if (!validate.first) {
                            Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                            return@SmallInput
                        }

                        convertorScreenModel.saveParameters {
                            convertorScreenModel.setCalck(str)
                            setStringData("currentUnit", "height")
                        }

                    },
                    onClick = {
                        convertorScreenModel.saveParameters {
                            convertorScreenModel.setCalck(height)
                            setStringData("currentUnit", "height")
                        }
                    }
                )
                if (currentUnit == "weight") convertorScreenModel.saveParameters {
                    setStringData("weight", calckBlock)
                }
                SmallInput(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.weight),
                    content = if (currentUnit == "weight") calckBlock else weight,
                    suffix = "kg",
                    selected = currentUnit == "weight",
                    copyPasteMenu = CopyPasteMenu.Full,
                    onPaste = { str ->
                        if (str == null) return@SmallInput
                        val validate = convertorScreenModel.validateValue(str)
                        if (!validate.first) {
                            Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                            return@SmallInput
                        }
                        convertorScreenModel.saveParameters {
                            convertorScreenModel.setCalck(str)
                            setStringData("currentUnit", "weight")
                        }

                    },
                    onClick = {
                        convertorScreenModel.saveParameters {
                            convertorScreenModel.setCalck(weight)
                            setStringData("currentUnit", "weight")
                        }
                    }
                )
            }
            CardContainer() {
                var dropdownMenuExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                dropdownMenuExpanded = true
                            }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.result),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = when {
                                result == "—" -> MaterialTheme.colorScheme.onSurfaceVariant
                                (result.toFloatOrNull()
                                    ?: 0f) < 18.5f -> MaterialTheme.colorScheme.tertiary

                                (result.toFloatOrNull()
                                    ?: 0f) < 25f -> MaterialTheme.colorScheme.primary

                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Text(
                            text = when {
                                result == "—" -> "—"
                                (result.toFloatOrNull() ?: 0f) < 18.5f -> "Underweight"
                                (result.toFloatOrNull() ?: 0f) < 25f -> "Normal"
                                (result.toFloatOrNull() ?: 0f) < 30f -> "Overweight"
                                else -> "Obese"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val dropDownButtonList = listOf(
                            UniversalDropDownItem(
                                title = stringResource(R.string.copy),
                                iconResource = R.drawable.copy_standart,
                                onClick = {
                                    coroutine.launch {
                                        clipboardManager.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    "result",
                                                    result,
                                                )
                                            )
                                        )
                                    }
                                }

                            ),
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
            CardContainer() {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.chart),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    BMIScaleBar(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        bmiValue = result.toDoubleOrNull()
                    )
                }
            }

        }
    }


}

@Composable
private fun BMIScaleBar(
    modifier: Modifier = Modifier,
    bmiValue: Double?,
) {
    val labels = listOf(5, 15, 25, 35, 45, 55)

    val bmiMin = labels.first().toFloat()
    val bmiMax = labels.last().toFloat()



    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val density = LocalDensity.current

        val indicatorOffsetPx = remember(bmiValue, totalWidthPx) {
            val value = bmiValue?.toFloat() ?: bmiMin
            val ratio = ((value - bmiMin) / (bmiMax - bmiMin)).coerceIn(0f, 1f)
            ratio * totalWidthPx
        }

        val indicatorOffsetDp = with(density) { indicatorOffsetPx.toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF4FC3F7), // Underweight
                            Color(0xFF81C784), // Normal
                            Color(0xFFFFF176), // Overweight
                            Color(0xFFE57373),  // Obese
                        )
                    )
                )
        )


        Box(
            modifier = Modifier
                .height(26.dp)
                .width(3.dp)
                .offset(x = animateDpAsState(indicatorOffsetDp - 3.dp).value)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        labels.forEach {
            Text(
                text = it.toString(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
