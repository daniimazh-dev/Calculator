package com.daniil.calculator.convertorscreen.convertor.buttonpanel

import android.content.ClipData
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.convertor.unit.UnitSelect
import com.daniil.calculator.convertorscreen.convertor.unit.ifNullable
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import kotlinx.coroutines.launch

@Composable
fun ConvertorCalckBlockPanel(
    modifier: Modifier = Modifier,
    convertorData: ConvertorData,
    convertorScreenModel: ConvertorScreenModel,
    onChangeSize: (IntSize) -> Unit
) {
    val calckBlock by convertorScreenModel.calckBlock.collectAsState()
    val activeScreen by convertorScreenModel.currentConvertor.collectAsState()
    val inputUnit by convertorScreenModel.currentUnit.collectAsState()
    val clipboardManager = LocalClipboard.current
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()

    val units = remember { convertorScreenModel.convertorCore.getUnits(activeScreen?.id!!) }

    val firstUnit: ConvertorUnit = convertorScreenModel.getParameter(
        key = "firstUnit",
        defaultValue = NullableUnit
    ) as ConvertorUnit

    val secondUnit: ConvertorUnit = convertorScreenModel.getParameter(
        key = "secondUnit",
        defaultValue = NullableUnit
    ) as ConvertorUnit

    val currentUnit: ConvertorUnit = convertorScreenModel.getParameter(
        key = "currentUnit",
        defaultValue = NullableUnit
    ) as ConvertorUnit

    Row(modifier = modifier
        .fillMaxWidth()
        .combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {},
            onLongClick = {
                dropDownMenuExpanded = true
            },
        ).onGloballyPositioned {
            onChangeSize(it.size)
        },

        verticalAlignment = Alignment.CenterVertically
    ) {
        UnitSelect(
            currentUnit = currentUnit.ifNullable { inputUnit },
            unitList = units,
            horizontalAlignment = Alignment.End,
            convertorScreenModel = convertorScreenModel,
            onSelected = { unit ->
                convertorScreenModel.saveParameters {
                    when (currentUnit) {
                        firstUnit -> {
                            if (secondUnit == unit) setObject("secondUnit", firstUnit)
                            setObject("firstUnit", unit)

                        }
                        secondUnit -> {
                            if (firstUnit == unit) setObject("firstUnit", secondUnit)
                            setObject("secondUnit", unit)
                        }
                    }
                    convertorScreenModel.setCurrentUnit(unit, false)
                    setObject("currentUnit", unit)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Column() {
            val dropDownButtonList = listOf(
                UniversalDropDownItem(
                    title = stringResource(R.string.copy),
                    iconResource = R.drawable.copy_standart,
                    onClick = {
                        coroutine.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "copy",
                                        calckBlock,
                                    )
                                )
                            )
                        }
                    }

                ),
                UniversalDropDownItem(
                    title = stringResource(R.string.paste),
                    iconResource = R.drawable.paste_standart,
                    onClick = {
                        coroutine.launch {
                            val data = clipboardManager.getClipEntry()?.clipData?.getItemAt(0)?.text
                            convertorScreenModel.setCalck(data?.toString() ?: calckBlock)
                        }
                    }
                )
            )
            UniversalDropDownMenu(
                expanded = dropDownMenuExpanded,
                buttonList = dropDownButtonList,
                onDismissRequest = {
                    dropDownMenuExpanded = false
                }
            )
            Text(text = calckBlock, fontSize = 32.sp)
        }



    }

}