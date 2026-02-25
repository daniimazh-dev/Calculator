package com.daniil.calculator.settingsscreen.itemtype.customitem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.settingsscreen.defaultitem.settingContainer
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

private fun readImageFile(file: File): ByteArray? {
    if (!file.exists()) return null
    return try {
        file.readBytes()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun saveImageFile(file: File, bytes: ByteArray): Boolean {
    return try {
        FileOutputStream(file).use { stream ->
            stream.write(bytes)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


@Composable
fun ImagePicker(setting: DynamicSetting) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val imageFile = remember { File(context.filesDir, "background_image.jpeg") }
    var checked by remember { mutableStateOf(setting.value.toBoolean()) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: return@let
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 40, stream)
            val compressedBytes = stream.toByteArray()

            coroutine.launch(Dispatchers.IO) {
                if (saveImageFile(imageFile, compressedBytes)) {
                    DynamicSettingsManager.launchTrigger(setting.id)
                    DynamicSettingsManager.setValue(setting.id, true)
                }
            }
        }

    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .settingContainer(setting.enabled) {
                checked = !checked
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
        Switch(
            modifier = Modifier,
            checked = checked,
            enabled = setting.enabled,
            onCheckedChange = {
                checked = it
                if (it) {
                    pickImage.launch("image/*")
                }
                DynamicSettingsManager.setValue(setting.id, it)

            }
        )

    }
}
