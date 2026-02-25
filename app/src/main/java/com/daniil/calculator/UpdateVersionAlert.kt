package com.daniil.calculator

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.daniil.calculator.core.DaniilServerAPI
import com.daniil.calculator.core.RetrofitDaniilServerInstance
import com.daniil.calculator.core.VersionRequest
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager


@Composable
fun UpdateVersionAlert() {
    val skipVersionRequest = DynamicSettingsManager.getValue("version_request").toBoolean()
    if (skipVersionRequest) return

    val context = LocalContext.current
    val server = DaniilServerAPI()
    var showAlert by remember { mutableStateOf(false) }

    val currentVersion = getCurrentVersion(context)
    val simulateVersion = DynamicSettingsManager.getValue("imitate_version")?.toIntOrNull()
    var globalVersion by remember { mutableStateOf<VersionRequest?>(null) }

    val currentVersionCode = remember {
        simulateVersion?.let {
            val label = "Version imitation is used: simulate: $it, current: $currentVersionCode"
            Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
            simulateVersion
        } ?: currentVersionCode
    }

    LaunchedEffect(Unit) {
        try {
            val response = server.getLastVersion()
            if (response.isSuccessful) {
                globalVersion = response.body()
                com.daniil.calculator.globalVersion =  globalVersion
                showAlert = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    val _globalVersion = globalVersion ?: return
    if (_globalVersion.versionCode > currentVersionCode && showAlert) {
        AlertDialog(
            title = {
                Text(stringResource(R.string.new_version) + " " +  _globalVersion.versionName)
            },
            text = {
                Column() {
                    Text(_globalVersion.whatsNew)
                }
            },
            onDismissRequest = {
                showAlert = false
            },
            dismissButton = {
                TextButton(onClick = {
                    showAlert = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val useTestServer = DynamicSettingsManager.getValue("use_test_server").toBoolean()
                    val url = when {
                        useTestServer -> RetrofitDaniilServerInstance.TEST_URL
                        else -> RetrofitDaniilServerInstance.BASE_URL
                    }
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        _globalVersion.uri.ifEmpty { "$url/available-version/${_globalVersion.versionName}" }.toUri()
                    )
                    context.startActivity(intent)
                    showAlert = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            }

        )
    }

}


fun getCurrentVersion(context: Context): String {
    val packageName = context.packageName
    val version = try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0L)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName ?: "Unknown"
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        "Unknown"
    }
    return version
}