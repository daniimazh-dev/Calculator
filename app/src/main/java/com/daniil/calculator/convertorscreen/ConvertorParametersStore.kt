package com.daniil.calculator.convertorscreen

import com.daniil.calculator.universal.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDate

class ConvertorParametersStore {
    private var params: MutableMap<String, List<Parameter>> = mutableMapOf()
    fun setParameter(
        convertorName: String,
        data: ParameterBuilder.() -> Unit
    ) {
        val parameters = params[convertorName]?.toMutableList() ?: mutableListOf()

        val param = ParameterBuilder().apply(data).build()
        param.forEach { param ->
            val index = parameters.indexOfFirst { param.key == it.key }
            if (index != -1) {
                parameters[index] = param
            } else {
                parameters.add(param)
            }
        }
        params[convertorName] = parameters
    }
    fun clearAllParameter(
        convertorName: String,
    ) {
        params[convertorName] = emptyList()
    }
    fun deleteParameter(
        convertorName: String,
        key: String,
    ) {
        val parameters = params[convertorName]?.toMutableList() ?: mutableListOf()
        val find = parameters.find { it.key == key }
        if (find != null) {
            parameters.remove(find)
            params[convertorName] = parameters
        }

    }


    fun getParameter(
        convertorName: String,
        key: String,
        defaultValue: Any? = null
    ): Any? {
        val parameters = params[convertorName]?.toMutableList() ?: mutableListOf()
        val result = parameters.find { it.key == key }
        if (result?.data == null) return defaultValue
        return if (result.isJson) Json.decodeFromString(result.data) else result.data

    }

    fun getAllOf(convertorName: String): List<Parameter>? = params[convertorName]
    fun getAll(): Map<String, List<Parameter>> = params

    fun setAll(data: Map<String, List<Parameter>>) {
        params = data.toMutableMap()
    }
    fun setAllOf(convertorName: String, data:  List<Parameter>) {
        params[convertorName] = data
    }
}


class ParameterBuilder() {
    val module = SerializersModule {
        contextual(LocalDate::class, LocalDateSerializer)
    }
    val json = Json {
        serializersModule = module
        encodeDefaults = true
    }
    val parameters = mutableListOf<Parameter>()


    fun setStringData(key: String, str: String?) {
        parameters.add(Parameter(
            key = key,
            isJson = false,
            data = str
        ))

    }
    fun setIntData(key: String, int: Int?) {
        parameters.add(Parameter(
            key = key,
            isJson = false,
            data = int?.toString()
        ))
    }
    fun setFloatData(key: String, float: Float?) {
        parameters.add(Parameter(
            key = key,
            isJson = false,
            data = float?.toString()
        ))
    }
    fun settDoubleData(key: String, double: Double?) {
        parameters.add(Parameter(
            key = key,
            isJson = false,
            data = double?.toString()
        ))
    }
    inline fun <reified T> setObject(key: String, any: T) {
        parameters.add(Parameter(
            key = key,
            isJson = true,
            data = json.encodeToString<T>(any)
        ))
    }

    fun build(): List<Parameter> {
        return parameters
    }

}


@Serializable
data class Parameter(
    val key: String,
    val isJson: Boolean = false,
    val data: String?
)

