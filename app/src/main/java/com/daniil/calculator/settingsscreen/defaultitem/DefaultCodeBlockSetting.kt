package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager

@Composable
fun DynamicSettingRenderManager.DefaultCodeBlockSettingItem(
    setting: DynamicSetting,
    enableAlert: Boolean,
    onClick: () -> Unit
) {

    var showAlert by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .settingContainer(setting.enabled) {
                if (enableAlert) showAlert = true else onClick()
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
    }
    if (showAlert && enableAlert) {
        AlertDialog(
            title = { Text(setting.title) },
            text = {
                Text(setting.description)
            },
            onDismissRequest = {
                showAlert = false
            },
            dismissButton = {
                TextButton(onClick = {
                    showAlert = false
                }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onClick()
                    showAlert = false
                }) {
                    Text("OK")
                }
            },
        )
    }
}
