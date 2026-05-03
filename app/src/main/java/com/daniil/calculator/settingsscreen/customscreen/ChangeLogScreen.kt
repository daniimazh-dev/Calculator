package com.daniil.calculator.settingsscreen.customscreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.core.DaniilServerAPI
import com.daniil.calculator.currentVersionCode
import com.daniil.csb.SettingsNavigationModel
import com.daniil.csb.SettingsProvider
import com.daniil.csb.screens.CustomScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Composable
fun CustomScreen.CustomScreenScope.ChangeLogScreen(
    navigationScreenModel: SettingsNavigationModel
) {
    var currentLogIndex by remember { mutableStateOf<Int?>(null) }
    val simulateVersion by SettingsProvider.getValue<String>("imitate_version").collectAsState()

    val currentVersionCode = remember { simulateVersion.toIntOrNull() ?: currentVersionCode }
    val changeLogs by produceState<List<ChangeLogData>?>(emptyList()) {
        value = try {
            val list = DaniilServerAPI().getChangeLog().body()?.changeLogList ?: emptyList()
            currentLogIndex =
                list.indexOfFirst { it.versionCode == currentVersionCode }.takeIf { it != -1 }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentLogIndex) {
        delay(200)
        currentLogIndex?.let { lazyListState.animateScrollToItem(it) }
    }
    var directionButton by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(lazyListState.isScrollInProgress) {
        val currentIndex = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: run {
            directionButton = null
            return@LaunchedEffect
        }
        val targetIndex = currentLogIndex ?: return@LaunchedEffect
        directionButton = when {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.index == targetIndex } -> null
            currentIndex > targetIndex -> -90
            currentIndex < targetIndex -> 90
            else -> null
        }

    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
        ) {

            ScreenTopBar(navigationModel = navigationScreenModel)

            if (changeLogs == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(62.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.wifi_lost_icon),
                            contentDescription = "Internet lost"
                        )
                        Text(stringResource(R.string.no_conection_server))
                    }
                }
            } else if (changeLogs?.isEmpty() == true) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = changeLogs!!,
                        key = { _, item -> item.version }) { index, item ->
                        ChangeLogItem(changeLogData = item, current = currentLogIndex == index)
                    }
                }
            }

        }

        val coroutine = rememberCoroutineScope()
        if (directionButton != null) {
            val rotateAnimation by animateFloatAsState(
                directionButton?.toFloat() ?: 0f
            )
            FilledIconButton(
                modifier = Modifier.padding(16.dp),
                onClick = {
                    coroutine.launch {
                        currentLogIndex?.let { lazyListState.animateScrollToItem(it) }

                    }
                }
            ) {
                Icon(
                    modifier = Modifier.graphicsLayer(
                        rotationZ = rotateAnimation
                    ),
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }

}


@Serializable
@Immutable
data class ChangeLogData(
    val updateType: ChangeLogUpdateType,
    val tags: List<ChangeLogVersionTag>,
    val time: String,
    val version: String,
    val uri: String? = null,
    val versionCode: Int,
    val description: String,
    val whatsNew: Map<String, List<String>>,
)

@Serializable
enum class ChangeLogUpdateType {
    Release,
    BigUpdate,
    DefaultUpdate,
    MinorUpdate,
    CriticalUpdate,
    BetaUpdate,
    BugFix,
}

@Serializable
enum class ChangeLogVersionTag {
    Stable,
    Beta,
    Unstable,
    Critical,
    BugFix,
    Global,
    Last,
}

@Composable
private fun ChangeLogItem(
    modifier: Modifier = Modifier,
    changeLogData: ChangeLogData,
    current: Boolean = false,
) {
    val versionText = when (changeLogData.updateType) {
        ChangeLogUpdateType.Release -> "Release"
        ChangeLogUpdateType.BigUpdate -> "Big update"
        ChangeLogUpdateType.DefaultUpdate -> "Update"
        ChangeLogUpdateType.MinorUpdate -> "Minor update"
        ChangeLogUpdateType.CriticalUpdate -> "Critical update"
        ChangeLogUpdateType.BugFix -> "Bug fix"
        ChangeLogUpdateType.BetaUpdate -> "Beta"
    }


    val typeColor = when (changeLogData.updateType) {
        ChangeLogUpdateType.Release -> MaterialTheme.colorScheme.primary
        ChangeLogUpdateType.BigUpdate -> MaterialTheme.colorScheme.tertiary
        ChangeLogUpdateType.DefaultUpdate -> MaterialTheme.colorScheme.secondary
        ChangeLogUpdateType.MinorUpdate -> MaterialTheme.colorScheme.outline
        ChangeLogUpdateType.CriticalUpdate -> MaterialTheme.colorScheme.onError
        ChangeLogUpdateType.BugFix -> MaterialTheme.colorScheme.outlineVariant
        ChangeLogUpdateType.BetaUpdate -> MaterialTheme.colorScheme.inversePrimary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(3.dp, typeColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (current) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(typeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(typeColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$versionText ${changeLogData.version}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = changeLogData.time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                changeLogData.tags.take(4).forEach { tag ->
                    val updateText = when (tag) {
                        ChangeLogVersionTag.Unstable -> "Experimental"
                        ChangeLogVersionTag.BugFix -> "Bug fix"
                        else -> tag.name
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text(updateText) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

            }


            Text(
                text = changeLogData.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "What's new:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                changeLogData.whatsNew.forEach { (key, list) ->
                    Text(key, fontWeight = FontWeight.Bold)
                    list.forEach { point ->
                        Row(verticalAlignment = Alignment.Top) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("• ", fontWeight = FontWeight.Bold)
                            Text(point, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                }
            }
        }
    }
}

@Preview()
@Composable
private fun PreviewChangeLogItem() {

    val tags = listOf(
        ChangeLogVersionTag.Stable,
        ChangeLogVersionTag.Beta,
        ChangeLogVersionTag.Critical,
        ChangeLogVersionTag.Unstable,
//        ChangeLogVersionTag.BugFix,
    )

    val changeLog = ChangeLogData(
        updateType = ChangeLogUpdateType.CriticalUpdate,
        tags = tags,
        time = "02.04.2025",
        version = "1.0.0",
        versionCode = 0,
        description = "Preview changelog item",
        uri = "",
        whatsNew = mapOf(
            "Header 1" to listOf("content 1", "content 2"),
            "Header 2" to listOf("content 3", "content 4")
        ),
    )
    ChangeLogItem(changeLogData = changeLog)
}


