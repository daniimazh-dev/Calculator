package com.daniil.calculator.convertorscreen.convertor.unit

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UnitButton(
    modifier: Modifier = Modifier,
    unit: ConvertorUnit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                interactionSource = interactionSource,
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            ),
            contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = unit.symbol,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                modifier = Modifier,
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = color
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UnitButton(unit = NullableUnit.copy(symbol = "test")) { }
}