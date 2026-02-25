package com.daniil.calculator.calculatorscreen.history

import com.daniil.calculator.universal.LocalDateTimeSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class HistoryData(
    val content: String,
    val result: String,
    val pinned: Boolean = false,
    val comment: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val time: LocalDateTime
)
