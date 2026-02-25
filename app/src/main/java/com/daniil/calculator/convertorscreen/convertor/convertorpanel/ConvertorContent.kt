package com.daniil.calculator.convertorscreen.convertor.convertorpanel

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertor
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.findOfId
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData

@Composable
fun ConvertorContent(
    modifier: Modifier = Modifier,
    convertorScreenModel: ConvertorScreenModel,
    convertorButtonData: ConvertorData,
    convertorData: CustomConvertor,
) {


    val viewListMode by convertorScreenModel.viewConvertorMode.collectAsState()
    val singleMode = convertorData.convertorScreen.singleViewMode
    AnimatedContent(
        modifier = modifier.clip(MaterialTheme.shapes.medium),
        targetState = viewListMode
    ) { mode ->
        convertorData.convertorScreen.viewScreens.findOfId(mode)?.render?.invoke()

    }

}