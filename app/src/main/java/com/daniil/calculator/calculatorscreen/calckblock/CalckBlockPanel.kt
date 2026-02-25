package com.daniil.calculator.calculatorscreen.calckblock

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.calculatorscreen.CalculatorScreenModel
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.homescreen.dataclass.getIcon
import com.daniil.calculator.convertorscreen.validateValue
import com.daniil.calculator.core.CalculatorCore
import com.daniil.calculator.openScreen
import com.daniil.calculator.universal.Copy
import com.daniil.calculator.universal.Paste
import com.daniil.calculator.universal.UniversalDropDownMenu
import com.daniil.calculator.universal.plus

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalckBlockPanel(
    modifier: Modifier = Modifier,
    calculatorScreenModel: CalculatorScreenModel,
    convertorScreenModel: ConvertorScreenModel,
    onSizeChange: (IntSize) -> Unit,
) {
    val context = LocalContext.current
    val calckBlock by calculatorScreenModel.calckBlock.collectAsState()
    val predictiveBlock by calculatorScreenModel.predictiveCalckBlock.collectAsState()
    val errorCalculate by calculatorScreenModel.errorCalculate.collectAsState()
    val animate by calculatorScreenModel.resultAnimate.collectAsState()


    var dropDownMenuExpanded by remember { mutableStateOf(false) }

    val animateResult = animateDpAsState(
        if (animate) (-15).dp else 0.dp
    )
    val animateResultSize = animateDpAsState(
        if (animate) 32.dp else 24.dp
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 68.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = {
                    dropDownMenuExpanded = true
                }
            )
            .onGloballyPositioned {
                onSizeChange(it.size)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {


        val activeScreen by convertorScreenModel.currentConvertor.collectAsState()

        val result = CalculatorCore.evaluate(calckBlock)
        val isVisible = activeScreen != null
                && convertorScreenModel.validateValue(result).first
        if (isVisible) {
            IconButton(onClick = {
                convertorScreenModel.goToConvertor(
                    buttonId = activeScreen!!.id,
                    customCalckBlock = result
                )
                openScreen.intValue = 1

            }) {
                val icon = activeScreen?.getIcon()?.let { ImageVector.vectorResource(it) }
                icon?.let {
                    Icon(
                        modifier = Modifier.size(34.dp),
                        imageVector = icon,
                        contentDescription = "go to convertor"
                    )
                }
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
        ) {

            UniversalDropDownMenu(
                expanded = dropDownMenuExpanded,
                buttonList = Copy(calckBlock) + Paste { str ->
                    val validate = calculatorScreenModel.validateCalckBlockValue(str)
                    if (validate.first) {
                        calculatorScreenModel.setCalck(validate.second ?: calckBlock)
                    } else {
                        Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                    }
                },
                onDismissRequest = {
                    dropDownMenuExpanded = false
                }
            )

            val isVisibleCalck = !animate
            AnimatedVisibility(
                visible = isVisibleCalck,
            ) {
                CalculatorInputField(
                    tokens = calculatorScreenModel.getTokensForUI(),
                    selectedTokenIndex = calculatorScreenModel.replaceTokenIndex.collectAsState().value,
                    onTokenClicked = { index, token -> calculatorScreenModel.setReplaceToken(index) },
                    onChangeToken = { index, token ->
                        val validate = calculatorScreenModel.validateCalckBlockValue(token)
                        if (validate.first) {
                            calculatorScreenModel.setToken(index, validate.second ?: "0")
                        } else {
                            Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCancelSelection = { calculatorScreenModel.dropReplaceToken() },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        animateColorAsState(
                            if (errorCalculate) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        ).value
                    )
                )

            }

            val isVisibleResult = (predictiveBlock != null) || animate
            AnimatedVisibility(
                visible = isVisibleResult,
            ) {
                Text(
                    modifier = Modifier.offset(y = animateResult.value),
                    text = predictiveBlock?.let { "= $predictiveBlock" }.orEmpty(),
                    fontSize = animateResultSize.value.value.sp
                )
            }

        }

    }

}