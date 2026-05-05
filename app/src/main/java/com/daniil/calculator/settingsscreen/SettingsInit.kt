package com.daniil.calculator.settingsscreen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.daniil.calculator.R
import com.daniil.calculator.core.DaniilServerAPI
import com.daniil.calculator.core.RetrofitDaniilServerInstance
import com.daniil.calculator.core.UserDataManager
import com.daniil.calculator.currentVersionCode
import com.daniil.calculator.settingsscreen.classes.createLink
import com.daniil.calculator.getCurrentVersion
import com.daniil.calculator.globalVersion
import com.daniil.calculator.settingsscreen.customscreen.AboutAppScreen
import com.daniil.calculator.settingsscreen.customscreen.ChangeLogScreen
import com.daniil.calculator.settingsscreen.customscreen.logs.LogManager
import com.daniil.calculator.settingsscreen.customscreen.logs.LogsScreen
import com.daniil.calculator.settingsscreen.itemtype.customitem.AboutDeveloper
import com.daniil.calculator.settingsscreen.itemtype.customitem.EsterEgg
import com.daniil.calculator.settingsscreen.itemtype.customitem.NewVersion
import com.daniil.calculator.ui.theme.appLogoBackground
import com.daniil.csb.SettingsNavigationModel
import com.daniil.csb.SettingsProvider
import com.daniil.csb.classes.Select
import com.daniil.csb.classes.createAction
import com.daniil.csb.classes.createColorPicker
import com.daniil.csb.classes.createCustomSetting
import com.daniil.csb.classes.createRedirect
import com.daniil.csb.classes.createSelect
import com.daniil.csb.classes.createStringData
import com.daniil.csb.classes.createSwitch
import com.daniil.csb.screens.createAbstractScreen
import com.daniil.csb.screens.createCustomScreen
import com.daniil.csb.screens.createScreen
import com.daniil.csb.settingui.DefaultSettingUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun settingsInit(
    settingsNavigationModel: SettingsNavigationModel,
    context: Context,
    coroutineScope: CoroutineScope
) {
    settingsNavigationModel.initialize(context)

    val mainScreen = createScreen("Main") {
        newGroup(
            id = "General", name = "General",
            createSelect("convertor_list_view") {
                title = "Convertor list view"
                description = "Select the display type of the convector list"
                val options = listOf(
                    Select.Option(id = "grid", title = "Grid"),
                    Select.Option(id = "column", title = "Column"),
                )
                defaultValue = options[0]
                this.options = options
            },
            createRedirect("experimental_features_redirect") {
                title = "Experimental feathers"
                description = "Unstable and unfinished feasters"
                redirectToId = "Experimental_feathers_screen"
                navigationModel = settingsNavigationModel
            },
            createSelect("language") {
                title = "App language"
                description = "Change app language"
                val options = listOf(
                    Select.Option(id = "System", title = "System"),
                    Select.Option(id = "en", title = "English"),
                    Select.Option(id = "uk", title = "Ukrainian"),
                )
                defaultValue = options[0]
                this.options = options
            },
        )
        newGroup(
            id = "theme", name = "Theme",
            createSelect("theme_mode") {
                title = "Theme mode"
                description = "Enable dark or light theme"
                val options = listOf(
                    Select.Option(id = "System", title = "System"),
                    Select.Option(id = "Light", title = "Light"),
                    Select.Option(id = "Dark", title = "Dark"),
                )
                defaultValue = options[0]
                this.options = options
            },
            createSwitch("custom_color_scheme") {
                title = "Custom color"
                description = "Enable custom theme color"
                defaultValue = false
            },
            createColorPicker("color_accent") {
                title = "Color accent"
                description = "Pick a main theme color"
                defaultValue = Color.Blue
            },

            )
        newGroup(
            id = "calculator",
            name = "Calculator",
            createSwitch("save_history") {
                title = "Save history"
                description = "Save history of calculator operation"
                defaultValue = true
            },
            createSwitch("button_vibration_enable") {
                title = "Button vibration"
                description = "Enable button vibration when pressed"
                defaultValue = true
            },
        )
        newGroup(
            id = "about", name = "About",
            createRedirect("about_redirect") {
                title = "About"
                description = "About app and contacts"

                redirectToId = "About_screen"
                navigationModel = settingsNavigationModel
            },
        )
    }
    val experimentalScreen = createScreen("Experimental_feathers_screen") {
        title = "Experimental features"
        newGroup(
            createSwitch("experimental_convertor_buttons") {
                title = "Experimental converters"
                description = "Show experimental converters"
                defaultValue = false
            },
            createSwitch("unavailable_convertor_buttons") {
                title = "Unavailable converters"
                description = "Show unavailable converters"
                defaultValue = true
            },
            createCustomSetting<String>("background_image") {
                title = "Background image"
                description = "Set image as background"
                defaultValue = ""
                fun saveImageFile(file: File, bytes: ByteArray): Boolean {
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
                content = {

                    val imageFile = remember { File(context.filesDir, "background_image.jpeg") }
                    val pickImage = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            val bytes =
                                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            if (bytes != null) {
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                val stream = ByteArrayOutputStream()
                                bitmap?.compress(Bitmap.CompressFormat.JPEG, 40, stream)
                                val compressedBytes = stream.toByteArray()
                                SettingsProvider.setValue("background_image_enable", false)
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (saveImageFile(imageFile, compressedBytes)) {
                                        delay(200)
                                        SettingsProvider.setValue("background_image_enable", true)
                                    }
                                }
                                return@rememberLauncherForActivityResult
                            }
                        }
                        SettingsProvider.setValue("background_image_enable", false)
                    }
                    val imageEnable by SettingsProvider.getValue<Boolean>("background_image_enable")
                        .collectAsState()
                    DefaultSettingUI(
                        title = { Text(title) },
                        icon = { },
                        description = { Text(description) },
                        display = {
                            FilledIconButton(
                                onClick = { pickImage.launch("image/*") },
                                colors = IconButtonDefaults.iconButtonColors()
                                    .copy(
                                        containerColor = if (!imageEnable) MaterialTheme.colorScheme.surfaceContainerHigh
                                        else MaterialTheme.colorScheme.primary
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.files_icon),
                                    contentDescription = "Files"
                                )
                            }
                        },
                        onClick = { pickImage.launch("image/*") }
                    )

                }
            },
        )
    }
    val aboutScreen = createCustomScreen("About_screen") {
        title = "About"
        @Composable
        fun linkIcon(resourceId: Int) {
            Box(
                modifier = Modifier.width(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(36.dp),
                    painter = painterResource(resourceId),
                    contentDescription = "Link icon"
                )
            }
        }
        register(
            createCustomSetting<Unit>("new_version") {
                defaultValue = Unit
                content = {
                    NewVersion()
                }
                onClick = {

                    val simulateVersion = SettingsProvider.getValue<String>("imitate_version").value
                    val currentVersionCode = simulateVersion.toIntOrNull() ?: currentVersionCode
                    if ((globalVersion?.versionCode ?: 0) > currentVersionCode) {
                        val useTestServer =
                            SettingsProvider.getValue<Boolean>("use_test_server").value
                        val url = when {
                            useTestServer -> RetrofitDaniilServerInstance.TEST_URL
                            else -> RetrofitDaniilServerInstance.BASE_URL
                        }
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            globalVersion?.uri?.ifEmpty { "$url/available-version/${globalVersion?.versionName}" }
                                ?.toUri()
                        )
                        context.startActivity(intent)
                    }
                }

            },
            createRedirect("app_version") {
                title = "App version"
                description = getCurrentVersion(context)
                redirectToId = "changeLog_screen"
                navigationModel = settingsNavigationModel
            },
            createCustomSetting<Boolean>("about_developer") {

                val clickCount = mutableIntStateOf(0)
                defaultValue = false
                content = {
                    AboutDeveloper(clickCount.intValue)
                }
                onClick = {
                    val isDeveloperEnable =
                        SettingsProvider.getValue<Boolean>("developer_enable").value
                    if (!isDeveloperEnable) {
                        clickCount.intValue += 1
                        if (clickCount.intValue > 4) {
                            SettingsProvider.setValue("developer_enable", true)
                            Toast.makeText(context, "Developer mode is enabled", Toast.LENGTH_SHORT)
                                .show()
                            clickCount.intValue = 0
                            settingsNavigationModel.goToScreen("developer_screen")
                        }
                    } else {
                        settingsNavigationModel.goToScreen("developer_screen")
                    }
                }
            },
            createLink("server_contact") {
                title = "Server"
                description = "My server and version catalog"
                link = "http://31.134.109.18:70"
                painter = { linkIcon(R.drawable.server_icon) }
            },
            createLink("tg_contact") {
                title = "Telegram"
                link = "https://t.me/calculator_release"
                description = "Telegram chanel"
                painter = { linkIcon(R.drawable.telegram) }
            },
            createLink("github_contact") {
                title = "GitHub"
                description = "My GitHub open source project"
                link = "https://github.com/daniimazh-dev/Calculator"
                painter = { linkIcon(R.drawable.github) }
            },
        )

        modifier = Modifier

        content = {
            AboutAppScreen(settingsNavigationModel = settingsNavigationModel)
        }
    }
    val changelogScreen = createCustomScreen("changeLog_screen") {
        title = "ChangeLog"
        content = {
            ChangeLogScreen(navigationScreenModel = settingsNavigationModel)
        }
    }
    val developerScreen = createScreen("developer_screen") {
        title = "Developer menu"
        newGroup(createSwitch("developer_enable") {
            defaultValue = false
            title = "Developer mode"
            description = "Enable developer mode"
        })
        newGroup(
            "debag", "Debag",
            createRedirect("logs_redirect") {
                title = "Logs"
                description = "All application logs"
                redirectToId = "logs_screen"
                navigationModel = settingsNavigationModel
            },
            createSwitch("collect_logs") {
                title = "Collect logs"
                description = "Collect application logs"
                defaultValue = true
            },
            createSwitch("save_logs") {
                title = "Save logs"
                description = "Save logs to memory"
                defaultValue = false
            }
        )
        newGroup(
            "server", "Server",
            createSwitch("locale_mode") {
                title = "Local mode"
                description = "Disconnect any connection to the server"
                defaultValue = false
            },
            createSwitch("use_test_server") {
                title = "Use test server"
                description = "Switch connection to test server"
                defaultValue = false
            },
            createAction("ping_test") {
                val server = DaniilServerAPI()
                title = "Ping test"
                description = "Test connection to server"
                action = {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val result = server.health()
                            val massage = when (result.code()) {
                                200 -> "Successful"
                                404 -> "Not found 404"
                                else -> "Unknown code: ${result.code()}"
                            }
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, massage, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "Error, more detail in log", Toast.LENGTH_SHORT).show()
                            }
                            LogManager.e("Ping test", "Error ping test:\n${e.stackTraceToString().take(200)}")
                        }

                    }

                }
            },
            createCustomSetting<Unit>("current_token") {
                defaultValue = Unit
                title = "Token"
                onClick = {
                    UserDataManager.token.value?.let {
                        coroutineScope.launch {
                            val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val data = ClipData.newPlainText("token", it)
                            clipboardManager.setPrimaryClip(data)
                        }
                    }
                }
                content = {
                    DefaultSettingUI(
                        title = { Text(title) },
                        description = { Text(UserDataManager.token.collectAsState().value.orEmpty()) },
                        display = {
                            FilledIconButton(
                                colors = IconButtonDefaults.iconButtonColors()
                                    .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                onClick = onClick
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.copy_standart),
                                    contentDescription = "Copy"
                                )
                            }
                        },
                        onClick = onClick
                    )
                }
            },
            createAction("drop_token") {
                title = "Reset token"
                description = "Reset device ID"
                alertTitle = "Reset device ID"
                requestAlert = true
                action = {
                    if (it) UserDataManager.dropToken(context)
                }
            }
        )

        newGroup(
            "version", "Version",
            createSwitch("version_request") {
                title = "Disable version request"
                description = "Disable request version to server"
                defaultValue = true
            },
            createStringData("imitate_version") {
                title = "Imitate app version"
                description = "Imitate version code"
            },
        )

    }
    val easterEgg = createCustomScreen("ester_egg_screen") {
        title = "Calculator"

        modifier = Modifier.background(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    0.1f to appLogoBackground,
                    0.6f to Color.Transparent
                )
            )
        )
        content = {
            Column(
                modifier = Modifier
            ) {
                ScreenTopBar(navigationModel = settingsNavigationModel)
                EsterEgg()
            }
        }
    }
    val logsScreen = createCustomScreen("logs_screen") {
        title = "Logs"
        content = {
            LogsScreen(navigationModel = settingsNavigationModel)
        }
    }
    val abstractSettings = createAbstractScreen(
        "AbstractScreen",
        createSwitch("background_image_enable") { defaultValue = false },
        createSwitch("beta_alert_show") { defaultValue = true },
        createSwitch("first_open_app") { defaultValue = true },
        createSwitch("first_add_history") { defaultValue = true }
    )

    settingsNavigationModel.setScreensHeap(
        mainScreen,
        experimentalScreen,
        aboutScreen,
        changelogScreen,
        easterEgg,
        developerScreen,
        logsScreen,
        abstractSettings
    )

    coroutineScope.launch(Dispatchers.IO) {
        SettingsProvider.loadData(context)
    }

}

