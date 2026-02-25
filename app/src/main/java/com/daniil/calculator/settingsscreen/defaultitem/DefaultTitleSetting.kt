package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager

@Composable
fun DynamicSettingRenderManager.DefaultTitleSettingItem(setting: DynamicSetting) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center

    ) {
//        Box(
//            modifier = Modifier
//                .clip(MaterialTheme.shapes.extraLarge)
//                .background(MaterialTheme.colorScheme.surfaceVariant),
//        ) {
//
//        }
        Text(
            text = setting.title,
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 12.dp),
            style = MaterialTheme.typography.titleMedium,
        )
    }

}