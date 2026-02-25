package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager

@Composable
fun DynamicSettingRenderManager.DefaultSwitchSettingItem(
    setting: DynamicSetting,
) {
    val checked = DynamicSettingsManager.getValueState(setting.id).value.toBoolean()
    val enabled by remember(setting.enabled) { mutableStateOf(setting.enabled) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .settingContainer(enabled) {
                DynamicSettingsManager.setValue(setting.id, checked)
            }
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = setting.title, fontWeight = FontWeight.Bold)
            Text(
                text = setting.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Switch(
            modifier = Modifier,
            checked = checked,
            enabled = enabled,
            onCheckedChange = {
                DynamicSettingsManager.setValue(setting.id, it)
            }
        )
    }


}