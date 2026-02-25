package com.daniil.calculator.settingsscreen.settings.manager

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.daniil.calculator.getCurrentVersion
import com.daniil.calculator.settingsscreen.defaultitem.DefaultCodeBlockSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultSwitchSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultColorPickerSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultCustomScreenSetting
import com.daniil.calculator.settingsscreen.defaultitem.DefaultInfoSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultListSelectSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultSliderSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultStringDataSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultTextSettingItem
import com.daniil.calculator.settingsscreen.defaultitem.DefaultTitleSettingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileNotFoundException
import java.io.File


enum class SettingType {
    Switch,
    CodeBlock,
    StringData,
    ListSelect,
    Slider,
    ColorPicker,
    Title,
    Text,
    Info,
    CustomScreen,
    Unknown;

    companion object {
        fun fromString(value: String): SettingType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: Unknown
        }
    }
}

@Serializable
data class Parameter(
    val id: String,
    val name: String,
    val positionCode: Int,
)

@Serializable
data class DynamicSetting(
    val id: String, // unique identifier, access to the element
    val title: String,
    val description: String = "",
    val group: String? = null,
    val type: String, // Typed for displaying the element
    var enabled: Boolean = true, // Is the setting enabled, as a button
    var hide: Boolean = false,
    val defaultValue: String? = null, // Default value for reset
    var value: String? = null, // Current setting value
    val static: Boolean? = false, // Will the value be stored. Optional
    val parameters: List<Parameter>? = null, // Settings for settings with selection
    val icon: String? = null,  // Id icon. Optional
)

private fun DynamicSetting.toSaveSetting(): SaveSetting {
    val save = SaveSetting(
        id = this.id,
        value = this.value,
        enabled = this.enabled,
        hide = this.hide
    )
    return save
}

@Serializable
private data class SaveSetting(
    val id: String,
    val value: String?,
    val enabled: Boolean,
    val hide: Boolean = false,
)
/*
UI use:
get():
    val settingTrigger = DynamicSettingsManager.trigger("setting.id").value
    val value = remember (settingTrigger) { mutableStateOf(DynamicSettingsManager.getValue("dark_mode") as? String ?: "null") }
    Text(value.value)
set():
    DynamicSettingsManager.setValue("setting.id", "New value")

*/



object DynamicSettingsManager {
    private lateinit var appContext: Context
    private val settings: MutableList<DynamicSetting> = mutableListOf()

    val triggers = mutableMapOf<String, MutableState<Boolean>>()
    var variables = mapOf<String, Any>()



    private const val SETTINGS_FILE_PREFIX = "settings/settings"
    private const val SETTINGS_FILE_SAVE = "settings_locale.json"
    private fun getLocaleFile(): String {
        val localeSuffix = appContext.resources.configuration.locales[0].language
        var fileName = "${SETTINGS_FILE_PREFIX}_$localeSuffix.json"
        try {
            appContext.assets.open(fileName)
        } catch (e: FileNotFoundException) {
            fileName = "${SETTINGS_FILE_PREFIX}_en.json"
        }
        return fileName
    }

    private val jsonParser = Json {
        ignoreUnknownKeys = true
    }

    fun getAll(group: String? = null): List<DynamicSetting> = settings.filter { it.group == group }

    fun getSetting(id: String): DynamicSetting? =
        settings.find { it.id == id }

    fun enabledSetting(
        id: String,
        enabled: Boolean,
        enabledTrigger: Boolean = true,
    ) {
        getSetting(id)?.enabled = enabled
        if (enabledTrigger) launchTrigger(id)

    }

    fun hideSetting(
        id: String,
        hide: Boolean,
        enabledTrigger: Boolean = true,
    ) {
        getSetting(id)?.hide = hide
        if (enabledTrigger) launchTrigger(id)

    }

    fun getValue(id: String): String? = getSetting(id)?.value

    @Composable
    fun getValueState(id: String): State<String?> {
        return remember(getSetting(id)?.value, trigger(id).value) {
            mutableStateOf(getSetting(id)?.value)
        }
    }


    fun setValue(id: String, newValue: Any?) {
        val index = settings.indexOfFirst { it.id == id }
        if (index != -1) {
            settings[index] = settings[index].copy(value = newValue.toString())
            launchTrigger(id)
        }
    }

    private fun trigger(id: String): MutableState<Boolean> =
        triggers.getOrPut(id) { mutableStateOf(false) }

    fun launchTrigger(id: String) {
        trigger(id)
        triggers[id]?.value?.let { triggers[id]?.value = !it }
    }

    fun resetToDefault(id: String) {
        getSetting(id)?.let { setting ->
            if (setting.defaultValue != null) {
                setting.value = setting.defaultValue
            }
            launchTrigger(id)
        }
    }

    fun resetAllToDefaults() {
        settings.forEach { setting ->
            resetToDefault(setting.id)
        }

    }

    suspend fun loadSettings(context: Context, values: Map<String, Any>) =
        withContext(Dispatchers.IO) {
            appContext = context.applicationContext
            variables = values
            val templateJson =
                appContext.assets.open(getLocaleFile()).bufferedReader().use { it.readText() }
            val templateSettings: List<DynamicSetting> =
                jsonParser.decodeFromString(templateJson)

            val file = File(appContext.filesDir, SETTINGS_FILE_SAVE)

            if (!file.exists()) {
                file.writeText(jsonParser.encodeToString(templateSettings))
                settings.clear()
                settings.addAll(templateSettings)
                return@withContext
            }

            try {
                val savedJson = file.readText()
                val savedSettings: List<SaveSetting> =
                    jsonParser.decodeFromString(savedJson)
                val savedMap = savedSettings.associateBy { it.id }

                val merged = templateSettings.map { template ->
                    val saved = savedMap[template.id]
                    template.copy(
                        title = insertValue(template.title),
                        description = insertValue(template.description),
                        value = saved?.value ?: template.defaultValue ?: template.value,
                        enabled = saved?.enabled ?: template.enabled,
                        hide = saved?.hide ?: false
                    )
                }

                settings.clear()
                settings.addAll(merged)
            } catch (e: Exception) {
                file.writeText(jsonParser.encodeToString(templateSettings))
                settings.clear()
                settings.addAll(templateSettings)
            }
        }

    suspend fun saveSettings() = withContext(Dispatchers.IO) {
        val file = File(appContext.filesDir, SETTINGS_FILE_SAVE)
        val toSave = settings
            .filter { it.static == null || !it.static }
            .map { it.toSaveSetting() }
        file.writeText(jsonParser.encodeToString(toSave))
    }

    private fun insertValue(str: String): String {
        if (!str.contains('{')) return str
        var result = str
        var isOpen = false
        var varName = ""
        for (c in str) {
            if (c == '}') {
                isOpen = false
                if (varName.isBlank()) continue
                val new = variables[varName] ?: continue
                result = result.replace("{$varName}", new.toString())
                varName = ""
            }
            if (isOpen) {
                varName += c
            }
            if (c == '{') {
                isOpen = true
            }

        }
        return result
    }
}


private typealias DynamicSettingRenderer = @Composable (DynamicSetting) -> Unit


object DynamicSettingRenderManager {

    private val customTypeRenderers = mutableMapOf<String, DynamicSettingRenderer>()
    private val customIdRenderers = mutableMapOf<String, DynamicSettingRenderer>()

    val groupIcon: MutableMap<String, Int> = mutableMapOf()


    fun setIcon(iconName: String, painter: Int) {
        groupIcon.put(iconName, painter)
    }

    fun getIcon(iconMame: String?): Int? {
        iconMame ?: return null
        return groupIcon[iconMame]
    }

    fun setIconMap(iconMap: Map<String, Int>) {
        groupIcon.clear()
        groupIcon.putAll(iconMap)
    }

    fun clearCustomRenders() {
        customTypeRenderers.clear()
        customIdRenderers.clear()
    }

    fun registerRendererByType(type: String, renderer: DynamicSettingRenderer) {
        if (customTypeRenderers[type] == null) {
            customTypeRenderers.put(type, renderer)
        }
    }

    fun registerRendererByType(type: SettingType, renderer: DynamicSettingRenderer) {
        if (customTypeRenderers[type.name] == null) {
            customTypeRenderers.put(type.name, renderer)
        }
    }

    fun getRendererByType(type: String): DynamicSettingRenderer {
        return customTypeRenderers[type] ?: defaultRenderers[SettingType.fromString(type)]
        ?: { setting ->
            Text("Unknown setting type: ${setting.type}")
            null
        }
    }

    fun getRendererByType(type: SettingType): DynamicSettingRenderer {
        return defaultRenderers[type]
            ?: { setting ->
                Text("Unknown setting type: ${setting.type}")
                null
            }
    }

    fun registerRendererById(id: String, renderer: DynamicSettingRenderer) {
        if (customIdRenderers[id] == null) {
            customIdRenderers[id] = renderer
        }
    }

    fun getRendererById(id: String): DynamicSettingRenderer? {
        if (DynamicSettingsManager.getSetting(id)?.hide == true) return null
        return customIdRenderers[id]
    }

    @Composable
    fun RendererSetting(setting: DynamicSetting) {
        if (setting.hide) return
        val renderer = getRendererById(setting.id) ?: getRendererByType(setting.type)
        renderer(setting)
    }

    private val defaultRenderers: Map<SettingType, DynamicSettingRenderer> = mapOf(
        SettingType.Switch to @Composable { setting ->
            DefaultSwitchSettingItem(setting)
        },
        SettingType.Slider to @Composable { setting ->
            DefaultSliderSettingItem(setting)
        },
        SettingType.ColorPicker to @Composable { setting ->
            DefaultColorPickerSettingItem(setting)
        },
        SettingType.CustomScreen to @Composable { setting ->
            DefaultCustomScreenSetting(setting) {
                val render = getRendererById(setting.value.toString())
            }
        },
        SettingType.ListSelect to @Composable { setting ->
            DefaultListSelectSettingItem(setting)

        },
        SettingType.StringData to @Composable { setting ->
            DefaultStringDataSettingItem(setting)
        },
        SettingType.Text to @Composable { setting ->
            DefaultTextSettingItem(setting)

        },
        SettingType.Title to @Composable { setting ->
            DefaultTitleSettingItem(setting)

        },
        SettingType.Info to @Composable { setting ->
            DefaultInfoSettingItem(setting)
        },
        SettingType.CodeBlock to @Composable { setting ->
            DefaultCodeBlockSettingItem(setting, enableAlert = true) {

            }
        },
    )
}

