package com.daniil.calculator.settingsscreen.itemtype.customitem

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.daniil.calculator.R
import com.daniil.calculator.core.RetrofitDaniilServerInstance
import com.daniil.calculator.currentVersionCode
import com.daniil.calculator.globalVersion
import com.daniil.calculator.settingsscreen.defaultitem.settingContainer
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import kotlin.text.ifEmpty


@Composable
fun NewVersion(
    setting: DynamicSetting,
) {
    val context = LocalContext.current
    val simulateVersion = DynamicSettingsManager.getValue("imitate_version")?.toIntOrNull()

    val currentVersionCode = remember {
        simulateVersion ?: currentVersionCode
    }
    if ((globalVersion?.versionCode ?: 0) > currentVersionCode) {
//        Box(modifier = Modifier
//            .clip(MaterialTheme.shapes.medium)
//            .background(MaterialTheme.colorScheme.primary),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = stringResource(R.string.new_version),
//                modifier = Modifier.padding(horizontal = 4.dp)
//            )
//        }

        Box(

            modifier = Modifier
                .settingContainer(setting.enabled) {
                    val useTestServer = DynamicSettingsManager.getValue("use_test_server").toBoolean()
                    val url = when {
                        useTestServer -> RetrofitDaniilServerInstance.TEST_URL
                        else -> RetrofitDaniilServerInstance.BASE_URL
                    }
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        globalVersion?.uri?.ifEmpty { "$url/available-version/${globalVersion?.versionName}" }?.toUri()
                    )
                    context.startActivity(intent)
                },
            contentAlignment = Alignment.CenterEnd
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.new_version), fontWeight = FontWeight.Bold)
                Text(
                    text = globalVersion?.versionName.orEmpty(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to download",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }



}

