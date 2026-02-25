package com.daniil.calculator.calculatorscreen.buttons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daniil.calculator.calculatorscreen.CalculatorScreenModel
import com.daniil.calculator.universal.ButtonUI

@Composable
fun ButtonPanel(
    modifier: Modifier = Modifier,
    buttonSize: Dp,
    calckScreenModel: CalculatorScreenModel,
) {
    val mode by calckScreenModel.buttonPanelExpandedMode.collectAsState()
    val allButtons by calckScreenModel.buttons.collectAsState()

    val spaceBy = if (mode) 8.dp else 12.dp
    LazyVerticalGrid(
        modifier = modifier
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(spaceBy),
        horizontalArrangement = Arrangement.spacedBy(spaceBy),
        columns = GridCells.Fixed(if (mode) 5 else 4)
    ) {
        itemsIndexed(
            items = if (mode) allButtons else allButtons.filter { it.expanded },
            key = { _, item -> item.id }
        ) { _, item ->
            ButtonUI(
                buttonData = item,
                size = buttonSize,
                modifier = Modifier.animateItem()
            )
        }
    }
}

