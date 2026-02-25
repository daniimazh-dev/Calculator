package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color


@Composable
fun ConvertorLayoutScope.CardContainer(
    modifier: Modifier = Modifier,
    groupedValue: GroupedValue? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentPadding: PaddingValues = PaddingValues.Zero,
    contentAlignment: Alignment = Alignment.Center,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val clipModifier = if (groupedValue != null) {
        modifier.clip(groupedValue.corner)
    } else {
        modifier.clip(MaterialTheme.shapes.large)
    }
    val modifier = if (onClick != null || onLongClick != null) {
        clipModifier.combinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = { onLongClick?.invoke() }
        )
    } else clipModifier

    Box(
        modifier = modifier
            .background(containerColor),
    ) {
        Box(
            modifier = Modifier
        ) {
            Box(
                modifier = Modifier
                    .padding(contentPadding),
                contentAlignment = contentAlignment
            ) {
                content()
            }
        }

    }
}
