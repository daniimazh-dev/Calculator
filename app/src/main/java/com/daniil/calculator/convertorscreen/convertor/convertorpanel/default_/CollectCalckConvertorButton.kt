package com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_

import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_0
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_1
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_2
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_3
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_4
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_5
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_6
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_7
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_8
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_9
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_Clear
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_Del
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_Minus
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_Point


import com.daniil.calculator.universal.ButtonData


class ButtonsStackStorage(
    val model: ConvertorScreenModel,
    val column: Int = 4,
) {
    init {
        Buttons.model = model
    }
}


val ButtonsStackStorage.DefaultButtons: ButtonsStack
    get() {
        val buttonStack = ButtonsStack(
            column = column,
            buttons = listOf(
                Buttons.Default.B_7,
                Buttons.Default.B_8,
                Buttons.Default.B_9,
                Buttons.General.B_Clear,
                Buttons.Default.B_4,
                Buttons.Default.B_5,
                Buttons.Default.B_6,
                Buttons.General.B_Del,
                Buttons.Default.B_1,
                Buttons.Default.B_2,
                Buttons.Default.B_3,
                Buttons.Empty,
                Buttons.Empty,
                Buttons.Default.B_0,
                Buttons.Default.B_Point,
                Buttons.Empty
            )
        )
        return buttonStack
    }


val ButtonsStackStorage.DefaultButtonsWithMinus: ButtonsStack
    get() {
        val buttonStack = ButtonsStack(
            column = column,
            buttons = listOf(
                Buttons.Default.B_7,
                Buttons.Default.B_8,
                Buttons.Default.B_9,
                Buttons.General.B_Clear,
                Buttons.Default.B_4,
                Buttons.Default.B_5,
                Buttons.Default.B_6,
                Buttons.General.B_Del,
                Buttons.Default.B_1,
                Buttons.Default.B_2,
                Buttons.Default.B_3,
                Buttons.Default.B_Minus, // Minus
                Buttons.Empty,
                Buttons.Default.B_0,
                Buttons.Default.B_Point,
                Buttons.Empty
            )
        )
        return buttonStack
    }

//        ButtonData(content = "hide", painterIcon = R.drawable.arrow_down, type = ButtonUi.Special, onClick = { convertorScreenModel.setCurrentInputUnit(ConvertorUnit("Null", "null")) }),

private val emptyButton = ButtonData(content = "", type = ButtonUi.Empty, onClick = { })


enum class ButtonUi {
    Default,
    Empty,
    Second,
    Function,
    Special
}

data class ButtonsStack(
    val column: Int = 4,
    val buttons: List<ButtonData>,
)

object Buttons {
    lateinit var model: ConvertorScreenModel
    val Empty = emptyButton

    object Default
    val Default.B_0: ButtonData
        get() = ButtonData(content = "0", onClick = { model.handleButtonClick("0") })
    val Default.B_1: ButtonData
        get() = ButtonData(content = "1", onClick = { model.handleButtonClick("1") })
    val Default.B_2: ButtonData
        get() = ButtonData(content = "2", onClick = { model.handleButtonClick("2") })
    val Default.B_3: ButtonData
        get() = ButtonData(content = "3", onClick = { model.handleButtonClick("3") })
    val Default.B_4: ButtonData
        get() = ButtonData(content = "4", onClick = { model.handleButtonClick("4") })
    val Default.B_5: ButtonData
        get() = ButtonData(content = "5", onClick = { model.handleButtonClick("5") })
    val Default.B_6: ButtonData
        get() = ButtonData(content = "6", onClick = { model.handleButtonClick("6") })
    val Default.B_7: ButtonData
        get() = ButtonData(content = "7", onClick = { model.handleButtonClick("7") })
    val Default.B_8: ButtonData
        get() = ButtonData(content = "8", onClick = { model.handleButtonClick("8") })
    val Default.B_9: ButtonData
        get() = ButtonData(content = "9", onClick = { model.handleButtonClick("9") })
    val Default.B_Point: ButtonData
        get() = ButtonData(content = ".", onClick = { model.handleButtonClick(".") })
    val Default.B_Minus: ButtonData
        get() = ButtonData(content = "-", type = ButtonUi.Second,onClick = { model.handleButtonClick("-") })


    object General
    val General.B_Clear: ButtonData
        get() = ButtonData(
            content = "C",
            type = ButtonUi.Special,
            onClick = { model.setCalck("0") })
    val General.B_Del: ButtonData
        get() = ButtonData(
            content = "delete",
            painterIcon = R.drawable.backspace_icon,
            type = ButtonUi.Special,
            onClick = { model.removeLastCalck() },
            onPressed = { model.removeLastCalck() }
        )


    object Laters
    val Laters.B_A: ButtonData
        get() = ButtonData(content = "A", type = ButtonUi.Second, onClick = { model.handleButtonClick("A") })
    val Laters.B_B: ButtonData
        get() = ButtonData(content = "B", type = ButtonUi.Second,  onClick = { model.handleButtonClick("B") })
    val Laters.B_C: ButtonData
        get() = ButtonData(content = "C", type = ButtonUi.Second,  onClick = { model.handleButtonClick("C") })
    val Laters.B_D: ButtonData
        get() = ButtonData(content = "D", type = ButtonUi.Second,  onClick = { model.handleButtonClick("D") })
    val Laters.B_F: ButtonData
        get() = ButtonData(content = "F", type = ButtonUi.Second,  onClick = { model.handleButtonClick("F") })
    val Laters.B_G: ButtonData
        get() = ButtonData(content = "G", type = ButtonUi.Second,  onClick = { model.handleButtonClick("G") })
    val Laters.B_E: ButtonData
        get() = ButtonData(content = "E", type = ButtonUi.Second,  onClick = { model.handleButtonClick("E") })
    val Laters.B_H: ButtonData
        get() = ButtonData(content = "H", type = ButtonUi.Second,  onClick = { model.handleButtonClick("H") })
    val Laters.B_I: ButtonData
        get() = ButtonData(content = "I", type = ButtonUi.Second,  onClick = { model.handleButtonClick("I") })
    val Laters.B_J: ButtonData
        get() = ButtonData(content = "J", type = ButtonUi.Second,  onClick = { model.handleButtonClick("J") })
}