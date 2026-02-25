package com.daniil.calculator.convertorscreen.convertor.buttonpanel

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonsStack
import com.daniil.calculator.universal.ButtonUI

@Composable
fun ConvertorCalckButtonPanel(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    buttonStack: ButtonsStack
) {
    if (buttonStack.buttons.isNotEmpty()) {
        LazyVerticalGrid(
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 2.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            columns = GridCells.Fixed(buttonStack.column)
        ) {
            itemsIndexed(buttonStack.buttons) { index, item ->
                ButtonUI(
                    modifier = Modifier.animateItem(),
                    size = size,
                    buttonData = item
                )
            }
        }
    } else {
        Spacer(modifier = Modifier.size(2.dp))
    }

}