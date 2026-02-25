package com.daniil.calculator.universal

import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonUi

data class ButtonData(
    val content: String,
    val id: String = content,
    val expanded: Boolean = true,
    val onClick: () -> Unit = {},
    val onPressed: () -> Unit = {},
    val type: ButtonUi = ButtonUi.Default,
    val painterIcon: Int? = null,
    val lottieJson: String? = null,
)