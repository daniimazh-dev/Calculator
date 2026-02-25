package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager

@Composable
fun DynamicSettingRenderManager.DefaultSliderSettingItem(setting: DynamicSetting) {

    val trigger = DynamicSettingsManager.getValueState(setting.id).value
    val valueState = remember(trigger) {
        mutableStateOf(
            (setting.value as? Number)?.toFloat() ?: 0f
        )
    }

    val range = setting.parameters?.let {
        val min = (it[0] as Number).toFloat()
        val max = (it[1] as Number).toFloat()
        min..max
    } ?: 0f..1f

    val step =
        setting.parameters?.getOrNull(2)?.let { (it as Number).toFloat() } ?: 0f

    val steps =
        if (step > 0f) ((range.endInclusive - range.start) / step).toInt() - 1 else 0

    Column {
        Text(text = setting.title, fontWeight = FontWeight.Bold)
        Row {
            Text(
                modifier = Modifier.weight(1f),
                text = setting.description,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "%.2f".format(valueState.value),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Slider(
            value = valueState.value,
            onValueChange = {
                valueState.value = it
                DynamicSettingsManager.setValue(setting.id, it)
            },
            valueRange = range,
            steps = steps
        )

    }


}