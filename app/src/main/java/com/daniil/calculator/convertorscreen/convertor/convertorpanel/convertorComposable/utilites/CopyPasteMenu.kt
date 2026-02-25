package com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayoutScope
import com.daniil.calculator.universal.Copy
import com.daniil.calculator.universal.Paste
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import kotlinx.coroutines.launch


enum class CopyPasteMenu {
    Full,
    CopyOnly,
    None
}

@Composable
fun ConvertorLayoutScope.CopyPasteMenu(
    expanded: Boolean,
    copyPasteMenu: CopyPasteMenu,
    onDismissRequest: () -> Unit,
    customDropdownList: List<UniversalDropDownItem> = emptyList(),
    onCopy: () -> String,
    onPaste: (String?) -> Unit,
) {
    val dropDownButtonList =
        when (copyPasteMenu) {
            CopyPasteMenu.Full -> {
                customDropdownList + Copy(onCopy()) + Paste { onPaste(it) }

            }
            CopyPasteMenu.CopyOnly -> {
                customDropdownList + Copy(onCopy())
            }
            CopyPasteMenu.None -> customDropdownList
        }
    UniversalDropDownMenu(
        expanded = expanded,
        buttonList = dropDownButtonList,
        onDismissRequest = {
            onDismissRequest()
        }
    )
}
