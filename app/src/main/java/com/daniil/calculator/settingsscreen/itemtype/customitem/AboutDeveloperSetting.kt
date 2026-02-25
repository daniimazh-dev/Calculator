package com.daniil.calculator.settingsscreen.itemtype.customitem

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.openScreen
import com.daniil.calculator.settingsscreen.defaultitem.settingContainer
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager


@Composable
fun AboutDeveloperSetting(
    setting: DynamicSetting,
) {
    val context = LocalContext.current
    var clickCount by remember { mutableIntStateOf(0) }
    val isDeveloperEnable = DynamicSettingsManager.getValue("developer_enable").toBoolean()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.settingContainer(setting.enabled) {
            if (!isDeveloperEnable) {
                clickCount += 1
                if (clickCount > 4) {
                    DynamicSettingsManager.setValue("developer_enable", true)
                    Toast.makeText(context, "Developer mode is enabled", Toast.LENGTH_SHORT).show()
                    clickCount = 0
                }
            }

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
        if (clickCount == 0) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.numeration_system_icon),
                contentDescription = "dev",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text("$clickCount/5")
        }

    }
}
