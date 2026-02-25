package com.daniil.calculator.convertorscreen.convertor.unit

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

val NullableUnit = ConvertorUnit(
    id = "null",
    symbol = "-",
)

fun  ConvertorUnit.ifNullable(
    onTrue: () -> ConvertorUnit
): ConvertorUnit {
    return if (this == NullableUnit) onTrue() else this
}

@Immutable
@Serializable
data class ConvertorUnit(
    val id: String,
    val name: String = id,
    val symbol: String,

)
