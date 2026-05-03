package com.daniil.calculator.settingsscreen.itemtype.customitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.currentVersionCode
import com.daniil.calculator.globalVersion
import com.daniil.csb.SettingsProvider

@Composable
fun NewVersion() {

    val simulateVersion by SettingsProvider.getValue<String>("imitate_version").collectAsState()

    val currentVersionCode = remember {
        simulateVersion.toIntOrNull() ?: currentVersionCode
    }
    if ((globalVersion?.versionCode ?: 0) > currentVersionCode) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
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