package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.CardContainer
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayoutScope
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedValue
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.UnitSelect
import com.daniil.calculator.universal.UniversalDropDownItem


@Composable
fun ConvertorLayoutScope.BigInputWithUnit(
    modifier: Modifier = Modifier,
    currentUnit: ConvertorUnit,
    unitList: List<ConvertorUnit> = this.unitList,
    title: String,
    content: Any,
    selected: Boolean = false,
    copyPasteMenu: CopyPasteMenu = CopyPasteMenu.None,
    customDropdownList: List<UniversalDropDownItem> = emptyList(),
    groupedValue: GroupedValue? = null,
    onPaste: ((String?) -> Unit)? = null,
    onChangeUnit: (ConvertorUnit) -> Unit,
    onClick: () -> Unit,
) {
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    CardContainer(
        modifier = modifier,
        groupedValue = groupedValue,
        contentPadding = PaddingValues(16.dp),
        onClick = {
            onClick()
        },
        onLongClick = {
            if (copyPasteMenu != CopyPasteMenu.None)
                dropDownMenuExpanded = true
        }
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {

                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    val scale by animateFloatAsState(if (selected) 1.15f else 1f)
                    Text(
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                        text = content.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color =
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,

                        ),

                        autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 20.sp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                }
            }
            UnitSelect(
                currentUnit = currentUnit,
                unitList = unitList,
                convertorScreenModel = convertorScreenModel
            ) {
                onChangeUnit(it)
            }

            CopyPasteMenu(
                expanded = dropDownMenuExpanded,
                customDropdownList = customDropdownList,
                copyPasteMenu = copyPasteMenu,
                onDismissRequest = {
                    dropDownMenuExpanded = false
                },
                onCopy = {
                    return@CopyPasteMenu content.toString()
                },
                onPaste = {
                    onPaste?.invoke(it)
                }
            )
        }
    }
}