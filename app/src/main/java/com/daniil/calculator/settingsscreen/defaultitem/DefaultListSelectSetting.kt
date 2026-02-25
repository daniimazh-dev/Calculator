package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import com.daniil.calculator.settingsscreen.settings.manager.Parameter

@Composable
fun DynamicSettingRenderManager.DefaultListSelectSettingItem(setting: DynamicSetting) {
    var alertShow by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf(setting.value) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .settingContainer(setting.enabled) {
                alertShow = true
            },
    ) {


        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(text = setting.title, fontWeight = FontWeight.Bold)
            Text(
                text = setting.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                modifier = Modifier.padding(6.dp),
                text = setting.parameters?.find { value == it.id }?.name ?: "—",
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
            )
            AlertPicker(
                expanded = alertShow,
                setting = setting,
                onConfirm = {
                    it?.let {
                        DynamicSettingsManager.setValue(setting.id, it.id)
                        value = it.id
                    }
                    alertShow = false
                },
                value = value.orEmpty(),
                onDismissRequest = {
                    alertShow = false
                }
            )
        }
    }
}

@Composable
private fun AlertPicker(
    expanded: Boolean,
    value: String,
    setting: DynamicSetting,
    onConfirm: (Parameter?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (expanded) {
        var selected by remember {
            mutableStateOf(setting.parameters?.indexOfFirst { it.id == value })
        }

        AlertDialog(
            title = {
                Text(
                    text = setting.title,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    setting.parameters?.forEachIndexed { index, content ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .clickable {
                                    selected = index
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ), contentAlignment = Alignment.Center
                            ) {
                                if (selected == index) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                            Text(content.name)
                        }
                    }
                }
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(setting.parameters?.getOrNull(selected ?: 0))
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismissRequest()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}


@Preview
@Composable
private fun Preview() {
    val list = listOf("1 param", "2 param", "3 param")
    val selected = 1
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        list.forEach { content ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .fillMaxWidth(0.5f)
                    .clickable {
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ), contentAlignment = Alignment.Center
                ) {
                    if (list[selected] == content) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
                Text(content)
            }
        }
    }
}