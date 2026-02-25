package com.daniil.calculator.convertorscreen.convertor.unit

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun UnitSelect(
    modifier: Modifier = Modifier,
    currentUnit: ConvertorUnit,
    unitList: List<ConvertorUnit>,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    onSelected: (ConvertorUnit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var specialExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var lastSelect by remember { mutableStateOf(currentUnit) }

    LaunchedEffect(isPressed) {
        if (!isPressed && specialExpanded) {
            specialExpanded = false
            onSelected(lastSelect)
        }
    }
    DropdownUnit(
        expanded = specialExpanded,
        onItemSelected = {
            lastSelect = it
        },
        unitList = unitList,
        currentUnit = currentUnit,
        horizontalAlignment = horizontalAlignment
    ) {
        UnitButton(
            modifier = modifier,
            unit = currentUnit,
            interactionSource = interactionSource,
            onClick = { expanded = true },
            onLongClick = { specialExpanded = true }
        )
    }

    UnitAlert(
        expanded = expanded,
        unitList = unitList,
        oldSelected = currentUnit,
        onDismissRequest = {
            expanded = false
        },
        onSelected = {
            onSelected(it)
            expanded = false
        }
    )
}