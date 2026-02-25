package com.daniil.calculator.settingsscreen.itemtype.customitem

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import androidx.core.net.toUri
import com.daniil.calculator.R
import com.daniil.calculator.settingsscreen.defaultitem.settingContainer
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager

@Composable
fun LinkSetting(
    setting: DynamicSetting,
    customLink: String? = null,
    enableAlert: Boolean
) {
    val context = LocalContext.current
    val link = customLink ?: setting.value?.toString() ?: return
    val goTo = {
        val intent = Intent(Intent.ACTION_VIEW, link.toUri())
        context.startActivity(intent)
    }

    var showAlert by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .settingContainer(setting.enabled) {
                if (enableAlert) {
                    showAlert = true
                } else {
                    goTo()
                }
            }

    ) {
        DynamicSettingRenderManager.getIcon(setting.icon)?.let {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(it),
                contentDescription = "Icon",
            )
        }
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = setting.title, fontWeight = FontWeight.Bold)
            Text(
                text = setting.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Go to ${setting.value}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (showAlert && enableAlert) {
        AlertDialog(
            icon = {
                DynamicSettingRenderManager.getIcon(setting.icon)?.let {
                    Image(
                        modifier = Modifier.size(46.dp),
                        painter = painterResource(it),
                        contentDescription = "Icon",
                    )
                }
            },
            title = { Text(stringResource(R.string.follow_link), fontSize = 22.sp) },
            text = {
                Text(setting.value.toString())
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
                    goTo()
                    showAlert = false
                }) {
                    Text("OK")
                }
            },
        )
    }
}