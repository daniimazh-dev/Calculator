package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.CardContainer
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayoutScope
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedValue
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.universal.UniversalDropDownItem


@Composable
fun ConvertorLayoutScope.BigResult(
    modifier: Modifier = Modifier,
    title: String,
    content: Any,
    suffix: String? = null,
    description: String? = null,
    copyPasteMenu: CopyPasteMenu = CopyPasteMenu.None,
    customDropdownList: List<UniversalDropDownItem> = emptyList(),
    groupedValue: GroupedValue? = null,
    onPaste: ((String?) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val suffix = suffix?.let { " $suffix" } ?: ""
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    CardContainer(
        modifier = modifier
            .fillMaxWidth(),
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

        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    BasicText(
                        text = content.toString() + suffix,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 20.sp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    description?.let {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }


                }
            }


            CopyPasteMenu(
                expanded = dropDownMenuExpanded,
                copyPasteMenu = copyPasteMenu,
                customDropdownList = customDropdownList,
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
