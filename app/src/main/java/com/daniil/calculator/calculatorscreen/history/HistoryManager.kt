package com.daniil.calculator.calculatorscreen.history

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime

class HistoryManager {
    private val history = mutableStateListOf<HistoryData>()

    fun getHistory(): List<HistoryData> = history
    fun addHistory(historyData: HistoryData) {
        if (historyData.result == "Error") return
        history.add(historyData)
    }

    fun addHistory(content: String, result: String) {
        if (result == "Error") return
        history.add(
            HistoryData(
                content = content,
                result = result,
                time = LocalDateTime.now()
            )
        )
    }

    fun removeHistory(item: HistoryData) {
        history.remove(item)
    }

    fun pinnedItem(
        item: HistoryData
    ) {
        val index = history.indexOf(item)
        val item = history[index]
        history[index] = item.copy(pinned = !item.pinned)
    }

    fun addComment(
        item: HistoryData,
        comment: String?
    ) {
        val index = history.indexOf(item)
        val item = history[index]
        history[index] = item.copy(comment = comment)
    }

    fun clearHistory() {
        history.clear()
    }


    private fun getFile(fileName: String, context: Context): File =
        File(context.filesDir, fileName)

    suspend fun replaceContent(fileName: String, content: String, context: Context) =
        withContext(Dispatchers.IO) {
            getFile(fileName, context).writeText(content)
        }

    suspend fun readFile(fileName: String, context: Context): List<String>? =
        withContext(Dispatchers.IO) {
            val file = getFile(fileName, context)
            if (file.exists()) file.readLines() else null
        }

    suspend fun createFile(fileName: String, context: Context) = withContext(Dispatchers.IO) {
        val file = getFile(fileName, context)
        if (!file.exists()) file.createNewFile()
    }


    suspend fun load(context: Context) {
        readFile("history.json", context)?.let { json ->
            if (!json.isNotEmpty()) return
            val loadHistory = Json.decodeFromString<List<HistoryData>>(json.joinToString("\n"))
            history.clear()
            history.addAll(loadHistory)
        } ?: run {
            createFile("history.json", context)
        }

    }

    suspend fun save(context: Context) {
        val json = Json.encodeToString<List<HistoryData>>(history.toList())
        replaceContent("history.json", json, context)
    }
}