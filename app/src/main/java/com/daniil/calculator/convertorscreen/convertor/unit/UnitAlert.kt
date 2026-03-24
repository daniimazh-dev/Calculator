package com.daniil.calculator.convertorscreen.convertor.unit

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.appcompat.widget.DialogTitle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daniil.calculator.MainActivity
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.core.ConvertorCore
import com.daniil.calculator.universal.SearchTopBar
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import com.daniil.calculator.universal.simpleVerticalScrollbar
import com.daniil.calculator.utilites.CustomOverscrollEffect
import kotlinx.coroutines.delay

@Composable
fun UnitAlert(
    expanded: Boolean,
    title: String = stringResource(R.string.select_unit),
    onDismissRequest: () -> Unit,
    onSelected: (unit: ConvertorUnit) -> Unit,
    unitList: List<ConvertorUnit>,
    convertorScreenModel: ConvertorScreenModel? = null,
    oldSelected: ConvertorUnit,
) {
    if (!expanded) return
    var selected by remember { mutableStateOf(oldSelected) }
    var searchFilter by remember { mutableStateOf("") }

    val unitList = remember { unitList.toMutableStateList() }
    val context = LocalContext.current

    val vibrationEnabled = DynamicSettingsManager.getValue("button_vibration_enable").toBoolean()
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val effect = VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)


    val filterUnit by remember {
        derivedStateOf {
            unitList.filter {
                it.name.contains(searchFilter, ignoreCase = true) ||
                        it.symbol.contains(searchFilter, ignoreCase = true)
            }
        }
    }
    val scrollState = rememberLazyListState()
    val list by remember {
        derivedStateOf {
            val all = if (searchFilter.isBlank()) unitList else filterUnit
            val pinned = all.filter { it.pinned == true }
            val unpinned = all.filterNot { it.pinned == true }
            pinned + unpinned
        }
    }
    LaunchedEffect(Unit) {
        delay(200)
        scrollState.animateScrollToItem(list.indexOf(selected).coerceIn(0, unitList.size))
    }
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSelected(selected)
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Text(title)
        },
        text = {
            Column(modifier = Modifier.fillMaxHeight(0.7f)) {
                SearchTopBar(
                    onSearchChange = {
                        searchFilter = it
                    }
                )
                val scope = rememberCoroutineScope()
                val overscroll = remember(scope) { CustomOverscrollEffect(scope) }

                LazyColumn(
                    state = scrollState,
                    overscrollEffect = overscroll,
                    modifier = Modifier
                        .simpleVerticalScrollbar(scrollState)
                        .clip(MaterialTheme.shapes.medium),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        key = { "${it.id}_${it.symbol}" },
                        items = list
                    ) { item ->
                        fun onPinned() {
                            val inverse = item.pinned?.let { !it } ?: true
                            convertorScreenModel?.convertorCore?.pinUnit(
                                id = item.id,
                                isPinned = inverse
                            )

                            val index = unitList.indexOfFirst { it.id == item.id }
                            if (index != -1) {
                                convertorScreenModel?.units?.value[index] = item.copy(pinned = inverse)
                                unitList[index] = item.copy(pinned = inverse)
                            }
                        }

                        SelectItem(
                            modifier = Modifier.animateItem(),
                            unit = item,
                            selected = item.id == selected.id,
                            onClick = {
                                selected = item
                                if (vibrationEnabled) vibrator.vibrate(effect)
                            },
                            onSelectedClick = {
                                onSelected(selected)
                                if (vibrationEnabled) vibrator.vibrate(effect)
                            },
                            onPinned = convertorScreenModel?.let { { onPinned() } }
                        )
                    }
                }
            }

        }
    )
}

@Composable
private fun SelectItem(
    modifier: Modifier = Modifier,
    unit: ConvertorUnit,
    selected: Boolean,
    onClick: () -> Unit,
    onSelectedClick: () -> Unit,
    onPinned: (() -> Unit)?,
) {
    var dropdownMenuOpen by remember { mutableStateOf(false) }

    val animateSize = animateDpAsState(
        targetValue = if (selected) 6.dp else 14.dp
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = animateSize.value)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .combinedClickable(
                onClick = {
                    if (selected) onSelectedClick()
                    else onClick()
                },
                onLongClick = {
                    dropdownMenuOpen = true
                }
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = animateSize.value, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val text = remember {
                when {
                    unit.symbol.isBlank() -> unit.name
                    unit.name.isBlank() -> unit.symbol
                    else -> unit.name + " (${unit.symbol})"
                }
            }
            if (unit.pinned == true) {
                Icon(
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(45f),
                    painter = painterResource(R.drawable.pinned_icon),
                    contentDescription = "pinned"
                )
            }

            Text(
                modifier = Modifier.padding(6.dp),
                text = text,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        modifier = Modifier.padding(2.dp),
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }


            if (onPinned != null) {
                val dropDownItems = listOf(
                    UniversalDropDownItem(
                        title = if (unit.pinned == true) stringResource(R.string.unpin) else stringResource(
                            R.string.pin
                        ),
                        iconResource = if (unit.pinned == true) R.drawable.unpin_icon else R.drawable.pinned_icon,
                        onClick = {
                            onPinned()
                        }
                    )
                )
                UniversalDropDownMenu(
                    expanded = dropdownMenuOpen,
                    buttonList = dropDownItems,
                ) {
                    dropdownMenuOpen = false
                }

            }

        }
    }


}

