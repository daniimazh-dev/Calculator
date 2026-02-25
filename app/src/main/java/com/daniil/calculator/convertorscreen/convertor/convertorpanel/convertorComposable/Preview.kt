package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.BigInput
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.BigInputWithUnit
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.BigResult
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallInput
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallInputWithUnit
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallResult
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData



@Preview
@Composable
private fun Preview() {
    val convertorScreenModel = viewModel() { ConvertorScreenModel() }
    ConvertorLayout(
        modifier = Modifier,
        convertorScreenModel = convertorScreenModel,
        unitList = emptyList(),
        convertorData = ConvertorData(
            id = "Test",
            startUnit = NullableUnit,
            painterName = "test",
        )
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GroupedLayout() { groupedValue ->
                SmallInputWithUnit(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Title",
                    content = "153535678765",
                    onClick = {},
                    onPaste = {},
                    currentUnit = NullableUnit,
                    copyPasteMenu = CopyPasteMenu.Full,
                    onChangeUnit = {},
                )
                SmallInput(
                    modifier = Modifier,
                    title = "Title",
                    content = "553545678",
                    suffix = "cm",
                    contentAlignment = Alignment.CenterStart,
                    onClick = {}
                )

                BigInputWithUnit(
                    modifier = Modifier,
                    title = "Title",
                    content = "5938838676",
                    onChangeUnit = {},
                    currentUnit = NullableUnit,
                    onClick = {}
                )
                BigInput(
                    modifier = Modifier,
                    title = "Title",
                    suffix = "cm",
                    content = "5938838676",
                    contentAlignment = Alignment.CenterEnd,
                    onClick = {}
                )
                SmallResult(
                    modifier = Modifier,
                    title = "Title",
                    content = "57837487",
                    suffix = "cm",
                    reversedLayout = false,
                    selected = true,
                    onClick = {}
                )
                BigResult(
                    modifier = Modifier,
                    title = "Title",
                    content = "5397639876",
                    suffix = "cm",
                    description = "saved: 2828",
                    onClick = {}
                )
            }


        }

    }
}
