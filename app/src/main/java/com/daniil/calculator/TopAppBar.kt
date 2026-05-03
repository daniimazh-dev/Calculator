package com.daniil.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.ConvertorScreens
import com.daniil.calculator.convertorscreen.homescreen.dataclass.getIcon
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.universal.UniversalDropDownMenu
import com.daniil.csb.SettingsProvider
import kotlinx.coroutines.delay

@Composable
fun CustomTopAppBar(
    modifier: Modifier = Modifier,
    portraitMode: Boolean,
    convertorScreenModel: ConvertorScreenModel,
) {


    val currentConvertor by convertorScreenModel.currentConvertor.collectAsState()
    val screenMode by convertorScreenModel.currentScreen.collectAsState()

    val items: @Composable () -> Unit = {
        AppBarItem(
            icon = R.drawable.calculator_icon,
            contentDescription = "calculator",
            isActive = openScreen.intValue == 0,
            onClick = {
                openScreen.intValue = 0
            }
        )

        AppBarItem(
            icon = when (screenMode) {
                ConvertorScreens.Home -> R.drawable.directory_sync_icon
                ConvertorScreens.Convertor -> {
                    currentConvertor?.getIcon() ?: R.drawable.calculator_icon
                }

            },
            contentDescription = "convertor",
            isActive = openScreen.intValue == 1,
            onClick = {
                openScreen.intValue = 1
            }
        )

        AppBarItem(
            icon = R.drawable.settings_icon,
            contentDescription = "settings",
            isActive = openScreen.intValue == 2,
            onClick = {
                openScreen.intValue = 2
            }
        )
        Indication()
    }


    if (portraitMode) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items()
        }
    } else {
        Column(
            modifier = modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items()
        }
    }

}

@Composable
private fun Indication() {
    val localeMode by SettingsProvider.getValue<Boolean>("locale_mode").collectAsState()
    val useTestServer by SettingsProvider.getValue<Boolean>("use_test_server").collectAsState()
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    when {
        localeMode -> {
            Column(
                modifier = Modifier.clickable {
                    dropdownMenuExpanded = true
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.wifi_lost_icon),
                    contentDescription = "locale mode"

                )
                Text("Locale mode", fontSize = 8.sp)
                UniversalDropDownMenu(
                    expanded = dropdownMenuExpanded,
                    buttonList = listOf(
                        UniversalDropDownItem(
                        title = "Enable",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "enable"
                            )
                        },
                        autoClose = true,
                        onClick = {
                            SettingsProvider.setValue("locale_mode", false)

                        }
                    )),
                    onDismissRequest = {
                        dropdownMenuExpanded = false
                    }
                )
            }
        }

        useTestServer -> {
            Column(
                modifier = Modifier.clickable {
                    dropdownMenuExpanded = true
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.data_rate),
                    contentDescription = "Test server"
                )
                Text("Test server", fontSize = 8.sp)
                UniversalDropDownMenu(
                    expanded = dropdownMenuExpanded,
                    buttonList = listOf(
                        UniversalDropDownItem(
                        title = "Enable",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "enable"
                            )
                        },
                        autoClose = true,
                        onClick = {
                            SettingsProvider.setValue("use_test_server", false)

                        }
                    )),
                    onDismissRequest = {
                        dropdownMenuExpanded = false
                    }
                )
            }
        }

        else -> return
    }

}

@Composable
private fun AppBarItem(
    icon: Int,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit = {},
) {
    var isClicked by remember { mutableStateOf(false) }
    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(200)
            isClicked = false
        }
    }

    val animateScale = animateFloatAsState(
        targetValue = if (isClicked) 0.8f else if (isActive) 1.2f else 1f,
        animationSpec = tween(500)
    )

    IconButton(
        modifier = Modifier,
        onClick = {
            if (!isActive) onClick()
            isClicked = true
        },
    ) {
        AnimatedContent(
            targetState = icon,
            transitionSpec = {
                (fadeIn() + scaleIn(
                    initialScale = 0.5f,
                    animationSpec = tween(220, delayMillis = 90)
                ))
                    .togetherWith(fadeOut())
            }
        ) { icon ->
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer(
                        alpha = if (isActive) 1f else 0.5f,
                        scaleX = animateScale.value,
                        scaleY = animateScale.value
                    ),
                imageVector = ImageVector.vectorResource(icon),
                contentDescription = contentDescription
            )

        }

    }

}