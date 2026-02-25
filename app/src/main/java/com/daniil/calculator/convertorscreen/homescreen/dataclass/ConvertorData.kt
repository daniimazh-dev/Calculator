package com.daniil.calculator.convertorscreen.homescreen.dataclass

import androidx.compose.runtime.Stable
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.Parameter
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import kotlinx.serialization.Serializable

@Serializable
data class ConvertorData(
    val id: String,
    val title: String = id,
    val painterName: String,
    val currentViewMode: String? = null,
    val startUnit: ConvertorUnit,
    @Stable
    val favorite: Boolean = false,
    @Stable
    val release: ConvertorReleseState = ConvertorReleseState.Verified,
    val saveParameters: List<Parameter> = listOf(),
    val description: String? = null,
    val calckBlock: String = "0"
)