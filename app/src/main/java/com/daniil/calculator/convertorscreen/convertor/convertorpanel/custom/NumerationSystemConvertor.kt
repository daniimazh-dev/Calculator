package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.content.Context
import androidx.compose.ui.res.stringResource
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons
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
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_A
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_B
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_C
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_Clear
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_D
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_Del
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_E
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_F
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_G
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_H
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_I
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.Buttons.B_J
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonsStack
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.StandardConvertor
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData


class NumerationSystemConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
    context: Context,
): StandardConvertor(convertorData, model, context) {
    override fun onCreate() {
        super.onCreate()
        setContent {
            mode(id = "Comparison", name = stringResource(R.string.expanded), painterId = R.drawable.view3_icon) {
                content = {
                    expandedContent()
                }
                buttonMap(registerNumerationSystemButtons())
                showClackPanel.value = false
            }
//            mode( id = "List", name = stringResource(R.string.diminished), painterId =  R.drawable.view1_icon) {
//                content = {
//                    diminishedContent()
//                }
//                buttonMap(registerNumerationSystemButtons())
//            }
        }


    }

}



fun NumerationSystemConvertorImplementation.registerNumerationSystemButtons(): Map<ConvertorUnit, ButtonsStack>
= with(mutableMapOf<ConvertorUnit, ButtonsStack>()){

    val units = getUnits().ifEmpty { return@with this }
    // Binary (BIN)

    put(units[0], ButtonsStack(
        column = 4,
        buttons = listOf(
            Buttons.Default.B_1,
            Buttons.Default.B_0,
            Buttons.General.B_Clear,
            Buttons.General.B_Del,
            )
        )
    )

    // Ternary (TRI)
    put(units.find { it.id == "Ternary" }!!, ButtonsStack(
        column = 4,
        buttons = listOf(
            Buttons.Default.B_0,
            Buttons.Default.B_1,
            Buttons.Default.B_2,
            Buttons.General.B_Clear,
            Buttons.Empty,
            Buttons.Empty,
            Buttons.Empty,
            Buttons.General.B_Del,
            )
        )
    )

    // Quaternary (QUA)
    put(units.find { it.id == "Quaternary" }!!, ButtonsStack(
        column = 4,
        buttons =  listOf(
            Buttons.Default.B_1,
            Buttons.Default.B_2,
            Buttons.Default.B_3,
            Buttons.General.B_Clear,
            Buttons.Empty,
            Buttons.Default.B_0,
            Buttons.Empty,
            Buttons.General.B_Del,
        )
    ))
    // Quinary (QUIN)
    put(units.find { it.id == "Quinary" }!!, ButtonsStack(
        column = 4,
        buttons = listOf(
            Buttons.Default.B_1,
            Buttons.Default.B_2,
            Buttons.Default.B_3,
            Buttons.General.B_Clear,
            Buttons.Default.B_4,
            Buttons.Default.B_0,
            Buttons.Empty,
            Buttons.General.B_Del,
            )
        )
    )


    // Octal (OCT)
    put(units.find { it.id == "Octal" }!!, ButtonsStack(
        column = 4,
        buttons = listOf(
            Buttons.Default.B_1,
            Buttons.Default.B_2,
            Buttons.Default.B_3,
            Buttons.General.B_Clear,
            Buttons.Default.B_4,
            Buttons.Default.B_5,
            Buttons.Default.B_6,
            Buttons.General.B_Del,
            Buttons.Empty,
            Buttons.Default.B_0,
            Buttons.Empty,
            Buttons.Empty,
            )
        )
    )
    // Decimal (DEC) default
    put(units.find { it.id == "Decimal" }!!, ButtonsStack(
            column = 4,
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
                Buttons.Empty,
                Buttons.Empty
            )
        )
    )

    // Duodecimal (DUO)

    put(units.find { it.id == "Duodecimal" }!!, ButtonsStack(
            column = 4,
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
                Buttons.Laters.B_A,
                Buttons.Empty,
                Buttons.Default.B_0,
                Buttons.Empty,
                Buttons.Laters.B_B
            )
        )
    )

    // Hexadecimal (Hex)
    put(units.find { it.id == "Hexadecimal" }!!, ButtonsStack(
            column = 4,
            buttons = listOf(
                Buttons.Laters.B_A,
                Buttons.Laters.B_B,
                Buttons.Laters.B_C,
                Buttons.Laters.B_D,
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
                Buttons.Laters.B_F,
                Buttons.Empty,
                Buttons.Default.B_0,
                Buttons.Empty,
                Buttons.Laters.B_E

            )
        )
    )



    // Vigesimal (VIG)
    put(units.find { it.id == "Vigesimal" }!!, ButtonsStack(
        column = 5,
        buttons = listOf(
            Buttons.Laters.B_A,
            Buttons.Laters.B_B,
            Buttons.Laters.B_C,
            Buttons.Laters.B_D,
            Buttons.Laters.B_F,
            Buttons.Laters.B_E,
            Buttons.Default.B_7,
            Buttons.Default.B_8,
            Buttons.Default.B_9,
            Buttons.General.B_Clear,
            Buttons.Laters.B_G,
            Buttons.Default.B_4,
            Buttons.Default.B_5,
            Buttons.Default.B_6,
            Buttons.General.B_Del,
            Buttons.Laters.B_H,

            Buttons.Default.B_1,
            Buttons.Default.B_2,
            Buttons.Default.B_3,
            Buttons.Laters.B_I,
            Buttons.Empty,
            Buttons.Empty,

            Buttons.Default.B_0,
            Buttons.Empty,
            Buttons.Laters.B_J,

            )
        )
    )


    return@with this

}