package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayoutScope
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData

@Composable
fun ConvertorLayoutScope.AddButton(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(6.dp),
    size: Dp = 42.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.padding(padding)
    ) {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable {
                    onClick()
                }
        ) {
            Icon(
                modifier = Modifier.size(size),
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

}

@Preview
@Composable
private fun Preview() {
    val convertorScreenModel = viewModel() { ConvertorScreenModel() }
    ConvertorLayout(
        modifier = Modifier.size(100.dp),
        convertorScreenModel = convertorScreenModel,
        unitList = emptyList(),
        containerColor = Color.Transparent,
        convertorData = ConvertorData(
            id = "test",
            startUnit = NullableUnit,
            painterName = "test",
        )
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center
        ) {
            AddButton() { }
        }
    }
}