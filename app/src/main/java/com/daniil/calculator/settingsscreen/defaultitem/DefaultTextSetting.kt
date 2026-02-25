package com.daniil.calculator.settingsscreen.defaultitem

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.universal.Copy
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import kotlinx.coroutines.launch

@Composable
fun DynamicSettingRenderManager.DefaultTextSettingItem(setting: DynamicSetting) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .settingContainer(setting.enabled, onLongClick = {
                dropdownMenuExpanded = true
            })
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(text = setting.title, fontWeight = FontWeight.Bold)
            Text(
                text = setting.description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Icon(
            modifier = Modifier,
            imageVector = ImageVector.vectorResource(getIcon(setting.icon) ?: R.drawable.info_icon),
            contentDescription = "info",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        UniversalDropDownMenu(
            expanded = dropdownMenuExpanded,
            buttonList = listOf(Copy(setting.description)),
            onDismissRequest = {
                dropdownMenuExpanded = false
            }
        )

    }
}