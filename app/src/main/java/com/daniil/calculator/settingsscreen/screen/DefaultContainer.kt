package com.daniil.calculator.settingsscreen.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.settingsscreen.SettingsScreenModel
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingRenderManager
import com.daniil.calculator.settingsscreen.settings.manager.SettingType
import com.daniil.calculator.utilites.CustomOverscrollEffect
import com.daniil.calculator.utilites.customOverscroll
import kotlin.math.roundToInt

@Composable
fun DynamicSettingRenderManager.DefaultContainer(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    title: String? = null,
    settingsScreenModel: SettingsScreenModel,
    onBackStack: () -> Unit = { settingsScreenModel.backStack() },
    settings: List<DynamicSetting>,
) {
    var animatedOverscrollAmount  by remember { mutableFloatStateOf(0f) }

    val settingMap = remember {
        settings.groupByItem(null) {
            when (it.type) {
                SettingType.Title.name -> it.title
                else -> null
            }
        }
    }

    Box(
        modifier = modifier
            .customOverscroll(
                lazyListState,
                onNewOverscrollAmount = { animatedOverscrollAmount = it }
            )
            .offset { IntOffset(0, animatedOverscrollAmount.roundToInt()) }
    ) {
    DefaultAppBar(
        modifier = Modifier,
        title = title,
        onBackStack = { onBackStack() },
    ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(settingMap.keys.toList()) { key ->
                    Column() {
                        settingMap[key]?.first?.let {
                            if (!it.enabled) return@let
                            RendererSetting(it)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            settingMap[key]?.second?.forEach { setting ->
                                RendererSetting(setting)
                            }

                        }
                    }


                }

            }
        }
    }


}

private inline fun <T, K> Iterable<T>.groupByItem(
    defaultKey: K,
    keySelector: (T) -> K?,
): Map<K, Pair<T?, List<T>>> {

    val result = LinkedHashMap<K, Pair<T?, MutableList<T>>>()
    var currentKey: K? = null
    var hasHeader = false

    for (item in this) {
        val key = keySelector(item)

        if (key != null) {
            hasHeader = true
            currentKey = key
            result[key] = item to mutableListOf()
        } else {
            if (currentKey != null) {
                result[currentKey]?.second?.add(item)
            } else {
                val section = result.getOrPut(defaultKey) {
                    null to mutableListOf()
                }
                section.second.add(item)
            }
        }
    }

    return result.mapValues { it.value.first to it.value.second }
}


@Composable
fun DefaultAppBar(
    modifier: Modifier = Modifier,
    title: String?,
    onBackStack: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (title == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
        return
    }
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
        )
        {
            Row(
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    onBackStack()
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
//                        tint = MaterialTheme.colorScheme.onSurface,

                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
//                    color = MaterialTheme.colorScheme.onSurface,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 12.sp,
                        maxFontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
            }
        }


        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }

}

