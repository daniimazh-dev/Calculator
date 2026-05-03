package com.daniil.calculator

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.daniil.calculator.core.RetrofitDaniilServerInstance
import com.daniil.csb.SettingsProvider
import kotlin.text.ifEmpty
import androidx.compose.runtime.collectAsState

@Composable
fun BetaVersionAlert() {
    val context = LocalContext.current
    val currentVersion = getCurrentVersion(context)
    if (!currentVersion.contains("Beta")) return

    if (SettingsProvider.getValue<Boolean>("beta_alert_show").collectAsState().value) {
        AlertDialog(
            title = {
                Text("This is beta version")
            },
            text = {

                Column() {
                    Text("This version is unstable and is only needed for testing and debugging.\n" +
                            "It contains experimental features that will be added later in the full release.\n" +
                            "The application may freeze or crash.\n" +
                            " We recommend updating when the full version is released.\n" +
                            " Also, log information and crashes are sent to the server for debugging.\n")
                }
            },
            onDismissRequest = {
                SettingsProvider.setValue("beta_alert_show", false)
            },
            confirmButton = {
                TextButton(onClick = {
                    SettingsProvider.setValue("beta_alert_show", false)
                }) {
                    Text(stringResource(R.string.ok))
                }
            }

        )
    }
}