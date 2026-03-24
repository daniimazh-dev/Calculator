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
    val pinned: Boolean? = null
) {
    companion object {
        fun ConvertorUnitJson.fromJsonType(): ConvertorUnit {
            return ConvertorUnit(
                id = this.id,
                name = this.name,
                pinned = this.pinned,
                symbol = this.symbol,
            )
        }
    }
}