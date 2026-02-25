package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp


class GroupedLayoutScope()


@Composable
fun ConvertorLayoutScope.GroupedLayout(
    modifier: Modifier = Modifier,
    containerShape: Shape = MaterialTheme.shapes.large,
    contentCornerShape: RoundedCornerShape = RoundedCornerShape(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    scrollState: ScrollState? = null,
    content: @Composable GroupedLayoutScope.(GroupedValue) -> Unit,
) {

    val modifier = if (scrollState != null) {
        modifier.verticalScroll(scrollState)
    } else modifier

    Column(
        modifier = modifier
            .clip(containerShape),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        val groupedValue = GroupedValue(contentCornerShape)
        GroupedLayoutScope().content(groupedValue)
    }
}