package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.CardContainer
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayoutScope
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedValue
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.universal.UniversalDropDownItem


@Composable
fun ConvertorLayoutScope.SmallResult(
    modifier: Modifier = Modifier,
    title: String,
    content: Any,
    suffix: String? = null,
    selected: Boolean = false,
    reversedLayout: Boolean = false,
    copyPasteMenu: CopyPasteMenu = CopyPasteMenu.None,
    customDropdownList: List<UniversalDropDownItem> = emptyList(),
    groupedValue: GroupedValue? = null,
    onPaste: ((String?) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val suffix = suffix?.let { " $suffix" } ?: ""
    var dropDownMenuExpanded by remember { mutableStateOf(false) }

    @Composable
    fun content() {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = content.toString() + suffix,
                style = MaterialTheme.typography.bodyLarge,
                color = if (!selected) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary
            )
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

    @Composable
    fun FlowRowScope.title() {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = if (reversedLayout) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            Text(
                textAlign = TextAlign.End,
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (!selected) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.primary
            )

        }
    }


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
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()

        ) {
            if (reversedLayout) {
                title()
                content()
            } else {
                content()
                title()
            }
        }
    }
}
