package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData


class ConvertorLayoutScope(
    val convertorScreenModel: ConvertorScreenModel,
    val convertorData: ConvertorData,
    val unitList: List<ConvertorUnit>,
) {

}


@Composable
fun ConvertorLayout(
    modifier: Modifier = Modifier,
    convertorScreenModel: ConvertorScreenModel,
    unitList: List<ConvertorUnit>,
    convertorData: ConvertorData,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    scrollState: ScrollState? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ConvertorLayoutScope.(innerPadding: PaddingValues) -> Unit,
) {
    val innerPadding = contentPadding
    val modifier = if (scrollState != null) {
        modifier.verticalScroll(scrollState)
    } else modifier
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
//            .shadow(4.dp, shape)
            .background(containerColor),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement
    ) {
        ConvertorLayoutScope(
            convertorScreenModel = convertorScreenModel,
            unitList = unitList,
            convertorData = convertorData
        ).content(innerPadding)
    }

}

data class GroupedValue(
    val corner: RoundedCornerShape
)
