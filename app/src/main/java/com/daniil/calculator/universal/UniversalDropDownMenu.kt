package com.daniil.calculator.universal

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.csb.SettingsProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun UniversalDropDownMenu(
    expanded: Boolean = true,
    buttonList: List<UniversalDropDownItem>,
    enabled: Boolean = true,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

    val vibrationEnabled by SettingsProvider.getValue<Boolean>("button_vibration_enable").collectAsState()
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val effect = VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)

    DropdownMenu(
        expanded = if (enabled) expanded else false,
        onDismissRequest = {
            onDismissRequest()
        },
        shape = MaterialTheme.shapes.medium
    ) {
        buttonList.filterNot { it.title.isEmpty() }.forEach { item ->
            var isClicked by remember { mutableStateOf(false) }

            val animateButton by animateFloatAsState(
                if (isClicked) 0.15f else 0f
            )
            LaunchedEffect(isClicked) {
                if (isClicked) {
                    delay(150)
                    isClicked = false
                    if (item.autoClose) onDismissRequest()
                }
            }

            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 32.dp)
                    .clickable(
                        onClick = {
                            if (item.enabled) {
                                isClicked = true
                                if (vibrationEnabled) vibrator.vibrate(effect)
                                item.onClick()
                            }
                        }
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .graphicsLayer(
                            scaleX = 1f - animateButton,
                            scaleY = 1f - animateButton,
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.iconResource != null) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = ImageVector.vectorResource(item.iconResource),
                            contentDescription = "icon"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (item.icon != null) {
                        item.icon()
                        Spacer(modifier = Modifier.width(6.dp))

                    }

                    Text(
                        modifier = Modifier.weight(1f),
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

            }
        }


    }
}


@Composable
fun Copy(
    content: Any,
    label: String = "Copy lable",
): UniversalDropDownItem {
    val clipboardManager = LocalClipboard.current
    val coroutine = rememberCoroutineScope()
    return UniversalDropDownItem(
        title = stringResource(R.string.copy),
        iconResource = R.drawable.copy_standart,
        onClick = {
            coroutine.launch {
                clipboardManager.setClipEntry(
                    ClipEntry(
                        ClipData.newPlainText(
                            label,
                            content.toString(),
                        )
                    )
                )
            }
        }
    )
}




@Composable
fun Paste(
    onPaste: (String?) -> Unit,
): UniversalDropDownItem {
    val clipboardManager = LocalClipboard.current
    val coroutine = rememberCoroutineScope()
    return UniversalDropDownItem(
        title = stringResource(R.string.paste),
        iconResource = R.drawable.paste_standart,
        onClick = {
            coroutine.launch {
                val data = clipboardManager.getClipEntry()?.clipData?.getItemAt(0)?.text
                onPaste(data?.toString())
            }
        }
    )
}

operator fun UniversalDropDownItem.plus(item: UniversalDropDownItem): List<UniversalDropDownItem> {
    return listOf(this, item)
}

operator fun Iterable<UniversalDropDownItem>.plus(item: UniversalDropDownItem): List<UniversalDropDownItem> {
    return  this + listOf(item)

}


data class UniversalDropDownItem(
    val title: String,
    val iconResource: Int? = null,
    val icon: (@Composable () -> Unit)? = null,
    val enabled: Boolean = true,
    val autoClose: Boolean = true,
    val onClick: () -> Unit,
) {
    companion object {
        val None = UniversalDropDownItem(
            title = "",
            onClick = {}
        )
    }
}