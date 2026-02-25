package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager

@Composable
fun DynamicSettingRenderManager.DefaultStringDataSettingItem(setting: DynamicSetting) {
    val value = DynamicSettingsManager.getValueState(setting.id).value.orEmpty()
    var showAlert by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .settingContainer(setting.enabled) {
                showAlert = true
            }
    ) {

        Column() {
            Text(text = setting.title, fontWeight = FontWeight.Bold)
            Text(
                text = setting.description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            modifier = Modifier
                .padding(6.dp)
                .weight(1f),
            text = DynamicSettingsManager.getValueState(setting.id).value.orEmpty(),
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.End
        )


        Icon(
            imageVector = Icons.Default.Create,
            contentDescription = null,
        )
    }
    if (showAlert) {
        Alert(
            startValue = DynamicSettingsManager.getValue(setting.id).orEmpty(),
            title = setting.title,
            onConfirm = {
                DynamicSettingsManager.setValue(setting.id, it)
                showAlert = false
            },
            onDismissRequest = {
                showAlert = false
            }
        )
    }


}

@Composable
private fun Alert(
    startValue: String,
    title: String,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var text by remember { mutableStateOf(startValue) }
    AlertDialog(
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                },
                modifier = Modifier.fillMaxWidth()
            )
            text
        },
        onDismissRequest = {
            onDismissRequest()
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(text)
            }) {
                Text(stringResource(R.string.ok))
            }
        },
    )

}