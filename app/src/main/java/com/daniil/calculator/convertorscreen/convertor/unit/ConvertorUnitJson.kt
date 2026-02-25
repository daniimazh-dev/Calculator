package com.daniil.calculator.convertorscreen.convertor.unit

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ConvertorUnitJson(
    val id: String,
    val name: String = id,
    val symbol: String,
    val multiplier: Double? = null,
    val saveData: Boolean = false,
) {
    companion object {
        fun ConvertorUnitJson.fromJsonType(): ConvertorUnit {
            return ConvertorUnit(
                id = this.id,
                name = this.name,
                symbol = this.symbol,
            )
        }
    }
}