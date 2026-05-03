package com.daniil.calculator.settingsscreen.classes

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.daniil.csb.R
import com.daniil.csb.SaveSettingPackage
import com.daniil.csb.classes.ComposeSetting
import com.daniil.csb.classes.utils.ItemGroupPosition
import com.daniil.csb.screens.ScreenInstance
import com.daniil.csb.settingui.DefaultSettingUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Link(
    id: String,
    override val title: String,
    val link: String,
    val painter: (@Composable () -> Unit)?,
    override val description: String,
    enabled: Boolean = true,
    override var isSaveSetting: Boolean = false
) : ComposeSetting<String>() {
    private var _value = MutableStateFlow<String>(this@Link.link)
    override val value = _value.asStateFlow()

    private var _enable = MutableStateFlow(enabled)
    override val enabled = _enable.asStateFlow()

    override fun enabled(state: Boolean) {
        _enable.value = state
    }

    override fun changeValue(newValue: String) {
        _value.value = newValue
    }

    override fun resetToDefault() {}
    override fun saveLogic(): SaveSettingPackage = SaveSettingPackage.UnitPackage(id, enabled.value)
    override fun loadLogic(pack: SaveSettingPackage?) {
        if (pack == null) return
        enabled(pack.enable)
    }
    override var id: String = id

    class LinkBuilderScope() {
        var link: String? = null
        var painter: (@Composable () -> Unit)? = null
        var title = "Link"
        var description = ""
        var enabled = true
        var isSaveSetting = false
    }

    class Builder(
        val id: String,
        builderScope: LinkBuilderScope.() -> Unit = {}
    ) {
        val scope = LinkBuilderScope().apply(builderScope)
        fun create(): Link = with(scope) {
            return Link(id, title, link!!, painter, description, enabled, isSaveSetting)
        }
    }

    override val focusState = MutableStateFlow(false)

    @Composable
    override fun UI(screen: ScreenInstance, position: ItemGroupPosition) {
        val context = LocalContext.current
        val focusState by this.focusState.collectAsState()
        val enabled by this.enabled.collectAsState()
//        val value by this.value.collectAsState()
        var isAlertOpen by retain { mutableStateOf(false) }

        DefaultSettingUI(
            modifier = Modifier,
            focusState = focusState,
            itemGroupPosition = position,
            enabled = enabled,
            title = { if (!title.isBlank()) Text(title) },
            description = { if (!description.isBlank()) Text(description) },
            icon = ::painter.invoke(),
            display = {
                FilledIconButton(
                    enabled = enabled,
                    colors = IconButtonDefaults.iconButtonColors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    onClick = {
                        isAlertOpen = true
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_forward_icon),
                        contentDescription = "Follow the link"
                    )
                }
            },
            onClick = {
                isAlertOpen = true
            }
        )
        if (isAlertOpen) {
            AlertDialog(
                icon = {
                    painter?.invoke()
                },
                title = { Text(stringResource(com.daniil.calculator.R.string.follow_link), fontSize = 22.sp) },
                text = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = description,
                        textAlign = TextAlign.Center
                    )
                },
                onDismissRequest = {
                    isAlertOpen = false
                },
                dismissButton = {
                    TextButton(onClick = {
                        isAlertOpen = false
                    }) {
                        Text("Cancel")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, this@Link.link.toUri())
                        context.startActivity(intent)
                        isAlertOpen = false
                    }) {
                        Text("OK")
                    }
                },
            )
        }
    }
}

fun createLink(
    id: String,
    builder: Link.LinkBuilderScope.() -> Unit
): Link {
    return Link.Builder(id, builder).create()
}