package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager

@Composable
fun DynamicSettingRenderManager.DefaultInfoSettingItem(setting: DynamicSetting) {
    Box(
        modifier = Modifier
            .settingContainer(setting.enabled)
    ) {
        Column(modifier = Modifier.padding(vertical = 2.dp, horizontal = 12.dp)) {
            Text(
                text = setting.title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = setting.description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

    }
}