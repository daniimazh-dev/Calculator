package com.daniil.calculator.convertorscreen.report

import androidx.compose.runtime.Immutable
import com.daniil.calculator.settingsscreen.customscreen.logs.ConvertorLogData
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ReportRequest(
    val title: String,
    val description: String,
    val email: String,
    val images: List<ByteArray>,
    val convertorDataId: String,
    val logs: List<ConvertorLogData>,
    val packageName: String,
    val token: String?
)