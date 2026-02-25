package com.daniil.calculator.convertorscreen.convertor.topbar

import FancyTabBar
import FancyTabBarData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertor
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.findOfId
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorReleseState
import com.daniil.calculator.convertorscreen.homescreen.ReleaseStateTable
import com.daniil.calculator.universal.UniversalDropDownMenu
import kotlinx.coroutines.delay

@Composable
fun ConvertorCalckTopBar(
    modifier: Modifier = Modifier,
    convertorScreenModel: ConvertorScreenModel,
    convertorData: CustomConvertor,
    convertorButtonData: ConvertorData,
) {
    val context = LocalContext.current
    var isMoreVertClicked by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(convertorButtonData.favorite) }
    var isFavoriteClicked by remember { mutableStateOf(false) }

    val state = remember { convertorButtonData.release }
    val viewListMode by convertorScreenModel.viewConvertorMode.collectAsState()


    val viewScreen = convertorData.convertorScreen.viewScreens.findOfId(viewListMode)

    val massageShow =
        convertorButtonData.description != null || viewScreen?.description != null
    var isMassageShow by rememberSaveable { mutableStateOf(massageShow) }

    var dropdownMenuExpanded by remember { mutableStateOf(false) }


    val viewConvertorMode by convertorScreenModel.viewConvertorMode.collectAsState()

    val animateMoreVertButton = animateFloatAsState(
        targetValue = if (isMoreVertClicked) 0.7f
        else 1f,
        animationSpec = tween(500)
    )
    val animateScaleFavoriteButton = animateFloatAsState(
        targetValue = if (isFavoriteClicked) 0.7f
        else 1f,
        animationSpec = tween(500)
    )
    LaunchedEffect(isMoreVertClicked, isFavoriteClicked) {
        if (isMoreVertClicked) {
            delay(200)
            isMoreVertClicked = false
        }
        if (isFavoriteClicked) {
            delay(200)
            isFavoriteClicked = false
        }
    }

    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    convertorScreenModel.goToHome()
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = convertorButtonData.title,
                    style = MaterialTheme.typography.headlineMedium,
                    autoSize = TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

            }
            Spacer(modifier = Modifier.width(8.dp))
            ReleaseStateTable(release = convertorButtonData.release)
            IconButton(
                enabled = state != ConvertorReleseState.Experimental && state != ConvertorReleseState.Unavailable,
                onClick = {
                    isFavoriteClicked = true
                    isFavorite = !isFavorite
                    convertorScreenModel.setFavoriteButton(
                        convertorData = convertorButtonData,
                        favorite = isFavorite
                    )
                }) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(
                            scaleX = animateScaleFavoriteButton.value,
                            scaleY = animateScaleFavoriteButton.value
                        ),
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite"
                )
            }
            IconButton(
                onClick = {
                    isMoreVertClicked = true
                    dropdownMenuExpanded = true
                }
            ) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(
                            scaleX = animateMoreVertButton.value,
                            scaleY = animateMoreVertButton.value
                        ),
                    tint = MaterialTheme.colorScheme.onSurface,
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "MoreVert"
                )
                UniversalDropDownMenu(
                    expanded = dropdownMenuExpanded,
                    enabled = viewScreen?.customDropdownMenu != null,
                    buttonList = viewScreen?.customDropdownMenu ?: emptyList(),
                    onDismissRequest = {
                        dropdownMenuExpanded = false
                    }
                )
            }


        }


        val mapKey = convertorData.convertorScreen.viewScreens.keys
        val entries = mapKey.map {
            FancyTabBarData(
                id = it.id,
                name = it.name,
                painterId = it.painterId,

            )
        }

        FancyTabBar(
            modifier = Modifier.padding(horizontal = 16.dp),
            selectedIndex = run {
                val keyList = convertorData.convertorScreen.viewScreens.keys
                val key = keyList.find {
                    it.id == (viewListMode ?: convertorData.convertorScreen.startViewModeId)
                }
                mapKey.indexOf(key)
            },
            entries = entries,
            onSelected = {
                convertorScreenModel.viewConvertorMode.value = it
            }
        )

        AnimatedVisibility(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            visible = isMassageShow
        ) {
            MassageBox(
                modifier = Modifier.padding(top = 9.dp),
                onDismissRequest = {
                    isMassageShow = false
                },
                closeable = viewScreen?.description?.invoke()?.closeable ?: true,
                content = {
                    Column() {
                        convertorButtonData.description?.let {
                            Text(text = convertorButtonData.description)
                        }
                        viewScreen?.description?.invoke()?.content?.invoke()
//                        when (convertorData.state) {
//                            ConvertorReleseState.Experimental -> {
//                                Text(text = "This converter is experimental!", color = Color.Yellow)
//                            }
//                            ConvertorReleseState.Unavailable -> {
//                                Text(text = "This converter is unavailable!", color = Color.Red)
//                            }
//                            else -> {}
//                        }
                    }
                })


        }


    }
}



@Composable
private fun MassageBox(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
    closeable: Boolean,
    onDismissRequest: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier
                .padding(6.dp)
                .padding(start = 10.dp)
                .weight(1f),
//            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
        if (closeable) {
            IconButton(onClick = {
                onDismissRequest?.invoke()
            }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

    }

}