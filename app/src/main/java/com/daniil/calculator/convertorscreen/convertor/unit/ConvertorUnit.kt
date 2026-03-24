package com.daniil.calculator.convertorscreen.convertor.unit

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

val NullableUnit = ConvertorUnit(
    id = "null",
    symbol = "-",
)

fun ConvertorUnit.ifNullable(
    onTrue: () -> ConvertorUnit
): ConvertorUnit {
    return if (this == NullableUnit) onTrue() else this
}

@Serializable
data class ConvertorUnit(
    val id: String,
    val name: String = id,
    val symbol: String,
    val pinned: Boolean? = null
)
