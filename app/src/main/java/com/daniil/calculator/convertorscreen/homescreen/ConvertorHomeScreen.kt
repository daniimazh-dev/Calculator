package com.daniil.calculator.convertorscreen.homescreen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorReleseState
import com.daniil.calculator.universal.SearchTopBar
import com.daniil.calculator.universal.simpleVerticalScrollbar
import com.daniil.calculator.utilites.customOverscroll
import com.daniil.csb.SettingsProvider
import com.daniil.csb.classes.Select
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ConvertorHomeScreen(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    convertorScreenModel: ConvertorScreenModel,
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val filterExperimentalButtons by
        SettingsProvider.getValue<Boolean>("experimental_convertor_buttons").collectAsState()
    val filterUnavailableButtons by
        SettingsProvider.getValue<Boolean>("unavailable_convertor_buttons").collectAsState()

    val buttons by convertorScreenModel.convertors.collectAsState()

    if (buttons.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }

        }
        return
    }

    var buttonsMap = remember {
        if (filterExperimentalButtons) {
            buttons.mapValues { (_, list) ->
                val list = list.filter {
                    if (!filterUnavailableButtons) {
                        it.release != ConvertorReleseState.Unavailable
                    } else true
                }
                list.ifEmpty { emptyList() }
            }
        } else {
            buttons.mapValues { (_, list) ->
                val list = list.filter {
                    it.release == ConvertorReleseState.Verified || it.release == ConvertorReleseState.Beta
                }
                list.ifEmpty { emptyList() }
            }
        }
    }
    buttonsMap = buttonsMap.filter { it.value.isNotEmpty() }
    val scrollState = convertorScreenModel.scrollState





    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val row = if (isPortrait) 3 else 5

        CategoryList(
            modifier = Modifier
                .fillMaxSize(),
            scrollState = scrollState,
            row = row,
            buttonsMap = buttonsMap,
            convertorScreenModel = convertorScreenModel,
            animatedVisibilityScope = animatedVisibilityScope
        )


    }

}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CategoryList(
    modifier: Modifier,
    buttonsMap: Map<String, List<ConvertorData>>,
    scrollState: LazyListState,
    row: Int,
    convertorScreenModel: ConvertorScreenModel,
    animatedVisibilityScope: AnimatedVisibilityScope,

) {
    var searchFilter by remember { mutableStateOf("") }

    val filteredButtons by remember {
    derivedStateOf {
        if (searchFilter.isBlank()) buttonsMap
        else buttonsMap.mapValues { (key, list) ->
            list.filter { it.title.contains(searchFilter, ignoreCase = true) }
        }
        }
    }


    var searchFocused by remember { mutableStateOf(false) }

    val space = 4.dp
    val favoriteTitle = stringResource(R.string.favorite)
    val items by remember(searchFilter) {
        derivedStateOf {
            val map = if (searchFilter.isBlank()) buttonsMap else filteredButtons
            val favorite = mutableListOf<ConvertorData>()
            val other = map
                .mapValues { (_, list) ->
                    val partition = list.partition { it.favorite }
                    favorite.addAll(partition.first)
                    partition.second
                }
            mapOf(favoriteTitle to favorite) + other
        }
    }
    var animatedOverscrollAmount  by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = Modifier
            .simpleVerticalScrollbar(scrollState)
            .customOverscroll(
                scrollState,
                onNewOverscrollAmount = { animatedOverscrollAmount = it }
            )
            .offset { IntOffset(0, animatedOverscrollAmount.roundToInt()) }
    ) {
        LazyColumn(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium),
            state = scrollState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space)
        ) {
            item {
                SearchTopBar(
                    modifier = Modifier,
                    onSearchChange = { searchFilter = it },
                    onFocused = { searchFocused = it }
                )
            }

            items.forEach { (key, buttons) ->
                if (buttons.isNotEmpty()) {
                    item(key = "${key}_header") { CategoryHeader(title = key) }
                    item(key = key + searchFilter) {
                        CategorySection(
                            buttons = buttons,
                            row = row,
                            convertorScreenModel = convertorScreenModel,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CategorySection(
    buttons: List<ConvertorData>,
    convertorScreenModel: ConvertorScreenModel,
    row: Int,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val viewMode by SettingsProvider.getValue<Select.Option>("convertor_list_view").collectAsState()

    if (viewMode.id == "column") {
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            buttons.forEach { item ->
                ConvertorItemList(
                    convertorData = item,
                    animatedVisibilityScope = animatedVisibilityScope
                ) {
                    convertorScreenModel.goToConvertor(item.id)
                }
            }
        }
    } else {
        AdaptiveGridLayout(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium),
            columns = row,
            horizontalSpacing = 4.dp,
            verticalSpacing = 4.dp
        ) {
            buttons.forEach { item ->
                ConvertorItemTile(
                    modifier = Modifier.height(112.dp),
                    convertorData = item,
                    animatedVisibilityScope = animatedVisibilityScope
                ) {
                    convertorScreenModel.goToConvertor(item.id)
                }
            }
        }

    }
}


@Composable
fun AdaptiveGridLayout(
    modifier: Modifier = Modifier,
    columns: Int,
    horizontalSpacing: Dp = 4.dp,
    verticalSpacing: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        val spacingX = horizontalSpacing.roundToPx()
        val spacingY = verticalSpacing.roundToPx()

        val rows = measurables.chunked(columns)

        val placeablesPerRow = rows.map { row ->
            val count = row.size
            val totalSpacing = spacingX * (count - 1)
            val itemWidth = ((constraints.maxWidth - totalSpacing) / count)
                .coerceAtLeast(0)

            row.map {
                it.measure(
                    constraints.copy(
                        minWidth = itemWidth,
                        maxWidth = itemWidth
                    )
                )
            }
        }

        val height = placeablesPerRow.sumOf { row ->
            row.maxOf { it.height }
        } + spacingY * (placeablesPerRow.size - 1)

        layout(constraints.maxWidth, height.coerceAtLeast(0)) {
            var y = 0

            placeablesPerRow.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }

                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + spacingX
                }

                y += rowHeight + spacingY
            }
        }
    }
}
