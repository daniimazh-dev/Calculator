package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.CardContainer
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayoutScope
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedValue
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.universal.UniversalDropDownItem


@Composable
fun ConvertorLayoutScope.SmallInput(
    modifier: Modifier = Modifier,
    title: String,
    content: Any,
    suffix: String? = null,
    selected: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(6.dp),
    copyPasteMenu: CopyPasteMenu= CopyPasteMenu.None,
    customDropdownList: List<UniversalDropDownItem> = emptyList(),
    contentAlignment: Alignment = Alignment.CenterStart,
    groupedValue: GroupedValue? = null,
    onPaste: ((String?) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val suffix = suffix?.let { " $suffix" } ?: ""
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    CardContainer(
        modifier = modifier,
        groupedValue = groupedValue,
        contentPadding = contentPadding,
        containerColor = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = {
            onClick()
        },
        onLongClick = {
            if (copyPasteMenu != CopyPasteMenu.None)
                dropDownMenuExpanded = true
        }
    ) {

        Box(
            contentAlignment = contentAlignment,
            modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = content.toString() + suffix,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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