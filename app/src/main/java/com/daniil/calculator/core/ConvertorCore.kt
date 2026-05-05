package com.daniil.calculator.core

import android.content.Context
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnitJson
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnitJson.Companion.fromJsonType
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorReleseState
import com.daniil.calculator.settingsscreen.customscreen.logs.LogManager
import com.daniil.csb.SettingsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ConvertorCore(
    val model: ConvertorScreenModel,
    val locale: String = "en",
) {
    private var units: MutableMap<String, List<ConvertorUnitJson>> = mutableMapOf()
    private var convertors: MutableMap<String, List<ConvertorData>> = mutableMapOf()
    private val UNITS_FILE_ASSET= "units/units_$locale.json"
    private val UNITS_FILE_SAVE = "units_local.json"
    private val CONVERTORS_FILE_ASSET = "convertors/convertors_$locale.json"
    private val CONVERTORS_FILE_SAVE = "convertors_local.json"

    private lateinit var appContext: Context

    private val jsonParser = Json {
        ignoreUnknownKeys = true
    }


    suspend fun load(
        context: Context
    ) = withContext(Dispatchers.IO) {
        appContext = context.applicationContext

        LogManager.i("ConvertorCore started", content = "ConvertorCore fun \"load\" is started")
        LogManager.i("ConvertorCore started", content = "ConvertorCore fun \"loadConvertorsData\" is started")
        loadConvertorsData(context)
        LogManager.i("ConvertorCore started", content = "ConvertorCore fun \"loadUnitData\" is started")
        loadUnitData(context)
        LogManager.c("ConvertorCore all complete", content = "Load ALL data COMPLETE.\nLoad COMPLETE -> RETURN: COMPLETE")
        val localMode = SettingsProvider.getValue<Boolean>("locale_mode").value
        val useTestServer = SettingsProvider.getValue<Boolean>("use_test_server").value
        LogManager.d("Local mode", content = localMode.toString())
        LogManager.d("Use test server", content = useTestServer.toString())
    }


    suspend fun checkConvertorsRelease() = withContext(Dispatchers.IO) {
        val convertorCopy = convertors
        try {
            val convertorsStatus = DaniilServerAPI().getConvertorsStatus()
            if (!convertorsStatus.isSuccessful) return@withContext
            convertors = convertorCopy.mapValues { (key, value) ->
                value.map {
                    val releaseInt = convertorsStatus.body()?.blocked[it.id]
                    val release = when (releaseInt) {
                        0 -> ConvertorReleseState.Unavailable
                        2 -> ConvertorReleseState.Beta
                        3 -> ConvertorReleseState.Experimental
                        null -> it.release
                        else -> ConvertorReleseState.Verified
                    }
                    it.copy(release = release)
                }
            }.toMutableMap()
        } catch (_: Exception) {
            return@withContext
        }

    }

    suspend fun loadConvertorsData(context: Context) = withContext(Dispatchers.IO) {
        appContext = context.applicationContext

        LogManager.i("ConvertorCore perform", content = "Read template convertors data from \"convertors/ + $CONVERTORS_FILE_ASSET\"")
        val templateJson = context.assets.open(CONVERTORS_FILE_ASSET).bufferedReader().use { it.readText() }
        val templateButtons = try {
            jsonParser.decodeFromString<Map<String, List<ConvertorData>>>(templateJson)
        } catch (e: Exception) {
            LogManager.e("ConvertorCore stopped", content = "Read template convertors data is FAILED.\nLoad FAILED -> RETURN: ERROR")
            e.printStackTrace()
            return@withContext
        }

        LogManager.i("ConvertorCore perform", content = "Search saved file \"$CONVERTORS_FILE_SAVE\"")
        val file = File(appContext.filesDir, CONVERTORS_FILE_SAVE)

        if (!file.exists()) {
            LogManager.w("ConvertorCore perform", content = "Search saved file is FAILED. No such file exist")

            file.writeText(templateJson)
            LogManager.i("ConvertorCore perform", content = "Create saved file with template data")
            convertors = templateButtons.toMutableMap()
            LogManager.c("ConvertorCore complete", content = "Load convertor data COMPLETE.\nLoad COMPLETE -> RETURN: COMPLETE")
            return@withContext

        }

        try {
            LogManager.i("ConvertorCore perform", content = "Read saved file")
            val savedJson = file.readText()
            val savedButtons: Map<String, List<ConvertorData>> =
                jsonParser.decodeFromString(savedJson)

            val merged: Map<String, List<ConvertorData>> =
                templateButtons.mapValues { (category, templateList) ->
                    val savedList = savedButtons[category].orEmpty()
                    val savedMap = savedList.associateBy { it.id }
                    templateList.map { template ->
                        val saved = savedMap[template.id]
                        if (saved != null) {
                            template.copy(
                                calckBlock = saved.calckBlock,
                                startUnit = saved.startUnit,
                                saveParameters = saved.saveParameters,
                                favorite = saved.favorite,
                                release = saved.release,
                                currentViewMode = saved.currentViewMode
                            )
                        } else template
                    }
                }

            convertors = merged.toMutableMap()
            LogManager.c("ConvertorCore complete", content = "Load convertor data COMPLETE.\nLoad COMPLETE -> RETURN: COMPLETE")

        } catch (e: Exception) {
            LogManager.t("ConvertorCore except", content = "Read saved data is FAILED. Replace saved file with a template")
            e.printStackTrace()
            file.writeText(templateJson)
            LogManager.c("ConvertorCore complete", content = "Load convertor data COMPLETE.\nLoad COMPLETE -> RETURN: EXCEPT COMPLETE")
        }
    }



    suspend fun saveConvertorsData(buttonMap: Map<String, List<ConvertorData>>? = null) =
        withContext(Dispatchers.IO) {
            val file = File(appContext.filesDir, CONVERTORS_FILE_SAVE)
            val toSave = buttonMap ?: convertors
            file.writeText(jsonParser.encodeToString(toSave))
        }

    suspend fun loadUnitData(
        context: Context
    ) = withContext(Dispatchers.IO) {
        appContext = context.applicationContext

        LogManager.i("ConvertorCore perform", content = "Read template units data from \"$UNITS_FILE_ASSET\"")
        val templateUnit = try {
            val templateJson =
                context.assets.open(UNITS_FILE_ASSET).bufferedReader().use { it.readText() }
            jsonParser.decodeFromString<MutableMap<String, List<ConvertorUnitJson>>>(templateJson)
        } catch (e: Exception) {
            LogManager.e("ConvertorCore stopped", content = "Read template units data is FAILED\nLoad FAILED -> RETURN: ERROR")
            e.printStackTrace()
            return@withContext
        }

        LogManager.i("ConvertorCore perform", content = "Search saved file \"$UNITS_FILE_SAVE\"")
        val file = File(appContext.filesDir, UNITS_FILE_SAVE)

        if (!file.exists()) {
            LogManager.w("ConvertorCore perform", content = "Search saved file is FAILED. No such file exist")
            LogManager.i("ConvertorCore perform", content = "Create saved file with template data")
            saveUnitData()
            LogManager.c("ConvertorCore complete", content = "Load units data COMPLETE.\nLoad COMPLETE -> RETURN: COMPLETE")
        }

        try {
            LogManager.i("ConvertorCore perform", content = "Read saved file")
            val savedJson = file.readText()
            val savedUnit: Map<String, List<ConvertorUnitJson>> =
                jsonParser.decodeFromString(savedJson)

            val merged: Map<String, List<ConvertorUnitJson>> =
                templateUnit.mapValues { (category, templateList) ->
                    val savedList = savedUnit[category].orEmpty()
                    val savedIDs = savedList.map { it.id }
                    templateList.filter { it.id !in savedIDs } + savedList
                }

            units = merged.toMutableMap()
            LogManager.c("ConvertorCore complete", content = "Load units data COMPLETE.\nLoad COMPLETE -> RETURN: COMPLETE")


        } catch (e: Exception) {
            LogManager.t("ConvertorCore except", content = "Read saved data is FAILED. Replace saved file with a template")
            e.printStackTrace()
            saveUnitData()
            LogManager.c("ConvertorCore complete", content = "Load units data COMPLETE.\nLoad COMPLETE -> RETURN: EXCEPT COMPLETE")
        }
    }

    suspend fun saveUnitData(unitMap: Map<String, List<ConvertorUnitJson>>? = null) =
    withContext(Dispatchers.IO) {
        LogManager.i("ConvertorCore perform", content = "Save units data")
        try {
            val file = File(appContext.filesDir, UNITS_FILE_SAVE)
            val toSave = (unitMap ?: units).mapValues { (key, value) ->
                value.filter { unitJson -> unitJson.saveData || unitJson.pinned != null }.ifEmpty { emptyList() }
            }
            file.writeText(jsonParser.encodeToString(toSave))
            LogManager.c("ConvertorCore complete", content = "Save units data COMPLETE.\nLoad COMPLETE -> RETURN: COMPLETE")

        } catch (e: Exception) {
            LogManager.e("ConvertorCore error", content = "Save units data is FAILED")
            e.printStackTrace()
        }

    }


    fun clearSavedUnit(
        group: String
    ) {
        LogManager.i("ConvertorCore call", content = "Call: Reset unit group to default.\nClear saved unit with parm \"saveData\" == true in grope: \"$group\"")

        val savedUnits = units[group]?.filter { !it.saveData }

        savedUnits?.let {
            units[group] = it
            LogManager.c("ConvertorCore call complete", content = "Call COMPLETE. Units group: \"$group\" is reset to default")
        } ?: run {
            LogManager.d("ConvertorCore call", content = "Units is null? ${units.isEmpty()}")
            LogManager.e("ConvertorCore call failure", content = "Search unit group: \"$group\" is failed. No such item exist")
        }
    }

    fun getUnits(group: String): List<ConvertorUnit> {
        return units[group]?.map { it.fromJsonType() } ?: emptyList()
    }

    fun getUnitsFlow(group: String): Flow<MutableList<ConvertorUnit>> = flow {
        emit(getUnits(group).toMutableList())
    }

    fun searchGroupByUnit(id: String): String? {
        units.forEach { (key, list) -> if (list.find { it.id == id } != null) return key }
        return null
    }

    fun setUnits(
        group: String,
        unitsList: List<ConvertorUnitJson>,
    ) {
        LogManager.i("ConvertorCore call", content = "Call: Set unit group: \"$group\"")
        units[group] = unitsList
    }

    fun pinUnit(
        id: String,
        isPinned: Boolean
    ): String {
        val group = searchGroupByUnit(id) ?: return "Group not found"
        val unit = units[group]?.find { it.id == id }?.copy(pinned = isPinned) ?: return "unit not found"
        val index = units[group]?.indexOfFirst { it.id == unit.id }!!
        if (index == -1) return "Index not found"
        val list = units[group]!!.toMutableList()
        list.removeIf { it.id == unit.id }
        list.add(index, unit)
        units[group] = list
        return unit.toString()
    }

    fun addToUnits(
        group: String,
        unit: ConvertorUnitJson,
    ) {
        LogManager.i("ConvertorCore call", content = "Call: Add unit group: \"$group\"")

        units[group]?.let {
            val index = it.indexOfFirst { item -> item.id == unit.id }
            if (index == -1) {
                units[group] = it + listOf(unit)
                LogManager.i("ConvertorCore call perform", content = "Unit is add")

            } else {
                val mutList = it.toMutableList()
                mutList[index] = unit.copy(pinned = it[index].pinned)
                units[group] = mutList
                LogManager.i("ConvertorCore call perform", content = "Unit is replace")
            }
        } ?: run {
            LogManager.e("ConvertorCore call failure", content = "Search unit group: \"$group\" is failed. No such item exist")
        }
    }

    fun getUnit(group: String, id: String): ConvertorUnit? {
        return units[group]?.map { it.fromJsonType() }?.find { it.id == id }
    }
    fun getUnit(id: String): ConvertorUnit? {
        val group = searchGroupByUnit(id) ?: return null
        return units[group]?.map { it.fromJsonType() }?.find { it.id == id }
    }

    fun getAllUnits() = units

    fun getConvertorList(): List<ConvertorData> = convertors.values.flatten()
    fun getConvertorsMap(): MutableMap<String, MutableList<ConvertorData>> {
        val merged = mutableMapOf<String, MutableList<ConvertorData>>()
        convertors.forEach { template ->
            val list = template.value
            merged[template.key] = list.toMutableList()
        }
        return merged
    }

    fun getConvertorByGroups(group: String): List<ConvertorData>? = convertors[group]


    fun getStartListUnits(group: String): List<ConvertorUnit>? {
        val default = getUnits(group).toMutableList()
        val item = getStartUnit(group) ?: return null
        default.remove(item)
        default.add(0, item)
        return default
    }

    fun getStartUnit(group: String): ConvertorUnit? {
        val startUnit = convertors.values.flatten().find { it.id == group }?.startUnit
        return units.values.flatten().find { it.id == startUnit?.id }?.fromJsonType() ?: startUnit
    }

    fun convert(
        value: String,
        from: ConvertorUnit,
        to: ConvertorUnit,
        convertorId: String,
    ): String {
        val result = try {
            val key = convertorId
            val from = units[key]?.firstOrNull() { it.id == from.id }
                ?: run {
                    LogManager.w("ConvertorCore convert except", content = "Not found unit parm \"from\".\nkey: \"$key\", unitName: \"${from.id}\"")
                    return "-"
                }
            val to = units[key]?.firstOrNull() { it.id == to.id }
                ?: run {
                    LogManager.w("ConvertorCore convert except", content = "Not found unit parm \"to\".\nkey: \"$key\", unitName: \"${from.id}\"")
                    return "-"
                }
            when (key) {
                "Temperature" -> convertTemperature(value.toDouble(), from, to).toString()
                "Numeration_system" -> convertNumberSystem(value, from, to)
                else -> convertDefault(value.toDouble(), from, to).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogManager.w("ConvertorCore convert error", content = "Error convert.\nfrom: \"${from.id}\" to: \"${to.id}\"")
            "Error"
        }
        return result
    }

    // 🔹 Звичайні одиниці (через multiplier)
    private fun convertDefault(
        value: Double,
        from: ConvertorUnitJson,
        to: ConvertorUnitJson,
    ): Double {
        if (from.multiplier == null || to.multiplier == null) return Double.NaN
        val baseValue = value * from.multiplier
        return baseValue / to.multiplier
    }

    // 🔹 Температура
// 🔹 Температура (універсальна конверсія)
    private fun convertTemperature(
        value: Double,
        from: ConvertorUnitJson,
        to: ConvertorUnitJson,
    ): Double {
        // Конвертуємо у °C
        val inCelsius = when (from.symbol) {
            "°C" -> value
            "K" -> value - 273.15
            "°F" -> (value - 32) * 5 / 9
            "°R" -> (value - 491.67) * 5 / 9
            "°De" -> 100 - value * 2 / 3
            "°N" -> value * 100 / 33
            "°Ré" -> value * 5 / 4
            "°Rø" -> (value - 7.5) * 40 / 21
            else -> value
        }

        // Конвертуємо з °C у цільову систему
        return when (to.symbol) {
            "°C" -> inCelsius
            "K" -> inCelsius + 273.15
            "°F" -> inCelsius * 9 / 5 + 32
            "°R" -> (inCelsius + 273.15) * 9 / 5
            "°De" -> (100 - inCelsius) * 3 / 2
            "°N" -> inCelsius * 33 / 100
            "°Ré" -> inCelsius * 4 / 5
            "°Rø" -> inCelsius * 21 / 40 + 7.5
            else -> inCelsius
        }
    }

    // 🔹 Системи числення
    private fun convertNumberSystem(
        value: String,
        from: ConvertorUnitJson,
        to: ConvertorUnitJson,
    ): String {
        return try {
            val fromBase = from.multiplier?.toInt()?.toString() ?: return "Error"
            val toBase = to.multiplier?.toInt()?.toString() ?: return "Error"

            // конвертація з будь-якої системи в десяткову

            val decimalValue = if (fromBase != "10") {
                value.uppercase().toLong(fromBase.toInt())
            } else value.toLong()

//            // конвертація з десяткової в цільову
            decimalValue.toString(toBase.toInt()).uppercase()

        } catch (e: Exception) {
            e.printStackTrace()
            "Error"
        }

    }
}