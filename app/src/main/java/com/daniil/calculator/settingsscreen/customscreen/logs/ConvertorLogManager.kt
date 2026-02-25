package com.daniil.calculator.settingsscreen.customscreen.logs

import android.content.Context
import androidx.compose.runtime.Immutable
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object LogManager {
    private val logList = mutableListOf<ConvertorLogData>()
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val SAVE_LOG_FILE = "logs_local.json"
    val Json = Json { ignoreUnknownKeys = true }


    fun getLogData() = logList
    fun clearLogData() {
        logList.clear()
    }
    fun filterType(type: ConvertorLogType?): List<ConvertorLogData> {
        if (type == null) return getLogData()
        return logList.filter { it.type == type }
    }
    fun filterName(name: String): List<ConvertorLogData> {
        return logList.filter { it.name == name }
    }
    suspend fun loadLogs(context: Context) = withContext(Dispatchers.IO) {
        if (!DynamicSettingsManager.getValue("save_logs").toBoolean()) return@withContext
        val file = File(context.filesDir, SAVE_LOG_FILE)
        if (!file.exists()) {
            file.createNewFile()
            return@withContext
        }
        try {
            val json = file.bufferedReader().use { it.readText() }
            val list = Json.decodeFromString<List<ConvertorLogData>>(json)
            logList.clear()
            logList.addAll(list)
        } catch (e: Exception) {
            return@withContext
        }

    }
    suspend fun saveLogs(context: Context) = withContext(Dispatchers.IO) {
        if (!DynamicSettingsManager.getValue("save_logs").toBoolean()) return@withContext
        val file = File(context.filesDir, SAVE_LOG_FILE)
        if (!file.exists()) {
            file.createNewFile()
        }
        try {
            val json = Json.encodeToString(logList)
            file.writeText(json)
        } catch (e: Exception) {

        }

    }

    fun i(
        name: String,
        content: String,
        codeId: String? = null,
        decorator: LogDecorator? = localDefaultLogDecorator
    ) {
        if (!DynamicSettingsManager.getValue("collect_logs").toBoolean()) return
        logList.add(
            ConvertorLogData(
                codeId = codeId,
                name = name,
                content = content,
                type = ConvertorLogType.Info,
                time = formatter.format(LocalTime.now()),
                decorator = decorator
            )
        )
    }
    fun d(
        name: String,
        content: String,
        codeId: String? = null,
        decorator: LogDecorator? = localDefaultLogDecorator
    ) {
        if (!DynamicSettingsManager.getValue("collect_logs").toBoolean()) return

        logList.add(
            ConvertorLogData(
                codeId = codeId,
                name = name,
                content = content,
                type = ConvertorLogType.Debug,
                time = formatter.format(LocalTime.now()),
                decorator = decorator
            )
        )
    }
    fun w(
        name: String,
        content: String,
        codeId: String? = null,
        decorator: LogDecorator? = localDefaultLogDecorator
    ) {
        if (!DynamicSettingsManager.getValue("collect_logs").toBoolean()) return
        logList.add(
            ConvertorLogData(
                codeId = codeId,
                name = name,
                content = content,
                type = ConvertorLogType.Warning,
                time = formatter.format(LocalTime.now()),
                decorator = decorator
            )
        )
    }
    fun e(
        name: String,
        content: String,
        codeId: String? = null,
        decorator: LogDecorator? = localDefaultLogDecorator
    ) {
        if (!DynamicSettingsManager.getValue("collect_logs").toBoolean()) return

        logList.add(
            ConvertorLogData(
                codeId = codeId,
                name = name,
                content = content,
                type = ConvertorLogType.Error,
                time = formatter.format(LocalTime.now()),
                decorator = decorator
            )
        )
    }
    fun t(
        name: String,
        content: String,
        codeId: String? = null,
        decorator: LogDecorator? = localDefaultLogDecorator
    ) {
        if (!DynamicSettingsManager.getValue("collect_logs").toBoolean()) return

        logList.add(
            ConvertorLogData(
                codeId = codeId,
                name = name,
                content = content,
                type = ConvertorLogType.TrashedError,
                time = formatter.format(LocalTime.now()),
                decorator = decorator
            )
        )
    }
    fun c(
        name: String,
        content: String,
        codeId: String? = null,
        decorator: LogDecorator? = localDefaultLogDecorator
    ) {
        if (!DynamicSettingsManager.getValue("collect_logs").toBoolean()) return

        logList.add(
            ConvertorLogData(
                codeId = codeId,
                name = name,
                content = content,
                type = ConvertorLogType.Complete,
                time = formatter.format(LocalTime.now()),
                decorator = decorator
            )
        )
    }
    fun k(
        name: String,
        content: String,
        codeId: String? = null,
        decorator: LogDecorator? = localDefaultLogDecorator
    ) {
        if (!DynamicSettingsManager.getValue("collect_logs").toBoolean()) return

        logList.add(
            ConvertorLogData(
                codeId = codeId,
                name = name,
                content = content,
                type = ConvertorLogType.Key,
                time = formatter.format(LocalTime.now()),
                decorator = decorator
            )
        )
    }
}

private val localDefaultLogDecorator: String? = null


@Immutable
@Serializable
data class ConvertorLogData(
    val codeId: String?,
    val name: String,
    val content: String,
    val type: ConvertorLogType,
    val time: String,
    val decorator: String?,
)

typealias LogDecorator = String

@Serializable
enum class ConvertorLogType {
    Info,
    Debug,
    Error,
    Warning,
    TrashedError,
    Complete,
    Key
}