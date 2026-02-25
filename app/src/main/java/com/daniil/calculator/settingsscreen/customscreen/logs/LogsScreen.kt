package com.daniil.calculator.settingsscreen.customscreen.logs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.settingsscreen.SettingsScreenModel
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import com.daniil.calculator.universal.simpleVerticalScrollbar
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun LogsScreen(
    setting: DynamicSetting,
    settingsScreenModel: SettingsScreenModel,
) {


    var filter by remember { mutableStateOf<ConvertorLogType?>(null) }

    val logs = LogManager.filterType(filter)

    var dropDownMenuExpanded by remember { mutableStateOf(false) }


    val dropDownButtonList = listOf(
        UniversalDropDownItem(
            title = "None",
            onClick = {
                filter = null
            },
            autoClose = false
        ),
    ) + ConvertorLogType.entries.map {
        UniversalDropDownItem(
            title = it.name,
            onClick = {
                filter = it
            },
            autoClose = false
        )
    }
    Column(
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    settingsScreenModel.backStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
                Text(
                    text = "Logs",
                    style = MaterialTheme.typography.titleLarge
                )
            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        dropDownMenuExpanded = true
                    }
                ) {
                    Text("Filter: ${filter?.name ?: "None"}")
                }
                if (dropDownMenuExpanded) {
                    UniversalDropDownMenu(
                        expanded = dropDownMenuExpanded,
                        buttonList = dropDownButtonList,
                        onDismissRequest = {
                            dropDownMenuExpanded = false
                        }
                    )
                }
                IconButton(onClick = {
                    LogManager.clearLogData()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.delete_icon),
                        contentDescription = "Clear"
                    )
                }
            }



        }
        val lazyListState = rememberLazyListState()
        if (logs.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .simpleVerticalScrollbar(
                        state = lazyListState,
                        reversedLayout = true
                    ),
                state = lazyListState
            ) {
                itemsIndexed(
                    items = logs.reversed(),
                    key = { index, item -> index to item.type }
                ) { index, item ->
                    LogItem(item = item)
                }

            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(42.dp),
                        painter = painterResource(R.drawable.info_icon),
                        contentDescription = "Info"
                    )
                    Text("No logs")
                    if (filter != null) {
                        Button(
                            onClick = {
                                filter = null
                            }
                        ) {
                            Text("Clear filters")
                        }
                    }

                }

            }
        }
    }

}

@Composable
private fun LogItem(
    modifier: Modifier = Modifier,
    item: ConvertorLogData,
) {
    var fullShow by remember { mutableStateOf(false) }

    val iconData: Pair<Color, Char> = remember {
        when (item.type) {
            ConvertorLogType.Info -> Color.White to 'I'
            ConvertorLogType.Debug -> Color.Cyan to 'D'
            ConvertorLogType.Error -> Color.Red to 'E'
            ConvertorLogType.Warning -> Color.Yellow to 'W'
            ConvertorLogType.TrashedError -> Color.Magenta to 'T'
            ConvertorLogType.Complete -> Color.Green to 'C'
            ConvertorLogType.Key -> Color.Gray to 'K'

        }
    }
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }

    if (item.type != ConvertorLogType.Key) {

        Row(
            modifier = Modifier
                .fillMaxWidth()

                .animateContentSize()
                .background(
                    iconData.first.copy(alpha = 0.5f)
                )
                .clickable {
                    fullShow = !fullShow
                }
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconData.first),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconData.second.toString(),
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                            .copy(color = Color.Black)
                    )
                    Text(
                        text = remember { item.time.format(formatter) },
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(
                    text = item.content,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (fullShow) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.labelSmall
                        .copy(color = Color.Black)

                )
            }


        }
    } else {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color.DarkGray),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
                    .copy(color = Color.Black),
                textAlign = TextAlign.Center
            )
            Text(
                text = remember { item.time.format(formatter) },
                style = MaterialTheme.typography.labelSmall
            )
        }
    }


}

@Preview
@Composable
private fun Preview() {
    Column() {
        ConvertorLogType.entries.forEach {
            LogItem(
                item = ConvertorLogData(
                    codeId = UUID.randomUUID().toString(),
                    name = "Test",
                    content = "Texting composable item\nitem has 6 type:\nInfo, Warning, Error, Debug, TrashedError, Complete",
                    type = it,
                    time = "12.01.2026",
                    decorator = LogDecorator()
                )
            )
        }
    }


}